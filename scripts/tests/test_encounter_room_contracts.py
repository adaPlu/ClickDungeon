from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class EncounterRoomContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_encounter_room_generation_contracts(self):
        text = self.read("Assets/ClickDungeon/Dungeon/Generation/EncounterRoomGenerator.cs")
        for token in (
            "floor.FloorIndex == 1",
            "35",
            "75",
            "NextInt(3, 6)",
            "EncounterDoorKind.Closed",
            "EncounterDoorKind.Locked",
        ):
            self.assertIn(token, text)

        layout = self.read("Assets/ClickDungeon/Dungeon/Runtime/EncounterRoomLayout.cs")
        self.assertIn("public const int Width = 3", layout)
        self.assertIn("public const int Height = 3", layout)

    def test_floor_state_exposes_at_most_one_encounter_room(self):
        text = self.read("Assets/ClickDungeon/Dungeon/Runtime/FloorState.cs")
        self.assertIn("EncounterRoom", text)
        self.assertIn("SetEncounterRoom", text)
        self.assertIn("Floor already has an encounter room", text)

    def test_generation_has_room_validator_and_main_board_doorway_attachment(self):
        validator = self.read("Assets/ClickDungeon/Dungeon/Generation/EncounterRoomValidator.cs")
        for token in ("3", "5", "Start", "Exit", "EncounterRewardTier.Higher"):
            self.assertIn(token, validator)

        generator = self.read("Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs")
        self.assertIn("EncounterRoomGenerator", generator)
        self.assertIn("SetEncounterRoom", generator)
        self.assertIn("tile.door_closed", generator)
        self.assertIn("tile.door_locked", generator)


if __name__ == "__main__":
    unittest.main()
