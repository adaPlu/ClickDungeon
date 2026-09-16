# ClickDungeon Encounter Rooms and Canonical Tile Runtime Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the approved dungeon tile sheets into a 25-tile canonical runtime library and add deterministic 3x3 same-floor encounter rooms behind closed/locked doors with persistent combat state and exactly-once rewards.

**Architecture:** Keep the existing 5x5 `FloorState` untouched and add encounter-room generation/state beside it. Pure room layout/generation stays in `ClickDungeon.Dungeon`; door interaction, combat/reward orchestration, and persistence mapping stay in `ClickDungeon.Application`; save DTOs/migration stay in `ClickDungeon.Save`; the existing deterministic `LootResolver`, `RewardGrantService`, and `RewardLedger` remain reward authority. Runtime tile PNGs are deterministically extracted from the approved reference composites and Unity alone generates `.meta` files.

**Tech Stack:** C#/.NET source contracts, Python 3.12 + pytest validators/tooling, Pillow for deterministic sprite extraction, GitHub Actions, Unity 6 import/runtime verification when authentic Unity project metadata exists.

**Spec:** `docs/superpowers/specs/2026-09-16-encounter-room-tile-runtime-design.md`

## Global Constraints

- Work on branch `new`; execute in an isolated worktree created with `superpowers:using-git-worktrees`.
- Shipping/player-facing branding remains exactly `ClickDungeon`; legacy `ClickDungeon2` text from reference imagery is non-authoritative.
- `FloorState.Width == 5` and `FloorState.Height == 5` remain unchanged.
- Canonical tile count becomes exactly **25** and includes distinct `tile.door_closed`, `tile.door_locked`, and `tile.door_open` identities.
- Approved visual sources are `docs/reference/latest/10-core-gameplay.png`, `11-dungeon-tiles-a.png`, and `12-dungeon-tiles-b.png`.
- Do not use image generation for any supplied tile; isolate/crop the approved sheet art.
- Floor 1 has no encounter room; Floors 2+ use deterministic 35% room eligibility; at most one room per floor.
- Spawned rooms use deterministic 75% `Closed` / 25% `Locked` door selection.
- Each encounter room is exactly 3x3 and contains 3-5 monsters.
- Closed doors open freely; locked doors consume exactly one `item.key.dungeon`; `item.key.royal` is not used.
- Reward chest(s) are visible but sealed while any room monster survives.
- Locked rooms always use the stronger reward tier.
- Run save schema advances from version 1 to version 2 with explicit v1->v2 migration.
- Room/chest reward identity is stable and duplicate-safe; use transaction IDs `room:<floorIndex>:<roomId>:chest:<index>`.
- Do not hand-author Unity `.meta`, GUID, scene YAML, `ProjectSettings`, or `Packages`; Unity 6 must generate/authenticate those artifacts.
- RED -> GREEN for every behavioral change, then affected regression suite, then commit.
- Engine-free tests and bot soak do not substitute for Unity compile/EditMode/PlayMode/built-player verification.

---

## File Structure

**Modify**
- `Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs` — add the 25th canonical tile (`tile.door_closed`).
- `Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs` — attach deterministic optional encounter-room layout generation to floors.
- `Assets/ClickDungeon/Dungeon/Runtime/FloorState.cs` — expose at most one encounter-room layout reference without changing 5x5 cells.
- `Assets/ClickDungeon/Progression/InventoryState.cs` — deterministic definition-level quantity removal for Dungeon Keys.
- `Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs` — room entry/exit, door interaction, monster defeat notification, and room chest commit orchestration.
- `Assets/ClickDungeon/Application/Persistence/GameSessionPersistenceOrchestrator.cs` — autosave on door, room-combat, and room-reward state changes through existing stable autosave reasons.
- `Assets/ClickDungeon/Save/RunSave.cs` — schema-v2 encounter-room DTOs.
- `Assets/ClickDungeon/Save/SaveSchema.cs` — set `RunVersion = 2`.
- `Assets/ClickDungeon/Save/SaveRepository.cs` — register built-in v1->v2 run migration in default construction.
- `Assets/ClickDungeon/Save/SaveValidator.cs` — validate encounter-room state.
- `scripts/build_production_art_manifest.py` / generated `art/production-art-manifest.json` — include the new canonical door tile via registry parsing.
- `scripts/bot/Program.cs` / `scripts/bot/ClickDungeon.BotSoak.csproj` — compile new runtime files and exercise room flows.
- `.github/workflows/source-validation.yml` — install Pillow and run deterministic tile extraction verification.
- source-contract tests under `scripts/tests/` — lock the new contracts.

