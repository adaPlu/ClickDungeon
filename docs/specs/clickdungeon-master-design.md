# ClickDungeon Master Design Specification

**Status:** Approved 2026-09-14  
**Repository:** `Clickd`  
**Player-facing title:** `ClickDungeon`  
**Targets:** Windows, Android, iOS  
**Engine:** Unity 6, C#  
**Primary references:** `docs/reference/01-*.png` through `08-*.png`

## 1. Executive Game Summary

ClickDungeon is a 2D top-down tile-based dungeon crawler built around compact roguelike runs. The initial game uses deterministic 5×5 dungeon floors, progressive discovery, turn-based movement and combat, traps, treasure, equipment, hero classes, bosses, floor advancement, and persistent meta progression.

Core loop: **Choose Hero → Enter Floor → Explore Tiles → Reveal Threats/Rewards → Fight/Interact → Find Keys/Loot → Reach Exit → Descend → Grow Stronger → Boss/Event → Complete or Lose Run → Apply Persistent Progress.**

The game may use genre-level concepts associated with classic mobile dungeon crawlers, but artwork, text, characters, UI implementation, source code, balance, content, and narrative must remain original.

## 2. Image-by-Image Requirements

### Image 1 — Title / Main Menu

Defines the warm torch-lit stone-and-gold visual language and the hierarchy: ClickDungeon logo, Sir Clickington showcase, Continue/Play, hero progression, currencies, daily reward, and navigation.

Interactive controls: hero portrait/name opens hero profile/select; gold/gem plus buttons route to currency/shop flows; Continue resumes a suspended run; Play starts run flow; Hero Select, Inventory, Talents, Shop, Settings and Quit navigate to their respective screens; the Crown is a Challenges/Achievements route; Mail is Inbox/rewards; Daily Reward claims once per valid period; notification badges indicate actionable content. Quit is Windows-specific and is hidden or reinterpreted on mobile.

### Image 2 — Core Gameplay

Defines a 5×5 board with top HUD for hero portrait, level, HP, class resource, currencies, floor identity, and utility controls. The bottom action row exposes Move, Slash, Shield, Dash and Potion, with Inventory, Talents and Shop access. Board examples include hero, monsters, chest, key, locked door, spikes, bomb, pits and stairs. Enemy health bars and a clear selected-cell highlight are required presentation patterns.

Visibility is simulation state independent of art: `Unseen → Discovered → Visible/Resolved`. XP exists as progression but is not required as a permanent gameplay HUD bar.

### Images 3–4 — Dungeon Tiles

The initial canonical registry contains 24 concepts: stone floor, pit trap, bomb trap, spike trap, stair up, locked stair up, stair down, locked stair down, wall, wall corner, key, chest closed, chest open, door locked, door open, torch, cracked floor, mossy floor, water, lava, shadow/void, pressure plate, teleport, and healing fountain.

High-resolution source masters may be retained, while the canonical runtime tile export is 256×256 transparent PNG. Tile presentation is layered so terrain cannot replace content or actors.

### Image 5 — Hero Roster

Initial roster: Ironheart/Knight, Shadowcut/Rogue, Emberwisp/Wizard, Windsong/Ranger, Lightbringer/Cleric, Rageclaw/Berserker, Gearspark/Engineer, Dawnward/Paladin. Every hero requires a master pose, HUD portrait, roster icon, mini-gameplay representation, and minimum Idle/Attack/Hit/Victory/Defeat animation states.

Exact numeric balance and unlock costs are design data, not extracted facts.

### Image 6 — Monsters and Bosses

Initial enemy identities: Goblin Brute King, Crowned Slime, Skeleton Warrior, Bat Swarm Leader, Mimic Chest, Fire Imp, Armored Boar, Spooky Spellbook, Cave Spider, Theater Curtain Demon. Threat categories include Brute, Swarm, Undead, Magic, Beast and Boss. Minimum animation states: Spawn, Idle, Attack, Hit, Defeat. Bosses require mechanics beyond increased statistics.

### Image 7 — Explicit Gameplay Rules

Requirements: 5×5 movement, trap interaction/damage, chest placement/discovery, key pickup, locked-object interaction, enemy turns/encounter triggers, floor completion, exit progression, readable stone-dungeon presentation, warm torch lighting, readable props, clean top-down composition, ClickDungeon branding, repeated-click chest opening, special-key chests, reward burst, and base-slice completion before biome expansion.

