# ClickDungeon Foundation & Canonical Content Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the greenfield Unity foundation for ClickDungeon with strict assembly boundaries, stable content IDs, the canonical 24-tile manifest, a player-facing branding guard, and separate Sir Clickington/Ironheart identities sharing one Knight class definition.

**Architecture:** Build a Unity 6 project whose content definitions and simulation contracts are separated from Unity presentation. Definitions are immutable data; mutable runtime state will be introduced by later plans. Phase 0–1 establishes the dependency spine and validation gates without implementing dungeon generation, combat, menus, or production art.

**Tech Stack:** Unity 6 (6000.x, exact patch pinned by `ProjectVersion.txt` when the project is created), C#, Unity Test Framework/NUnit, Unity Input System package reserved for later presentation work, Git, GitHub Actions static validation.

**Spec:** `docs/specs/clickdungeon-master-design.md`

## Global Constraints

- Repository name: `Clickd`.
- Player-facing product name: `ClickDungeon`.
- `ClickDungeon2` must not appear in player-facing runtime content.
- Target platforms remain Windows, Android, and iOS.
- Initial logical dungeon board is 5×5; this plan defines contracts only and does not implement the generator.
- Initial canonical tile registry contains exactly the 24 concepts defined by reference image 4.
- Sir Clickington and Ironheart must share `class.knight` mechanics while retaining different hero identity, art, flavor, dialogue, and story IDs.
- Mutable runtime state must not be stored in shared ScriptableObject definitions.
- Production art is not imported in this plan; reference sheets stay under `docs/reference/`.
- No release signing credentials or provider secrets are created or guessed.
- Use TDD: failing test first, minimal implementation second, fresh verification third.
- Commit after each task-level deliverable.

---

## File Structure Locked by This Plan

```text
Clickd/
├── Assets/
│   └── ClickDungeon/
│       ├── Core/
│       │   ├── ClickDungeon.Core.asmdef
│       │   ├── Brand/ProductBrand.cs
│       │   └── Content/ContentId.cs
│       ├── Content/
│       │   ├── ClickDungeon.Content.asmdef
│       │   ├── Definitions/TileDefinition.cs
│       │   ├── Definitions/HeroClassDefinition.cs
│       │   ├── Definitions/HeroIdentityDefinition.cs
│       │   ├── Registries/TileRegistry.cs
│       │   ├── Registries/HeroClassRegistry.cs
│       │   ├── Registries/HeroIdentityRegistry.cs
│       │   └── Canonical/CanonicalContent.cs
│       ├── Application/
│       │   ├── ClickDungeon.Application.asmdef
│       │   └── Bootstrap/GameBootstrap.cs
│       ├── Editor/
│       │   ├── ClickDungeon.Editor.asmdef
│       │   └── Validation/ContentValidationMenu.cs
│       └── Tests/
│           └── EditMode/
│               ├── ClickDungeon.Tests.EditMode.asmdef
│               ├── BrandingTests.cs
│               ├── ContentIdTests.cs
│               ├── TileRegistryTests.cs
│               └── HeroIdentityTests.cs
├── Packages/manifest.json
├── ProjectSettings/ProjectVersion.txt
├── scripts/validate_player_branding.py
├── scripts/validate_reference_manifest.py
├── .github/workflows/source-validation.yml
├── .gitignore
├── README.md
└── docs/...
```

Responsibilities are intentionally narrow: Core owns dependency-free primitives; Content owns immutable definitions and registries; Application owns startup wiring only; Editor owns validation integration; Tests own EditMode contracts; scripts provide Unity-independent CI guards.

---

### Task 1: Create the Unity 6 Project Skeleton and Assembly Boundaries

**Files:**
- Create: `.gitignore`
- Create: `ProjectSettings/ProjectVersion.txt` via Unity project creation, not hand-authored with a fabricated patch
- Create: `Packages/manifest.json` via Unity project creation/package manager
- Create: `Assets/ClickDungeon/Core/ClickDungeon.Core.asmdef`
- Create: `Assets/ClickDungeon/Content/ClickDungeon.Content.asmdef`
- Create: `Assets/ClickDungeon/Application/ClickDungeon.Application.asmdef`
- Create: `Assets/ClickDungeon/Editor/ClickDungeon.Editor.asmdef`
- Create: `Assets/ClickDungeon/Tests/EditMode/ClickDungeon.Tests.EditMode.asmdef`

