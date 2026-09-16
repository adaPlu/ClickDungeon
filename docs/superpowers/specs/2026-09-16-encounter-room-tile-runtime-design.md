# ClickDungeon Encounter Rooms and Canonical Tile Runtime Design

**Status:** Approved in chat 2026-09-16; written-spec review pending  
**Branch:** `new`  
**Applies to:** canonical dungeon tiles, 5x5 floor generation, same-floor encounter rooms, room rewards, persistence, bot/contract validation

## Goal

Turn the two approved dungeon tile sheets into a complete spawnable runtime tile library and add deterministic same-floor monster rooms behind closed or locked doors without changing the main 5x5 dungeon board.

The feature must preserve ClickDungeon's existing deterministic simulation authority, exactly-once reward behavior, save/recovery guarantees, and reference-art fidelity. The approved reference imagery remains the visual authority; generated or substitute art is not permitted when the supplied sheets already contain the required tile.

## Visual authority

The authoritative references for this feature are:

- `docs/reference/latest/10-core-gameplay.png` — 5x5 dungeon composition and tile readability.
- `docs/reference/latest/11-dungeon-tiles-a.png` — first dungeon tile sheet.
- `docs/reference/latest/12-dungeon-tiles-b.png` — second dungeon tile sheet.
- `docs/art/reference-art-source-of-truth.md` — precedence and fidelity policy.

The two tile sheets are source composites only. Runtime must use isolated sprites extracted from those sheets. The composite sheets themselves must never be copied into `Assets/ClickDungeon/Art/Runtime/` as replacement runtime sprites.

## Canonical tile library

The canonical registry expands from 24 to **25 unique logical tiles**. Repeated appearances of the same tile across the two sheets are reconciled to one canonical runtime identity. `door_closed`, `door_locked`, and `door_open` are deliberately distinct because they represent different gameplay states.

| Tile ID | Layer | Runtime sprite |
| --- | --- | --- |
| `tile.floor_stone` | BaseTerrain | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_floor_stone.png` |
| `tile.floor_cracked` | BaseTerrain | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_floor_cracked.png` |
| `tile.floor_moss` | BaseTerrain | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_floor_moss.png` |
| `tile.water` | BaseTerrain | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_water.png` |
| `tile.lava` | BaseTerrain | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_lava.png` |
| `tile.shadow` | BaseTerrain | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_shadow.png` |
| `tile.trap_pit` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_trap_pit.png` |
| `tile.trap_bomb` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_trap_bomb.png` |
| `tile.trap_spike` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_trap_spike.png` |
| `tile.pressure_plate` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_pressure_plate.png` |
| `tile.stair_up` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_stair_up.png` |
| `tile.stair_up_locked` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_stair_up_locked.png` |
| `tile.stair_down` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_stair_down.png` |
| `tile.stair_down_locked` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_stair_down_locked.png` |
| `tile.wall` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_wall.png` |
| `tile.wall_corner` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_wall_corner.png` |
| `tile.door_closed` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_door_closed.png` |
| `tile.door_locked` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_door_locked.png` |
| `tile.door_open` | Structure | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_door_open.png` |
| `tile.key` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_key.png` |
| `tile.chest_closed` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_chest_closed.png` |
| `tile.chest_open` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_chest_open.png` |
| `tile.torch` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_torch.png` |
| `tile.teleport` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_teleport.png` |
| `tile.fountain_heal` | Content | `Assets/ClickDungeon/Art/Runtime/Tiles/tile_fountain_heal.png` |

The canonical tile registry and its validator must require exactly these 25 identities and unique sprite paths.

### Chest presentation compatibility

The supplied closed/open chest artwork is authoritative. The same approved crop supplies both the board-tile role and any existing chest-presentation runtime role. If the production-art manifest requires `Assets/ClickDungeon/Art/Runtime/Chest/closed.png` and `open.png`, those files must be reconciled from the same approved chest source art rather than redrawn or substituted. They are role-specific runtime assets, not alternate visual designs.

