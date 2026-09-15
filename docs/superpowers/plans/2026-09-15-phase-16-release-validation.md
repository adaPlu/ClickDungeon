# ClickDungeon Phase 16 Release Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the Phase-15 source-contract-clean ClickDungeon reconstruction into an evidence-backed Unity 6 release candidate with authentic project metadata, executable runtime smoke, platform builds/exports, artifact inspection, visual acceptance evidence, mutation guards, reconciled release documentation, and a final `/Gaudit` on one exact `new` head SHA.

**Architecture:** Keep the existing deterministic/domain/application contracts authoritative and add a thin Unity runtime/editor/build-validation layer around them. Release evidence is explicit and machine-readable: source checks remain first, Unity-generated metadata is never fabricated, runtime smoke uses ordered log markers, platform artifacts are inspected independently from signing, visual acceptance is recorded against the pinned reference bundle, and every gate is classified PASS, FAIL, or BLOCKED. Any repair is driven by a failing gate and must preserve deterministic gameplay, exactly-once rewards, canonical content IDs, and source mutation guards.

**Tech Stack:** Unity 6, C#, Unity Test Framework, Unity Editor build APIs, Python 3.12, pytest, PowerShell/Bash, GitHub Actions, Windows x64, Android ARM64/IL2CPP APK+AAB, iOS ARM64/IL2CPP Xcode export.

**Spec:** `docs/superpowers/specs/2026-09-15-phase-16-release-validation-design.md`

## Global Constraints

- Work only on branch `new`; branch `OLD` is out of scope and must not be modified, merged, rebased, or used as an implementation source.
- The Phase-16 baseline is `main@10fb5ca5360039c70681686dde4c0d008c49c74a`; the approved design commit is `e90eda5412765313efcc6bc1d06749a5215b032a`.
- Player-facing/runtime branding is exactly `ClickDungeon`; `ClickDungeon2` is forbidden in shipping/player-facing runtime text.
- Gameplay/system authority remains `docs/specs/clickdungeon-master-design.md` plus committed canonical definitions/tests.
- Visual authority remains the pinned bundle under `docs/reference/latest/`; reference composites must never be copied into runtime art folders as fake isolated sprites.
- Windows release target is x64; Android is ARM64 + IL2CPP + APK/AAB; iOS is ARM64 + IL2CPP + Xcode export.
- Unity project metadata (`ProjectSettings`, `Packages`, `.meta`, scene YAML) must be created/reconciled by an actual Unity 6 editor. Do not hand-author guessed Unity metadata.
- Source validation remains the first gate and continues to run pytest plus every existing validator.
- The minimum built-player smoke sequence is `CD_SMOKE_BOOT -> CD_SMOKE_MAIN_MENU -> CD_SMOKE_START_GAME -> CD_SMOKE_DUNGEON_READY`.
- Runtime smoke success requires explicit log markers in order; launch-only success or screenshot inference is insufficient.
- The launch gameplay board remains exactly 5×5.
- Production-art strict validation must require each manifest-required runtime asset and its Unity `.meta`; missing isolated art is a release blocker and must never be hidden with reference-sheet stand-ins.
- No Android keystore, keystore password, iOS certificate, provisioning profile, App Store credential, Windows code-signing certificate, Unity license, or provider secret is invented or committed.
- Unsigned build/export verification may PASS independently; signing/device/store gates remain BLOCKED until authorized external credentials/devices exist.
- Every behavioral change follows RED → focused GREEN → full affected regression verification → commit.
- Unity import/build steps must leave no unexplained tracked-file mutation.
- `/graphRepair` is run only when evidence identifies an actual dependency/integration defect.
- `/Gaudit` is the final broad audit; completion requires no unresolved repository-controlled release blocker.

---

### Task 16.1: Release Evidence Model and Source-Only Phase-16 Readiness Gate

**Files:**
- Create: `scripts/release_evidence.py`
- Create: `scripts/validate_phase16_readiness.py`
- Create: `scripts/tests/test_phase16_release_evidence.py`
- Modify: `.github/workflows/source-validation.yml`

**Interfaces:**
- Consumes: repository paths, exact Git SHA supplied through `GITHUB_SHA` or `git rev-parse HEAD`, existing source validators, `ProductBrand.PlayerFacingName`, `art/production-art-manifest.json`.
- Produces: `GateStatus`, `GateResult`, `ReleaseEvidenceReport`, deterministic JSON output at a caller-supplied path, and a source-only readiness validator that reports absent Unity/build evidence as `BLOCKED` rather than pretending it passed.

- [ ] **Step 1: Write the failing evidence-model tests**

Create `scripts/tests/test_phase16_release_evidence.py` with these exact behavioral checks:

```python
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts"))

from release_evidence import GateResult, GateStatus, ReleaseEvidenceReport


def test_gate_status_vocabulary_is_exact():
    assert [s.value for s in GateStatus] == ["PASS", "FAIL", "BLOCKED"]


def test_report_round_trips_exact_sha_and_gate_results(tmp_path):
    report = ReleaseEvidenceReport(
        head_sha="0123456789abcdef0123456789abcdef01234567",
        gates=[
            GateResult("source", GateStatus.PASS, "source contracts green"),
            GateResult("unity_compile", GateStatus.BLOCKED, "Unity evidence unavailable"),
        ],
    )
    target = tmp_path / "evidence.json"
    report.write_json(target)
    payload = json.loads(target.read_text())
    assert payload["head_sha"] == report.head_sha
    assert payload["gates"][0]["status"] == "PASS"
    assert payload["gates"][1]["status"] == "BLOCKED"


def test_report_rejects_non_full_sha():
    try:
        ReleaseEvidenceReport(head_sha="abc123", gates=[])
    except ValueError as exc:
        assert "40-character" in str(exc)
    else:
        raise AssertionError("short SHA must be rejected")


def test_any_fail_makes_report_unsuccessful_but_blocked_does_not_become_pass():
    blocked = ReleaseEvidenceReport(
        "0123456789abcdef0123456789abcdef01234567",
        [GateResult("signing", GateStatus.BLOCKED, "external credential required")],
    )
    failed = ReleaseEvidenceReport(
        "0123456789abcdef0123456789abcdef01234567",
        [GateResult("runtime", GateStatus.FAIL, "Dungeon Ready not reached")],
    )
    assert blocked.has_failures is False
    assert blocked.is_release_clean is False
    assert failed.has_failures is True
    assert failed.is_release_clean is False
```