**Interfaces:**
- Consumes: approved design only.
- Produces: assembly dependency graph where Content references Core, Application references Core+Content, Editor references Core+Content+Application, and EditMode tests reference Core+Content+Application.

- [ ] **Step 1: Create the Unity project using Unity 6**

Create it from Unity Hub/Unity CLI with a supported Unity 6 editor installed on the execution machine. Immediately commit the generated `ProjectSettings/ProjectVersion.txt`; that file is the authoritative exact editor patch. Do not copy the Unity version from another ClickDungeon repository.

- [ ] **Step 2: Add a Unity `.gitignore`**

Use this minimum content:

```gitignore
[Ll]ibrary/
[Tt]emp/
[Oo]bj/
[Bb]uild/
[Bb]uilds/
[Ll]ogs/
UserSettings/
MemoryCaptures/
Recordings/
.vs/
.idea/
.gradle/
*.csproj
*.sln
*.user
*.userprefs
*.pidb
*.booproj
*.svd
*.pdb
*.mdb
*.opendb
*.VC.db
sysinfo.txt
```

- [ ] **Step 3: Create the Core asmdef**

`Assets/ClickDungeon/Core/ClickDungeon.Core.asmdef`:

```json
{
  "name": "ClickDungeon.Core",
  "rootNamespace": "ClickDungeon.Core",
  "references": [],
  "autoReferenced": true
}
```

- [ ] **Step 4: Create the Content asmdef**

```json
{
  "name": "ClickDungeon.Content",
  "rootNamespace": "ClickDungeon.Content",
  "references": ["ClickDungeon.Core"],
  "autoReferenced": true
}
```

- [ ] **Step 5: Create the Application asmdef**

```json
{
  "name": "ClickDungeon.Application",
  "rootNamespace": "ClickDungeon.Application",
  "references": ["ClickDungeon.Core", "ClickDungeon.Content"],
  "autoReferenced": true
}
```

- [ ] **Step 6: Create Editor and EditMode test asmdefs**

Editor assembly:

```json
{
  "name": "ClickDungeon.Editor",
  "rootNamespace": "ClickDungeon.Editor",
  "references": ["ClickDungeon.Core", "ClickDungeon.Content", "ClickDungeon.Application"],
  "includePlatforms": ["Editor"],
  "autoReferenced": true
}
```

EditMode tests:

```json
{
  "name": "ClickDungeon.Tests.EditMode",
  "rootNamespace": "ClickDungeon.Tests.EditMode",
  "references": ["ClickDungeon.Core", "ClickDungeon.Content", "ClickDungeon.Application"],
  "optionalUnityReferences": ["TestAssemblies"],
  "includePlatforms": ["Editor"],
  "autoReferenced": false
}
```

- [ ] **Step 7: Open the project and verify assembly compilation**

Expected: Unity Console contains no compile errors and the five assemblies are recognized.

- [ ] **Step 8: Commit**

```bash
git add .gitignore Assets Packages ProjectSettings
git commit -m "chore: initialize ClickDungeon Unity foundation"
```

---

### Task 2: Establish Product Branding as a Tested Contract

**Files:**
- Create: `Assets/ClickDungeon/Core/Brand/ProductBrand.cs`
- Create: `Assets/ClickDungeon/Tests/EditMode/BrandingTests.cs`
- Create: `scripts/validate_player_branding.py`

**Interfaces:**
- Consumes: `ClickDungeon.Core`.
- Produces: `ProductBrand.PlayerFacingName : string` and a source-tree branding validator.

- [ ] **Step 1: Write the failing branding unit test**

```csharp
using ClickDungeon.Core.Brand;
using NUnit.Framework;

namespace ClickDungeon.Tests.EditMode
{
    public sealed class BrandingTests
    {
        [Test]
        public void PlayerFacingName_IsClickDungeon()
        {
            Assert.That(ProductBrand.PlayerFacingName, Is.EqualTo("ClickDungeon"));
        }

        [Test]
        public void PlayerFacingName_DoesNotContainLegacySuffix()
        {
            Assert.That(ProductBrand.PlayerFacingName, Does.Not.Contain("ClickDungeon2"));
        }
    }
}
```

