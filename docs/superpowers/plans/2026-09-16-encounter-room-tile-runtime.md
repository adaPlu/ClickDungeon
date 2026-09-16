# ClickDungeon Encounter Rooms and Canonical Tile Runtime Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a 25-tile canonical runtime library from the approved dungeon sheets and add deterministic 3x3 same-floor encounter rooms behind closed/locked doors with persistent combat state and exactly-once rewards.

**Architecture:** The existing 5x5 `FloorState` remains the parent dungeon board. Pure encounter-room layout/generation lives in `ClickDungeon.Dungeon`; door, combat, reward, and room-session behavior lives in `ClickDungeon.Application`; save-v2 DTOs/migration live in `ClickDungeon.Save`; deterministic loot and reward-ledger authority remain in `ClickDungeon.Progression`. Tile PNGs are deterministically extracted from the approved composites; Unity 6 alone generates `.meta` files.

**Tech Stack:** C#/.NET, Python 3.12, pytest, Pillow, GitHub Actions, Unity 6.

**Spec:** `docs/superpowers/specs/2026-09-16-encounter-room-tile-runtime-design.md`

## Global Constraints

- Work on branch `new`; execution starts in an isolated worktree created with `superpowers:using-git-worktrees`.
- Player-facing branding remains exactly `ClickDungeon`.
- `FloorState.Width == 5` and `FloorState.Height == 5` remain unchanged.
- Canonical tile count becomes exactly 25, with distinct `tile.door_closed`, `tile.door_locked`, and `tile.door_open`.
- Approved visual sources are `docs/reference/latest/10-core-gameplay.png`, `11-dungeon-tiles-a.png`, and `12-dungeon-tiles-b.png`.
- Supplied tile art must be cropped/reconciled, not regenerated.
- Floor 1 has no encounter room. Floors 2+ use deterministic 35% room eligibility. At most one room exists per floor.
- Spawned rooms use deterministic 75% closed-door / 25% locked-door selection.
- Each room is exactly 3x3 and contains 3-5 monsters.
- Closed doors open freely. Locked doors consume exactly one `item.key.dungeon`. `item.key.royal` is never used for ordinary encounter rooms.
- Room reward chests are visible but sealed while any room monster survives.
- Locked rooms always use the higher reward tier.
- Run-save schema advances from 1 to 2 with explicit v1->v2 migration.
- Room chest transaction IDs use `room:{floorIndex}:{roomId}:chest:{index}` and remain exactly-once.
- Never hand-author Unity `.meta`, GUIDs, scene YAML, `ProjectSettings`, or `Packages`.
- Every behavior follows RED -> GREEN -> affected regression suite -> commit.
- Engine-free tests and bot soak do not substitute for Unity compile/EditMode/PlayMode/built-player verification.

---

## File Structure

**Create**
- `Assets/ClickDungeon/Dungeon/Runtime/EncounterDoorKind.cs`
- `Assets/ClickDungeon/Dungeon/Runtime/EncounterRewardMode.cs`
- `Assets/ClickDungeon/Dungeon/Runtime/EncounterMonsterSpawn.cs`
- `Assets/ClickDungeon/Dungeon/Runtime/EncounterRoomLayout.cs`
- `Assets/ClickDungeon/Dungeon/Generation/EncounterRoomGenerator.cs`
- `Assets/ClickDungeon/Dungeon/Generation/EncounterRoomValidator.cs`
- `Assets/ClickDungeon/Application/Gameplay/EncounterRoomRuntimeState.cs`
- `Assets/ClickDungeon/Application/Gameplay/EncounterRoomService.cs`
- `Assets/ClickDungeon/Application/Persistence/EncounterRoomSaveMapper.cs`
- `Assets/ClickDungeon/Save/RunSaveV1ToV2Migration.cs`
- `art/dungeon-tile-crops.json`
- `scripts/extract_dungeon_tiles.py`
- `scripts/tests/test_dungeon_tile_extraction.py`
- `scripts/tests/test_encounter_room_contracts.py`

**Modify**
- `Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs`
- `Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs`
- `Assets/ClickDungeon/Dungeon/Runtime/FloorState.cs`
- `Assets/ClickDungeon/Progression/InventoryState.cs`
- `Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs`
- `Assets/ClickDungeon/Application/Persistence/GameSessionPersistenceOrchestrator.cs`
- `Assets/ClickDungeon/Save/RunSave.cs`
- `Assets/ClickDungeon/Save/SaveSchema.cs`
- `Assets/ClickDungeon/Save/SaveRepository.cs`
- `Assets/ClickDungeon/Save/SaveValidator.cs`
- `art/production-art-manifest.json`
- `scripts/bot/ClickDungeon.BotSoak.csproj`
- `scripts/bot/Program.cs`
- `scripts/tests/test_tile_registry_contracts.py`
- `scripts/tests/test_save_progression_contracts.py`
- `scripts/tests/test_bot_soak.py`
- `.github/workflows/source-validation.yml`

---

### Task 1: Expand the canonical tile registry to 25

**Files:**
- Modify: `scripts/tests/test_tile_registry_contracts.py`
- Modify: `Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs`
- Modify: `Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs`
- Regenerate: `art/production-art-manifest.json`