- [ ] **Step 2: Run the focused RED gate**

Run:

```bash
python3 -m pytest -q scripts/tests/test_phase16_release_evidence.py
```

Expected: FAIL with `ModuleNotFoundError: No module named 'release_evidence'`.

- [ ] **Step 3: Implement the evidence model minimally**

Create `scripts/release_evidence.py` with this public surface:

```python
from dataclasses import asdict, dataclass
from enum import Enum
import json
from pathlib import Path
import re

_SHA = re.compile(r"^[0-9a-f]{40}$")

class GateStatus(Enum):
    PASS = "PASS"
    FAIL = "FAIL"
    BLOCKED = "BLOCKED"

@dataclass(frozen=True)
class GateResult:
    gate: str
    status: GateStatus
    detail: str

class ReleaseEvidenceReport:
    def __init__(self, head_sha: str, gates: list[GateResult]):
        if not _SHA.fullmatch(head_sha):
            raise ValueError("head_sha must be a lowercase 40-character Git SHA")
        self.head_sha = head_sha
        self.gates = tuple(gates)

    @property
    def has_failures(self) -> bool:
        return any(g.status is GateStatus.FAIL for g in self.gates)

    @property
    def is_release_clean(self) -> bool:
        return bool(self.gates) and all(g.status is GateStatus.PASS for g in self.gates)

    def to_dict(self) -> dict:
        return {
            "schema_version": 1,
            "head_sha": self.head_sha,
            "gates": [
                {"gate": g.gate, "status": g.status.value, "detail": g.detail}
                for g in self.gates
            ],
        }

    def write_json(self, path: Path) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(self.to_dict(), indent=2, sort_keys=True) + "\n")
```

- [ ] **Step 4: Implement the source-only readiness validator**

Create `scripts/validate_phase16_readiness.py` so it:

1. Resolves the exact head SHA from `GITHUB_SHA` when present, otherwise `git rev-parse HEAD`.
2. Verifies `Assets/ClickDungeon/Core/Brand/ProductBrand.cs` contains `PlayerFacingName = "ClickDungeon"`.
3. Verifies `docs/reference/latest/09-title-main-menu.png`, `10-core-gameplay.png`, `11-dungeon-tiles-a.png`, `12-dungeon-tiles-b.png`, `16-sir-clickington.png`, and `21-hero-roster.png` exist.
4. Runs `python3 scripts/validate_production_art.py` in source mode and records PASS/FAIL.
5. Records `unity_metadata` as BLOCKED when `ProjectSettings/ProjectVersion.txt`, `Packages/manifest.json`, or `Packages/packages-lock.json` is absent; records PASS only when all three exist.
6. Runs `python3 scripts/validate_production_art.py --strict`; records strict art as PASS when it succeeds and FAIL when it fails because missing required runtime assets are repository-controlled release blockers.
7. Records Windows/Android/iOS build evidence and signing as BLOCKED in source-only mode; it must never infer PASS from configuration contracts alone.
8. Writes `build/phase16/source-readiness.json` and exits nonzero only for source/repository FAIL results, not external BLOCKED results.

Use the evidence model rather than ad hoc status strings.

- [ ] **Step 5: Run focused GREEN and full source regression**

Run:

```bash
python3 -m pytest -q scripts/tests/test_phase16_release_evidence.py
python3 scripts/validate_phase16_readiness.py
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
git diff --check
```

Expected: evidence-model tests PASS; source validators PASS. `validate_phase16_readiness.py` may report `unity_metadata=BLOCKED` and strict production art `FAIL` before Tasks 16.2 and 16.5; that failure is expected evidence, not permission to weaken the validator.

- [ ] **Step 6: Keep the existing Source Validation job first and publish the source-readiness JSON even on a blocked/release-failing Phase-16 state**

Modify `.github/workflows/source-validation.yml` only after the validator behavior is proven. Keep the existing `source-contracts` job and add a final evidence step that does not mask source validator failures:

```yaml
      - name: Record Phase 16 source readiness
        if: always()
        shell: bash
        run: python3 scripts/validate_phase16_readiness.py || true
      - name: Upload Phase 16 source readiness
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: phase16-source-readiness
          path: build/phase16/source-readiness.json
          if-no-files-found: error
```

Do not add `continue-on-error` to the existing source-contract step.

- [ ] **Step 7: Commit**

```bash
git add scripts/release_evidence.py scripts/validate_phase16_readiness.py scripts/tests/test_phase16_release_evidence.py .github/workflows/source-validation.yml
git commit -m "test: add phase 16 release evidence gate"
```

---

### Task 16.2: Establish Authentic Unity 6 Project Metadata and Import Baseline

**Files:**
- Create via Unity: `ProjectSettings/ProjectVersion.txt`
- Create/reconcile via Unity: `ProjectSettings/ProjectSettings.asset`
- Create/reconcile via Unity Package Manager: `Packages/manifest.json`
- Create/reconcile via Unity Package Manager: `Packages/packages-lock.json`
- Create via Unity import: `.meta` files for tracked `Assets/` content where Unity requires them
- Create: `scripts/tests/test_unity_project_contracts.py`
- Create: `scripts/validate_unity_project_contracts.py`
- Create: `docs/validation/phase-16-unity-import.md`