- [ ] **Step 2: Run the EditMode test and prove it fails**

Use Unity Test Runner or batchmode for `BrandingTests`. Expected: compile/test failure because `ProductBrand` does not exist.

- [ ] **Step 3: Implement the minimum branding type**

```csharp
namespace ClickDungeon.Core.Brand
{
    public static class ProductBrand
    {
        public const string PlayerFacingName = "ClickDungeon";
    }
}
```

- [ ] **Step 4: Re-run `BrandingTests`**

Expected: both tests pass.

- [ ] **Step 5: Write a Unity-independent branding scanner**

`scripts/validate_player_branding.py`:

```python
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
PLAYER_FACING_ROOTS = [
    ROOT / "Assets" / "ClickDungeon",
]
ALLOWED = {
    ROOT / "Assets" / "ClickDungeon" / "Tests" / "EditMode" / "BrandingTests.cs",
}

violations = []
for base in PLAYER_FACING_ROOTS:
    if not base.exists():
        continue
    for path in base.rglob("*"):
        if not path.is_file() or path in ALLOWED:
            continue
        if path.suffix.lower() not in {".cs", ".json", ".txt", ".asset", ".prefab", ".unity", ".uxml"}:
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        if "ClickDungeon2" in text:
            violations.append(path.relative_to(ROOT).as_posix())

if violations:
    print("Legacy player-facing brand found:")
    for violation in violations:
        print(f" - {violation}")
    sys.exit(1)

print("Player-facing branding validation passed.")
```

- [ ] **Step 6: Run the scanner**

Run:

```bash
python scripts/validate_player_branding.py
```

Expected: `Player-facing branding validation passed.`

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Core/Brand Assets/ClickDungeon/Tests/EditMode/BrandingTests.cs scripts/validate_player_branding.py
git commit -m "test: enforce ClickDungeon player-facing brand"
```

---

### Task 3: Add Stable Content IDs

**Files:**
- Create: `Assets/ClickDungeon/Core/Content/ContentId.cs`
- Create: `Assets/ClickDungeon/Tests/EditMode/ContentIdTests.cs`

**Interfaces:**
- Produces: immutable `ContentId` value type with `Value`, equality, `ToString()`, and `Parse(string)`.

- [ ] **Step 1: Write failing tests**

```csharp
using System;
using ClickDungeon.Core.Content;
using NUnit.Framework;

namespace ClickDungeon.Tests.EditMode
{
    public sealed class ContentIdTests
    {
        [TestCase("terrain.stone")]
        [TestCase("class.knight")]
        [TestCase("hero.sir_clickington")]
        public void Parse_AcceptsCanonicalIds(string value)
        {
            Assert.That(ContentId.Parse(value).Value, Is.EqualTo(value));
        }

        [TestCase("")]
        [TestCase("Terrain.Stone")]
        [TestCase("terrain stone")]
        [TestCase("terrain..stone")]
        public void Parse_RejectsInvalidIds(string value)
        {
            Assert.Throws<ArgumentException>(() => ContentId.Parse(value));
        }

        [Test]
        public void EqualValues_AreEqual()
        {
            Assert.That(ContentId.Parse("class.knight"), Is.EqualTo(ContentId.Parse("class.knight")));
        }
    }
}
```

- [ ] **Step 2: Run and prove failure**

Expected: `ContentId` missing.

- [ ] **Step 3: Implement `ContentId`**

```csharp
using System;
using System.Text.RegularExpressions;