**Interfaces:**
- Produces `tile.door_closed -> Art/Runtime/Tiles/tile_door_closed.png`.
- Preserves all existing 24 tile identities and layers.

- [ ] **Step 1: Write the failing registry test**

```python
EXPECTED["tile.door_closed"] = "Structure"

def test_exact_25_canonical_tile_ids_and_layers(self):
    text = (ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs").read_text()
    found = dict(re.findall(
        r'Add\("([a-z0-9_.]+)",\s*"[^"]+",\s*TileLayer\.([A-Za-z]+)', text
    ))
    self.assertEqual(found, EXPECTED)
    self.assertEqual(len(found), 25)
    self.assertIn('Art/Runtime/Tiles/tile_door_closed.png', text)
```

- [ ] **Step 2: Run RED**

```bash
python3 -m pytest -q scripts/tests/test_tile_registry_contracts.py
```

Expected: FAIL because the current registry contains 24 entries and lacks `tile.door_closed`.

- [ ] **Step 3: Implement the minimal registry change**

```csharp
var items = new List<TileDefinition>(25);
Add("tile.door_closed", "Closed Door", TileLayer.Structure,
    "Art/Runtime/Tiles/tile_door_closed.png", items);
Add("tile.door_locked", "Locked Door", TileLayer.Structure,
    "Art/Runtime/Tiles/tile_door_locked.png", items);
Add("tile.door_open", "Open Door", TileLayer.Structure,
    "Art/Runtime/Tiles/tile_door_open.png", items);
```

Change the integration assertion in `DungeonGenerator`:

```csharp
if (CanonicalTiles.All.Count != 25)
    throw new InvalidOperationException("Canonical tile registry is incomplete.");
```

- [ ] **Step 4: Regenerate and verify GREEN**

```bash
python3 scripts/build_production_art_manifest.py
python3 -m pytest -q scripts/tests/test_tile_registry_contracts.py scripts/tests/test_production_art_contracts.py
python3 scripts/validate_tile_registry.py
python3 scripts/validate_production_art.py
```

Expected: PASS and generated manifest includes `art.tile.door_closed`.

- [ ] **Step 5: Commit**

```bash
git add Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs \
        Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs \
        scripts/tests/test_tile_registry_contracts.py \
        art/production-art-manifest.json
git commit -m "feat: add canonical closed door tile"
```

---

### Task 2: Extract the 25 approved tile sprites deterministically

**Files:**
- Create: `art/dungeon-tile-crops.json`
- Create: `scripts/extract_dungeon_tiles.py`
- Create: `scripts/tests/test_dungeon_tile_extraction.py`
- Create runtime PNGs under `Assets/ClickDungeon/Art/Runtime/Tiles/`
- Create: `Assets/ClickDungeon/Art/Runtime/Chest/closed.png`
- Create: `Assets/ClickDungeon/Art/Runtime/Chest/open.png`
- Modify: `.github/workflows/source-validation.yml`

**Interfaces:**
- `extract_all(root: Path) -> dict[str, str]` returns SHA-256 hashes keyed by output filename.
- `--check` re-extracts to a temporary directory and byte-compares tracked output.

- [ ] **Step 1: Write failing extraction tests**

```python
from pathlib import Path
import json
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
EXPECTED = {
    "tile_floor_stone.png", "tile_trap_pit.png", "tile_trap_bomb.png",
    "tile_trap_spike.png", "tile_stair_up.png", "tile_stair_up_locked.png",
    "tile_stair_down.png", "tile_stair_down_locked.png", "tile_wall.png",
    "tile_wall_corner.png", "tile_door_closed.png", "tile_door_locked.png",
    "tile_door_open.png", "tile_key.png", "tile_chest_closed.png",
    "tile_chest_open.png", "tile_torch.png", "tile_floor_cracked.png",
    "tile_floor_moss.png", "tile_water.png", "tile_lava.png",
    "tile_shadow.png", "tile_pressure_plate.png", "tile_teleport.png",
    "tile_fountain_heal.png",
}

def test_manifest_covers_exact_outputs():
    data = json.loads((ROOT / "art/dungeon-tile-crops.json").read_text())
    assert {entry["output"] for entry in data["tiles"]} == EXPECTED

def test_outputs_are_rgba_256_square():
    for name in EXPECTED:
        image = Image.open(ROOT / "Assets/ClickDungeon/Art/Runtime/Tiles" / name)
        assert image.mode == "RGBA"
        assert image.size == (256, 256)
```

- [ ] **Step 2: Run RED**

```bash
python3 -m pip install Pillow
python3 -m pytest -q scripts/tests/test_dungeon_tile_extraction.py
```

Expected: FAIL because crop manifest/script/runtime outputs do not exist.

- [ ] **Step 3: Add the exact crop map**

Use source key `a` for `docs/reference/latest/11-dungeon-tiles-a.png` and `b` for `docs/reference/latest/12-dungeon-tiles-b.png`:

```json
{
  "schema_version": 1,
  "sources": {
    "a": "docs/reference/latest/11-dungeon-tiles-a.png",
    "b": "docs/reference/latest/12-dungeon-tiles-b.png"
  },
  "tiles": [
    {"output":"tile_floor_stone.png","source":"b","box":[15,159,179,326]},
    {"output":"tile_trap_pit.png","source":"b","box":[207,159,373,326]},
    {"output":"tile_trap_bomb.png","source":"b","box":[397,159,563,326]},
    {"output":"tile_trap_spike.png","source":"b","box":[588,159,755,326]},
    {"output":"tile_stair_up.png","source":"b","box":[779,159,945,326]},
    {"output":"tile_stair_up_locked.png","source":"b","box":[969,159,1135,326]},
    {"output":"tile_stair_down.png","source":"b","box":[1159,159,1325,326]},
    {"output":"tile_stair_down_locked.png","source":"b","box":[1349,159,1514,326]},
    {"output":"tile_wall.png","source":"b","box":[16,428,169,579]},
    {"output":"tile_wall_corner.png","source":"b","box":[188,428,341,579]},
    {"output":"tile_key.png","source":"b","box":[356,428,509,579]},
    {"output":"tile_chest_closed.png","source":"b","box":[522,428,675,579]},
    {"output":"tile_chest_open.png","source":"b","box":[694,428,847,579]},
    {"output":"tile_door_locked.png","source":"b","box":[862,428,1015,579]},
    {"output":"tile_door_open.png","source":"b","box":[1041,428,1194,579]},
    {"output":"tile_torch.png","source":"b","box":[1220,428,1363,579]},
    {"output":"tile_floor_cracked.png","source":"b","box":[22,678,173,823]},
    {"output":"tile_floor_moss.png","source":"b","box":[196,678,347,823]},
    {"output":"tile_water.png","source":"b","box":[371,678,525,823]},
    {"output":"tile_lava.png","source":"b","box":[544,678,693,823]},
    {"output":"tile_shadow.png","source":"b","box":[718,678,871,823]},
    {"output":"tile_pressure_plate.png","source":"b","box":[894,678,1033,823]},
    {"output":"tile_teleport.png","source":"b","box":[1088,678,1236,823]},
    {"output":"tile_fountain_heal.png","source":"b","box":[1310,678,1490,823]},
    {"output":"tile_door_closed.png","source":"a","box":[405,552,554,731]}
  ]
}
```

- [ ] **Step 4: Implement extraction**

```python
from collections import deque
from hashlib import sha256
from pathlib import Path
from PIL import Image
import json

SIZE = 256
BACKGROUND_MAX = 32

def clear_edge_background(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    px = rgba.load()
    width, height = rgba.size
    queue = deque([(x, 0) for x in range(width)] + [(x, height - 1) for x in range(width)] +
                  [(0, y) for y in range(height)] + [(width - 1, y) for y in range(height)])
    seen = set()
    while queue:
        x, y = queue.popleft()
        if x < 0 or y < 0 or x >= width or y >= height or (x, y) in seen:
            continue
        seen.add((x, y))
        r, g, b, a = px[x, y]
        if max(r, g, b) > BACKGROUND_MAX:
            continue
        px[x, y] = (r, g, b, 0)
        queue.extend(((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)))
    return rgba

def normalize(image: Image.Image) -> Image.Image:
    bbox = image.getchannel("A").getbbox()
    if bbox is None:
        raise ValueError("crop contains no visible tile pixels")
    trimmed = image.crop(bbox)
    trimmed.thumbnail((248, 248), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    canvas.alpha_composite(trimmed, ((SIZE - trimmed.width) // 2, (SIZE - trimmed.height) // 2))
    return canvas
```

`extract_all()` reads the JSON, crops each source, applies `clear_edge_background()` and `normalize()`, writes PNGs, copies the closed/open chest tile bytes to `Art/Runtime/Chest/closed.png` and `open.png`, and returns SHA-256 hashes.

- [ ] **Step 5: Extract and verify**

```bash
python3 scripts/extract_dungeon_tiles.py
python3 scripts/extract_dungeon_tiles.py --check
python3 -m pytest -q scripts/tests/test_dungeon_tile_extraction.py
```

Expected: PASS. Visually inspect a temporary contact sheet and verify no labels, neighboring tiles, or composite-sheet background remain.

- [ ] **Step 6: Add CI dependency/check**

```yaml
- name: Install test dependencies
  run: python3 -m pip install --disable-pip-version-check pytest Pillow

- name: Verify deterministic dungeon tile extraction
  run: python3 scripts/extract_dungeon_tiles.py --check
```

- [ ] **Step 7: Commit**

```bash
git add art/dungeon-tile-crops.json scripts/extract_dungeon_tiles.py \
        scripts/tests/test_dungeon_tile_extraction.py .github/workflows/source-validation.yml \
        Assets/ClickDungeon/Art/Runtime/Tiles \
        Assets/ClickDungeon/Art/Runtime/Chest/closed.png \
        Assets/ClickDungeon/Art/Runtime/Chest/open.png
git commit -m "art: extract canonical dungeon tile sprites"
```

Do not add `.meta` files here.

---

### Task 3: Add pure 3x3 encounter-room generation

**Files:**
- Create the six Dungeon files listed in File Structure.
- Modify: `FloorState.cs`, `DungeonGenerator.cs`
- Create: `scripts/tests/test_encounter_room_contracts.py`

