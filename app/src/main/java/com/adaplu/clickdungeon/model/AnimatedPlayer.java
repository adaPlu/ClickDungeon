package com.adaplu.clickdungeon.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.adaplu.clickdungeon.R;
import com.adaplu.clickdungeon.util.SoundManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages sprite-sheet animation frames for the player character.
 * Optimized for high-resolution assets by calculating optimal sample sizes.
 */
public class AnimatedPlayer {
    private static final String TAG = "AnimatedPlayer";
    
    /** Target frame dimension for UI display (e.g., in dp). */
    private static final int TARGET_DISPLAY_SIZE = 128;

    /** Map of action name to ordered frame strip. */
    private final Map<String, Bitmap[]> animations = new HashMap<>();
    /** Dimensions of a single frame within the sprite sheet. */
    private int frameWidth;
    private int frameHeight;
    /** Number of frames per action strip. */
    private int frameCount;
    /** Time per frame in milliseconds. */
    private final long frameDuration;
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
        Bitmap spriteSheet = decodeSpriteSheet(context, spriteSheetResId, frameCount);
        
        if (spriteSheet == null) {
            this.frameWidth = Math.max(1, frameWidth);
            this.frameHeight = Math.max(1, frameHeight);
            this.frameCount = Math.max(1, frameCount);
            return;
        }

        // Derive frame sizing from the sprite sheet (4 rows: idle/move/attack/defend).
        int rows = 4;
        this.frameHeight = spriteSheet.getHeight() / rows;
        this.frameCount = spriteSheet.getWidth() / this.frameHeight;
        this.frameWidth = spriteSheet.getWidth() / this.frameCount;

        String[] actions = {"idle", "move", "attack", "defend"};
        try {
            for (int i = 0; i < actions.length; i++) {
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
            if (spriteSheet != null && !spriteSheet.isRecycled()) {
                spriteSheet.recycle();
            }
        } catch (IllegalArgumentException | OutOfMemoryError error) {
            Log.w(TAG, "Unable to slice player sprite sheet", error);
            animations.clear();
        }
    }

    private int getSpriteResourceForClass(PlayerClass playerClass) {
        switch (playerClass) {
            case KNIGHT: return R.drawable.knight_sprite_sheet;
            case RANGER: return R.drawable.knight_sprite_sheet; // Fallback
            case THIEF:  return R.drawable.thief_sprite_sheet;
            case WIZARD: return R.drawable.wizard_sprite_sheet;
            default:     return R.drawable.knight_sprite_sheet;
        }
    }

    private Bitmap decodeSpriteSheet(Context context, int resId, int frameCount) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, bounds);

        int targetWidth = frameCount * TARGET_DISPLAY_SIZE;
        int targetHeight = 4 * TARGET_DISPLAY_SIZE;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds, targetWidth, targetHeight);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        
        try {
            return BitmapFactory.decodeResource(context.getResources(), resId, options);
        } catch (OutOfMemoryError error) {
            Log.w(TAG, "Unable to decode player sprite sheet - OOM", error);
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int targetHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > targetHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= targetHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public Bitmap getCurrentFrame() {
        long now = System.currentTimeMillis();
        if (actionStartTime == 0) actionStartTime = now;

        if (!"idle".equals(currentAction) && now - actionStartTime > 800) {
            setAction("idle");
        }

        Bitmap[] frames = animations.get(currentAction);
        if (frames == null || frames.length == 0) {
            frames = animations.get("idle");
        }
        if (frames == null || frames.length == 0) return null;

        int frameIndex = (int) (((now - actionStartTime) / frameDuration) % frames.length);
        return frames[frameIndex];
    }

    public void setAction(String action) {
        if (!currentAction.equals(action)) {
            currentAction = action;
            actionStartTime = System.currentTimeMillis();
            SoundManager.playForClass(playerClass.name(), action);
        }
    }

    public void reset() {
        currentAction = "idle";
        actionStartTime = 0;
    }

    public String getCurrentAction() { return currentAction; }
    public PlayerClass getPlayerClass() { return playerClass; }
}