**Interfaces:**
- Consumes: real installed Unity 6 editor; current `new` source tree.
- Produces: authoritative editor version/package state and a clean first-import mutation record. Later Unity test/build jobs consume these exact files.

- [ ] **Step 1: Write the failing Unity-project contract test before creating metadata**

Create `scripts/tests/test_unity_project_contracts.py`:

```python
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[2]


def test_authentic_unity_project_metadata_exists_and_targets_unity_6():
    version = ROOT / "ProjectSettings/ProjectVersion.txt"
    manifest = ROOT / "Packages/manifest.json"
    lock = ROOT / "Packages/packages-lock.json"
    assert version.is_file()
    assert manifest.is_file()
    assert lock.is_file()
    text = version.read_text()
    match = re.search(r"m_EditorVersion:\s*(\S+)", text)
    assert match, text
    assert match.group(1).startswith("6000."), match.group(1)
    assert isinstance(json.loads(manifest.read_text()).get("dependencies"), dict)
    assert isinstance(json.loads(lock.read_text()).get("dependencies"), dict)


def test_project_name_and_company_do_not_reintroduce_clickdungeon2_runtime_branding():
    settings = (ROOT / "ProjectSettings/ProjectSettings.asset").read_text()
    assert "productName: ClickDungeon" in settings
    assert "ClickDungeon2" not in settings
```

- [ ] **Step 2: Run focused RED**

Run:

```bash
python3 -m pytest -q scripts/tests/test_unity_project_contracts.py
```

Expected: FAIL because `ProjectSettings`/`Packages` are absent on the Phase-15 baseline.

- [ ] **Step 3: Generate metadata with a real Unity 6 editor, never by hand**

Using Unity Hub and an installed supported Unity 6 editor, create an empty 2D Core project named `ClickDungeonBootstrap`. Close the editor. Copy the generated `ProjectSettings/` and `Packages/` directories from that Unity-created project into the repository root. Reopen the repository root with the same Unity editor, let package resolution and first import complete, then set **Project Settings → Player → Product Name** to `ClickDungeon` and save.

Do not edit `ProjectVersion.txt`, `ProjectSettings.asset`, `manifest.json`, `packages-lock.json`, scene YAML, or generated `.meta` GUIDs to manufacture expected values. Package changes must be made through Unity Package Manager or editor-supported settings and then saved.

- [ ] **Step 4: Capture and classify first-import mutation**

Before opening Unity, capture:

```bash
git status --porcelain=v1 > build/phase16/pre-import-status.txt
git diff --binary > build/phase16/pre-import.diff
```

After import/package resolution and editor save, capture:

```bash
git status --porcelain=v1 > build/phase16/post-import-status.txt
git diff --binary > build/phase16/post-import.diff
git diff --check
```

Review every tracked change. Keep only deterministic Unity project state and required `.meta` files. Delete transient `Library/`, `Logs/`, `Temp/`, `obj/`, `UserSettings/`, and build-output directories if created; they remain ignored.

- [ ] **Step 5: Implement the Unity-project validator**

Create `scripts/validate_unity_project_contracts.py` that parses the generated metadata and requires:

- `m_EditorVersion` begins with `6000.`;
- `productName: ClickDungeon` exists and `ClickDungeon2` does not;
- both package JSON files parse and contain dependency dictionaries;
- every tracked file below `Assets/` that Unity imports has a sibling `.meta`, and every tracked directory below `Assets/ClickDungeon/` has a `.meta` generated by Unity;
- ignored transient Unity folders are not tracked.

The validator must report concrete paths for missing/forbidden state and exit nonzero on violations.

- [ ] **Step 6: Run GREEN, headless import/compile, and mutation guard**

Run the same Unity 6 editor in batch mode against the repository:

```text
Unity -batchmode -nographics -quit -projectPath <repository-root> -logFile build/phase16/unity-import.log
```

The executor must substitute the actual repository absolute path through the shell invocation, not commit a machine-local path. Expected: Unity exits 0 and the log has no compile error.

Then run:

```bash
python3 -m pytest -q scripts/tests/test_unity_project_contracts.py
python3 scripts/validate_unity_project_contracts.py
git diff --check
git status --porcelain=v1
```

Re-running the headless import from an intentionally clean committed tree must not create unexplained tracked changes.

- [ ] **Step 7: Record import evidence**

Create `docs/validation/phase-16-unity-import.md` with the exact editor version from `ProjectVersion.txt`, exact head SHA tested, batch command, exit status, compile result, and a table with `PASS/FAIL/BLOCKED` for metadata authenticity, import, compile, package resolution, and mutation guard. Do not claim EditMode, PlayMode, builds, signing, or devices here.

- [ ] **Step 8: Commit**

```bash
git add ProjectSettings Packages Assets scripts/tests/test_unity_project_contracts.py scripts/validate_unity_project_contracts.py docs/validation/phase-16-unity-import.md
git commit -m "build: establish authentic unity 6 project state"
```

---

### Task 16.3: Add Unity Runtime Bootstrap and Ordered Smoke Markers