**Create**
- `Assets/ClickDungeon/Dungeon/Runtime/EncounterDoorKind.cs` — `Closed` / `Locked` enum.
- `Assets/ClickDungeon/Dungeon/Runtime/EncounterRewardMode.cs` — `PremiumChest` / `MultiChest` enum plus reward-tier enum.
- `Assets/ClickDungeon/Dungeon/Runtime/EncounterMonsterSpawn.cs` — immutable generated monster spawn descriptor.
- `Assets/ClickDungeon/Dungeon/Runtime/EncounterRoomLayout.cs` — immutable 3x3 generated room layout/metadata.
- `Assets/ClickDungeon/Dungeon/Generation/EncounterRoomGenerator.cs` — deterministic room eligibility, door type, doorway, monster spawns, reward mode/tier.
- `Assets/ClickDungeon/Dungeon/Generation/EncounterRoomValidator.cs` — room/door generation invariants.
- `Assets/ClickDungeon/Application/Gameplay/EncounterRoomRuntimeState.cs` — mutable door/monster/chest state using combat/progression types.
- `Assets/ClickDungeon/Application/Gameplay/EncounterRoomService.cs` — door opening, clear transition, chest unlock and room-reward resolution.
- `Assets/ClickDungeon/Application/Persistence/EncounterRoomSaveMapper.cs` — runtime <-> save-v2 conversion.
- `Assets/ClickDungeon/Save/RunSaveV1ToV2Migration.cs` — explicit sequential migration.
- `art/dungeon-tile-crops.json` — deterministic source/composite crop map.
- `scripts/extract_dungeon_tiles.py` — crop, edge-background alpha cleanup, proportional 256x256 normalization, chest-role reconciliation.
- `scripts/tests/test_encounter_room_contracts.py` — static/source contracts.
- `scripts/tests/test_dungeon_tile_extraction.py` — crop map/output determinism contracts.

---

### Task 1: Expand the canonical tile registry from 24 to 25

**Files:**
- Modify: `scripts/tests/test_tile_registry_contracts.py`
- Modify: `Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs`
- Modify: `Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs`
- Regenerate: `art/production-art-manifest.json`

**Interfaces:**
- Consumes: existing `TileDefinition(ContentId, string, TileLayer, string)` and canonical registry pattern.
- Produces: canonical `ContentId.Parse("tile.door_closed")` mapped to `Art/Runtime/Tiles/tile_door_closed.png`; `CanonicalTiles.All.Count == 25`.

- [ ] **Step 1: Write the failing registry test**

Update `EXPECTED` and the count assertion:

```python
EXPECTED["tile.door_closed"] = "Structure"

# rename the test to make the contract explicit
def test_exact_25_canonical_tile_ids_and_layers(self):
    text = (ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs").read_text()
    found = dict(re.findall(
        r'Add\("([a-z0-9_.]+)",\s*"[^"]+",\s*TileLayer\.([A-Za-z]+)',
        text,
    ))
    self.assertEqual(found, EXPECTED)
    self.assertEqual(len(found), 25)
    self.assertIn('Art/Runtime/Tiles/tile_door_closed.png', text)
```

- [ ] **Step 2: Run the focused test and prove RED**

Run:

```bash
python3 -m pytest -q scripts/tests/test_tile_registry_contracts.py
```

Expected: FAIL because `tile.door_closed` is absent and the registry still contains 24 entries.

- [ ] **Step 3: Implement the minimal registry change**

In `CanonicalTiles.Build()` change capacity to 25 and add the new identity adjacent to the other door states:

```csharp
var items = new List<TileDefinition>(25);
Add("tile.door_closed", "Closed Door", TileLayer.Structure,
    "Art/Runtime/Tiles/tile_door_closed.png", items);
Add("tile.door_locked", "Locked Door", TileLayer.Structure,
    "Art/Runtime/Tiles/tile_door_locked.png", items);
Add("tile.door_open", "Open Door", TileLayer.Structure,
    "Art/Runtime/Tiles/tile_door_open.png", items);
```

In `DungeonGenerator.Generate()` replace the old hardcoded integration assertion:

```csharp
if (CanonicalTiles.All.Count != 25)
    throw new InvalidOperationException("Canonical tile registry is incomplete.");
```

- [ ] **Step 4: Regenerate the art manifest and verify GREEN**

Run:

```bash
python3 scripts/build_production_art_manifest.py
python3 -m pytest -q scripts/tests/test_tile_registry_contracts.py scripts/tests/test_production_art_contracts.py
python3 scripts/validate_tile_registry.py
python3 scripts/validate_production_art.py
```

Expected: all PASS; source-mode production art count increases by one and includes `art.tile.door_closed`.

- [ ] **Step 5: Commit**

```bash
git add Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs \
        Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs \
        scripts/tests/test_tile_registry_contracts.py \
        art/production-art-manifest.json
git commit -m "feat: add canonical closed door tile"
```

---

### Task 2: Deterministically extract all 25 approved runtime tile sprites

**Files:**
- Create: `art/dungeon-tile-crops.json`
- Create: `scripts/extract_dungeon_tiles.py`
- Create: `scripts/tests/test_dungeon_tile_extraction.py`
- Create outputs: `Assets/ClickDungeon/Art/Runtime/Tiles/*.png`
- Create outputs: `Assets/ClickDungeon/Art/Runtime/Chest/closed.png`, `open.png`
- Modify: `.github/workflows/source-validation.yml`

**Interfaces:**
- Consumes: `docs/reference/latest/11-dungeon-tiles-a.png`, `docs/reference/latest/12-dungeon-tiles-b.png`.
- Produces: `extract_all(root: Path) -> dict[str, str]` where values are SHA-256 hashes of normalized 256x256 PNG outputs.

- [ ] **Step 1: Add a RED extraction contract test**

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

def test_crop_manifest_covers_exact_25_outputs():
    data = json.loads((ROOT / "art/dungeon-tile-crops.json").read_text())
    assert {entry["output"] for entry in data["tiles"]} == EXPECTED