### Image 8 — Sir Clickington / Lord Blobert / Reward Sequence

Sir Clickington is a unique identity with Neutral, Happy, Confident, Worried, Shocked, Angry, Victorious and Defeated portrait states. His presentation archetype is **Mascot**, but his mechanical class ID remains `class.knight`, shared with Ironheart; he retains unique art, text, dialogue and campaign data and must not fork the Knight combat implementation. Lord Blobert is a named campaign boss built from reusable boss mechanics. The visible chest sequence is Anticipation → Burst/Reveal → comedic reaction → Triumph, supported by a finer technical state machine.

## 3. Canonical Game Rules

Explicit rules: 5×5 initial board; grid movement; traps and damage; discoverable dungeon contents; keys and locks; repeated-interaction chests; special-key chests; enemy turns; exits/floor progression; stone launch biome; readable layered presentation; eight initial hero classes; distinct Sir Clickington identity; bosses with special mechanics; player-facing branding is ClickDungeon; biome expansion follows the base slice.

Inferred launch rules: orthogonal movement; one player command followed by deterministic enemy resolution; seeded generation; most meaningful actions consume a turn; persistent profile state is separated from run state; suspended runs can resume; equipment modifies computed runtime stats; status effects resolve at deterministic boundaries.

## 4. Core Gameplay Loop

Generate a deterministic floor; place the hero safely; reveal initial information; accept and validate a player command; resolve movement/ability/interaction; resolve tile effects; resolve enemies in deterministic order; resolve statuses/deaths/rewards/objectives; update visibility; autosave stable run state; repeat until the exit is valid; advance to next floor or boss/event; apply only valid persistent rewards when the run ends.

## 5. Unity Architecture

Preferred model: immutable/data-driven definitions + mutable POCO runtime state + deterministic pure-C# simulation + Unity presentation. Simulation is authoritative; animation/audio/VFX never grant damage, loot, currency or progression.

Assemblies: `ClickDungeon.Core`, `ClickDungeon.Content`, `ClickDungeon.Simulation`, `ClickDungeon.Dungeon`, `ClickDungeon.Combat`, `ClickDungeon.Progression`, `ClickDungeon.Narrative`, `ClickDungeon.Persistence`, `ClickDungeon.Application`, `ClickDungeon.Presentation`, `ClickDungeon.UI`, `ClickDungeon.Platform`, `ClickDungeon.Editor`, `ClickDungeon.Tests`.

Dependency direction: **Definition Data → Runtime State → Simulation Logic → Application Commands/Events → Presentation Adapters → Unity UI/Sprite/Animation/Audio/VFX**.

Major services: `GameBootstrap`, `ApplicationStateMachine`, `GameSessionService`, `RunService`, content registries, `DungeonGenerator`, `FloorValidator`, `VisibilityResolver`, `TileInteractionResolver`, `TurnResolver`, `CombatResolver`, `AbilityResolver`, `StatusEffectResolver`, `EnemyAiResolver`, `LootResolver`, `InventoryService`, `EquipmentResolver`, `ExperienceService`, `ProgressionService`, `ChestRewardService`, `CurrencyService`, `SaveService`, `SaveMigrationService`, `NarrativeService`, `CampaignService`, `DialogueService`, `InputRouter`, `ScreenNavigator`, `AudioService`, `VfxService`, `AnimationCoordinator`, `PlatformService` and `LifecycleService`.

## 6. Dungeon and Tile Architecture

Every logical coordinate has independent layers: **Base Terrain → Structure → Content → Actor → State Overlay**. Definitions include stable ID, display name, category, layer, sprite set, reveal policy, movement policy, interaction definition, tags, state schema, link policy, animation/audio/VFX cues and persistence policy. Runtime state stores coordinate, definition IDs, visibility, mutable state values, link IDs and resolved state.

Locked/unlocked and closed/open art are presentation states of logical entities rather than duplicated behavior classes.

## 7. Dungeon Generation

Use a deterministic PRNG independent of `UnityEngine.Random`. Derive a floor from `RunSeed + GenerationVersion + FloorIndex + AttemptIndex`. Create start and distant exit, guaranteed traversable backbone, optional blockers, difficulty budget, mandatory progression cells, keys/locks, enemies, traps, treasure and specials, then validate. Invalid floors regenerate deterministically with incremented attempt index.