**Interfaces:**
- `EncounterRoomLayout EncounterRoomGenerator.TryGenerate(ulong runSeed, FloorState floor, int generationVersion)`; returns `null` when no room is selected.
- `FloorState.EncounterRoom` read-only property and one-time `SetEncounterRoom(EncounterRoomLayout)`.

- [ ] **Step 1: Write RED source contracts**

```python
def test_encounter_room_generation_contracts(self):
    text = self.read("Assets/ClickDungeon/Dungeon/Generation/EncounterRoomGenerator.cs")
    for token in ("floor.FloorIndex == 1", "35", "75", "NextInt(3, 6)",
                  "EncounterDoorKind.Closed", "EncounterDoorKind.Locked"):
        self.assertIn(token, text)
    layout = self.read("Assets/ClickDungeon/Dungeon/Runtime/EncounterRoomLayout.cs")
    self.assertIn("public const int Width = 3", layout)
    self.assertIn("public const int Height = 3", layout)
```

- [ ] **Step 2: Run RED**

```bash
python3 -m pytest -q scripts/tests/test_encounter_room_contracts.py
```

Expected: FAIL because encounter-room files do not exist.

- [ ] **Step 3: Define room types**

```csharp
public enum EncounterDoorKind { Closed, Locked }
public enum EncounterRewardMode { PremiumChest, MultiChest }
public enum EncounterRewardTier { Normal, Higher }
```

`EncounterMonsterSpawn` stores `EntityId`, canonical enemy `DefinitionId`, and room-local `FloorCoordinate Position`. `EncounterRoomLayout` stores stable `RoomId`, parent floor index, main-board doorway coordinate, door kind, room seed, 3x3 cell grid, read-only monster spawns, reward mode, and reward tier.

- [ ] **Step 4: Implement deterministic selection**

```csharp
public EncounterRoomLayout TryGenerate(ulong runSeed, FloorState floor, int generationVersion)
{
    if (floor == null) throw new ArgumentNullException(nameof(floor));
    if (floor.FloorIndex == 1) return null;
    var seed = MixSeed(runSeed, floor.FloorIndex, generationVersion);
    var rng = new DeterministicRng(seed);
    if (rng.NextInt(0, 100) >= 35) return null;

    var kind = rng.NextInt(0, 100) < 75 ? EncounterDoorKind.Closed : EncounterDoorKind.Locked;
    var doorway = ChooseDoorway(floor, rng);
    var roomSeed = MixRoomSeed(seed, doorway);
    var roomRng = new DeterministicRng(roomSeed);
    var monsterCount = roomRng.NextInt(3, 6);
    var tier = kind == EncounterDoorKind.Locked ? EncounterRewardTier.Higher : EncounterRewardTier.Normal;
    var mode = roomRng.NextInt(0, 2) == 0 ? EncounterRewardMode.PremiumChest : EncounterRewardMode.MultiChest;
    return BuildLayout(floor.FloorIndex, doorway, kind, roomSeed, monsterCount, mode, tier, roomRng);
}
```

`ChooseDoorway()` enumerates coordinates in stable x-major/y-minor order, excluding `Start` and `Exit`. `BuildLayout()` fills a 3x3 stone room, reserves `(1,2)` as the room entrance, uses 3-5 unique monster positions from the other eight cells, and derives stable entity IDs from floor, doorway, and ordinal.

- [ ] **Step 5: Add validator and attach room to parent floor**

Validator rejects wrong parent floor, out-of-bounds/start/exit doorway, wrong 3x3 size, duplicate monster coordinates, monster count outside 3-5, and locked rooms without `Higher` tier.

```csharp
public EncounterRoomLayout EncounterRoom { get; private set; }

public void SetEncounterRoom(EncounterRoomLayout room)
{
    if (room == null) throw new ArgumentNullException(nameof(room));
    if (EncounterRoom != null) throw new InvalidOperationException("Floor already has an encounter room.");
    EncounterRoom = room;
}
```

At the end of `DungeonGenerator.Generate()` attach the room and set the doorway structure to `tile.door_closed` or `tile.door_locked`.

- [ ] **Step 6: Run GREEN/regressions**

