package com.example.clickdungeon;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.adapter.InventoryAdapter;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.ItemDefinition;
import com.example.clickdungeon.util.FeedbackManager;
import com.example.clickdungeon.util.InventoryManager;
import com.example.clickdungeon.util.ItemCatalog;
import com.example.clickdungeon.util.SoundManager;
import com.google.gson.Gson;

import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.inventory_title);
        setContentView(R.layout.dialog_inventory);

        RecyclerView recycler = findViewById(R.id.recyclerInventory);
        TextView goldView = findViewById(R.id.textInventoryGold);
        TextView platinumView = findViewById(R.id.textInventoryPlatinum);
        TextView mpView = findViewById(R.id.textInventoryMp);
        TextView weaponView = findViewById(R.id.textEquippedWeapon);
        TextView armorView = findViewById(R.id.textEquippedArmor);
        TextView statView = findViewById(R.id.textStatDelta);
        TextView emptyView = findViewById(R.id.textEmptyInventory);
        View statAllocation = findViewById(R.id.layoutStatAllocation);
        TextView statPointsView = findViewById(R.id.textStatPoints);
        TextView statStrengthView = findViewById(R.id.textStatStrength);
        TextView statDexterityView = findViewById(R.id.textStatDexterity);
        TextView statConstitutionView = findViewById(R.id.textStatConstitution);
        TextView statIntelligenceView = findViewById(R.id.textStatIntelligence);
        View statStrengthButton = findViewById(R.id.buttonStatStrength);
        View statDexterityButton = findViewById(R.id.buttonStatDexterity);
        View statConstitutionButton = findViewById(R.id.buttonStatConstitution);
        View statIntelligenceButton = findViewById(R.id.buttonStatIntelligence);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        List<InventoryItem> items = InventoryManager.loadInventory(this);
        CharacterProfile profile = loadProfile();
        InventoryAdapter adapter = new InventoryAdapter(items, item -> {
            if (profile == null) {
                return;
            }
            if (!toggleEquip(profile, item.getName())) {
                android.widget.Toast.makeText(this, R.string.equip_not_allowed, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            saveProfile(profile);
            updateEquippedSummary(profile, weaponView, armorView, statView);
        });
        recycler.setAdapter(adapter);
        goldView.setText(getString(R.string.gold_display_dynamic, InventoryManager.getGold(this)));
        platinumView.setText(getString(R.string.platinum_display_dynamic, InventoryManager.getPlatinum(this)));
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        if (profile != null) {
            updateEquippedSummary(profile, weaponView, armorView, statView);
            mpView.setText(getString(R.string.mp_display_dynamic, profile.getCurrentMP(), profile.getMaxMP()));
            bindStatAllocation(profile,
                    statAllocation,
                    statPointsView,
                    statStrengthView,
                    statDexterityView,
                    statConstitutionView,
                    statIntelligenceView,
                    statStrengthButton,
                    statDexterityButton,
                    statConstitutionButton,
                    statIntelligenceButton,
                    weaponView,
                    armorView,
                    statView,
                    mpView);
        } else if (statAllocation != null) {
            statAllocation.setVisibility(View.GONE);
            mpView.setText(getString(R.string.mp_display_dynamic, 0, 0));
        }
    }

    private CharacterProfile loadProfile() {
        String json = getSharedPreferences("player_profile", MODE_PRIVATE).getString("profile", null);
        return json != null ? new Gson().fromJson(json, CharacterProfile.class) : null;
    }

    private void saveProfile(CharacterProfile profile) {
        getSharedPreferences("player_profile", MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(profile))
                .apply();
    }

    private boolean toggleEquip(CharacterProfile profile, String itemName) {
        ItemDefinition definition = ItemCatalog.getItemDefinition(itemName);
        if (definition == null || definition.getEquipSlot() == ItemDefinition.EquipSlot.NONE) {
            return false;
        }
        switch (definition.getEquipSlot()) {
            case WEAPON:
                if (itemName.equals(profile.getEquippedWeaponName())) {
                    profile.unequipWeapon();
                    showToast(R.string.unequip_success, itemName);
                } else {
                    profile.equipWeapon(itemName);
                    showToast(R.string.equip_success, itemName);
                }
                playEquipFeedback();
                return true;
            case ARMOR:
                if (itemName.equals(profile.getEquippedArmorName())) {
                    profile.unequipArmor();
                    showToast(R.string.unequip_success, itemName);
                } else {
                    profile.equipArmor(itemName);
                    showToast(R.string.equip_success, itemName);
                }
                playEquipFeedback();
                return true;
            default:
                return false;
        }
    }

    private void updateEquippedSummary(CharacterProfile profile,
                                       TextView weaponView,
                                       TextView armorView,
                                       TextView statView) {
        String weapon = profile.getEquippedWeaponName() != null ? profile.getEquippedWeaponName() : "None";
        String armor = profile.getEquippedArmorName() != null ? profile.getEquippedArmorName() : "None";
        weaponView.setText(getString(R.string.equipped_weapon_label, weapon));
        armorView.setText(getString(R.string.equipped_armor_label, armor));
        statView.setText(getString(R.string.equipped_stat_delta,
                profile.getBaseAttack(),
                profile.getAttackBonus(),
                profile.getBaseDefense(),
                profile.getDefenseBonus()));
    }

    private void bindStatAllocation(CharacterProfile profile,
                                    View allocationView,
                                    TextView pointsView,
                                    TextView strengthView,
                                    TextView dexterityView,
                                    TextView constitutionView,
                                    TextView intelligenceView,
                                    View strengthButton,
                                    View dexterityButton,
                                    View constitutionButton,
                                    View intelligenceButton,
                                    TextView weaponView,
                                    TextView armorView,
                                    TextView statView,
                                    TextView mpView) {
        if (allocationView == null) {
            return;
        }
        allocationView.setVisibility(View.VISIBLE);
        Runnable refresh = () -> {
            updateStatViews(profile, pointsView, strengthView,
                    dexterityView, constitutionView, intelligenceView);
            mpView.setText(getString(R.string.mp_display_dynamic, profile.getCurrentMP(), profile.getMaxMP()));
        };
        refresh.run();

        strengthButton.setOnClickListener(v -> {
            if (profile.increaseStrength(1)) {
                saveProfile(profile);
                refresh.run();
                updateEquippedSummary(profile, weaponView, armorView, statView);
            }
        });
        dexterityButton.setOnClickListener(v -> {
            if (profile.increaseDexterity(1)) {
                saveProfile(profile);
                refresh.run();
                updateEquippedSummary(profile, weaponView, armorView, statView);
            }
        });
        constitutionButton.setOnClickListener(v -> {
            if (profile.increaseConstitution(1)) {
                saveProfile(profile);
                refresh.run();
            }
        });
        intelligenceButton.setOnClickListener(v -> {
            if (profile.increaseIntelligence(1)) {
                saveProfile(profile);
                refresh.run();
            }
        });
    }

    private void updateStatViews(CharacterProfile profile,
                                 TextView pointsView,
                                 TextView strengthView,
                                 TextView dexterityView,
                                 TextView constitutionView,
                                 TextView intelligenceView) {
        pointsView.setText(getString(R.string.stat_points_available, profile.getAvailableStatPoints()));
        strengthView.setText(getString(R.string.stat_label_strength, profile.getStrength()));
        dexterityView.setText(getString(R.string.stat_label_dexterity, profile.getDexterity()));
        constitutionView.setText(getString(R.string.stat_label_constitution, profile.getConstitution()));
        intelligenceView.setText(getString(R.string.stat_label_intelligence, profile.getIntelligence()));
    }

    private void showToast(int messageResId, String itemName) {
        android.widget.Toast.makeText(this, getString(messageResId, itemName), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void playEquipFeedback() {
        SoundManager.syncMuteFromSettings(this);
        boolean played = SoundManager.playAndReport(SoundManager.KEY_EFFECT_EQUIP);
        if (!played) {
            FeedbackManager.playSound(this, FeedbackManager.SoundEffect.POSITIVE);
        }
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
    }
}
