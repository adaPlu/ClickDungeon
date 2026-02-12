package com.example.clickdungeon.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.AnimatedMonster;
import com.example.clickdungeon.model.Monster;

import java.util.Locale;

/**
 * Builds {@link AnimatedMonster} instances that mirror the stats of an existing {@link Monster}
 * while wiring up the correct sprite sheets and sound effects for rendering outside combat.
 */
public final class MonsterAnimationHelper {

    private MonsterAnimationHelper() {
    }

    /**
     * Builds an AnimatedMonster that mirrors a base Monster instance.
     */
    @Nullable
    public static AnimatedMonster createAnimatedClone(@NonNull Context context,
                                                      @NonNull Monster source) {
        Config config = Config.forType(source.getMonsterType());
        if (config == null) {
            return null;
        }
        AnimatedMonster clone = new AnimatedMonster(
                context.getApplicationContext(),
                source.getMonsterType(),
                source.getMaxHP(),
                source.getAttack(),
                source.getDefense(),
                config.frameCount,
                config.frameDurationMs,
                config.spriteSheetRes,
                config.moveSoundRes,
                config.attackSoundRes,
                config.defendSoundRes);
        clone.setHasRangedAttack(source.hasRangedAttack());
        clone.setFamily(source.getFamily());
        clone.setAffinity(source.getAffinity());
        clone.setBoss(source.isBoss());
        clone.setBossPhaseCount(source.getBossPhaseCount());
        int hpDelta = clone.getCurrentHP() - source.getCurrentHP();
        if (hpDelta > 0) {
            // Sync the clone's current HP to the source by applying damage delta.
            clone.takeDamage(hpDelta);
        }
        return clone;
    }

    /**
     * Sprite sheet and sound configuration per monster type.
     */
    private static final class Config {
        final int frameCount;
        final long frameDurationMs;
        final int spriteSheetRes;
        final int moveSoundRes;
        final int attackSoundRes;
        final int defendSoundRes;

        Config(int frameCount,
               long frameDurationMs,
               int spriteSheetRes,
               int moveSoundRes,
               int attackSoundRes,
               int defendSoundRes) {
            this.frameCount = frameCount;
            this.frameDurationMs = frameDurationMs;
            this.spriteSheetRes = spriteSheetRes;
            this.moveSoundRes = moveSoundRes;
            this.attackSoundRes = attackSoundRes;
            this.defendSoundRes = defendSoundRes;
        }

        /**
         * Resolves a config for a monster type, defaulting to slime if unknown.
         */
        @Nullable
        static Config forType(@Nullable String type) {
            if (type == null) {
                return defaultConfig();
            }
            switch (type.toLowerCase(Locale.ROOT)) {
                case "goblin":
                    return new Config(4, 120,
                            R.drawable.goblin_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "skeleton":
                    return new Config(4, 120,
                            R.drawable.skeleton_sprite_sheet,
                            R.raw.skeleton_move,
                            R.raw.skeleton_attack,
                            R.raw.skeleton_defend);
                case "orc":
                    return new Config(4, 120,
                            R.drawable.orc_sprite_sheet,
                            R.raw.orc_move,
                            R.raw.orc_attack,
                            R.raw.orc_defend);
                case "troll":
                    return new Config(4, 140,
                            R.drawable.troll_sprite_sheet,
                            R.raw.troll_move,
                            R.raw.troll_attack,
                            R.raw.troll_defend);
                case "witch":
                    return new Config(4, 130,
                            R.drawable.witch_sprite_sheet,
                            R.raw.witch_move,
                            R.raw.witch_attack,
                            R.raw.witch_defend);
                case "demon":
                    return new Config(4, 130,
                            R.drawable.demon_sprite_sheet,
                            R.raw.demon_move,
                            R.raw.demon_attack,
                            R.raw.demon_defend);
                case "vampire":
                    return new Config(4, 120,
                            R.drawable.vampire_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "rat":
                    return new Config(4, 120,
                            R.drawable.rat_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "bat":
                    return new Config(4, 120,
                            R.drawable.bat_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "spider":
                    return new Config(4, 120,
                            R.drawable.spider_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "wolf":
                    return new Config(4, 120,
                            R.drawable.wolf_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "bandit":
                    return new Config(4, 120,
                            R.drawable.bandit_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "cultist":
                    return new Config(4, 120,
                            R.drawable.cultist_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "warlock":
                    return new Config(4, 120,
                            R.drawable.warlock_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "wraith":
                    return new Config(4, 120,
                            R.drawable.wraith_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "golem":
                    return new Config(4, 120,
                            R.drawable.golem_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "lich":
                    return new Config(4, 120,
                            R.drawable.lich_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "hellhound":
                    return new Config(4, 120,
                            R.drawable.hellhound_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "revenant":
                    return new Config(4, 120,
                            R.drawable.revenant_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "archdemon":
                    return new Config(4, 140,
                            R.drawable.archdemon_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "ancient wyrm":
                case "ancient_wyrm":
                    return new Config(4, 150,
                            R.drawable.ancient_wyrm_sprite_sheet,
                            R.raw.goblin_move,
                            R.raw.goblin_attack,
                            R.raw.goblin_defend);
                case "dragon":
                    return new Config(4, 150,
                            R.drawable.dragon_sprite_sheet,
                            R.raw.dragon_move,
                            R.raw.dragon_attack,
                            R.raw.dragon_defend);
                case "slime":
                default:
                    return defaultConfig();
            }
        }

        /**
         * Returns the default configuration used for unknown types.
         */
        private static Config defaultConfig() {
            return new Config(4, 120,
                    R.drawable.slime_sprite_sheet,
                    R.raw.slime_move,
                    R.raw.slime_attack,
                    R.raw.slime_defend);
        }
    }
}
