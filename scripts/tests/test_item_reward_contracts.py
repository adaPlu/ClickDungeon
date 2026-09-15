from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class ItemRewardContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_item_taxonomy_and_rarities_cover_reference_library(self):
        text = self.read("Assets/ClickDungeon/Content/Definitions/ItemDefinition.cs")
        for token in ("Weapon", "Armor", "Offhand", "Accessory", "Consumable", "Key", "Currency", "QuestItem", "Relic"):
            self.assertIn(token, text)
        for token in ("Common", "Uncommon", "Rare", "Epic", "Legendary"):
            self.assertIn(token, text)
        for token in ("EquipmentSlot", "Tags", "SpriteContractPath", "Stackable", "MaxStack"):
            self.assertIn(token, text)

    def test_named_showcase_items_are_canonical(self):
        text = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalItems.cs")
        expected = (
            "item.weapon.dragonslayer", "item.armor.void_plate", "item.weapon.celestial_staff",
            "item.offhand.aegis_of_dawn", "item.armor.crown_of_kings", "item.armor.boots_of_swiftness",
        )
        for content_id in expected:
            self.assertIn(content_id, text)
        for tag in ("helmet", "shield", "boots", "ring", "amulet", "potion", "scroll", "bomb", "food", "utility", "chest"):
            self.assertIn(f'"{tag}"', text)

    def test_inventory_and_equipment_are_runtime_state_not_definition_assets(self):
        inventory = self.read("Assets/ClickDungeon/Progression/InventoryState.cs")
        equipment = self.read("Assets/ClickDungeon/Progression/EquipmentState.cs")
        for token in ("InventoryEntry", "InstanceId", "DefinitionId", "Quantity", "Add", "Remove"):
            self.assertIn(token, inventory)
        for token in ("EquipmentSlot", "Equip", "Unequip", "GetEquipped"):
            self.assertIn(token, equipment)
        joined = inventory + equipment
        for forbidden in ("UnityEngine", "ScriptableObject", "MonoBehaviour"):
            self.assertNotIn(forbidden, joined)

    def test_loot_resolution_uses_explicit_seed_and_no_external_entropy(self):
        text = self.read("Assets/ClickDungeon/Progression/LootResolver.cs")
        self.assertIn("ulong seed", text)
        self.assertIn("Resolve", text)
        self.assertIn("MixSeed", text)
        for forbidden in ("System.Random", "UnityEngine.Random", "DateTime", "Guid.NewGuid"):
            self.assertNotIn(forbidden, text)

    def test_reward_ledger_rejects_duplicate_transaction_ids(self):
        text = self.read("Assets/ClickDungeon/Progression/RewardLedger.cs")
        for token in ("TryBegin", "Commit", "Rollback", "IsCommitted"):
            self.assertIn(token, text)
        self.assertIn("HashSet", text)

    def test_reward_grant_service_owns_inventory_and_currency_mutation(self):
        text = self.read("Assets/ClickDungeon/Progression/RewardGrantService.cs")
        for token in ("RewardLedger", "InventoryState", "CurrencyState", "Grant", "TryBegin", "Commit"):
            self.assertIn(token, text)
        for forbidden in ("Animation", "Animator", "Vfx", "ParticleSystem", "ClickDungeon.Presentation"):
            self.assertNotIn(forbidden, text)

    def test_currency_state_is_integer_and_overflow_safe(self):
        text = self.read("Assets/ClickDungeon/Progression/CurrencyState.cs")
        for token in ("GetBalance", "Credit", "Debit", "long"):
            self.assertIn(token, text)
        self.assertNotRegex(text, r"\b(float|double)\b")


if __name__ == "__main__":
    unittest.main()