def test_runtime_tiles_are_256_square_rgba():
    for name in EXPECTED:
        image = Image.open(ROOT / "Assets/ClickDungeon/Art/Runtime/Tiles" / name)
        assert image.size == (256, 256)
        assert image.mode == "RGBA"
```

- [ ] **Step 2: Run the test and prove RED**

```bash
python3 -m pip install Pillow
python3 -m pytest -q scripts/tests/test_dungeon_tile_extraction.py
```

Expected: FAIL because the crop manifest/script/runtime tile outputs do not exist.

- [ ] **Step 3: Add the exact crop manifest**

Use source key `a` for `11-dungeon-tiles-a.png` and `b` for `12-dungeon-tiles-b.png`. Record these pixel boxes `[left, top, right, bottom]`:

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

- [ ] **Step 4: Implement deterministic extraction**

Core functions in `scripts/extract_dungeon_tiles.py`:

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
    queue = deque()
    seen = set()
    for x in range(width):
        queue.append((x, 0)); queue.append((x, height - 1))
    for y in range(height):
        queue.append((0, y)); queue.append((width - 1, y))
    while queue:
        x, y = queue.popleft()
        if (x, y) in seen or x < 0 or y < 0 or x >= width or y >= height:
            continue
        seen.add((x, y))
        r, g, b, a = px[x, y]
        if max(r, g, b) > BACKGROUND_MAX:
            continue
        px[x, y] = (r, g, b, 0)
        queue.extend(((x-1,y),(x+1,y),(x,y-1),(x,y+1)))
    return rgba

def normalize(image: Image.Image) -> Image.Image:
    alpha = image.getchannel("A")
    bbox = alpha.getbbox()
    if bbox is None:
        raise ValueError("crop contains no visible tile pixels")
    trimmed = image.crop(bbox)
    trimmed.thumbnail((248, 248), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    canvas.alpha_composite(trimmed, ((SIZE-trimmed.width)//2, (SIZE-trimmed.height)//2))
    return canvas

def extract_all(root: Path) -> dict[str, str]:
    config = json.loads((root / "art/dungeon-tile-crops.json").read_text())
    sources = {k: Image.open(root / v) for k, v in config["sources"].items()}
    out_dir = root / "Assets/ClickDungeon/Art/Runtime/Tiles"
    out_dir.mkdir(parents=True, exist_ok=True)
    hashes = {}
    for entry in config["tiles"]:
        crop = sources[entry["source"]].crop(tuple(entry["box"]))
        output = normalize(clear_edge_background(crop))
        path = out_dir / entry["output"]
        output.save(path, format="PNG", optimize=False)
        hashes[entry["output"]] = sha256(path.read_bytes()).hexdigest()
    (root / "Assets/ClickDungeon/Art/Runtime/Chest").mkdir(parents=True, exist_ok=True)
    for source_name, role_name in (("tile_chest_closed.png", "closed.png"), ("tile_chest_open.png", "open.png")):
        data = (out_dir / source_name).read_bytes()
        (root / "Assets/ClickDungeon/Art/Runtime/Chest" / role_name).write_bytes(data)
    return hashes
```

The script must also provide `--check`, which extracts to a temporary directory and byte-compares all 25 tile files plus the two chest-role copies against tracked outputs.

- [ ] **Step 5: Extract, visually inspect, and verify deterministic output**

Run:

```bash
python3 scripts/extract_dungeon_tiles.py
python3 scripts/extract_dungeon_tiles.py --check
python3 -m pytest -q scripts/tests/test_dungeon_tile_extraction.py
```

Manually inspect a contact sheet generated only for review (not runtime) and verify no labels, neighboring tiles, or sheet background remain. In particular verify `tile_door_closed.png`, `tile_door_locked.png`, `tile_chest_closed.png`, and `tile_chest_open.png` against the approved references.

- [ ] **Step 6: Add extraction verification to CI**

Change the dependency install step to:

```yaml
- name: Install test dependencies
  run: python3 -m pip install --disable-pip-version-check pytest Pillow
```

Add after pytest/source validators:

```yaml
- name: Verify deterministic dungeon tile extraction
  run: python3 scripts/extract_dungeon_tiles.py --check
```

- [ ] **Step 7: Commit**

```bash
git add art/dungeon-tile-crops.json scripts/extract_dungeon_tiles.py \
        scripts/tests/test_dungeon_tile_extraction.py .github/workflows/source-validation.yml \
        Assets/ClickDungeon/Art/Runtime/Tiles Assets/ClickDungeon/Art/Runtime/Chest/closed.png \
        Assets/ClickDungeon/Art/Runtime/Chest/open.png
git commit -m "art: extract canonical dungeon tile sprites"
```

Do not add `.meta` files in this task; Unity has not generated them yet.

---

### Task 3: Add pure encounter-room layout and deterministic generation

**Files:**
- Create: `Assets/ClickDungeon/Dungeon/Runtime/EncounterDoorKind.cs`
- Create: `Assets/ClickDungeon/Dungeon/Runtime/EncounterRewardMode.cs`
- Create: `Assets/ClickDungeon/Dungeon/Runtime/EncounterMonsterSpawn.cs`
- Create: `Assets/ClickDungeon/Dungeon/Runtime/EncounterRoomLayout.cs`
- Create: `Assets/ClickDungeon/Dungeon/Generation/EncounterRoomGenerator.cs`
- Create: `Assets/ClickDungeon/Dungeon/Generation/EncounterRoomValidator.cs`
- Modify: `Assets/ClickDungeon/Dungeon/Runtime/FloorState.cs`
- Modify: `Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs`
- Create: `scripts/tests/test_encounter_room_contracts.py`