Validation proves start/exit validity, traversal, key-before-lock reachability, valid teleporter/pressure-plate links, legal actor placement, safe start, exactly-once mandatory objectives and resolvable content IDs.

## 8. Combat

Turn order: accept command → validate → resolve player action → immediate tile effects → deaths → enemy intents → enemies ordered by initiative then stable entity ID → end-turn statuses/environment → rewards/objectives → presentation events.

Recommended understandable launch damage: `Base = AbilityFlatDamage + floor(Attack × AbilityScaling)`; `Mitigation = floor(Defense × ArmorCoefficient)`; `Damage = max(1, Base - Mitigation)`. Crits are deterministic data-driven rolls. Generic status infrastructure supports poison, burn, bleed, stun, slow, vulnerable, shielded, regeneration and marked effects.

## 9. Hero and Class System

`HeroIdentityDefinition` is separate from `HeroClassDefinition`. The class owns mechanics; identity owns name, art, flavor, dialogue, campaign and unlock presentation. Initial class identities are Knight, Rogue, Wizard, Ranger, Cleric, Berserker, Engineer and Paladin. Stats, growth, equipment and ability values are data definitions subject to balance iteration.

## 10. Sir Clickington

`hero.sir_clickington.classId == class.knight` and `hero.ironheart.classId == class.knight`. Identity IDs, art IDs, flavor, dialogue and story IDs must differ. Sir Clickington has a dedicated comedic campaign whose events are driven through reusable campaign/dialogue definitions rather than combat forks.

## 11. Monsters and Bosses

`MonsterDefinition`, `BossDefinition`, `EnemyBehaviorDefinition`, `EnemyAbilityDefinition` and `EnemyRuntimeState` are registry-driven. Unknown IDs fail validation in development and never silently map to unrelated content in production. Boss definitions support phase conditions, behavior sets, telegraphs, summons, arena effects, dialogue triggers and unique reward tables.

## 12. Items, Loot, Equipment, Inventory and Rewards

Initial categories: Weapon, Armor, Offhand, Accessory, Consumable, Key, Currency and Quest/Narrative Item. Definitions are immutable; item instances carry instance ID, definition ID, quantity and rolled mutable data. Equipment restrictions use tags. Loot rolls derive from stable run/floor/source/roll context.

## 13. Chest Reward Sequence

Technical states: `Closed → Interacting → Opening → RewardCommitted → Reveal → Presentation → Collection → Complete`. Normal chests default to configurable repeated interactions; special chests may require key tags. Reward generation and inventory/currency mutation occur exactly once at the `Opening → RewardCommitted` boundary, are persisted, and only then drive presentation. Animation never grants rewards.

## 14. Progression and Difficulty

Separate run progression, hero progression and account/meta progression. Depth increases threat budget, trap complexity, elites, special interactions and reward budget rather than only multiplying HP. Stone dungeon is the launch biome.

## 15. UI/UX Screens

Required screens: Title/Main Menu, Gameplay, Hero Select, Hero/Profile, Inventory, Talents/Abilities, Settings, Shop shell, Reward presentation, Story/Dialogue, Pause, Daily Reward and Inbox route. Desktop keeps the broad reference composition; mobile preserves hierarchy while collapsing secondary panels intelligently.

## 16. Art Asset Manifest

Reference sheets are not automatically production sprites. Keep references in `docs/reference/`; runtime art lives under `Assets/ClickDungeon/Art/Runtime/{Heroes,Monsters,Bosses,Tiles,Items,UI,VFX,Chests}`. Runtime tiles are canonical 256×256 transparent PNGs with consistent pivots/PPU. Existing GUIDs are preserved once production assets exist. Validators reject missing and duplicate canonical art IDs.

## 17. Animation

Heroes: Idle, Attack, Hit, Victory, Defeat plus ability-specific clips where needed. Monsters: Spawn, Idle, Attack, Hit, Defeat. Bosses add Telegraph, special attacks, phase transitions and summons. Tile animation includes torch, bomb, spikes, doors, chests, teleport, fountain, lava/water/shadow and pressure plate. Simulation events are authoritative.

## 18. Audio/VFX

Hooks cover UI, hero/enemy attacks and hits, deaths, traps, key/door, teleport, healing, chest interaction/opening, rewards, floor completion, bosses, abilities, victory and defeat. Disabling audio/VFX must not alter gameplay results.

## 19. Input Architecture

