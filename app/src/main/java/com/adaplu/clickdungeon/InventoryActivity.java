package com.adaplu.clickdungeon;

import com.adaplu.clickdungeon.util.SecurePreferences;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adaplu.clickdungeon.adapter.InventoryAdapter;
import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.InventoryItem;
import com.adaplu.clickdungeon.model.ItemDefinition;
import com.adaplu.clickdungeon.util.FeedbackManager;
import com.adaplu.clickdungeon.util.InventoryManager;
import com.adaplu.clickdungeon.util.ItemCatalog;
import com.adaplu.clickdungeon.util.SoundManager;
import com.google.gson.Gson;

import java.util.List;

/**
 * InventoryActivity displays collected items, equipment, and the simplified three-stat summary.
 */
public class InventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.inventory_title);
        setContentView(R.layout.dialog_inventory);

        RecyclerView recycler = findViewById(R.id.recyclerInventory);
        TextView goldView = findViewById(R.id.textInventoryGold);
        TextView platinumView = findViewById(R.id.textInventoryPlatinum);
        TextView countView = findViewById(R.id.textInventoryCount);
        TextView weaponView = findViewById(R.id.textEquippedWeapon);
        TextView armorView = findViewById(R.id.textEquippedArmor);
        TextView statView = findViewById(R.id.textStatDelta);
        TextView emptyView = findViewById(R.id.textEmptyInventory);
        TextView changeLogTitle = findViewById(R.id.textInventoryChangeLogTitle);
        TextView changeLogView = findViewById(R.id.textInventoryChangeLog);
        TextView classXpView = findViewById(R.id.textCurrentClassXp);
        TextView healthView = findViewById(R.id.textStatHealth);
        TextView attackView = findViewById(R.id.textStatAttack);
        TextView defenseView = findViewById(R.id.textStatDefense);

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
            updateStatSummary(profile, classXpView, healthView, attackView, defenseView);
        });
        recycler.setAdapter(adapter);

        goldView.setText(getString(R.string.gold_display_dynamic, InventoryManager.getGold(this)));
        platinumView.setText(getString(R.string.platinum_display_dynamic, InventoryManager.getPlatinum(this)));
        if (countView != null) {
            int totalCount = 0;
            for (InventoryItem item : items) {
                totalCount += Math.max(0, item.getQuantity());
            }
            countView.setText(getString(R.string.inventory_item_count, totalCount));
        }
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        if (profile != null) {
            updateEquippedSummary(profile, weaponView, armorView, statView);
            updateStatSummary(profile, classXpView, healthView, attackView, defenseView);
        }

        bindChangeLog(changeLogTitle, changeLogView, InventoryManager.getInventoryChangeLog(this));
    }

    private CharacterProfile loadProfile() {
        String json = SecurePreferences.get(this, "player_profile").getString("profile", null);
        return json != null ? new Gson().fromJson(json, CharacterProfile.class) : null;
    }

    private void saveProfile(CharacterProfile profile) {
        SecurePreferences.get(this, "player_profile")
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

    private void updateStatSummary(CharacterProfile profile,
                                   TextView classXpView,
                                   TextView healthView,
                                   TextView attackView,
                                   TextView defenseView) {
        classXpView.setText(getString(R.string.class_xp_available, profile.getCurrentClassXp()));
        healthView.setText(getString(R.string.stat_label_health,
                profile.getCurrentHP(),
                profile.getMaxHP(),
                profile.getHealthBoost()));
        attackView.setText(getString(R.string.stat_label_attack,
                profile.getBaseAttack(),
                profile.getAttackBoost()));
        defenseView.setText(getString(R.string.stat_label_defense,
                profile.getBaseDefense(),
                profile.getDefenseBoost()));
    }

    private void bindChangeLog(TextView titleView, TextView logView, List<String> entries) {
        if (titleView == null || logView == null) {
            return;
        }
        if (entries == null || entries.isEmpty()) {
            titleView.setVisibility(View.GONE);
            logView.setVisibility(View.GONE);
            return;
        }
        titleView.setVisibility(View.VISIBLE);
        logView.setVisibility(View.VISIBLE);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(entries.get(i));
        }
        logView.setText(builder.toString());
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