**Interfaces:**
- Produces: `EncounterRoomLayout EncounterRoomGenerator.TryGenerate(ulong runSeed, FloorState floor, int generationVersion)` where `null` means no room.
- Produces: `FloorState.EncounterRoom` read-only property and `SetEncounterRoom(EncounterRoomLayout room)` one-time setter.

- [ ] **Step 1: Write failing source contracts for the room model**

```python
def test_encounter_room_contracts_exist(self):
    required = (
        "Assets/ClickDungeon/Dungeon/Runtime/EncounterDoorKind.cs",
        "Assets/ClickDungeon/Dungeon/Runtime/EncounterRewardMode.cs",
        "Assets/ClickDungeon/Dungeon/Runtime/EncounterMonsterSpawn.cs",
        "Assets/ClickDungeon/Dungeon/Runtime/EncounterRoomLayout.cs",
        "Assets/ClickDungeon/Dungeon/Generation/EncounterRoomGenerator.cs",
        "Assets/ClickDungeon/Dungeon/Generation/EncounterRoomValidator.cs",
    )
    for rel in required:
        self.assertTrue((ROOT / rel).is_file(), rel)

def test_generator_contract_is_floor2_plus_35_percent_75_25_and_3_to_5(self):
    text = (ROOT / "Assets/ClickDungeon/Dungeon/Generation/EncounterRoomGenerator.cs").read_text()
    for token in ("floor.FloorIndex == 1", "35", "75", "3", "5", "EncounterDoorKind.Closed", "EncounterDoorKind.Locked"):
        self.assertIn(token, text)
```

- [ ] **Step 2: Run and prove RED**

```bash
python3 -m pytest -q scripts/tests/test_encounter_room_contracts.py
```

Expected: FAIL because the room types/generator do not exist.

- [ ] **Step 3: Add exact pure-domain types**

```csharp
public enum EncounterDoorKind { Closed, Locked }
public enum EncounterRewardMode { PremiumChest, MultiChest }
public enum EncounterRewardTier { Normal, Higher }

public sealed class EncounterMonsterSpawn
{
    public string EntityId { get; }
    public ContentId DefinitionId { get; }
    public FloorCoordinate Position { get; }
    public EncounterMonsterSpawn(string entityId, ContentId definitionId, FloorCoordinate position)
    {
        EntityId = entityId ?? throw new ArgumentNullException(nameof(entityId));
        DefinitionId = definitionId;
        Position = position;
    }
}
```

`EncounterRoomLayout` must expose constants `Width = 3`, `Height = 3`, stable metadata, read-only monster spawns, a 3x3 `FloorCell[,]`, and an `IsInBounds(FloorCoordinate)` helper.

- [ ] **Step 4: Implement deterministic generation with integer thresholds**

Use the existing `DeterministicRng`, not `System.Random`:

```csharp
public EncounterRoomLayout TryGenerate(ulong runSeed, FloorState floor, int generationVersion)
{
    if (floor == null) throw new ArgumentNullException(nameof(floor));
    if (floor.FloorIndex == 1) return null;

    var seed = MixSeed(runSeed, floor.FloorIndex, generationVersion, 0x454E43524F4F4DUL);
    var rng = new DeterministicRng(seed);
    if (rng.NextInt(0, 100) >= 35) return null;

    var doorKind = rng.NextInt(0, 100) < 75
        ? EncounterDoorKind.Closed
        : EncounterDoorKind.Locked;
    var doorway = ChooseDoorway(floor, rng);
    var roomSeed = MixSeed(seed, doorway.X, doorway.Y, 0x524F4F4DUL);
    var roomRng = new DeterministicRng(roomSeed);
    var monsterCount = roomRng.NextInt(3, 6);
    var rewardTier = doorKind == EncounterDoorKind.Locked
        ? EncounterRewardTier.Higher
        : EncounterRewardTier.Normal;
    var rewardMode = roomRng.NextInt(0, 2) == 0
        ? EncounterRewardMode.PremiumChest
        : EncounterRewardMode.MultiChest;

    return BuildLayout(floor.FloorIndex, doorway, doorKind, roomSeed,
        monsterCount, rewardMode, rewardTier, roomRng);
}
```

`ChooseDoorway` must enumerate legal board coordinates in stable x-major/y-minor order, excluding `Start` and `Exit`, then choose `rng.NextInt(0, candidates.Count)`. `BuildLayout` uses canonical stone floor and stable room coordinates, reserves `(1,2)` as entry/exit, chooses unique monster coordinates from the other 8 cells, and derives entity IDs as `room:<floor>:<doorX>:<doorY>:monster:<ordinal>`.

- [ ] **Step 5: Add validator invariants**

`EncounterRoomValidator.Validate(FloorState parent, EncounterRoomLayout room)` must throw for: wrong parent floor, doorway equal to start/exit, doorway out of bounds, room not exactly 3x3, monster count outside 3-5, duplicate/out-of-bounds monster positions, or locked room with non-higher reward tier.