namespace ClickDungeon.Core.Content
{
    public readonly struct ContentId : IEquatable<ContentId>
    {
        private static readonly Regex Pattern = new(
            "^[a-z0-9]+(?:[._-][a-z0-9]+)*$",
            RegexOptions.Compiled | RegexOptions.CultureInvariant);

        public string Value { get; }

        private ContentId(string value) => Value = value;

        public static ContentId Parse(string value)
        {
            if (string.IsNullOrWhiteSpace(value) || !Pattern.IsMatch(value))
                throw new ArgumentException($"Invalid content id: '{value}'", nameof(value));

            return new ContentId(value);
        }

        public bool Equals(ContentId other) => string.Equals(Value, other.Value, StringComparison.Ordinal);
        public override bool Equals(object obj) => obj is ContentId other && Equals(other);
        public override int GetHashCode() => StringComparer.Ordinal.GetHashCode(Value ?? string.Empty);
        public override string ToString() => Value ?? string.Empty;
        public static bool operator ==(ContentId left, ContentId right) => left.Equals(right);
        public static bool operator !=(ContentId left, ContentId right) => !left.Equals(right);
    }
}
```

- [ ] **Step 4: Run `ContentIdTests`**

Expected: all pass.

- [ ] **Step 5: Commit**

```bash
git add Assets/ClickDungeon/Core/Content Assets/ClickDungeon/Tests/EditMode/ContentIdTests.cs
git commit -m "feat: add stable canonical content ids"
```

---

### Task 4: Define the Canonical 24-Tile Registry with Integrity Tests

**Files:**
- Create: `Assets/ClickDungeon/Content/Definitions/TileDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Registries/TileRegistry.cs`
- Create: `Assets/ClickDungeon/Content/Canonical/CanonicalContent.cs`
- Create: `Assets/ClickDungeon/Tests/EditMode/TileRegistryTests.cs`

**Interfaces:**
- Consumes: `ContentId`.
- Produces: `TileDefinition`, `TileCategory`, `TileLayer`, `TileRegistry`, and `CanonicalContent.Tiles`.

- [ ] **Step 1: Write failing registry tests**

```csharp
using System;
using ClickDungeon.Content.Canonical;
using ClickDungeon.Content.Registries;
using NUnit.Framework;

namespace ClickDungeon.Tests.EditMode
{
    public sealed class TileRegistryTests
    {
        [Test]
        public void CanonicalRegistry_HasExactlyTwentyFourTiles()
        {
            var registry = new TileRegistry(CanonicalContent.Tiles);
            Assert.That(registry.Count, Is.EqualTo(24));
        }

        [Test]
        public void CanonicalRegistry_ContainsRequiredIds()
        {
            var registry = new TileRegistry(CanonicalContent.Tiles);
            var required = new[] {
                "terrain.stone", "trap.pit", "trap.bomb", "trap.spike",
                "structure.stair_up", "structure.stair_up_locked",
                "structure.stair_down", "structure.stair_down_locked",
                "structure.wall", "structure.wall_corner", "item.key",
                "treasure.chest_closed", "treasure.chest_open",
                "structure.door_locked", "structure.door_open", "prop.torch",
                "terrain.cracked", "terrain.moss", "terrain.water", "terrain.lava",
                "terrain.shadow", "special.pressure_plate", "special.teleport",
                "special.healing_fountain"
            };

            foreach (var id in required)
                Assert.That(registry.Contains(id), Is.True, id);
        }