**Files:**
- Create: `Assets/ClickDungeon/Runtime/ClickDungeon.Runtime.asmdef`
- Create: `Assets/ClickDungeon/Runtime/ReleaseSmokeMarkers.cs`
- Create: `Assets/ClickDungeon/Runtime/ReleaseRuntimeState.cs`
- Create: `Assets/ClickDungeon/Runtime/ClickDungeonBootstrap.cs`
- Create: `Assets/ClickDungeon/Runtime/ReleaseSmokeDriver.cs`
- Create: `Assets/ClickDungeon/Editor/ClickDungeon.Editor.asmdef`
- Create: `Assets/ClickDungeon/Editor/ReleaseSceneBuilder.cs`
- Create via Unity editor script: `Assets/ClickDungeon/Scenes/Bootstrap.unity`
- Create: `scripts/tests/test_runtime_smoke_contracts.py`
- Create: `scripts/validate_runtime_smoke_contracts.py`

**Interfaces:**
- Consumes: `ProductBrand.PlayerFacingName`, `FloorState.Width/Height`, existing UI layout contracts, platform menu policy, deterministic application/domain state.
- Produces: one Unity bootstrap scene, runtime states `Boot`, `MainMenu`, `DungeonReady`, ordered machine-parseable log markers, and an opt-in command-line smoke driver activated only by `-releaseSmoke`.

- [ ] **Step 1: Write failing source-contract tests for runtime smoke**

Require these exact constants and state names:

```csharp
public const string Boot = "CD_SMOKE_BOOT";
public const string MainMenu = "CD_SMOKE_MAIN_MENU";
public const string StartGame = "CD_SMOKE_START_GAME";
public const string DungeonReady = "CD_SMOKE_DUNGEON_READY";
```

The test must also require references to `ProductBrand.PlayerFacingName`, `FloorState.Width`, `FloorState.Height`, and `-releaseSmoke`, and reject any runtime literal `ClickDungeon2`.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_runtime_smoke_contracts.py
```

Expected: FAIL because the runtime/bootstrap files do not exist.

- [ ] **Step 3: Implement runtime state and smoke markers**

Use this state vocabulary:

```csharp
namespace ClickDungeon.Runtime
{
    public enum ReleaseRuntimeState
    {
        Boot = 0,
        MainMenu = 1,
        DungeonReady = 2
    }

    public static class ReleaseSmokeMarkers
    {
        public const string Boot = "CD_SMOKE_BOOT";
        public const string MainMenu = "CD_SMOKE_MAIN_MENU";
        public const string StartGame = "CD_SMOKE_START_GAME";
        public const string DungeonReady = "CD_SMOKE_DUNGEON_READY";
    }
}
```

`ClickDungeonBootstrap : MonoBehaviour` must expose read-only `ReleaseRuntimeState State`, log `Boot` once during initialization, construct/show the main-menu presentation and log `MainMenu`, and expose `public void StartGame()` that logs `StartGame`, creates the canonical initial 5×5 dungeon presentation from `FloorState.Width`/`FloorState.Height`, switches state to `DungeonReady`, then logs `DungeonReady` only after the board and HUD are usable.

`ReleaseSmokeDriver : MonoBehaviour` must check `Environment.GetCommandLineArgs()` for exact token `-releaseSmoke`; when absent it does nothing. When present, it waits until the bootstrap reports `MainMenu`, invokes `StartGame()` once, waits until `DungeonReady`, then logs `CD_SMOKE_COMPLETE` and exits the player with code 0. If it times out after 30 seconds, it logs `CD_SMOKE_FAILURE` and exits nonzero.

- [ ] **Step 4: Create the Unity-generated bootstrap scene through an editor method**

`ReleaseSceneBuilder.Build()` must create a new scene through `EditorSceneManager.NewScene`, create one root `GameObject` named `ClickDungeonBootstrap`, attach `ClickDungeonBootstrap` and `ReleaseSmokeDriver`, save to `Assets/ClickDungeon/Scenes/Bootstrap.unity`, and replace `EditorBuildSettings.scenes` with that enabled scene. Run the method in Unity batch mode:

```text
Unity -batchmode -nographics -quit -projectPath <repository-root> -executeMethod ClickDungeon.Editor.ReleaseSceneBuilder.Build -logFile build/phase16/scene-builder.log
```

Commit the Unity-generated scene and `.meta`; do not hand-author scene YAML/GUIDs.

- [ ] **Step 5: Implement static smoke-contract validator**

`validate_runtime_smoke_contracts.py` must require all four ordered markers, the `-releaseSmoke` opt-in, `ProductBrand.PlayerFacingName`, `FloorState.Width`, `FloorState.Height`, the generated Bootstrap scene, and no player-facing `ClickDungeon2` literal under `Assets/ClickDungeon/Runtime`.

- [ ] **Step 6: Run GREEN and Unity compile**

```bash
python3 -m pytest -q scripts/tests/test_runtime_smoke_contracts.py
python3 scripts/validate_runtime_smoke_contracts.py
python3 -m pytest -q scripts/tests
```

Then headless-import/compile with Unity and require exit 0. Run `git diff --check` and the Unity project validator after the import.

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Runtime Assets/ClickDungeon/Editor Assets/ClickDungeon/Scenes scripts/tests/test_runtime_smoke_contracts.py scripts/validate_runtime_smoke_contracts.py
git commit -m "feat: add release runtime bootstrap and smoke markers"
```

---

### Task 16.4: Add Unity EditMode and PlayMode Release Tests

**Files:**
- Create via Unity: `Assets/ClickDungeon/Tests/EditMode/ClickDungeon.EditModeTests.asmdef`
- Create: `Assets/ClickDungeon/Tests/EditMode/ReleaseContractTests.cs`
- Create via Unity: `Assets/ClickDungeon/Tests/PlayMode/ClickDungeon.PlayModeTests.asmdef`
- Create: `Assets/ClickDungeon/Tests/PlayMode/ReleaseSmokePlayModeTests.cs`
- Create: `scripts/tests/test_unity_test_layout.py`