- [ ] **Step 6: Attach at most one room to `FloorState`**

```csharp
public EncounterRoomLayout EncounterRoom { get; private set; }

public void SetEncounterRoom(EncounterRoomLayout room)
{
    if (room == null) throw new ArgumentNullException(nameof(room));
    if (EncounterRoom != null) throw new InvalidOperationException("Floor already has an encounter room.");
    EncounterRoom = room;
}
```

At the end of `DungeonGenerator.Generate()`:

```csharp
var encounter = new EncounterRoomGenerator().TryGenerate(runSeed, floor, generationVersion);
if (encounter != null)
{
    EncounterRoomValidator.Validate(floor, encounter);
    floor.CellAt(encounter.Doorway).Structure = ContentId.Parse(
        encounter.DoorKind == EncounterDoorKind.Locked ? "tile.door_locked" : "tile.door_closed");
    floor.SetEncounterRoom(encounter);
}
return floor;
```

- [ ] **Step 7: Run focused and dungeon regressions**

```bash
python3 -m pytest -q scripts/tests/test_encounter_room_contracts.py scripts/tests/test_dungeon_contracts.py
python3 scripts/validate_dungeon_contracts.py
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add Assets/ClickDungeon/Dungeon scripts/tests/test_encounter_room_contracts.py
git commit -m "feat: generate deterministic encounter rooms"
```

---

### Task 4: Add deterministic key consumption and door interaction

**Files:**
- Modify: `Assets/ClickDungeon/Progression/InventoryState.cs`
- Create: `Assets/ClickDungeon/Application/Gameplay/EncounterRoomRuntimeState.cs`
- Create: `Assets/ClickDungeon/Application/Gameplay/EncounterRoomService.cs`
- Modify: `scripts/tests/test_encounter_room_contracts.py`
- Modify: `scripts/bot/ClickDungeon.BotSoak.csproj`
- Modify: `scripts/bot/Program.cs`

**Interfaces:**
- Produces: `bool InventoryState.TryRemoveDefinition(ContentId definitionId, int quantity)`.
- Produces: `bool EncounterRoomService.TryOpenDoor(EncounterRoomRuntimeState room, InventoryState inventory)`.

- [ ] **Step 1: Add RED contracts and bot assertions**

Add source assertions for `TryRemoveDefinition`, `TryOpenDoor`, `item.key.dungeon`, and absence of `item.key.royal` from the door service. Add a bot self-test that constructs one closed and one locked room and asserts:

```csharp
Require(service.TryOpenDoor(closedRoom, inventory), "closed door must open freely");
Require(inventory.Entries.Count == 0, "closed door must not consume a key");
Require(!service.TryOpenDoor(lockedRoom, inventory), "locked door must reject missing key");
inventory.Add("key-stack", ContentId.Parse("item.key.dungeon"), 2);
Require(service.TryOpenDoor(lockedRoom, inventory), "locked door must open with dungeon key");
Require(inventory.GetRequired("key-stack").Quantity == 1, "locked door must consume exactly one key");
Require(service.TryOpenDoor(lockedRoom, inventory), "already-open door is idempotent success");
Require(inventory.GetRequired("key-stack").Quantity == 1, "re-entry must not consume another key");
```

- [ ] **Step 2: Run bot and prove RED**

```bash
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 1
```

Expected: compile/assertion failure because room runtime/service/key-removal APIs do not exist.

- [ ] **Step 3: Implement deterministic definition-level removal**

```csharp
public bool TryRemoveDefinition(ContentId definitionId, int quantity)
{
    if (quantity <= 0) return false;
    var matches = new List<InventoryEntry>();
    var available = 0;
    foreach (var entry in entries.Values)
    {
        if (entry.DefinitionId != definitionId) continue;
        matches.Add(entry);
        available = checked(available + entry.Quantity);
    }
    if (available < quantity) return false;
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

- [ ] **Step 4: Implement door state and service**

`EncounterRoomRuntimeState` stores the generated layout, mutable `IsDoorOpen`, monster runtime states, `IsCleared`, and chest runtime state. Door implementation:

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

`MarkDoorOpen()` may transition only `false -> true` and the owning main-floor structure must be changed to `tile.door_open` by the gameplay session when the service reports success.

- [ ] **Step 5: Run focused regressions**

```bash
python3 -m pytest -q scripts/tests/test_encounter_room_contracts.py scripts/tests/test_item_reward_contracts.py
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 1
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add Assets/ClickDungeon/Progression/InventoryState.cs \
        Assets/ClickDungeon/Application/Gameplay/EncounterRoomRuntimeState.cs \
        Assets/ClickDungeon/Application/Gameplay/EncounterRoomService.cs \
        scripts/tests/test_encounter_room_contracts.py scripts/bot