        [Test]
        public void DuplicateId_Throws()
        {
            var duplicate = CanonicalContent.Tiles[0];
            Assert.Throws<InvalidOperationException>(() => new TileRegistry(new[] { duplicate, duplicate }));
        }
    }
}
```

- [ ] **Step 2: Run and prove failure**

Expected: missing tile definition/registry/canonical content types.

- [ ] **Step 3: Implement tile definition types**

```csharp
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public enum TileCategory { Terrain, Trap, Structure, Item, Treasure, Prop, Special }
    public enum TileLayer { BaseTerrain, Structure, Content }

    public sealed class TileDefinition
    {
        public ContentId Id { get; }
        public string DisplayName { get; }
        public TileCategory Category { get; }
        public TileLayer Layer { get; }
        public string SpriteId { get; }
        public bool BlocksMovement { get; }
        public bool ContainsContent { get; }
        public bool ModifiesTerrain { get; }
        public bool HasState { get; }
        public bool SupportsLink { get; }

        public TileDefinition(
            string id,
            string displayName,
            TileCategory category,
            TileLayer layer,
            string spriteId,
            bool blocksMovement = false,
            bool containsContent = false,
            bool modifiesTerrain = false,
            bool hasState = false,
            bool supportsLink = false)
        {
            Id = ContentId.Parse(id);
            DisplayName = displayName;
            Category = category;
            Layer = layer;
            SpriteId = spriteId;
            BlocksMovement = blocksMovement;
            ContainsContent = containsContent;
            ModifiesTerrain = modifiesTerrain;
            HasState = hasState;
            SupportsLink = supportsLink;
        }
    }
}
```

- [ ] **Step 4: Implement `TileRegistry`**

```csharp
using System;
using System.Collections.Generic;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Registries
{
    public sealed class TileRegistry
    {
        private readonly Dictionary<ContentId, TileDefinition> _byId = new();
        public int Count => _byId.Count;

        public TileRegistry(IEnumerable<TileDefinition> definitions)
        {
            foreach (var definition in definitions)
            {
                if (!_byId.TryAdd(definition.Id, definition))
                    throw new InvalidOperationException($"Duplicate tile id: {definition.Id}");
            }
        }

        public bool Contains(string id) => _byId.ContainsKey(ContentId.Parse(id));

        public TileDefinition Get(string id)
        {
            var contentId = ContentId.Parse(id);
            if (!_byId.TryGetValue(contentId, out var definition))
                throw new KeyNotFoundException($"Unknown tile id: {contentId}");
            return definition;
        }
    }
}
```

- [ ] **Step 5: Implement the exact 24-entry canonical manifest**

`CanonicalContent.Tiles` must instantiate the 24 IDs listed in the test and map sprite IDs to the reference naming convention. The exact runtime sprite IDs are:

```text
tile_floor_stone
tile_trap_pit
tile_trap_bomb
tile_trap_spike
tile_stair_up
tile_stair_up_locked
tile_stair_down
tile_stair_down_locked
tile_wall
tile_wall_corner
tile_key
tile_chest_closed
tile_chest_open
tile_door_locked
tile_door_open
tile_torch
tile_floor_cracked
tile_floor_moss
tile_water
tile_lava
tile_shadow
tile_pressure_plate
tile_teleport
tile_fountain_heal
```

Classification must follow the spec: terrain uses `BaseTerrain`; walls/stairs/doors use `Structure`; traps, keys, chests, torch, pressure plate, teleport and fountain use `Content`. Stateful/linked flags must be true for locks, chests and special linked structures where applicable.

- [ ] **Step 6: Run `TileRegistryTests`**

Expected: all pass, including exactly 24 definitions and duplicate rejection.

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Content Assets/ClickDungeon/Tests/EditMode/TileRegistryTests.cs
git commit -m "feat: define canonical dungeon tile registry"
```

---

### Task 5: Add Knight Class and Distinct Sir Clickington/Ironheart Identities

**Files:**
- Create: `Assets/ClickDungeon/Content/Definitions/HeroClassDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Definitions/HeroIdentityDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Registries/HeroClassRegistry.cs`
- Create: `Assets/ClickDungeon/Content/Registries/HeroIdentityRegistry.cs`
- Modify: `Assets/ClickDungeon/Content/Canonical/CanonicalContent.cs`
- Create: `Assets/ClickDungeon/Tests/EditMode/HeroIdentityTests.cs`

**Interfaces:**
- Produces: Knight definition, Ironheart identity, Sir Clickington identity, and registries that fail on duplicate/unknown IDs.

- [ ] **Step 1: Write failing identity tests**

```csharp
using ClickDungeon.Content.Canonical;
using ClickDungeon.Content.Registries;
using NUnit.Framework;

namespace ClickDungeon.Tests.EditMode
{
    public sealed class HeroIdentityTests
    {
        [Test]
        public void SirClickington_And_Ironheart_ShareKnightClass()
        {
            var heroes = new HeroIdentityRegistry(CanonicalContent.Heroes);
            Assert.That(heroes.Get("hero.sir_clickington").ClassId.Value, Is.EqualTo("class.knight"));
            Assert.That(heroes.Get("hero.ironheart").ClassId.Value, Is.EqualTo("class.knight"));
        }

        [Test]
        public void SirClickington_And_Ironheart_AreDistinctIdentities()
        {
            var heroes = new HeroIdentityRegistry(CanonicalContent.Heroes);
            var clickington = heroes.Get("hero.sir_clickington");
            var ironheart = heroes.Get("hero.ironheart");

            Assert.That(clickington.Id, Is.Not.EqualTo(ironheart.Id));
            Assert.That(clickington.ArtSetId, Is.Not.EqualTo(ironheart.ArtSetId));
            Assert.That(clickington.StoryId, Is.Not.EqualTo(ironheart.StoryId));
            Assert.That(clickington.FlavorText, Is.Not.EqualTo(ironheart.FlavorText));
        }

        [Test]
        public void KnightClass_ExistsExactlyOnce()
        {
            var classes = new HeroClassRegistry(CanonicalContent.HeroClasses);
            Assert.That(classes.Get("class.knight").DisplayName, Is.EqualTo("Knight"));
            Assert.That(classes.Count, Is.EqualTo(1));
        }
    }
}
```