**Interfaces:**
- Consumes: runtime bootstrap, `ProductBrand`, `FloorState`, main-menu/gameplay HUD contracts.
- Produces: Unity Test Framework evidence proving branding/5×5 invariants in EditMode and `MainMenu -> StartGame -> DungeonReady` in PlayMode.

- [ ] **Step 1: Write failing source-layout tests**

Require both test assembly definitions with `optionalUnityReferences: ["TestAssemblies"]`, EditMode test source, PlayMode test source, and the expected smoke state assertions.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_unity_test_layout.py
```

Expected: FAIL because Unity test assemblies do not exist.

- [ ] **Step 3: Implement EditMode release contract tests**

`ReleaseContractTests.cs` must assert:

```csharp
Assert.AreEqual("ClickDungeon", ProductBrand.PlayerFacingName);
Assert.AreEqual(5, FloorState.Width);
Assert.AreEqual(5, FloorState.Height);
Assert.AreEqual(5, GameplayHudContract.BoardColumns);
Assert.AreEqual(5, GameplayHudContract.BoardRows);
Assert.IsTrue(new MainMenuLayoutContract(ResponsiveLayoutMode.DesktopWide, new SafeAreaInsets(0,0,0,0))
    .IsVisible(MainMenuControlId.Quit, true));
Assert.IsFalse(new MainMenuLayoutContract(ResponsiveLayoutMode.MobileCompact, new SafeAreaInsets(0,0,0,0))
    .IsVisible(MainMenuControlId.Quit, false));