git commit -m "feat: add encounter room door interaction"
```

---

### Task 5: Implement room clearing and deterministic exactly-once rewards

**Files:**
- Modify: `Assets/ClickDungeon/Application/Gameplay/EncounterRoomRuntimeState.cs`
- Modify: `Assets/ClickDungeon/Application/Gameplay/EncounterRoomService.cs`
- Modify: `Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs`
- Modify: `Assets/ClickDungeon/Application/Persistence/GameSessionPersistenceOrchestrator.cs`
- Modify: `scripts/bot/Program.cs`

**Interfaces:**
- Produces: `bool EncounterRoomService.RecordMonsterDefeated(EncounterRoomRuntimeState room, string entityId)`.
- Produces: `bool EncounterRoomService.TryClaimChest(EncounterRoomRuntimeState room, int chestIndex, RewardGrantService grants, out RewardGrant reward)`.
- Produces: `bool GameplaySession.TryOpenEncounterDoor(InventoryState inventory)` and `bool GameplaySession.TryClaimEncounterChest(int chestIndex, out RewardGrant reward)`.

- [ ] **Step 1: Add RED bot scenarios for sealing, clear, reward stability, and duplicate rejection**

```csharp
Require(!service.TryClaimChest(room, 0, grants, out _), "reward must be sealed while monsters survive");
foreach (var monster in room.Monsters)
    service.RecordMonsterDefeated(room, monster.EntityId);
Require(room.IsCleared, "final monster must clear room");
Require(room.Chests.All(c => c.IsUnlocked), "clear must unlock every room chest");
Require(service.TryClaimChest(room, 0, grants, out var first), "first chest claim must succeed");
Require(first.TransactionId == $"room:{room.Layout.FloorIndex}:{room.Layout.RoomId}:chest:0", "room transaction identity must be stable");
Require(!service.TryClaimChest(room, 0, grants, out _), "duplicate room chest claim must be rejected");
```

- [ ] **Step 2: Run bot and prove RED**

```bash
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 1
```

Expected: compile/assertion failure for missing room-clear/reward methods.

- [ ] **Step 3: Implement deterministic chest count and reward resolution**

At runtime-state creation, derive chest count from layout reward mode/tier:

```csharp
var chestCount = layout.RewardMode == EncounterRewardMode.PremiumChest
    ? 1
    : (layout.RewardTier == EncounterRewardTier.Higher ? 3 : 2 + (int)(layout.RoomSeed % 2UL));
```

Each chest stores `Index`, `IsUnlocked`, `IsClaimed`, and stable `TransactionId`.

Use `LootResolver.Resolve(candidates, layout.RoomSeed ^ ((ulong)chestIndex * 0x9E3779B97F4A7C15UL))`. Define candidate sets explicitly in `EncounterRoomService`: normal tier includes Common/Uncommon/Rare canonical items with weights 60/30/10; higher tier excludes Common and uses Uncommon/Rare/Epic/Legendary weights 45/30/20/5. Resolve candidates by `CanonicalItems` rarity, not by hardcoded display strings.

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

- [ ] **Step 5: Wire gameplay session and autosave**

Add room APIs to `GameplaySession`; on successful door open set the parent doorway `Structure = tile.door_open`. Add orchestrator wrappers that request `AutosaveReason.ResolvedTurn` after door/monster state changes and `AutosaveReason.RewardCommitted` after successful room-chest claims; do not add a new autosave enum value.

- [ ] **Step 6: Run behavior and reward regressions**

```bash
python3 -m pytest -q scripts/tests/test_gameplay_session_contracts.py scripts/tests/test_item_reward_contracts.py scripts/tests/test_encounter_room_contracts.py
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 10
```

Expected: PASS with no duplicate grant.

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Application scripts/bot/Program.cs
git commit -m "feat: add encounter room combat rewards"
```

---

### Task 6: Persist encounter rooms with run-save schema v2

**Files:**
- Modify: `Assets/ClickDungeon/Save/SaveSchema.cs`
- Modify: `Assets/ClickDungeon/Save/RunSave.cs`
- Create: `Assets/ClickDungeon/Save/RunSaveV1ToV2Migration.cs`
- Modify: `Assets/ClickDungeon/Save/SaveRepository.cs`
- Modify: `Assets/ClickDungeon/Save/SaveValidator.cs`
- Create: `Assets/ClickDungeon/Application/Persistence/EncounterRoomSaveMapper.cs`
- Modify: `scripts/tests/test_save_progression_contracts.py`
- Modify: `scripts/bot/Program.cs`

**Interfaces:**
- Produces: `RunSave.EncounterRooms : List<EncounterRoomSave>`.
- Produces: `EncounterRoomSaveMapper.ToSave(EncounterRoomRuntimeState) -> EncounterRoomSave` and `FromSave(EncounterRoomSave, EncounterRoomLayout) -> EncounterRoomRuntimeState`.

- [ ] **Step 1: Write RED save/migration contracts**

Add to `test_save_progression_contracts.py`:

```python
def test_run_schema_v2_persists_encounter_rooms(self):
    schema = self.read("Assets/ClickDungeon/Save/SaveSchema.cs")
    run = self.read("Assets/ClickDungeon/Save/RunSave.cs")
    self.assertIn("RunVersion = 2", schema)
    for token in ("EncounterRoomSave", "EncounterRooms", "DoorKind", "DoorOpened",
                  "RoomSeed", "Monsters", "Cleared", "RewardMode", "RewardTier", "Chests"):
        self.assertIn(token, run)

def test_v1_to_v2_run_migration_is_registered(self):
    migration = self.read("Assets/ClickDungeon/Save/RunSaveV1ToV2Migration.cs")
    repository = self.read("Assets/ClickDungeon/Save/SaveRepository.cs")
    for token in ("FromVersion => 1", "ToVersion => 2", "EncounterRooms"):
        self.assertIn(token, migration)
    self.assertIn("RunSaveV1ToV2Migration", repository)
```