```bash
python3 -m pytest -q scripts/tests/test_encounter_room_contracts.py scripts/tests/test_dungeon_contracts.py
python3 scripts/validate_dungeon_contracts.py
```

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Dungeon scripts/tests/test_encounter_room_contracts.py
git commit -m "feat: generate deterministic encounter rooms"
```

---

### Task 4: Implement closed/locked door behavior and deterministic key consumption

**Files:**
- Modify: `InventoryState.cs`
- Create: `EncounterRoomRuntimeState.cs`, `EncounterRoomService.cs`
- Modify: `scripts/bot/ClickDungeon.BotSoak.csproj`, `scripts/bot/Program.cs`
- Modify: `scripts/tests/test_encounter_room_contracts.py`

**Interfaces:**
- `bool InventoryState.TryRemoveDefinition(ContentId definitionId, int quantity)`.
- `bool EncounterRoomService.TryOpenDoor(EncounterRoomRuntimeState room, InventoryState inventory)`.

- [ ] **Step 1: Add RED bot assertions**

```csharp
Require(service.TryOpenDoor(closedRoom, inventory), "closed door must open freely");
Require(!service.TryOpenDoor(lockedRoom, inventory), "locked door must reject missing key");
inventory.Add("key-stack", ContentId.Parse("item.key.dungeon"), 2);
Require(service.TryOpenDoor(lockedRoom, inventory), "locked door must open with dungeon key");
Require(inventory.GetRequired("key-stack").Quantity == 1, "exactly one key must be consumed");
Require(service.TryOpenDoor(lockedRoom, inventory), "already-open door is idempotent success");
Require(inventory.GetRequired("key-stack").Quantity == 1, "re-entry must not consume another key");
```

- [ ] **Step 2: Run RED**

```bash
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 1
```

Expected: compile/assertion failure because the new APIs do not exist.

- [ ] **Step 3: Add deterministic definition-level removal**

```csharp
public bool TryRemoveDefinition(ContentId definitionId, int quantity)
{
    if (quantity <= 0) return false;
    var matches = new List<InventoryEntry>();
    var total = 0;
    foreach (var entry in entries.Values)
    {
        if (entry.DefinitionId != definitionId) continue;
        matches.Add(entry);
        total = checked(total + entry.Quantity);
    }
    if (total < quantity) return false;
    matches.Sort((a, b) => StringComparer.Ordinal.Compare(a.InstanceId, b.InstanceId));
    var remaining = quantity;
    foreach (var entry in matches)
    {
        if (remaining == 0) break;
        var take = Math.Min(entry.Quantity, remaining);
        Remove(entry.InstanceId, take);
        remaining -= take;
    }
    return true;
}
```

- [ ] **Step 4: Implement door service**

```csharp
public bool TryOpenDoor(EncounterRoomRuntimeState room, InventoryState inventory)
{
    if (room == null) throw new ArgumentNullException(nameof(room));
    if (inventory == null) throw new ArgumentNullException(nameof(inventory));
    if (room.IsDoorOpen) return true;
    if (room.Layout.DoorKind == EncounterDoorKind.Locked &&
        !inventory.TryRemoveDefinition(ContentId.Parse("item.key.dungeon"), 1))
        return false;
    room.MarkDoorOpen();
    return true;
}
```

No reference to `item.key.royal` is allowed in this service.

- [ ] **Step 5: Run GREEN/regressions**

```bash
python3 -m pytest -q scripts/tests/test_encounter_room_contracts.py scripts/tests/test_item_reward_contracts.py
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 1
```

- [ ] **Step 6: Commit**

```bash
git add Assets/ClickDungeon/Progression/InventoryState.cs \
        Assets/ClickDungeon/Application/Gameplay/EncounterRoomRuntimeState.cs \
        Assets/ClickDungeon/Application/Gameplay/EncounterRoomService.cs \
        scripts/tests/test_encounter_room_contracts.py scripts/bot
git commit -m "feat: add encounter room door interaction"
```

---

### Task 5: Implement room clearing and exactly-once room rewards

**Files:**
- Modify: `EncounterRoomRuntimeState.cs`, `EncounterRoomService.cs`, `GameplaySession.cs`
- Modify: `GameSessionPersistenceOrchestrator.cs`
- Modify: `scripts/bot/Program.cs`

**Interfaces:**
- `bool RecordMonsterDefeated(EncounterRoomRuntimeState room, string entityId)`.
- `bool TryClaimChest(EncounterRoomRuntimeState room, int chestIndex, RewardGrantService grants, out RewardGrant reward)`.
- `GameplaySession` exposes room door/open/claim operations without changing normal turn ordering.

- [ ] **Step 1: Add RED reward assertions**

```csharp
Require(!service.TryClaimChest(room, 0, grants, out _), "reward must be sealed while monsters survive");
foreach (var monster in room.Monsters)
    service.RecordMonsterDefeated(room, monster.EntityId);
Require(room.IsCleared, "last monster must clear room");
Require(room.Chests.All(c => c.IsUnlocked), "clearing room must unlock chests");
Require(service.TryClaimChest(room, 0, grants, out var first), "first room chest claim must succeed");
var expectedId = $"room:{room.Layout.FloorIndex}:{room.Layout.RoomId}:chest:0";
Require(first.TransactionId == expectedId, "room transaction ID must be stable");
Require(!service.TryClaimChest(room, 0, grants, out _), "duplicate room claim must fail");
```

- [ ] **Step 2: Run RED**

```bash
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 1
```

- [ ] **Step 3: Build deterministic chest state**

Premium mode creates one chest. Multi-chest mode creates 2 normal-tier chests or 3 higher-tier chests. Each chest stores index, stable transaction ID, unlock state, and claim state.

```csharp
var count = layout.RewardMode == EncounterRewardMode.PremiumChest
    ? 1
    : (layout.RewardTier == EncounterRewardTier.Higher ? 3 : 2);
