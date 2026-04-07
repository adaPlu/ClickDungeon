package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.adapter.InventoryAdapter;
import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.InventoryItem;
import com.adaplu.clickdungeon.model.PlayerClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import com.google.gson.Gson;
import com.adaplu.clickdungeon.util.InventoryManager;
import com.adaplu.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
public class InventoryActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "player_prefs")
                .edit()
                .clear()
                .commit();
        InventoryManager.clearInventory(context);
        SharedPreferences profilePrefs = context.getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        profilePrefs.edit()
                .putString("profile", new Gson().toJson(new CharacterProfile("Inv", PlayerClass.KNIGHT)))
                .commit();
    }

    @Test
    public void emptyInventoryShowsEmptyState() {
        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();

        View emptyView = activity.findViewById(R.id.textEmptyInventory);
        assertEquals(View.VISIBLE, emptyView.getVisibility());
    }

    @Test
    public void inventoryCountAndChangeLogRender() {
        InventoryManager.adjustItemQuantity(context, "Healing Potion", 2);
        InventoryManager.recordInventoryChange(context, "Found Healing Potion");

        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();

        TextView countView = activity.findViewById(R.id.textInventoryCount);
        TextView changeLogView = activity.findViewById(R.id.textInventoryChangeLog);
        assertTrue(countView.getText().toString().contains("2"));
        assertTrue(changeLogView.getText().toString().contains("Found Healing Potion"));
    }

    @Test
    public void changeLog_emptyHidesViews() {
        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();

        View title = activity.findViewById(R.id.textInventoryChangeLogTitle);
        View log = activity.findViewById(R.id.textInventoryChangeLog);
        assertEquals(View.GONE, title.getVisibility());
        assertEquals(View.GONE, log.getVisibility());
    }

    @Test
    public void changeLog_nonEmptyShowsLatestEntriesInOrder() {
        InventoryManager.recordInventoryChange(context, "older");
        InventoryManager.recordInventoryChange(context, "newer");

        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();

        TextView log = activity.findViewById(R.id.textInventoryChangeLog);
        assertEquals(View.VISIBLE, activity.findViewById(R.id.textInventoryChangeLogTitle).getVisibility());
        assertEquals(View.VISIBLE, log.getVisibility());
        String text = log.getText().toString();
        assertTrue(text.startsWith("newer"));
        assertTrue(text.contains("older"));
    }

    @Test
    public void toggleEquip_weaponAndArmor_updatesSummaryAndUnequips() {
        InventoryManager.adjustItemQuantity(context, "Iron Sword", 1);
        InventoryManager.adjustItemQuantity(context, "Leather Armor", 1);

        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();
        RecyclerView recycler = activity.findViewById(R.id.recyclerInventory);
        InventoryAdapter adapter = (InventoryAdapter) recycler.getAdapter();
        assertNotNull(adapter);

        invokeInventoryClick(adapter, findItemByName(adapter, "Iron Sword"));
        invokeInventoryClick(adapter, findItemByName(adapter, "Leather Armor"));

        TextView weapon = activity.findViewById(R.id.textEquippedWeapon);
        TextView armor = activity.findViewById(R.id.textEquippedArmor);
        assertTrue(weapon.getText().toString().contains("Iron Sword"));
        assertTrue(armor.getText().toString().contains("Leather Armor"));

        invokeInventoryClick(adapter, findItemByName(adapter, "Iron Sword"));
        invokeInventoryClick(adapter, findItemByName(adapter, "Leather Armor"));
        assertTrue(weapon.getText().toString().contains("None"));
        assertTrue(armor.getText().toString().contains("None"));
    }

    @Test
    public void toggleEquip_nonEquipableItem_returnsFalse() {
        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();
        CharacterProfile profile = new CharacterProfile("NoEquip", PlayerClass.KNIGHT);

        boolean result = org.robolectric.util.ReflectionHelpers.callInstanceMethod(
                activity,
                "toggleEquip",
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(CharacterProfile.class, profile),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(String.class, "Healing Potion"));

        assertFalse(result);
    }

    @Test
    public void updateStatViews_rendersAllStatLabelsFromProfile() {
        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();
        CharacterProfile profile = new CharacterProfile("Stats", PlayerClass.WIZARD);
        profile.setAvailableStatPoints(4);
        profile.increaseStrength(1);
        profile.increaseDexterity(1);
        profile.increaseConstitution(1);
        profile.increaseIntelligence(1);

        TextView pointsView = activity.findViewById(R.id.textStatPoints);
        TextView strengthView = activity.findViewById(R.id.textStatStrength);
        TextView dexterityView = activity.findViewById(R.id.textStatDexterity);
        TextView constitutionView = activity.findViewById(R.id.textStatConstitution);
        TextView intelligenceView = activity.findViewById(R.id.textStatIntelligence);

        org.robolectric.util.ReflectionHelpers.callInstanceMethod(
                activity,
                "updateStatViews",
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(CharacterProfile.class, profile),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(TextView.class, pointsView),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(TextView.class, strengthView),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(TextView.class, dexterityView),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(TextView.class, constitutionView),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(TextView.class, intelligenceView));

        assertEquals(activity.getString(R.string.stat_points_available, profile.getAvailableStatPoints()),
                pointsView.getText().toString());
        assertEquals(activity.getString(R.string.stat_label_strength, profile.getStrength()),
                strengthView.getText().toString());
        assertEquals(activity.getString(R.string.stat_label_dexterity, profile.getDexterity()),
                dexterityView.getText().toString());
        assertEquals(activity.getString(R.string.stat_label_constitution, profile.getConstitution()),
                constitutionView.getText().toString());
        assertEquals(activity.getString(R.string.stat_label_intelligence, profile.getIntelligence()),
                intelligenceView.getText().toString());
    }

    @SuppressWarnings("unchecked")
    private InventoryItem findItemByName(InventoryAdapter adapter, String name) {
        try {
            java.lang.reflect.Field itemsField = InventoryAdapter.class.getDeclaredField("items");
            itemsField.setAccessible(true);
            java.util.List<InventoryItem> items = (java.util.List<InventoryItem>) itemsField.get(adapter);
            for (InventoryItem item : items) {
                if (name.equals(item.getName())) {
                    return item;
                }
            }
            throw new AssertionError("Item not found: " + name);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void invokeInventoryClick(InventoryAdapter adapter, InventoryItem item) {
        try {
            java.lang.reflect.Field listenerField = InventoryAdapter.class.getDeclaredField("listener");
            listenerField.setAccessible(true);
            InventoryAdapter.OnItemClickListener listener =
                    (InventoryAdapter.OnItemClickListener) listenerField.get(adapter);
            listener.onItemClick(item);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