- [ ] **Step 2: Run and prove RED**

```bash
python3 -m pytest -q scripts/tests/test_save_progression_contracts.py
```

Expected: FAIL because schema is v1 and DTO/migration do not exist.

- [ ] **Step 3: Define concrete v2 DTOs**

Add `EncounterChestSave`, `EncounterMonsterSave`, and `EncounterRoomSave`. Required properties:

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

`RunSave` adds:

```csharp
public List<EncounterRoomSave> EncounterRooms { get; set; } = new List<EncounterRoomSave>();
```

Set `SaveSchema.RunVersion = 2`.

- [ ] **Step 4: Implement and register the sequential migration**

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

Change the default `SaveRepository` constructor to create a run registry through a private helper:

```csharp
private static SaveMigrationRegistry<RunSave> CreateRunMigrations()
{
    var registry = new SaveMigrationRegistry<RunSave>();
    registry.Register(new RunSaveV1ToV2Migration());
    return registry;
}
```

- [ ] **Step 5: Validate v2 room data**

`SaveValidator.ValidateRun()` must require non-null `EncounterRooms`, unique room IDs, matching `ParentFloorIndex == save.FloorIndex`, doorway x/y within 0..4, monster count 3..5, chest count 1..3, unique chest transaction IDs, and claimed => unlocked. It must reject unsupported future run versions as before.

- [ ] **Step 6: Add runtime/save mapper and round-trip bot test**

`EncounterRoomSaveMapper` must preserve exact entity ID, definition ID, coordinate, current health, statuses, door state, clear state, reward mode/tier, chest unlock/claim state. Bot round-trip assertions must include a partially damaged surviving monster and one defeated monster, then compare restored values field-for-field.

- [ ] **Step 7: Run save and bot regressions**

```bash
python3 -m pytest -q scripts/tests/test_save_progression_contracts.py scripts/tests/test_encounter_room_contracts.py
python3 scripts/validate_save_progression_contracts.py
dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 10
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add Assets/ClickDungeon/Save Assets/ClickDungeon/Application/Persistence \
        scripts/tests/test_save_progression_contracts.py scripts/bot/Program.cs
git commit -m "feat: persist encounter rooms in run save v2"
```

---

### Task 7: Expand the gameplay bot into encounter-room soak coverage

**Files:**
- Modify: `scripts/bot/Program.cs`
- Modify: `scripts/bot/ClickDungeon.BotSoak.csproj`
- Modify: `scripts/tests/test_bot_soak.py`

**Interfaces:**
- Produces final line: `BOT_SOAK_PASS runs=<N> turns=<N> rooms=<N> closed=<N> locked=<N> room_rewards=<N> determinism=PASS reward_idempotency=PASS chest_idempotency=PASS room_idempotency=PASS`.

- [ ] **Step 1: Update the Python expectation first (RED)**

```python
def test_bot_soak_reports_encounter_room_invariants():
    result = run_bot(50)
    assert result.returncode == 0, result.stdout + result.stderr
    line = result.stdout.strip().splitlines()[-1]
    for token in ("BOT_SOAK_PASS", "rooms=", "closed=", "locked=", "room_rewards=",
                  "determinism=PASS", "room_idempotency=PASS"):
        assert token in line
```

- [ ] **Step 2: Run and prove RED**

```bash
python3 -m pytest -q scripts/tests/test_bot_soak.py
```

Expected: FAIL because the bot summary does not yet report encounter-room metrics.

- [ ] **Step 3: Add deterministic room-driving logic**

For each generated room, the bot must: attempt locked entry without key; add exactly one Dungeon Key and retry; enter; damage/defeat room monsters through normal combat helpers; attempt chest claim before final kill; save/restore once mid-room; clear remaining monsters; claim every chest; attempt every chest again; leave/re-enter; regenerate the same seed and compare room ID, doorway, door type, room seed, monster definitions/positions, reward mode/tier.

Use stable assertions and counters, not probabilistic `Random` calls. Ensure the 50-run seed set contains at least one closed and one locked room; if the existing consecutive seed window does not, use fixed supplemental seeds discovered by deterministic generator enumeration and commit those literal seed values into the bot test table.

- [ ] **Step 4: Emit the extended summary**

```csharp
Console.WriteLine(
    $"BOT_SOAK_PASS runs={runs} turns={turns} rooms={roomCount} closed={closedCount} " +
    $"locked={lockedCount} room_rewards={roomRewardCount} determinism=PASS " +
    "reward_idempotency=PASS chest_idempotency=PASS room_idempotency=PASS");
```

- [ ] **Step 5: Run several soak passes**

```bash
for i in 1 2 3; do
  dotnet run --project scripts/bot/ClickDungeon.BotSoak.csproj --configuration Release -- --runs 50
done
python3 -m pytest -q scripts/tests/test_bot_soak.py
```

Expected: all three bot passes and pytest PASS; each pass reports non-zero `rooms`, `closed`, `locked`, and `room_rewards`.

- [ ] **Step 6: Commit**

```bash
git add scripts/bot scripts/tests/test_bot_soak.py
git commit -m "test: soak encounter room gameplay flows"
```

