#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
SAVE = ROOT / "Assets/ClickDungeon/Save"
PROGRESSION = ROOT / "Assets/ClickDungeon/Progression"
APP = ROOT / "Assets/ClickDungeon/Application"


def fail(message):
    print(f"save-progression-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def require(path):
    if not path.is_file():
        fail(f"missing {path.relative_to(ROOT)}")
    return path.read_text()


def require_tokens(text, tokens, label):
    for token in tokens:
        if token not in text:
            fail(f"{label} missing {token}")


def main():
    asm = require(SAVE / "ClickDungeon.Save.asmdef")
    app_asm = require(APP / "ClickDungeon.Application.asmdef")
    schema = require(SAVE / "SaveSchema.cs")
    profile = require(SAVE / "ProfileSave.cs")
    run = require(SAVE / "RunSave.cs")
    settings = require(SAVE / "SettingsSave.cs")
    migration = require(SAVE / "SaveMigrationRegistry.cs")
    validator = require(SAVE / "SaveValidator.cs")
    repository = require(SAVE / "SaveRepository.cs")
    reasons = require(SAVE / "AutosaveReason.cs")
    orchestrator = require(APP / "Persistence/GameSessionPersistenceOrchestrator.cs")
    difficulty = require(PROGRESSION / "DepthDifficultyBudget.cs")
    run_progress = require(PROGRESSION / "RunProgressionState.cs")
    hero_progress = require(PROGRESSION / "HeroProgressionService.cs")
    progression_service = require(PROGRESSION / "ProgressionService.cs")

    require_tokens(asm, ('"name": "ClickDungeon.Save"', "ClickDungeon.Progression"), "save assembly")
    if "ClickDungeon.Save" not in app_asm:
        fail("Application does not reference Save assembly")
    require_tokens(schema, ("ProfileVersion", "RunVersion", "SettingsVersion"), "save schema")
    require_tokens(profile, ("SchemaVersion", "HeroProgression", "AccountProgression", "Currencies"), "profile save")
    require_tokens(run, (
        "SchemaVersion", "GenerationVersion", "RunSeed", "HeroId", "FloorIndex", "Board",
        "PlayerPosition", "PlayerHealth", "PlayerResource", "Inventory", "Statuses", "Enemies",
        "TileStates", "CommittedRewardTransactionIds", "Objectives", "RunProgression",
    ), "run save")
    require_tokens(settings, ("MasterVolume", "MusicVolume", "SfxVolume", "Fullscreen", "TextScale", "ReduceMotion", "ScreenShakeScale"), "settings save")
    require_tokens(migration, ("ISaveMigration", "FromVersion", "ToVersion", "Migrate", "migration gap", "future schema"), "migration registry")
    require_tokens(validator, ("ValidateRun", "RunVersion", "future schema", "HeroId", "Board"), "save validator")
    require_tokens(repository, (
        "TryLoadProfile", "TryLoadRun", "TryLoadSettings",
        "profileMigrations.Migrate", "runMigrations.Migrate", "settingsMigrations.Migrate",
    ), "save repository")
    require_tokens(reasons, ("ResolvedTurn", "RewardCommitted", "FloorTransition", "LifecyclePauseOrBackground"), "autosave reasons")
    require_tokens(orchestrator, (
        "ResolveTurn", "TryCommitChestReward", "CompleteFloorTransition", "OnLifecyclePauseOrBackground",
        "AutosaveReason.ResolvedTurn", "AutosaveReason.RewardCommitted", "AutosaveReason.FloorTransition",
        "AutosaveReason.LifecyclePauseOrBackground",
    ), "persistence orchestrator")
    require_tokens(difficulty, ("ThreatBudget", "TrapComplexity", "EliteBudget", "SpecialInteractionBudget", "RewardBudget"), "difficulty budget")
    require_tokens(run_progress, ("CompletedFloorTransactionIds", "TryCompleteFloor", "HashSet<string>", "BuildFloorTransactionId", '"progress:floor:{0}:{1}"'), "run progression")
    require_tokens(progression_service, ("BuildFloorTransactionId", "AwardExperience", "RecordFloorReached"), "progression service")
    require_tokens(hero_progress, ("AwardExperience", "ExperienceRequiredForLevel", "checked"), "hero progression")

    source_files = list(SAVE.rglob("*.cs")) + list(PROGRESSION.rglob("*.cs"))
    source = "\n".join(path.read_text() for path in source_files)
    for forbidden in (
        "ClickDungeon.Presentation", "MonoBehaviour", "GameObject", "ScriptableObject",
        "UnityEngine.Random", "System.Random", "Guid.NewGuid", "DateTime",
    ):
        if forbidden in source:
            fail(f"forbidden authority/runtime dependency found: {forbidden}")
    for forbidden in ("HealthMultiplier", "HpMultiplier", "EnemyHealthMultiplier"):
        if forbidden in difficulty:
            fail(f"difficulty collapsed to HP-only scaling: {forbidden}")

    print("save progression contracts: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