Unity-generated `.meta` files are required before strict production-art validation can pass. They must be created by Unity import, not hand-authored.

## Main floor remains 5x5

`FloorState.Width` and `FloorState.Height` remain exactly 5. Encounter rooms never enlarge, reshape, or append cells to the main board. The primary screen therefore keeps the visual composition shown in the core gameplay reference.

A doorway occupies a normal main-floor cell's `Structure` layer. That doorway links to a separate room state on the same dungeon floor.

## Encounter-room model

An encounter room is a separate deterministic **3x3 mini-room** attached to one doorway on the parent 5x5 floor. It uses the same terrain/structure/content/actor/state-overlay model as normal floor cells, but it has its own coordinates and state container.

Each room has at minimum:

- stable `roomId`;
- parent `floorIndex`;
- doorway coordinate on the 5x5 board;
- door type (`Closed` or `Locked`);
- opened state;
- deterministic room seed;
- 3x3 room cells;
- deterministic monster roster and positions;
- per-monster mutable combat state;
- cleared state;
- deterministic reward mode;
- reward chest count/state;
- exactly-once reward transaction IDs.

Only one encounter room may exist on a floor.

## Generation rules

Encounter-room placement is deterministic from the run seed, floor index, and generation version.

Rules:

- Floor 1 never contains an encounter room.
- Floors 2+ have a **35%** chance to contain one encounter room.
- A floor may contain **at most one** encounter room.
- Given that a room exists, **75%** use `tile.door_closed` and **25%** use `tile.door_locked`.
- Door placement must not overwrite the player start, floor exit, required stair/exit semantics, or make the main floor invalid/unreachable.
- A generated room must always be reachable through its doorway once the doorway's own access requirement is satisfied.
- Identical generation inputs must produce the same room existence, doorway, door type, room seed, monster roster/positions, and reward mode.

The existing floor validator remains authoritative for main-board validity; room-specific validation adds doorway and room invariants without weakening existing checks.

## Door behavior

### Closed door

`tile.door_closed` is a normal closed encounter-room door. It opens freely when the player interacts with it. No key is consumed. Once opened, its structure state becomes `tile.door_open` and remains open for the lifetime of the run/save.

### Locked door

`tile.door_locked` requires one `item.key.dungeon` from inventory. Attempting to open it without a Dungeon Key leaves both inventory and door state unchanged. A successful unlock consumes exactly one Dungeon Key and changes the structure state to `tile.door_open`.

The Royal Key is not used for ordinary encounter-room doors.

Opening either door reveals/allows entry to the attached 3x3 room. Reopening or re-entering an already opened door never consumes another key.

## Monster encounter

Each generated room contains **3 to 5 monsters**, chosen and positioned deterministically using the room seed and existing depth/difficulty systems.

Requirements:

- Monsters use canonical enemy definitions and normal combat rules.
- Monster count scales within the approved 3-5 range; difficulty may increase with depth through enemy selection/stats rather than exceeding the room's approved maximum population.
- Room reward chests are visible from room entry but are sealed/non-interactable while any room monster remains alive.
- Defeated monsters remain defeated when the player leaves and re-enters the room.
- Killing the final surviving monster sets the room to `cleared` and unlocks its reward chest(s).
- A cleared room never respawns monsters.

## Reward model

Room rewards are generated deterministically when the room is generated; clearing the room merely unlocks the already-determined reward state. Reloading or re-entering must never reroll it.

Two reward modes are supported:

1. **Premium chest** — one higher-value chest using an increased loot budget / higher-rarity candidate weighting.
2. **Multi-chest** — 2-3 normal-value chests, each resolved independently from stable derived seeds.

Normal closed-door rooms may resolve either reward mode at the normal room reward tier. Locked-door rooms always use the **higher reward tier**; they may resolve one premium chest or three standard chests with the improved locked-room loot budget.