- [ ] **Step 2: Run and prove failure**

Expected: hero/class types and canonical data do not yet exist.

- [ ] **Step 3: Implement immutable hero class definition**

```csharp
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public sealed class HeroClassDefinition
    {
        public ContentId Id { get; }
        public string DisplayName { get; }

        public HeroClassDefinition(string id, string displayName)
        {
            Id = ContentId.Parse(id);
            DisplayName = displayName;
        }
    }
}
```

- [ ] **Step 4: Implement immutable hero identity definition**

```csharp
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public sealed class HeroIdentityDefinition
    {
        public ContentId Id { get; }
        public ContentId ClassId { get; }
        public string DisplayName { get; }
        public string ArtSetId { get; }
        public string StoryId { get; }
        public string FlavorText { get; }

        public HeroIdentityDefinition(
            string id,
            string classId,
            string displayName,
            string artSetId,
            string storyId,
            string flavorText)
        {
            Id = ContentId.Parse(id);
            ClassId = ContentId.Parse(classId);
            DisplayName = displayName;
            ArtSetId = artSetId;
            StoryId = storyId;
            FlavorText = flavorText;
        }
    }
}
```

- [ ] **Step 5: Implement registries with duplicate/unknown failure**

Both registries follow the same semantics as `TileRegistry`: duplicates throw `InvalidOperationException`; `Get` of an unknown canonical ID throws `KeyNotFoundException`; no fallback content is returned.

- [ ] **Step 6: Add canonical Knight and two hero identities**

The exact canonical values are:

```text
class.knight                   -> Knight
hero.ironheart                 -> class.knight
hero.sir_clickington           -> class.knight
art.hero.ironheart             != art.hero.sir_clickington
story.hero.ironheart           != story.hero.sir_clickington
Sir Clickington flavor         -> "Brave. Loyal. Clickable."
Ironheart flavor               -> "Steadfast guardian of the dungeon road."
```

- [ ] **Step 7: Run `HeroIdentityTests`**

Expected: all pass.

- [ ] **Step 8: Commit**

```bash
git add Assets/ClickDungeon/Content Assets/ClickDungeon/Tests/EditMode/HeroIdentityTests.cs
git commit -m "feat: separate hero identity from knight mechanics"
```

---

### Task 6: Add Reference-Image Manifest Validation

**Files:**
- Create: `scripts/validate_reference_manifest.py`
- Create: `docs/reference/manifest.json`

**Interfaces:**
- Produces: machine-readable ordered list of the eight authoritative product reference sheets and SHA-256 checksums.

- [ ] **Step 1: Generate SHA-256 hashes for the eight files**

Run:

```bash
sha256sum docs/reference/01-*.png docs/reference/02-*.png docs/reference/03-*.png docs/reference/04-*.png docs/reference/05-*.png docs/reference/06-*.png docs/reference/07-*.png docs/reference/08-*.png
```

- [ ] **Step 2: Create `docs/reference/manifest.json`**

The JSON structure is:

```json
{
  "schemaVersion": 1,
  "orderedReferences": [
    {"index": 1, "path": "docs/reference/01-title-main-menu.png", "sha256": "<actual hash>"},
    {"index": 2, "path": "docs/reference/02-core-gameplay.png", "sha256": "<actual hash>"},
    {"index": 3, "path": "docs/reference/03-dungeon-tiles-original.png", "sha256": "<actual hash>"},
    {"index": 4, "path": "docs/reference/04-dungeon-tiles-canonical.png", "sha256": "<actual hash>"},
    {"index": 5, "path": "docs/reference/05-hero-roster.png", "sha256": "<actual hash>"},
    {"index": 6, "path": "docs/reference/06-monster-boss-roster.png", "sha256": "<actual hash>"},
    {"index": 7, "path": "docs/reference/07-gameplay-implementation-plan.png", "sha256": "<actual hash>"},
    {"index": 8, "path": "docs/reference/08-vertical-slice-and-reward-sequence.png", "sha256": "<actual hash>"}
  ]
}
```