---

### Task 8: Run the complete source gate and repair only evidence-backed regressions

**Files:**
- Modify only files proven necessary by failing tests/validators.

**Interfaces:**
- Produces: a clean source-level feature SHA with all pytest validators and 50-run bot soak green.

- [ ] **Step 1: Run complete source validation locally**

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
```

Expected: all PASS in source mode. `validate_production_art.py --strict` may still fail only for missing Unity-generated `.meta` or other previously missing Phase-16 art outside this feature.

- [ ] **Step 2: If any command fails, use systematic debugging before changing code**

For each failure, capture the exact failing test/validator output, identify the owning task contract, add/adjust the narrowest regression test, and make the minimum correction. Do not weaken counts, percentages, save validation, reward idempotency, or strict art requirements to make the gate green.

- [ ] **Step 3: Verify CI on the exact pushed SHA**

```bash
git push origin new
```

Observe GitHub Actions `Source Validation` on the pushed SHA and require: pytest PASS, every source validator PASS, deterministic tile-extraction check PASS, and C# bot soak PASS.

- [ ] **Step 4: Commit any evidence-backed repair separately**

```bash
git add <only-files-changed-by-the-repair>
git commit -m "fix: repair encounter room source validation"
```

Skip this commit step when no repair was required.

---

### Task 9: Import through Unity 6 and complete the player-facing art/runtime gate

**Files:**
- Unity-generated: `.meta` files for new/modified Assets.
- Authentic Unity project files: `ProjectSettings/`, `Packages/` only if generated by the real Unity 6 project import.
- Create/modify Unity presentation/runtime adapter files only after authentic Unity metadata exists, following the Phase-16 plan.

**Interfaces:**
- Consumes: exact source-green SHA from Task 8.
- Produces: Unity-authenticated imports for all 25 tiles, strict-art evidence, compile/EditMode/PlayMode evidence, visual verification of 5x5 + 3x3 surfaces.

- [ ] **Step 1: Open the exact `new` worktree in Unity 6 and allow a complete import**

Do not create `.meta` files manually. After import completes, close Unity and inspect:

```bash
git status --short
git diff --check
```

Expected: Unity-generated metadata/import changes only; `Library/`, `Temp/`, `Logs/`, and `Obj/` remain untracked/ignored.

- [ ] **Step 2: Run strict production-art validation**

```bash
python3 scripts/validate_production_art.py --strict
```

Expected for this feature's tile surface: all 25 tile PNGs and their `.meta` files are accepted; chest `closed.png`/`open.png` roles also have Unity-generated `.meta`. If strict validation fails on unrelated Phase-16 art categories, retain that exact failure as the next broader release blocker rather than weakening the validator.

- [ ] **Step 3: Continue the existing Phase-16 Unity gates**

Follow `docs/superpowers/plans/2026-09-15-phase-16-release-validation.md` from its Unity metadata/import GREEN boundary: Unity compile, EditMode, PlayMode, runtime smoke, Windows player, Android APK/AAB, iOS Xcode export, mutation guards, and artifact inspection.

For encounter rooms, add Unity tests that assert: 5x5 parent board remains 5x5; entering a room switches presentation to 3x3; door sprites map closed/locked/open correctly; sealed chest uses closed art; cleared/claimed chest uses open art; returning restores the same parent-floor state.

- [ ] **Step 4: Perform visual acceptance against the approved references**

Capture runtime evidence showing at minimum: normal closed door on 5x5 board; locked door on 5x5 board; open-door transition; 3x3 room with 3-5 monsters and visible sealed chest(s); cleared room with unlocked/openable chest(s). Compare tile silhouettes/edges/lighting against `11-dungeon-tiles-a.png` and `12-dungeon-tiles-b.png`.

- [ ] **Step 5: Commit only authentic Unity changes**

```bash
git add Assets ProjectSettings Packages
git status --short
git commit -m "build: import encounter room tiles in Unity 6"
git push origin new
```

Do not stage Unity cache folders.

---

## Final Verification

Before claiming this feature complete, run a fresh verification on the exact final `new` SHA:

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

When authentic Unity metadata exists, also require:

```bash
python3 scripts/validate_production_art.py --strict
```

Then require the Phase-16 Unity compile/EditMode/PlayMode/runtime/build gates on that same SHA. Only after those are green should `/Gaudit` and the broader `new -> main` release PR proceed.

## Plan Self-Review

- **Spec coverage:** every approved spec area is assigned: 25 tiles (Tasks 1-2), deterministic generation (Task 3), closed/locked door semantics (Task 4), 3-5 monsters + sealed rewards + stronger locked rewards + exactly-once chest IDs (Task 5), save-v2/migration (Task 6), automated bot/re-entry/determinism/duplicate checks (Task 7), complete source gate (Task 8), Unity import/strict-art/player-facing verification (Task 9).
- **Placeholder scan:** no TBD/TODO/future-fill steps; explicit paths, commands, signatures, and expected outcomes are present.
- **Type consistency:** `EncounterRoomLayout`, `EncounterRoomRuntimeState`, `EncounterRoomService`, `EncounterDoorKind`, `EncounterRewardMode`, `EncounterRewardTier`, `TryRemoveDefinition`, stable room transaction IDs, and save DTO names are used consistently across tasks.
