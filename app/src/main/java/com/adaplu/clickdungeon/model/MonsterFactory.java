package com.adaplu.clickdungeon.model;

import android.content.Context;

import com.adaplu.clickdungeon.R;

import java.util.Locale;

/**
 * Factory for building animated monsters from type identifiers.
 */
public class MonsterFactory {

    /**
     * Creates an AnimatedMonster with stats and sprite resources for the given type.
     *
     * @param context used to resolve sprite and sound resources
     * @param type case-insensitive monster key
     * @return configured AnimatedMonster instance
     */
    public static AnimatedMonster create(Context context, String type) {
        switch (type.toLowerCase(Locale.ROOT)) {
            case "slime":
                return new AnimatedMonster(context, "Slime", 3, 1, 0, 4, 120,
                        R.drawable.slime_sprite_sheet,
                        R.raw.slime_move,
                        R.raw.slime_attack,
                        R.raw.slime_defend);

            case "goblin":
                return new AnimatedMonster(context, "Goblin", 5, 2, 1, 4, 120,
                        R.drawable.goblin_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "skeleton":
                return new AnimatedMonster(context, "Skeleton", 4, 2, 1, 4, 120,
                        R.drawable.skeleton_sprite_sheet,
                        R.raw.skeleton_move,
                        R.raw.skeleton_attack,
                        R.raw.skeleton_defend);

            case "orc":
                return new AnimatedMonster(context, "Orc", 7, 3, 2, 4, 120,
                        R.drawable.orc_sprite_sheet,
                        R.raw.orc_move,
                        R.raw.orc_attack,
                        R.raw.orc_defend);

            case "troll":
                return new AnimatedMonster(context, "Troll", 10, 4, 3, 4, 140,
                        R.drawable.troll_sprite_sheet,
                        R.raw.troll_move,
                        R.raw.troll_attack,
                        R.raw.troll_defend);

            case "witch":
                return new AnimatedMonster(context, "Witch", 6, 3, 2, 4, 130,
                        R.drawable.witch_sprite_sheet,
                        R.raw.witch_move,
                        R.raw.witch_attack,
                        R.raw.witch_defend);

            case "vampire":
                return new AnimatedMonster(context, "Vampire", 6, 4, 3, 4, 120,
                        R.drawable.vampire_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "demon":
                return new AnimatedMonster(context, "Demon", 9, 5, 3, 4, 130,
                        R.drawable.demon_sprite_sheet,
                        R.raw.demon_move,
                        R.raw.demon_attack,
                        R.raw.demon_defend);

            case "rat":
                return new AnimatedMonster(context, "Rat", 3, 1, 0, 4, 120,
                        R.drawable.rat_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "bat":
                return new AnimatedMonster(context, "Bat", 3, 2, 0, 4, 120,
                        R.drawable.bat_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "spider":
                return new AnimatedMonster(context, "Spider", 4, 2, 1, 4, 120,
                        R.drawable.spider_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "wolf":
                return new AnimatedMonster(context, "Wolf", 5, 3, 1, 4, 120,
                        R.drawable.wolf_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "bandit":
                return new AnimatedMonster(context, "Bandit", 6, 3, 2, 4, 120,
                        R.drawable.bandit_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "cultist":
                return new AnimatedMonster(context, "Cultist", 6, 3, 2, 4, 120,
                        R.drawable.cultist_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "warlock":
                return new AnimatedMonster(context, "Warlock", 6, 4, 2, 4, 120,
                        R.drawable.warlock_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "wraith":
                return new AnimatedMonster(context, "Wraith", 7, 4, 3, 4, 120,
                        R.drawable.wraith_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "golem":
                return new AnimatedMonster(context, "Golem", 8, 4, 4, 4, 120,
                        R.drawable.golem_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "lich":
                return new AnimatedMonster(context, "Lich", 9, 5, 4, 4, 120,
                        R.drawable.lich_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "hellhound":
                return new AnimatedMonster(context, "Hellhound", 9, 6, 3, 4, 120,
                        R.drawable.hellhound_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "revenant":
                return new AnimatedMonster(context, "Revenant", 8, 5, 3, 4, 120,
                        R.drawable.revenant_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "archdemon":
                return new AnimatedMonster(context, "Archdemon", 11, 7, 5, 4, 140,
                        R.drawable.archdemon_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "ancient wyrm":
            case "ancient_wyrm":
                return new AnimatedMonster(context, "Ancient Wyrm", 12, 7, 5, 4, 150,
                        R.drawable.ancient_wyrm_sprite_sheet,
                        R.raw.goblin_move,
                        R.raw.goblin_attack,
                        R.raw.goblin_defend);

            case "dragon":
                return new AnimatedMonster(context, "Dragon", 12, 6, 4, 4, 150,
                        R.drawable.dragon_sprite_sheet,
                        R.raw.dragon_move,
                        R.raw.dragon_attack,
                        R.raw.dragon_defend);

            default:
                throw new IllegalArgumentException("Unknown monster type: " + type);
        }
    }
}