The executor must replace each `<actual hash>` with the SHA-256 produced in Step 1 before committing; angle-bracket placeholders are not allowed in the committed file.

- [ ] **Step 3: Write the validator**

```python
from hashlib import sha256
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "docs/reference/manifest.json"
data = json.loads(MANIFEST.read_text(encoding="utf-8"))
entries = data["orderedReferences"]

errors = []
if [entry["index"] for entry in entries] != list(range(1, 9)):
    errors.append("reference indices must be exactly 1..8")

for entry in entries:
    path = ROOT / entry["path"]
    if not path.is_file():
        errors.append(f"missing: {entry['path']}")
        continue
    actual = sha256(path.read_bytes()).hexdigest()
    if actual != entry["sha256"]:
        errors.append(f"hash mismatch: {entry['path']}")

if errors:
    for error in errors:
        print(error)
    sys.exit(1)

print("Reference manifest validation passed.")
```

- [ ] **Step 4: Run validator**

```bash
python scripts/validate_reference_manifest.py
```

Expected: `Reference manifest validation passed.`

- [ ] **Step 5: Commit**

```bash
git add docs/reference/manifest.json scripts/validate_reference_manifest.py
git commit -m "chore: lock authoritative reference image manifest"
```

---

### Task 7: Add Minimal Bootstrap Without Gameplay Logic

**Files:**
- Create: `Assets/ClickDungeon/Application/Bootstrap/GameBootstrap.cs`
- Create: `Assets/ClickDungeon/Editor/Validation/ContentValidationMenu.cs`

**Interfaces:**
- Consumes: `CanonicalContent`, registry constructors.
- Produces: one composition root that validates canonical content at startup/editor validation without adding mutable gameplay state.

- [ ] **Step 1: Add a failing bootstrap contract test to `HeroIdentityTests.cs` or a new `BootstrapTests.cs`**

Create `Assets/ClickDungeon/Tests/EditMode/BootstrapTests.cs`:

```csharp
using ClickDungeon.Application.Bootstrap;
using NUnit.Framework;

namespace ClickDungeon.Tests.EditMode
{
    public sealed class BootstrapTests
    {
        [Test]
        public void ValidateCanonicalContent_DoesNotThrow()
        {
            Assert.DoesNotThrow(GameBootstrap.ValidateCanonicalContent);
        }
    }
}
```

- [ ] **Step 2: Run and prove failure**

Expected: `GameBootstrap` missing.

- [ ] **Step 3: Implement composition validation**

```csharp
using ClickDungeon.Content.Canonical;
using ClickDungeon.Content.Registries;

namespace ClickDungeon.Application.Bootstrap
{
    public static class GameBootstrap
    {
        public static void ValidateCanonicalContent()
        {
            _ = new TileRegistry(CanonicalContent.Tiles);
            var classes = new HeroClassRegistry(CanonicalContent.HeroClasses);
            var heroes = new HeroIdentityRegistry(CanonicalContent.Heroes);

            foreach (var hero in CanonicalContent.Heroes)
                _ = classes.Get(hero.ClassId.Value);

            _ = heroes.Get("hero.sir_clickington");
            _ = heroes.Get("hero.ironheart");
        }
    }
}
```

- [ ] **Step 4: Run `BootstrapTests`**

Expected: pass.

- [ ] **Step 5: Add editor validation menu**

`ContentValidationMenu.cs` exposes a `ClickDungeon/Validate Canonical Content` editor menu item that calls `GameBootstrap.ValidateCanonicalContent()` and logs a success message only after no exception is thrown. It must not mutate assets.

- [ ] **Step 6: Run the editor menu once**