Use Unity Input System through `IPlayerInput → PlayerCommand → GameSession`. Commands include movement, targeted abilities, interactions, consumables, inventory and pause/cancel. Windows uses mouse/keyboard; Android/iOS use direct touch and platform back/lifecycle behavior. There is one gameplay-command layer.

## 20. Save/Data Architecture

Separate versioned `ProfileSave`, `RunSave` and `SettingsSave` DTOs. Never serialize scene objects as canonical saves. Run saves contain generation version, seed, hero, floor, board state, HP/resource, inventory, statuses, enemies, tile state, chest reward transaction IDs and objectives. Migrations are explicit and tested. Stable autosaves occur after resolved turns, reward commitment, floor transitions and lifecycle pause/background events.

## 21. Platform Requirements

Windows: x64, mouse/keyboard, scalable UI, fullscreen/windowed and common aspect ratios. Android: touch, back behavior, safe areas, variable ratios, lifecycle save, ARM64 IL2CPP, APK and AAB. iOS: touch, safe areas, iPhone/iPad scaling, lifecycle handling, IL2CPP, Xcode export and secure provider signing only.

## 22. Testing

TDD covers registry integrity, deterministic seeds, valid floor generation, interactions, links, traps, teleport, healing, chest rewards and duplicate prevention, combat, hero/class mapping, Sir Clickington invariants, monster/boss mappings, inventory/equipment, progression, save/load/migration, art registry, UI contracts and branding. Runtime smoke: **Boot → Main Menu → Start Game → Dungeon Ready**, then at least one basic interaction where feasible.

## 23. CI / Build

CI stages: source validation → metadata/content validation → tests → Unity EditMode/PlayMode → Windows build/runtime smoke → Android APK/AAB → iOS Xcode export → artifact inspection. Signing credentials are external secure configuration only. Build/import mutation guards fail on unexplained changes.

## 24. Implementation Phases

0. Reference Audit and Canonical Data
1. Project/Foundation Architecture
2. Deterministic Dungeon Simulation
3. Tile Interaction System
4. Combat
5. Heroes and Classes
6. Monsters and Bosses
7. Loot/Inventory/Equipment
8. Gameplay Presentation
9. Main Menu and Hero Selection
10. Special Tiles and Dungeon Events
11. Sir Clickington Campaign
12. Production Art Integration
13. Animation/Chest Rewards/VFX
14. Save/Progression
15. Windows/Android/iOS Polish
16. CI, Release Validation and Final Audit

Each phase requires fresh automated evidence plus the stated visual validation before proceeding.

## 25. Acceptance Criteria

Feature-complete means ClickDungeon branding is clean; deterministic 5×5 floors can be started/completed; discovery, movement, enemy turns, all initial tile concepts, traps, keys/locks, special tiles, registry-driven enemies, boss mechanics, loot/equipment, repeated-interaction chests with exactly-once rewards, floor progression, hero XP, run save/resume, persistent progression and Sir Clickington campaign work; art/animation/content validators pass; UI works across supported aspect ratios; Windows/Android/iOS build targets validate; signing secrets are never fabricated; runtime smoke passes; and final audit has no release blocker.

## 26. Risk Register

High risks: composite references mistaken for production sprites; visual scope overwhelming gameplay; mutable ScriptableObject state; platform-divergent RNG; unwinnable key/lock layouts; duplicate rewards; broken GUIDs; unknown IDs falling back silently; mobile UI crowding; save schema evolution; boss behavior becoming bespoke; branding residue. Mitigation is explicit manifests, pure simulation, deterministic PRNG, floor validators, transaction-style reward commitment, GUID preservation, responsive UI contracts, versioned migrations and automated guards.

## 27. Open Assumptions

Orthogonal movement; most meaningful commands consume a turn; blue HUD resource is class resource; XP need not be permanently visible; normal chest open-count is configurable with an initial design default of three; runtime tiles use 256×256 exports; Sir Clickington and Ironheart both map to Knight; Lord Blobert is a named campaign boss rather than generic Crowned Slime; gems exist but real-money commerce is outside core gameplay; Crown/Mail are valid routes but not launch-critical content; stone dungeon is the launch biome; a normal run can suspend/resume.

## 28. First Implementation Boundary

Create the greenfield Unity foundation, stable canonical IDs, branding contract, 24-tile manifest, Knight definition, separate Ironheart/Sir Clickington identities, validation tests and baseline CI. Do not start combat, procedural generation, production UI recreation or production art import until this foundation is green.