```

- [ ] **Step 4: Implement PlayMode smoke test**

`ReleaseSmokePlayModeTests.cs` must load `Bootstrap`, find `ClickDungeonBootstrap`, wait until `State == ReleaseRuntimeState.MainMenu`, invoke `StartGame()`, wait until `State == ReleaseRuntimeState.DungeonReady`, and assert the runtime board exposes exactly 25 cells. The test must fail after 10 seconds rather than wait forever.

- [ ] **Step 5: Run Unity EditMode and PlayMode suites**

Use the generated Unity editor version and run:

```text
Unity -batchmode -nographics -projectPath <repository-root> -runTests -testPlatform EditMode -testResults build/phase16/editmode-results.xml -logFile build/phase16/editmode.log -quit
Unity -batchmode -nographics -projectPath <repository-root> -runTests -testPlatform PlayMode -testResults build/phase16/playmode-results.xml -logFile build/phase16/playmode.log -quit
```

Expected: both commands exit 0, XML files report zero failures, and post-test `git status --porcelain=v1` shows no unexplained tracked mutation.

- [ ] **Step 6: Run full regression and commit**

```bash
python3 -m pytest -q scripts/tests
python3 scripts/validate_unity_project_contracts.py
python3 scripts/validate_runtime_smoke_contracts.py
git diff --check
git status --porcelain=v1
```

Commit:

```bash
git add Assets/ClickDungeon/Tests scripts/tests/test_unity_test_layout.py
git commit -m "test: add unity release validation suites"
```

---

### Task 16.5: Close Production-Art Strict Gate and Capture Structured Visual Acceptance

**Files:**
- Import through Unity to exact manifest paths: required files under `Assets/ClickDungeon/Art/Runtime/{Tiles,Heroes,Monsters,Bosses,Items,UI,VFX,Chest}/`
- Create via Unity: matching `.meta` files
- Create: `Assets/ClickDungeon/Editor/VisualAcceptanceCapture.cs`
- Create: `scripts/tests/test_visual_acceptance_contracts.py`
- Create: `scripts/validate_visual_acceptance.py`
- Create: `docs/validation/phase-16-visual-acceptance.md`

**Interfaces:**
- Consumes: `art/production-art-manifest.json`, pinned reference images, runtime Bootstrap scene, canonical UI/content contracts.
- Produces: strict production-art PASS, deterministic screenshot capture commands for desktop/mobile aspect profiles, and a human-reviewed structured acceptance record. Visual evidence never overrides gameplay/reward authority.

- [ ] **Step 1: Run the existing strict art gate as RED**

```bash
python3 scripts/validate_production_art.py --strict
```

Expected on the current Phase-15 tree: FAIL on the first missing runtime asset because runtime art folders contain only `.gitkeep`.

- [ ] **Step 2: Import real isolated production assets at manifest paths**

For every manifest entry with `required: true`, provide an actual isolated runtime sprite/image at its exact `relative_path`, then import it with Unity so Unity writes the matching `.meta` file. Use approved isolated source art or an explicit asset-production pipeline that creates a true isolated sprite. Do not copy `docs/reference/latest/*.png` composites into runtime paths, do not rename a reference sheet to satisfy the manifest, and do not fabricate `.meta` GUIDs.

If a required isolated source asset genuinely does not exist, keep this gate FAIL and stop this task as a repository-controlled release blocker; do not weaken `--strict`.

- [ ] **Step 3: Run strict art GREEN**

```bash
python3 scripts/validate_production_art.py --strict
```

Expected: PASS with all manifest-required runtime files and Unity `.meta` files present.

- [ ] **Step 4: Implement deterministic capture entry point**

Create `VisualAcceptanceCapture` with editor methods that start the Bootstrap scene in Play Mode and capture these exact evidence frames under ignored `build/phase16/visual/`:

- `main-menu-1920x1080.png`
- `gameplay-1920x1080.png`
- `main-menu-2532x1170.png`
- `gameplay-2532x1170.png`

The main-menu frames are captured only after `ReleaseRuntimeState.MainMenu`; gameplay frames only after `DungeonReady`. The capture method must set the requested Game View target dimensions before capture and must never overwrite canonical files in `docs/reference/latest/`.

- [ ] **Step 5: Write and run visual acceptance contract tests**

`test_visual_acceptance_contracts.py` must require the capture method names, the four exact output filenames, and a validator that requires the canonical reference paths `09-title-main-menu.png`, `10-core-gameplay.png`, `11-dungeon-tiles-a.png`, `12-dungeon-tiles-b.png`, `16-sir-clickington.png`, and `21-hero-roster.png`.

Run RED before validator implementation, then implement `validate_visual_acceptance.py` so it verifies evidence files exist/non-empty and emits the exact checklist categories below without performing fake pixel-perfect equivalence scoring.

- [ ] **Step 6: Perform structured visual review and record it**

Create `docs/validation/phase-16-visual-acceptance.md` with one row per item, each marked `PASS`, `FAIL`, or `BLOCKED` and linked to the capture filename/reference filename:

- runtime title reads `ClickDungeon` and Sir Clickington is the primary identity;
- Continue/Play prominence and hero/progression/currency/Daily Reward hierarchy match the reference intent;
- routes exist for Hero Select, Inventory, Talents, Shop, Settings; Quit is desktop-only;
- gameplay board is readable 5×5;
- top HUD, floor identity, selected-cell highlight, and bottom action row are readable;
- stone-dungeon/torch-lighting intent and canonical tile vocabulary are present;
- hero/enemy/chest identities use approved art rather than unrelated fallbacks;
- chest presentation preserves anticipation → reveal/burst → reaction → triumph intent;
- desktop and mobile captures preserve hierarchy without overlap/clipping.

Any FAIL here remains a release blocker until repaired and recaptured.

- [ ] **Step 7: Run full affected regression and commit**

```bash
python3 scripts/validate_production_art.py --strict
python3 scripts/validate_visual_acceptance.py
python3 -m pytest -q scripts/tests
git diff --check
```

Commit runtime assets, Unity-generated `.meta`, capture tooling, and the review record; do not commit `build/phase16/visual/*.png` unless the repository explicitly chooses them as evidence artifacts. GitHub Actions artifacts are preferred.

---

### Task 16.6: Add Deterministic Platform Build Entry Points and Artifact Inspectors

**Files:**
- Create: `Assets/ClickDungeon/Editor/ReleaseBuild.cs`
- Create: `scripts/inspect_release_artifacts.py`
- Create: `scripts/tests/test_release_artifact_inspection.py`
- Create: `docs/validation/phase-16-artifact-contract.md`

**Interfaces:**
- Consumes: `CanonicalBuildTargets`, generated Unity project, Bootstrap scene, Unity Editor build APIs.
- Produces: exact build outputs under `build/phase16/{windows,android-apk,android-aab,ios}/` and machine-readable inspection JSON tied to the exact head SHA.

- [ ] **Step 1: Write artifact-inspector RED tests**

Use temporary directories/zip files to require:

- Windows requires `ClickDungeon.exe` plus `ClickDungeon_Data/`;
- Android APK path is `ClickDungeon.apk` and AAB path is `ClickDungeon.aab`;
- iOS export requires `Unity-iPhone.xcodeproj/project.pbxproj` and `Classes/`;
- report includes the supplied 40-character head SHA and target;
- inspector rejects filenames/product metadata containing `ClickDungeon2`;
- missing artifacts are FAIL, never BLOCKED.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_release_artifact_inspection.py
```

Expected: FAIL because `inspect_release_artifacts.py` does not exist.

- [ ] **Step 3: Implement Unity build entry points**

`ReleaseBuild` exposes exactly these public static methods:

```csharp
public static void BuildWindows();
public static void BuildAndroidApk();
public static void BuildAndroidAab();
public static void ExportIos();
```

Shared configuration must set `PlayerSettings.productName = ProductBrand.PlayerFacingName`. Windows uses `BuildTarget.StandaloneWindows64`. Android sets `PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64`, scripting backend `IL2CPP`, then builds once with `EditorUserBuildSettings.buildAppBundle = false` to `build/phase16/android-apk/ClickDungeon.apk` and once with it `true` to `build/phase16/android-aab/ClickDungeon.aab`. iOS sets `IL2CPP` and builds to `build/phase16/ios/`. All methods use `BuildOptions.None` and throw when `BuildReport.summary.result != BuildResult.Succeeded`.

Do not set keystore passwords, certificates, provisioning profiles, or provider credentials in source.

- [ ] **Step 4: Implement artifact inspector**

`inspect_release_artifacts.py` accepts:

```text
--target windows|android-apk|android-aab|ios
--root <artifact-root>
--sha <40-character-sha>
--out <json-path>
```

It writes schema version 1, exact SHA, target, PASS/FAIL, and concrete findings. It checks structural presence noted above and scans text-readable metadata/file names for forbidden `ClickDungeon2`. It does not label unsigned artifacts as signed.

- [ ] **Step 5: Run GREEN tests and validator**

```bash
python3 -m pytest -q scripts/tests/test_release_artifact_inspection.py
python3 -m pytest -q scripts/tests
git diff --check
```

- [ ] **Step 6: Document artifact/signing semantics**

`docs/validation/phase-16-artifact-contract.md` must state that build/export structural PASS is independent from signing/device/store PASS, and list exact expected output paths for all four build classes.

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Editor/ReleaseBuild.cs scripts/inspect_release_artifacts.py scripts/tests/test_release_artifact_inspection.py docs/validation/phase-16-artifact-contract.md
git commit -m "build: add release builders and artifact inspection"
```

---

### Task 16.7: Prove Windows Built-Player Smoke

**Files:**
- Create: `scripts/run_windows_smoke.ps1`
- Create: `scripts/tests/test_windows_smoke_contract.py`
- Create: `docs/validation/phase-16-windows-smoke.md`

**Interfaces:**
- Consumes: `build/phase16/windows/ClickDungeon.exe`, `-releaseSmoke`, explicit runtime markers.
- Produces: player log plus pass/fail JSON proving marker order `BOOT < MAIN_MENU < START_GAME < DUNGEON_READY < COMPLETE` from a built Windows player.

- [ ] **Step 1: Write failing PowerShell contract test**

The Python test must read `run_windows_smoke.ps1` and require exact marker literals, `-releaseSmoke`, a finite timeout, nonzero exit on missing/out-of-order markers, and output path `build/phase16/windows-smoke.json`.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_windows_smoke_contract.py
```

Expected: FAIL because the runner does not exist.

- [ ] **Step 3: Implement smoke runner**

`run_windows_smoke.ps1` must:

1. Require `build/phase16/windows/ClickDungeon.exe`.
2. Delete any stale `build/phase16/windows-player.log`.
3. Launch `ClickDungeon.exe -releaseSmoke -logFile <absolute-log-path>`.
4. Wait at most 45 seconds.
5. Kill the process on timeout.
6. Read the final log and require each exact marker in strictly increasing string-index order.
7. Write JSON with `status`, `exe`, `log`, and `markers` to `build/phase16/windows-smoke.json`.
8. Exit 0 only if the process exits successfully and all markers are present in order.

- [ ] **Step 4: Build Windows and run smoke**

Run Unity `ReleaseBuild.BuildWindows`, then:

```powershell
pwsh -File scripts/run_windows_smoke.ps1
python scripts/inspect_release_artifacts.py --target windows --root build/phase16/windows --sha $(git rev-parse HEAD) --out build/phase16/windows-artifact.json
```

Expected: artifact inspection PASS and smoke PASS reaching Dungeon Ready.

- [ ] **Step 5: Record exact evidence and commit**

`docs/validation/phase-16-windows-smoke.md` records exact head SHA, Unity version, build result, artifact inspection result, player exit code, ordered marker evidence, and mutation status. Commit runner/test/report but keep generated build/log files as CI artifacts unless explicitly selected for source control.

---

### Task 16.8: Add Release Validation CI with Explicit Stage Boundaries

**Files:**
- Create: `.github/workflows/release-validation.yml`
- Create: `scripts/tests/test_release_workflow_contract.py`

**Interfaces:**
- Consumes: source validation, authentic Unity project metadata, Unity tests, build methods, artifact inspectors, smoke runner, visual validator.
- Produces: independent GitHub Actions jobs/artifacts for source, Unity tests/import mutation, Windows, Android APK/AAB, iOS export, visual evidence, and final mutation status on the exact commit.

- [ ] **Step 1: Write workflow-contract RED test**

Require the workflow to contain job IDs:

```text
source-contracts
unity-tests
windows-release
android-release
ios-export
release-evidence
```

Require later jobs to use `needs:` so source failure prevents false downstream PASS. Require artifact upload steps for test XML/logs and platform outputs. Reject any literal keystore password, provisioning profile data, private key, or Unity license value in the workflow.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_release_workflow_contract.py
```

Expected: FAIL because the workflow does not exist.

- [ ] **Step 3: Implement CI dependency graph**

Create `.github/workflows/release-validation.yml` triggered by `workflow_dispatch` and pull requests targeting `main` from `new`.

- `source-contracts` repeats the authoritative existing source gate; it does not replace `.github/workflows/source-validation.yml`.
- `unity-tests` needs `source-contracts`, uses the exact editor version from `ProjectSettings/ProjectVersion.txt`, runs import/compile + EditMode + PlayMode + post-import mutation guard, and uploads XML/logs.
- `windows-release` needs `unity-tests`, builds Windows x64, runs artifact inspection and built-player smoke, and uploads the Windows build plus evidence JSON/log.
- `android-release` needs `unity-tests`, builds APK and AAB with ARM64/IL2CPP, inspects both, and uploads both artifacts/evidence. Signing is not claimed unless an authorized secure credential path is actually configured.
- `ios-export` needs `unity-tests`, runs on macOS, exports Xcode, inspects structure, and uploads export/evidence. Archive/device/App Store signing are separate BLOCKED gates when credentials/devices are absent.
- `release-evidence` needs all prior jobs and assembles their machine-readable outputs for the exact `github.sha`.

Use GitHub/Unity Actions only through pinned major releases already validated for the repository environment; any Unity license is read from repository/organization secrets and never hard-coded. If the authorized Unity license is absent, Unity jobs are BLOCKED operationally and must not be represented as PASS in the release report.

- [ ] **Step 4: Add mutation guards to every Unity/build job**

Before the first Unity command each job records `git status --porcelain=v1` and `git diff --binary`. After import/tests/build it runs `git status --porcelain=v1`, `git diff --exit-code`, and `git diff --check`. Generated ignored build outputs are excluded naturally; any tracked mutation fails the job.

- [ ] **Step 5: Run workflow-contract GREEN and full source gate**

```bash
python3 -m pytest -q scripts/tests/test_release_workflow_contract.py
python3 -m pytest -q scripts/tests
python3 scripts/validate_phase16_readiness.py || true
git diff --check
```

- [ ] **Step 6: Commit, push, and observe CI**

```bash
git add .github/workflows/release-validation.yml scripts/tests/test_release_workflow_contract.py
git commit -m "ci: add phase 16 release validation pipeline"
git push origin new
```

Do not mark the phase green until each required repository-controlled job passes on the exact final SHA. Missing authorized Unity/signing credentials are documented as BLOCKED, not silently skipped as PASS.

---

### Task 16.9: Build Android/iOS, Reconcile Documentation, Final Audit, and `new -> main` PR

**Files:**
- Modify: `README.md`
- Create: `docs/validation/phase-16-release-validation.md`
- Modify only if evidence requires repair: files identified by `/Gaudit` or `/graphRepair`

**Interfaces:**
- Consumes: exact final head SHA, all CI/job/artifact evidence, visual review, signing posture, canonical references.
- Produces: reconciled repository status, final PASS/FAIL/BLOCKED matrix, `/Gaudit` result, and clean `new -> main` pull request.

- [ ] **Step 1: Execute and inspect Android outputs**

For one exact head SHA, run `BuildAndroidApk` and `BuildAndroidAab`. Inspect with:

```bash
python3 scripts/inspect_release_artifacts.py --target android-apk --root build/phase16/android-apk --sha $(git rev-parse HEAD) --out build/phase16/android-apk-evidence.json
python3 scripts/inspect_release_artifacts.py --target android-aab --root build/phase16/android-aab --sha $(git rev-parse HEAD) --out build/phase16/android-aab-evidence.json
```

Require structural PASS for both. Record signing as PASS only when an authorized external signing path produced an inspected signed artifact; otherwise record signing BLOCKED.

- [ ] **Step 2: Execute and inspect iOS Xcode export**

On the Unity-capable macOS environment, run `ExportIos`, then:

```bash
python3 scripts/inspect_release_artifacts.py --target ios --root build/phase16/ios --sha $(git rev-parse HEAD) --out build/phase16/ios-evidence.json
```

Require structural export PASS. Keep Xcode archive, physical-device install, provisioning, App Store signing, and notarization BLOCKED unless independently proven.

- [ ] **Step 3: Reconcile README with verified project state**

Replace stale claims such as `No gameplay implementation has been started yet` and `the next execution boundary is Phase 0–1`. The updated README must accurately state that deterministic gameplay/application/platform contracts and a Unity runtime/release-validation layer exist, name the current release-validation boundary, point to `docs/reference/latest/`, and retain the exact player-facing brand `ClickDungeon`.

Do not claim store-ready/signed/device-tested status unless the matching evidence exists.

- [ ] **Step 4: Write final Phase-16 validation report**

Create `docs/validation/phase-16-release-validation.md` with the exact final head SHA and one explicit status row for:

- source contracts/static validators;
- authentic Unity 6 metadata;
- Unity import/compile;
- EditMode tests;
- PlayMode tests;
- import mutation guard;
- strict production art;
- visual acceptance desktop;
- visual acceptance mobile;
- Windows x64 build;
- Windows artifact inspection;
- Windows built-player smoke;
- Android ARM64/IL2CPP APK;
- Android ARM64/IL2CPP AAB;
- Android artifact inspection;
- Android signing/device/store readiness;
- iOS ARM64/IL2CPP Xcode export;
- iOS export artifact inspection;
- iOS archive/signing/device/App Store readiness;
- final mutation guard;
- runtime branding;
- stale-document reconciliation;
- secrets scan;
- `/Gaudit` result;
- `new -> main` PR/CI state.

Each row is exactly `PASS`, `FAIL`, or `BLOCKED` with a concrete evidence reference/reason.

- [ ] **Step 5: Run final full validation on the exact candidate SHA**

Run all Python tests and validators, strict art, Unity EditMode/PlayMode, Windows build/smoke, Android builds, iOS export, artifact inspectors, visual validator, mutation checks, and secret scan. Record the resulting exact SHA after the final code/document commit and rerun required CI so evidence and code refer to the same SHA.

- [ ] **Step 6: Run `/Gaudit` and conditionally `/graphRepair`**

Run `/Gaudit` across repository structure, dependency/integration graph, release evidence, stale docs, branding, signing posture, mutation evidence, and CI. Classify every finding as release blocker, non-blocking defect, accepted limitation, or external BLOCKED requirement.

Only if `/Gaudit` identifies an actual dependency/integration defect, run `/graphRepair` on that defect, add/strengthen the regression test, apply the smallest repair, and rerun the complete affected chain plus `/Gaudit`. Do not run `/graphRepair` as a speculative rewrite.

- [ ] **Step 7: Open `new -> main` pull request only when repository-controlled blockers are zero**

The PR description must name the final head SHA, summarize PASS evidence, list any external BLOCKED signing/device/store gates without misrepresenting them as PASS, and link the final Phase-16 validation report. Required Source Validation and Release Validation checks must be green on that exact final SHA before merge.

- [ ] **Step 8: Final completion condition**

Phase 16 is complete only when the final report and `/Gaudit` show no unresolved repository-controlled release blocker, `new` is clean, the PR is mergeable, and required CI is green on the exact head. External signing/device/store gates may remain BLOCKED when credentials/devices are genuinely unavailable, exactly as the approved design allows.

---

## Plan Self-Review

**Spec coverage:** Tasks 16.1–16.9 cover branch/SHA evidence, authentic Unity metadata, source-first CI, compile/EditMode/PlayMode, ordered built-player smoke, visual acceptance against canonical references, strict production art, Windows/Android/iOS artifacts, mutation guards, signing separation, evidence-driven repair, stale README reconciliation, final `/Gaudit`, and `new -> main` PR verification.

**Placeholder scan:** The plan contains no `TBD`, no `TODO`, no instruction to invent credentials/metadata/art, and no generic “handle errors” step. Where external capabilities may be unavailable, the required outcome is explicit `BLOCKED` or task stop, not a fabricated pass.

**Type/signature consistency:** Runtime states are `Boot`, `MainMenu`, `DungeonReady`; smoke constants are `CD_SMOKE_BOOT`, `CD_SMOKE_MAIN_MENU`, `CD_SMOKE_START_GAME`, `CD_SMOKE_DUNGEON_READY`; artifact targets are `windows`, `android-apk`, `android-aab`, `ios`; release evidence statuses are exactly `PASS`, `FAIL`, `BLOCKED`; all later tasks consume those same names.