Expected: validation success log; `git status --short` remains clean.

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Application Assets/ClickDungeon/Editor Assets/ClickDungeon/Tests/EditMode/BootstrapTests.cs
git commit -m "feat: add canonical content bootstrap validation"
```

---

### Task 8: Add Baseline Source Validation CI

**Files:**
- Create: `.github/workflows/source-validation.yml`

**Interfaces:**
- Consumes: Python validators from Tasks 2 and 6.
- Produces: credential-free GitHub Actions gate that runs without Unity license secrets.

- [ ] **Step 1: Create the workflow**

```yaml
name: Source Validation

on:
  pull_request:
  push:
    branches: [main, develop]

permissions:
  contents: read

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with:
          python-version: '3.12'
      - name: Validate reference manifest
        run: python scripts/validate_reference_manifest.py
      - name: Validate player-facing branding
        run: python scripts/validate_player_branding.py
      - name: Verify repository has no tracked Unity transient directories
        shell: bash
        run: |
          if git ls-files | grep -E '^(Library|Temp|Logs|UserSettings)/'; then
            echo 'Unity transient files must not be tracked.' >&2
            exit 1
          fi
```

- [ ] **Step 2: Run both validators locally**

```bash
python scripts/validate_reference_manifest.py
python scripts/validate_player_branding.py
```

Expected: both pass.

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/source-validation.yml
git commit -m "ci: add credential-free source validation"
```

- [ ] **Step 4: Push the feature branch and observe CI**

Expected: Source Validation completes successfully on the exact pushed SHA.

---

### Task 9: Foundation Verification and Review Gate

**Files:**
- No new production files.
- Update: `README.md` only if commands or structure documented there differ from the verified result.

**Interfaces:**
- Consumes: Tasks 1–8.
- Produces: evidence that Phase 0–1 foundation is ready for the next plan.

- [ ] **Step 1: Run all EditMode tests from the pinned Unity editor**

Expected suites: `BrandingTests`, `ContentIdTests`, `TileRegistryTests`, `HeroIdentityTests`, `BootstrapTests`; all green.

- [ ] **Step 2: Run credential-free validators**

```bash
python scripts/validate_reference_manifest.py
python scripts/validate_player_branding.py
```

Expected: both pass.

- [ ] **Step 3: Run canonical validation from the Unity editor**

Expected: no exception and no working-tree mutation.

- [ ] **Step 4: Verify clean tree**

```bash
git status --short
```

Expected: no output.

- [ ] **Step 5: Perform adversarial foundation review**

Review specifically for: ScriptableObject/runtime-state leakage, duplicate canonical IDs, legacy branding leakage, missing references, fallback behavior for unknown IDs, Sir Clickington/Ironheart class duplication, and forbidden dependencies from Core to Unity presentation concerns.

Any defect found is fixed through a new failing regression test before implementation.

- [ ] **Step 6: Open a draft PR**

Title: `Foundation: canonical ClickDungeon content contracts`

PR body must include the exact Unity editor version from `ProjectVersion.txt`, test results, validator outputs, the reference manifest hash status, and a statement that no dungeon generation/combat/UI/art implementation is included.

- [ ] **Step 7: Stop at the integration boundary**

Do not merge automatically as part of this task. The next plan begins only after review evidence is green and the foundation PR is approved for integration.

---

## Self-Review Results

**Spec coverage for this plan:** Phase 0–1 requirements are covered: greenfield repository structure, player-facing brand contract, stable IDs, all 24 tile identities, Knight sharing rule, distinct Sir Clickington identity, authoritative reference manifest, bootstrap validation and credential-free CI. Gameplay generation, interactions, combat, remaining hero classes, enemies, inventory, presentation, save system and platform builds are intentionally outside this plan and will each receive dedicated implementation plans after the foundation gate.

**Placeholder scan:** The committed implementation must contain no angle-bracket hash placeholders; Task 6 explicitly requires inserting actual SHA-256 values before commit. No production task relies on unspecified functions or classes.

**Type consistency:** `ContentId.Parse`, `TileRegistry.Get/Contains`, `HeroClassRegistry.Get`, `HeroIdentityRegistry.Get`, `CanonicalContent.Tiles/HeroClasses/Heroes`, and `GameBootstrap.ValidateCanonicalContent` are the stable interfaces produced by this plan.