All item selection reuses the existing deterministic `LootResolver`. Reward granting reuses `RewardGrantService` and `RewardLedger`.

Every chest receives a stable transaction identity derived from the room, for example:

`room:<floorIndex>:<roomId>:chest:<index>`

A transaction may commit at most once. Repeated clicks, room re-entry, save/load, retries, or duplicate commands must not duplicate inventory/currency rewards.

## Persistence and migration

The run save schema advances from **version 1 to version 2**.

`RunSave` gains an `EncounterRooms` collection. Each `EncounterRoomSave` persists enough mutable state to resume exactly where the player left off, including:

- room identity and parent floor;
- doorway coordinate and door type;
- door opened/unlocked state;
- room seed;
- 3x3 cell state where mutable;
- monster entity IDs, definitions, positions, health, and statuses;
- cleared state;
- reward mode/tier;
- chest count and claimed/unlocked state;
- committed room reward transaction IDs when needed for explicit room reconstruction.

The existing top-level committed reward transaction ledger remains the final authority preventing duplicate grants.

A registered v1->v2 migration must advance exactly one schema version and initialize `EncounterRooms` to an empty collection for legacy saves. Existing v1 run state must otherwise remain unchanged. Mid-fight v2 saves restore exact surviving-monster health/statuses and keep defeated monsters absent/defeated. Cleared rooms remain cleared after reload.

## Rendering and presentation

The 5x5 board continues to render using independent `BaseTerrain -> Structure -> Content -> Actor -> StateOverlay` layers.

Encounter rooms use the same layered semantics in a 3x3 room view. Entering a room changes the active board presentation to the attached room without changing the logical parent floor index. Returning through the open doorway restores the parent 5x5 board state.

Door and chest state must use the supplied sprites:

- closed normal door -> `tile_door_closed.png`;
- locked door -> `tile_door_locked.png`;
- opened door -> `tile_door_open.png`;
- sealed/unclaimed reward -> approved closed-chest sprite;
- claimed/opened reward -> approved open-chest sprite.

No generic placeholder may substitute for these supplied assets once extraction is complete.

## Asset-processing contract

All 25 canonical tiles must be extracted/reconciled from the two approved dungeon tile sheets and committed as isolated runtime PNGs.

Processing rules:

- preserve the supplied artwork's appearance and transparent/edge treatment;
- do not use image generation to replace a tile that is present in the sheets;
- do not leave labels, sheet backgrounds, neighboring tiles, or captions in isolated sprites;
- repeated depictions of the same logical tile must resolve to one canonical tile identity;
- preserve existing Unity GUIDs when an equivalent tracked runtime sprite already exists;
- otherwise let Unity generate the new `.meta` file on import;
- never hand-author guessed Unity GUIDs or `.meta` files;
- strict production-art validation must eventually require every canonical tile runtime PNG and corresponding `.meta`.

## Testing strategy

Implementation is RED -> GREEN throughout.

### Tile/art contracts

Tests must first fail on the current 24-tile contract, then require exactly 25 canonical tile IDs and unique sprite paths including `tile.door_closed`. Source validation must also prove that all approved tile identities are represented in the production-art manifest. Strict validation remains expected to fail until actual runtime PNGs and Unity-generated `.meta` files exist.

### Encounter generation

Deterministic tests cover:

- no room on Floor 1;
- Floor 2+ eligibility;
- deterministic 35% room spawn rule;
- at most one room per floor;
- deterministic 75/25 closed-vs-locked door selection among spawned rooms;
- legal doorway placement;
- no start/exit overwrite;
- repeat generation equality for the same inputs;
- 3x3 room dimensions;
- 3-5 monster population.

Probability tests must validate the deterministic selection rule over a stable, sufficiently large fixed seed sample rather than relying on flaky random tolerance.

### Door behavior

Tests cover:

- closed door opens without key consumption;
- locked door rejects access with no Dungeon Key;
- locked door consumes exactly one `item.key.dungeon` on successful first unlock;
- open/re-entry never consumes an additional key;
- door sprite/state transitions to `tile.door_open` exactly once.

### Encounter/reward behavior

Tests cover:

- rewards visible but inaccessible while any monster survives;
- killing the final monster clears the room and unlocks rewards;
- leaving/re-entering preserves defeated/surviving monster state;
- cleared rooms never respawn monsters;
- locked rooms always use the higher reward tier;
- reward generation is stable for a room seed;
- every chest has a stable unique transaction ID;
- duplicate reward claims are rejected;
- multi-chest rewards cannot cross-duplicate one another.

### Save/migration behavior

Tests cover:

- schema version 2;
- v1->v2 migration produces empty room collection without altering legacy state;
- mid-fight save/load restores exact monster health/statuses;
- opened doors stay open;
- consumed keys remain consumed;
- cleared rooms stay cleared;
- reward claimed/unclaimed state survives reload;
- committed reward transactions cannot be granted again after reload.

### Automated bot soak

The existing engine-free C# bot expands to deliberately exercise encounter rooms across multiple deterministic seeds. It must:

- encounter both closed and locked doors;
- test locked-door failure without a key and success with a key;
- enter rooms and defeat every monster;
- verify reward sealing before clear and unlocking after clear;
- claim every room chest;
- attempt duplicate claims and prove rejection;
- leave/re-enter partially cleared and fully cleared rooms;
- replay identical seeds and compare room/door/monster/reward outcomes;
- preserve existing movement, health, combat, reward, and determinism invariants.

Unity PlayMode/built-player bot coverage is added after authentic Unity project metadata/import exists; engine-free tests do not substitute for Unity runtime verification.

## Validation and release gates

This feature does not weaken Phase-16 release validation.

Source-level completion requires all affected pytest/source validators and the C# bot soak to be green. Art-source validation must remain deterministic and clean.

Player-facing art completion additionally requires:

- all 25 isolated tile sprites present;
- approved chest runtime roles reconciled from the supplied chest art;
- Unity-generated `.meta` files present;
- strict production-art validation green;
- Unity compile/EditMode/PlayMode verification green once Unity metadata exists;
- runtime visual verification that the 5x5 board and 3x3 encounter room use the approved tile art correctly.

Platform builds and final `/Gaudit` remain part of the broader Phase-16 release sequence and are not bypassed by this feature.

## Non-goals

This change does not:

- enlarge the main dungeon beyond 5x5;
- add more than one encounter room per floor;
- add encounter rooms to Floor 1;
- use the Royal Key for ordinary room doors;
- add procedural image generation for supplied tiles;
- hand-author Unity metadata;
- introduce more than 5 monsters in the 3x3 room;
- change unrelated hero, campaign, shop, or platform systems.

## Acceptance criteria

The design is implemented when all of the following are true:

1. The canonical tile library contains exactly 25 spawnable logical tiles and distinguishes closed, locked, and open doors.
2. All tiles shown in the approved two-sheet dungeon set that are part of the canonical list exist as isolated runtime sprites from the supplied art.
3. The main dungeon remains exactly 5x5.
4. Floors 2+ deterministically support at most one 3x3 same-floor encounter room at the approved 35% frequency and 75/25 door split.
5. Closed doors open freely; locked doors require and consume exactly one Dungeon Key only on first successful unlock.
6. Encounter rooms contain exactly 3-5 deterministic monsters and keep reward chest(s) sealed until all are defeated.
7. Locked rooms guarantee the higher reward tier; all room rewards are deterministic and exactly-once.
8. Run-save schema v2 faithfully restores room, door, monster, chest, and reward state and migrates v1 saves safely.
9. Contract tests and the engine-free bot soak cover normal, locked, persistence, duplicate-reward, and determinism paths.
10. Once authentic Unity metadata is available, strict art/import and Unity runtime verification confirm the supplied visual assets are actually used.
