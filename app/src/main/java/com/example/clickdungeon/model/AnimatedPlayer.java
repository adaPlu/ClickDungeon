package com.example.clickdungeon.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.example.clickdungeon.R;
import com.example.clickdungeon.util.SoundManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages sprite-sheet animation frames for the player character.
 */
public class AnimatedPlayer {
    /** Map of action name to ordered frame strip. */
    private final Map<String, Bitmap[]> animations = new HashMap<>();
    /** Dimensions of a single frame within the sprite sheet. */
    private final int frameWidth;
    private final int frameHeight;
    /** Number of frames per action strip. */
    private final int frameCount;
    /** Time per frame in milliseconds. */
    private final long frameDuration; // duration in ms per frame
    /** Timestamp used to throttle frame advancement. */
    private long lastUpdateTime = 0;
    /** Index of the current frame within the active strip. */
    private int currentFrameIndex = 0;
    /** Timestamp when the current action started. */
    private long actionStartTime = 0;

    /** Active action name (idle/move/attack/defend). */
    private String currentAction = "idle";
    /** Player class used to select sprites and SFX. */
    private final PlayerClass playerClass;

    /**
     * Builds the animation frame map by slicing the class sprite sheet.
     */
    public AnimatedPlayer(Context context, PlayerClass playerClass, int frameWidth, int frameHeight, int frameCount, long frameDuration) {
        this.playerClass = playerClass;
        this.frameDuration = frameDuration;

        int spriteSheetResId = getSpriteResourceForClass(playerClass);
        Bitmap spriteSheet = BitmapFactory.decodeResource(context.getResources(), spriteSheetResId);

        // Derive frame sizing from the sprite sheet (4 rows: idle/move/attack/defend; columns = frames)
        int rows = 4;
        int derivedFrameHeight = rows > 0 ? spriteSheet.getHeight() / rows : spriteSheet.getHeight();
        if (derivedFrameHeight <= 0) {
            derivedFrameHeight = spriteSheet.getHeight();
        }
        // Derive columns from the sheet dimensions so assets can vary by class without code changes.
        int derivedFrameCount = Math.max(1, spriteSheet.getWidth() / Math.max(1, derivedFrameHeight));
        int derivedFrameWidth = spriteSheet.getWidth() / derivedFrameCount;

        this.frameWidth = derivedFrameWidth;
        this.frameHeight = derivedFrameHeight;
        this.frameCount = derivedFrameCount;

        // Assuming row 0 = idle, 1 = move, 2 = attack, 3 = defend
        // TODO: Extended states (hit, defeat, cast, loot, level_up) are spec'd in ANIMATION_ARCHITECTURE.md
        String[] actions = {"idle", "move", "attack", "defend"};
        for (int i = 0; i < actions.length && i < rows; i++) {
            Bitmap[] frames = new Bitmap[this.frameCount];
            for (int j = 0; j < this.frameCount; j++) {
                frames[j] = Bitmap.createBitmap(
                        spriteSheet,
                        j * this.frameWidth,
                        i * this.frameHeight,
                        this.frameWidth,
                        this.frameHeight);
            }
            animations.put(actions[i], frames);
        }
    }

    /** Resolves the sprite sheet resource for the selected class. */
    private int getSpriteResourceForClass(PlayerClass playerClass) {
        switch (playerClass) {
            case KNIGHT: return R.drawable.knight_sprite_sheet;
            case RANGER:
                // TODO: Switch to real ranger_sprite_sheet when available.
                // Current placeholder: use thief sprite until Ranger art is supplied.
                return R.drawable.thief_sprite_sheet;
            case THIEF:  return R.drawable.thief_sprite_sheet;
            case WIZARD: return R.drawable.wizard_sprite_sheet;
            default:     return R.drawable.knight_sprite_sheet;
        }
    }

    /**
     * Returns the current animation frame, advancing based on time elapsed.
     */
    public Bitmap getCurrentFrame() {
        long now = System.currentTimeMillis();

        // Initialize action start time on first use so frames compute deterministically.
        if (actionStartTime == 0) {
            actionStartTime = now;
        }

        // Automatically reset to idle after short animation delay
        if (!"idle".equals(currentAction) && now - actionStartTime > 600) {
            setAction("idle");
        }

        Bitmap[] frames = animations.get(currentAction);
        if (frames == null || frames.length == 0) {
            frames = animations.get("idle");
        }
        if (frames == null || frames.length == 0) {
            return null;
        }

        // Compute frame index from elapsed time since action start. This keeps
        // animations in sync with the action timeline even if the view loop
        // experiences jitter or suspended ticks.
        long elapsed = Math.max(0, now - actionStartTime);
        int frameIndex = (int) ((elapsed / frameDuration) % frames.length);
        currentFrameIndex = frameIndex;
        lastUpdateTime = now;

        return frames[currentFrameIndex];
    }

    /**
     * Switches the active action and plays the class-specific SFX.
     */
    public void setAction(String action) {
        if (!currentAction.equals(action)) {
            currentAction = action;
            currentFrameIndex = 0;
            lastUpdateTime = System.currentTimeMillis();
            actionStartTime = lastUpdateTime;

            // Use SoundManager to play the correct class-based sound
            SoundManager.playForClass(playerClass.name(), action);
        }
    }

    /** Resets animation state back to idle with frame index cleared. */
    public void reset() {
        currentFrameIndex = 0;
        lastUpdateTime = 0;
        currentAction = "idle";
    }

    /** Returns the active action string. */
    public String getCurrentAction() {
        return currentAction;
    }

    /** Returns the player class used to select sprites and SFX. */
    public PlayerClass getPlayerClass() {
        return playerClass;
    }
}
