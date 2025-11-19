package com.example.clickdungeon.model;

import android.content.Context;

import com.example.clickdungeon.R;

public class MonsterFactory {

    public static AnimatedMonster create(Context context, String type) {
        switch (type.toLowerCase()) {
            case "slime":
                return new AnimatedMonster(context, "Slime", 3, 1, 0, 6, 100,
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

            case "demon":
                return new AnimatedMonster(context, "Demon", 9, 5, 3, 4, 130,
                        R.drawable.demon_sprite_sheet,
                        R.raw.demon_move,
                        R.raw.demon_attack,
                        R.raw.demon_defend);


            case "dragon":
                return new AnimatedMonster(context, "Dragon", 12, 6, 4, 4, 150,
                        R.drawable.demon_sprite_sheet,
                        R.raw.dragon_move,
                        R.raw.dragon_attack,
                        R.raw.dragon_defend);

            default:
                throw new IllegalArgumentException("Unknown monster type: " + type);
        }
    }
}