```

- [ ] **Step 4: Implement clear transition and grant**

```csharp
public bool RecordMonsterDefeated(EncounterRoomRuntimeState room, string entityId)
{
    if (!room.TryMarkMonsterDefeated(entityId)) return false;
    if (room.Monsters.Any(m => !m.IsDefeated)) return true;
    room.MarkClearedAndUnlockChests();
    return true;
}

public bool TryClaimChest(EncounterRoomRuntimeState room, int chestIndex,
    RewardGrantService grants, out RewardGrant reward)
{
    reward = null;
    var chest = room.GetChest(chestIndex);
    if (!room.IsCleared || !chest.IsUnlocked || chest.IsClaimed) return false;
    var loot = ResolveRoomLoot(room, chestIndex);
    var transactionId = $"room:{room.Layout.FloorIndex}:{room.Layout.RoomId}:chest:{chestIndex}";
    reward = new RewardGrant(transactionId, transactionId + ":item", loot.ItemId, loot.Quantity);
    if (!grants.Grant(reward)) return false;
    chest.MarkClaimed();
    return true;
}
```

`ResolveRoomLoot()` reuses `LootResolver`. Normal tier uses Common/Uncommon/Rare candidates with weights 60/30/10. Higher tier uses Uncommon/Rare/Epic/Legendary candidates with weights 45/30/20/5. Candidate membership comes from `CanonicalItems` rarity.

- [ ] **Step 5: Wire gameplay session/autosave**

On successful door opening, set the main-floor doorway structure to `tile.door_open`. Door and monster-state changes request existing `AutosaveReason.ResolvedTurn`; successful room reward commits request `AutosaveReason.RewardCommitted`. Do not add a new autosave reason.

- [ ] **Step 6: Run GREEN/regressions**

```bash
python3 -m pytest -q scripts/tests/test_gameplay_session_contracts.py scripts/tests/test_item_reward_contracts.py scripts/tests/test_encounter_room_contracts.py
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 10
```

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Application scripts/bot/Program.cs
git commit -m "feat: add encounter room combat rewards"
```

---

### Task 6: Persist encounter-room state with run-save schema v2

**Files:**
- Modify: `SaveSchema.cs`, `RunSave.cs`, `SaveRepository.cs`, `SaveValidator.cs`
- Create: `RunSaveV1ToV2Migration.cs`, `EncounterRoomSaveMapper.cs`
- Modify: `scripts/tests/test_save_progression_contracts.py`, `scripts/bot/Program.cs`

**Interfaces:**
- `RunSave.EncounterRooms : List<EncounterRoomSave>`.
- `EncounterRoomSaveMapper.ToSave(EncounterRoomRuntimeState)` and `FromSave(EncounterRoomSave, EncounterRoomLayout)`.

- [ ] **Step 1: Add RED save contracts**

```python
def test_run_schema_v2_persists_encounter_rooms(self):
    schema = self.read("Assets/ClickDungeon/Save/SaveSchema.cs")
    run = self.read("Assets/ClickDungeon/Save/RunSave.cs")
    self.assertIn("RunVersion = 2", schema)
    for token in ("EncounterRoomSave", "EncounterRooms", "DoorKind", "DoorOpened",
                  "RoomSeed", "Monsters", "Cleared", "RewardMode", "RewardTier", "Chests"):
        self.assertIn(token, run)

def test_v1_to_v2_migration_is_registered(self):
    migration = self.read("Assets/ClickDungeon/Save/RunSaveV1ToV2Migration.cs")
    repository = self.read("Assets/ClickDungeon/Save/SaveRepository.cs")
    for token in ("FromVersion => 1", "ToVersion => 2", "EncounterRooms"):
        self.assertIn(token, migration)
    self.assertIn("RunSaveV1ToV2Migration", repository)
```

- [ ] **Step 2: Run RED**

```bash
python3 -m pytest -q scripts/tests/test_save_progression_contracts.py
```

- [ ] **Step 3: Add v2 DTOs**

```csharp
public sealed class EncounterChestSave
{
    public int Index { get; set; }
    public string TransactionId { get; set; } = string.Empty;
    public bool Unlocked { get; set; }
    public bool Claimed { get; set; }
}

public sealed class EncounterRoomSave
{
    public string RoomId { get; set; } = string.Empty;
    public int ParentFloorIndex { get; set; }
    public CoordinateSave Doorway { get; set; } = new CoordinateSave();
    public string DoorKind { get; set; } = string.Empty;
    public bool DoorOpened { get; set; }
    public ulong RoomSeed { get; set; }
    public bool Cleared { get; set; }
    public string RewardMode { get; set; } = string.Empty;
    public string RewardTier { get; set; } = string.Empty;
    public List<EnemyStateSave> Monsters { get; set; } = new List<EnemyStateSave>();
    public List<EncounterChestSave> Chests { get; set; } = new List<EncounterChestSave>();
}
```

Add `public List<EncounterRoomSave> EncounterRooms { get; set; } = new List<EncounterRoomSave>();` to `RunSave` and set `SaveSchema.RunVersion = 2`.

- [ ] **Step 4: Implement and register migration**

