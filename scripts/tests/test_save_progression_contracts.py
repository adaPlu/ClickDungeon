from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class SaveProgressionContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_save_assembly_exists_and_application_consumes_it(self):
        save_asm = self.read("Assets/ClickDungeon/Save/ClickDungeon.Save.asmdef")
        app_asm = self.read("Assets/ClickDungeon/Application/ClickDungeon.Application.asmdef")
        self.assertIn('"name": "ClickDungeon.Save"', save_asm)
        self.assertIn("ClickDungeon.Save", app_asm)

    def test_profile_run_and_settings_saves_are_independently_versioned(self):
        schema = self.read("Assets/ClickDungeon/Save/SaveSchema.cs")
        for token in ("ProfileVersion", "RunVersion", "SettingsVersion"):
            self.assertIn(token, schema)
        for name in ("ProfileSave", "RunSave", "SettingsSave"):
            text = self.read(f"Assets/ClickDungeon/Save/{name}.cs")
            self.assertIn("SchemaVersion", text)
            self.assertNotIn("UnityEngine", text)

    def test_run_save_contains_every_canonical_resume_boundary(self):
        text = self.read("Assets/ClickDungeon/Save/RunSave.cs")
        for token in (
            "GenerationVersion", "RunSeed", "HeroId", "FloorIndex", "Board",
            "PlayerPosition", "PlayerHealth", "PlayerResource", "Inventory",
            "Statuses", "Enemies", "TileStates", "CommittedRewardTransactionIds",
            "Objectives", "RunProgression", "EncounterRooms",
        ):
            self.assertIn(token, text)

    def test_run_schema_v2_persists_encounter_rooms(self):
        schema = self.read("Assets/ClickDungeon/Save/SaveSchema.cs")
        run = self.read("Assets/ClickDungeon/Save/RunSave.cs")
        self.assertIn("RunVersion = 2", schema)
        for token in (
            "EncounterRoomSave", "EncounterRooms", "DoorKind", "DoorOpened",
            "RoomSeed", "Monsters", "Cleared", "RewardMode", "RewardTier", "Chests",
        ):
            self.assertIn(token, run)

    def test_v1_to_v2_migration_is_registered(self):
        migration = self.read("Assets/ClickDungeon/Save/RunSaveV1ToV2Migration.cs")
        repository = self.read("Assets/ClickDungeon/Save/SaveRepository.cs")
        for token in ("FromVersion => 1", "ToVersion => 2", "EncounterRooms"):
            self.assertIn(token, migration)
        self.assertIn("RunSaveV1ToV2Migration", repository)

    def test_profile_save_owns_persistent_hero_and_account_progression(self):
        text = self.read("Assets/ClickDungeon/Save/ProfileSave.cs")
        for token in ("HeroProgression", "AccountProgression", "Currencies"):
            self.assertIn(token, text)
        self.assertNotIn("RunSave Run", text)

    def test_settings_save_includes_accessibility_and_audio_video_preferences(self):
        text = self.read("Assets/ClickDungeon/Save/SettingsSave.cs")
        for token in (
            "MasterVolume", "MusicVolume", "SfxVolume", "Fullscreen",
            "TextScale", "ReduceMotion", "ScreenShakeScale",
        ):
            self.assertIn(token, text)

    def test_progression_is_split_run_hero_account(self):
        for filename, typename in (
            ("RunProgressionState.cs", "RunProgressionState"),
            ("HeroProgressionState.cs", "HeroProgressionState"),
            ("AccountProgressionState.cs", "AccountProgressionState"),
        ):
            text = self.read(f"Assets/ClickDungeon/Progression/{filename}")
            self.assertIn(typename, text)
            self.assertNotIn("UnityEngine", text)

    def test_hero_xp_leveling_is_integer_deterministic_and_checked(self):
        text = self.read("Assets/ClickDungeon/Progression/HeroProgressionService.cs")
        for token in ("AwardExperience", "ExperienceRequiredForLevel", "checked"):
            self.assertIn(token, text)
        for forbidden in ("UnityEngine.Random", "System.Random", "DateTime", "Guid.NewGuid", "float ", "double "):
            self.assertNotIn(forbidden, text)

    def test_floor_progression_is_duplicate_safe(self):
        text = self.read("Assets/ClickDungeon/Progression/RunProgressionState.cs")
        self.assertIn("CompletedFloorTransactionIds", text)
        self.assertIn("TryCompleteFloor", text)
        self.assertIn("HashSet<string>", text)

    def test_floor_completion_identity_is_derived_from_run_seed_and_floor_index(self):
        run_state = self.read("Assets/ClickDungeon/Progression/RunProgressionState.cs")
        service = self.read("Assets/ClickDungeon/Progression/ProgressionService.cs")
        self.assertIn("BuildFloorTransactionId", run_state)
        self.assertIn('"progress:floor:{0}:{1}"', run_state)
        self.assertIn("BuildFloorTransactionId", service)
        for forbidden in ("Guid.NewGuid", "DateTime", "System.Random"):
            self.assertNotIn(forbidden, run_state + service)

    def test_depth_difficulty_scales_multiple_budgets_not_only_hp(self):
        text = self.read("Assets/ClickDungeon/Progression/DepthDifficultyBudget.cs")
        for token in ("ThreatBudget", "TrapComplexity", "EliteBudget", "SpecialInteractionBudget", "RewardBudget"):
            self.assertIn(token, text)
        for forbidden in ("HealthMultiplier", "HpMultiplier", "EnemyHealthMultiplier"):
            self.assertNotIn(forbidden, text)

    def test_migrations_are_sequential_explicit_and_future_versions_rejected(self):
        text = self.read("Assets/ClickDungeon/Save/SaveMigrationRegistry.cs")
        for token in ("ISaveMigration", "FromVersion", "ToVersion", "Migrate", "migration gap", "future schema"):
            self.assertIn(token, text)

    def test_save_validator_rejects_incomplete_or_future_run_saves(self):
        text = self.read("Assets/ClickDungeon/Save/SaveValidator.cs")
        for token in ("ValidateRun", "RunVersion", "future schema", "RunSeed", "HeroId", "Board"):
            self.assertIn(token, text)

    def test_repository_loads_all_save_types_through_migration_registries(self):
        text = self.read("Assets/ClickDungeon/Save/SaveRepository.cs")
        for token in (
            "TryLoadProfile", "TryLoadRun", "TryLoadSettings",
            "profileMigrations.Migrate", "runMigrations.Migrate", "settingsMigrations.Migrate",
        ):
            self.assertIn(token, text)

    def test_autosave_reasons_are_exactly_the_required_stable_boundaries(self):
        text = self.read("Assets/ClickDungeon/Save/AutosaveReason.cs")
        expected = ("ResolvedTurn", "RewardCommitted", "FloorTransition", "LifecyclePauseOrBackground")
        for token in expected:
            self.assertIn(token, text)

    def test_application_persistence_orchestrator_requests_required_checkpoints(self):
        text = self.read("Assets/ClickDungeon/Application/Persistence/GameSessionPersistenceOrchestrator.cs")
        for token in (
            "ResolveTurn", "TryCommitChestReward", "CompleteFloorTransition", "OnLifecyclePauseOrBackground",
            "AutosaveReason.ResolvedTurn", "AutosaveReason.RewardCommitted",
            "AutosaveReason.FloorTransition", "AutosaveReason.LifecyclePauseOrBackground",
        ):
            self.assertIn(token, text)

    def test_save_and_progression_layers_have_no_presentation_or_scene_authority(self):
        paths = list((ROOT / "Assets/ClickDungeon/Save").rglob("*.cs")) + list((ROOT / "Assets/ClickDungeon/Progression").rglob("*.cs"))
        text = "\n".join(path.read_text() for path in paths)
        for forbidden in ("ClickDungeon.Presentation", "MonoBehaviour", "GameObject", "ScriptableObject", "UnityEngine.Random"):
            self.assertNotIn(forbidden, text)

    def test_ci_runs_phase_14_validator(self):
        workflow = self.read(".github/workflows/source-validation.yml")
        self.assertIn("validate_save_progression_contracts.py", workflow)


if __name__ == "__main__":
    unittest.main()