```csharp
public sealed class RunSaveV1ToV2Migration : ISaveMigration<RunSave>
{
    public int FromVersion => 1;
    public int ToVersion => 2;
    public RunSave Migrate(RunSave source)
    {
        if (source == null) throw new ArgumentNullException(nameof(source));
        source.EncounterRooms = source.EncounterRooms ?? new List<EncounterRoomSave>();
        source.SchemaVersion = 2;
        return source;
    }
}
```

Register it from a private `CreateRunMigrations()` helper used by the default `SaveRepository` constructor.

- [ ] **Step 5: Validate room saves**

`SaveValidator.ValidateRun()` requires non-null room list, unique room IDs, parent floor match, doorway x/y in 0..4, 3-5 monsters, 1-3 chests, unique chest transaction IDs, and `Claimed => Unlocked`.

- [ ] **Step 6: Add mapper and round-trip test**

The mapper preserves room ID, doorway, door state, room seed, reward mode/tier, clear state, monster entity/definition/position/current health/statuses, and chest unlock/claim state. The bot saves a partially cleared room, restores it, and compares all mutable fields exactly.

- [ ] **Step 7: Run GREEN/regressions**

```bash
python3 -m pytest -q scripts/tests/test_save_progression_contracts.py scripts/tests/test_encounter_room_contracts.py
python3 scripts/validate_save_progression_contracts.py
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 10
```

- [ ] **Step 8: Commit**

```bash
git add Assets/ClickDungeon/Save Assets/ClickDungeon/Application/Persistence \
        scripts/tests/test_save_progression_contracts.py scripts/bot/Program.cs
git commit -m "feat: persist encounter rooms in run save v2"
```

---

### Task 7: Expand the deterministic bot soak for encounter rooms

**Files:**
- Modify: `scripts/bot/Program.cs`, `scripts/bot/ClickDungeon.BotSoak.csproj`
- Modify: `scripts/tests/test_bot_soak.py`

**Interfaces:**
- Final bot summary includes numeric `runs`, `turns`, `rooms`, `closed`, `locked`, and `room_rewards` fields plus four PASS markers.

- [ ] **Step 1: Update Python expectation first (RED)**

```python
def test_bot_soak_reports_encounter_room_invariants():
    result = run_bot(50)
    assert result.returncode == 0, result.stdout + result.stderr
    line = result.stdout.strip().splitlines()[-1]
    for token in ("BOT_SOAK_PASS", "rooms=", "closed=", "locked=", "room_rewards=",
                  "determinism=PASS", "room_idempotency=PASS"):
        assert token in line
```

- [ ] **Step 2: Run RED**

```bash
python3 -m pytest -q scripts/tests/test_bot_soak.py
```

Expected: FAIL because current bot output lacks room metrics.

- [ ] **Step 3: Add room-driving behavior**

For every generated room, bot flow is: test missing-key failure for locked door; add one Dungeon Key and retry; enter; attempt sealed chest; defeat monsters; save/restore once mid-fight; clear room; claim all chests; repeat every claim and require rejection; leave/re-enter; regenerate same seed and compare room ID, doorway, door kind, room seed, monster definitions/positions, reward mode/tier.

Use stable deterministic seed enumeration only. If the existing 50 sequential seeds do not include both door kinds, enumerate seeds from 1 upward until one closed and one locked example are found, then include those discovered integer seeds as fixed supplemental bot cases.

- [ ] **Step 4: Emit extended summary**

```csharp
Console.WriteLine(
    $"BOT_SOAK_PASS runs={runs} turns={turns} rooms={roomCount} closed={closedCount} " +
    $"locked={lockedCount} room_rewards={roomRewardCount} determinism=PASS " +
    "reward_idempotency=PASS chest_idempotency=PASS room_idempotency=PASS");
```

- [ ] **Step 5: Run three soak passes**

```bash
for i in 1 2 3; do
  dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 50
done
python3 -m pytest -q scripts/tests/test_bot_soak.py
```

Expected: all PASS and every pass reports non-zero `rooms`, `closed`, `locked`, and `room_rewards`.

- [ ] **Step 6: Commit**

```bash
git add scripts/bot scripts/tests/test_bot_soak.py
git commit -m "test: soak encounter room gameplay flows"
```

---

### Task 8: Run the complete source gate

**Files:**
- No planned code changes. Any repair requires a new failing regression test first.

**Interfaces:**
- Produces one exact source-green `new` SHA.

- [ ] **Step 1: Run all source checks**

```bash
python3 -m pytest -q scripts/tests
python3 scripts/validate_foundation_contracts.py
python3 scripts/validate_tile_registry.py
python3 scripts/validate_dungeon_contracts.py
python3 scripts/validate_combat_contracts.py
python3 scripts/validate_hero_contracts.py
python3 scripts/validate_enemy_contracts.py
python3 scripts/validate_item_reward_contracts.py
python3 scripts/validate_gameplay_session_contracts.py
python3 scripts/validate_presentation_ui_contracts.py
python3 scripts/validate_narrative_contracts.py
python3 scripts/validate_production_art.py
python3 scripts/validate_animation_reward_contracts.py
python3 scripts/validate_save_progression_contracts.py
python3 scripts/validate_recovery_parity.py
python3 scripts/validate_platform_contracts.py
python3 scripts/validate_platform_build_contracts.py
python3 scripts/extract_dungeon_tiles.py --check
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 50
git diff --check
git status --short
```

Expected: all source-mode checks PASS and worktree clean.

- [ ] **Step 2: Repair only evidence-backed failures**

If any command fails, invoke `superpowers:systematic-debugging`, capture the exact failure, add a focused failing regression test, make the minimum correction, rerun the failed command, then rerun Step 1. Do not relax tile count, spawn percentages, save validation, reward idempotency, or art strictness.

- [ ] **Step 3: Push and verify CI on the exact SHA**

```bash
git push origin new
git rev-parse HEAD
```

Require the GitHub `Source Validation` run for that exact SHA to show pytest PASS, every source validator PASS, deterministic tile extraction PASS, and C# bot soak PASS.

- [ ] **Step 4: Commit only if Step 2 produced a tested repair**

Use the exact files changed by that repair, for example:

```bash
git add Assets/ClickDungeon/Application/Gameplay/EncounterRoomService.cs scripts/bot/Program.cs
git commit -m "fix: repair encounter room source validation"
```

If Step 2 required no code change, do not create an empty repair commit.

---

### Task 9: Complete Unity import and player-facing verification

**Files:**
- Unity-generated `.meta` files for new runtime PNGs and code.
- Authentic `ProjectSettings/` and `Packages/` only if produced by real Unity 6 import.
- Unity test/presentation files added according to the existing Phase-16 plan after authentic metadata exists.

**Interfaces:**
- Consumes the exact source-green SHA from Task 8.
- Produces Unity-authenticated imports, strict-art evidence, compile/EditMode/PlayMode evidence, and runtime visual evidence.

- [ ] **Step 1: Open the exact `new` worktree in Unity 6 and finish import**

After import, close Unity and inspect:

```bash
git status --short
git diff --check
```

Expected: Unity-generated metadata/import changes only; `Library/`, `Temp/`, `Logs/`, and `Obj/` remain ignored.

- [ ] **Step 2: Run strict art validation**

```bash
python3 scripts/validate_production_art.py --strict
```

Expected for this feature: all 25 tile PNGs and corresponding Unity-generated `.meta` files pass; `Chest/closed.png` and `Chest/open.png` also have Unity-generated `.meta`. If another Phase-16 art category remains missing, preserve that exact failure as the next broader blocker.

- [ ] **Step 3: Continue the existing Phase-16 Unity gates**

Follow `docs/superpowers/plans/2026-09-15-phase-16-release-validation.md` from its Unity metadata/import GREEN boundary: compile, EditMode, PlayMode, runtime smoke, Windows build, Android APK/AAB, iOS Xcode export, mutation guards, and artifact inspection.

Encounter-room Unity tests must prove: parent board remains 5x5; entering a room displays 3x3; closed/locked/open door sprites map correctly; sealed chest uses closed art; claimed chest uses open art; exiting restores the same parent floor.

- [ ] **Step 4: Perform visual acceptance**

Capture runtime evidence for: normal closed door on 5x5 board; locked door on 5x5 board; open-door transition; 3x3 room with 3-5 monsters and visible sealed reward; cleared room with unlocked reward. Compare against the approved core-gameplay and tile-sheet references.

- [ ] **Step 5: Commit authentic Unity changes**

```bash
git add Assets ProjectSettings Packages
git status --short
git commit -m "build: import encounter room tiles in Unity 6"
git push origin new
```

Do not stage Unity cache directories.

---

## Final Verification

Run fresh on the exact final `new` SHA:

```bash
python3 -m pytest -q scripts/tests
python3 scripts/extract_dungeon_tiles.py --check
python3 scripts/validate_tile_registry.py
python3 scripts/validate_dungeon_contracts.py
python3 scripts/validate_item_reward_contracts.py
python3 scripts/validate_gameplay_session_contracts.py
python3 scripts/validate_save_progression_contracts.py
python3 scripts/validate_production_art.py
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 50
git diff --check
git status --short
```

When Unity-generated metadata exists, also require:

```bash
python3 scripts/validate_production_art.py --strict
```

Then require the broader Phase-16 Unity compile/EditMode/PlayMode/runtime/build gates on that same SHA. Only after those are green should `/Gaudit` and the `new -> main` release PR proceed.

## Plan Self-Review

- **Spec coverage:** Tasks 1-2 cover the 25-tile runtime/art contract; Task 3 covers deterministic same-floor generation; Task 4 covers closed/locked doors and Dungeon Key consumption; Task 5 covers 3-5 monsters, sealed rewards, stronger locked-room rewards, and exactly-once transactions; Task 6 covers save schema v2/migration; Task 7 covers bot determinism/re-entry/idempotency; Task 8 covers the complete source gate; Task 9 covers Unity/strict-art/player-facing validation.
- **Placeholder scan:** no unresolved implementation placeholders remain; every task names exact files, commands, interfaces, and expected outcomes.
- **Type consistency:** `EncounterRoomLayout`, `EncounterRoomRuntimeState`, `EncounterRoomService`, `EncounterDoorKind`, `EncounterRewardMode`, `EncounterRewardTier`, `TryRemoveDefinition`, save DTOs, and stable room transaction IDs are named consistently across tasks.
