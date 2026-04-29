package com.adaplu.clickdungeon.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.adaplu.clickdungeon.util.SoundManager;

/**
 * Monster subclass that manages sprite-sheet animation frames and action state.
 * Optimized to handle high-resolution assets by calculating optimal sample sizes.
 */
public class AnimatedMonster extends Monster {
    private static final String TAG = "AnimatedMonster";
    
    /** Target frame dimension for UI display (e.g., in dp). */
    private static final int TARGET_DISPLAY_SIZE = 128;

    /** Cached frame strips for each action row in the sprite sheet. */
    private Bitmap[] idleFrames, moveFrames, attackFrames, defendFrames;
    /** Number of frames in a single row of the sprite sheet. */
    private int frameCount;
    /** Dimensions for each frame extracted from the sprite sheet. */
    private int frameWidth, frameHeight;
    /** Current frame index within the active action strip. */
    private int currentFrame = 0;
    /** Timestamp of the last frame advance, used to pace animation. */
    private long lastFrameChangeTime = 0;
    /** Frame duration in milliseconds. */
    private final long frameLength; // in ms
    /** Timestamp when the current action started. */
    private long actionStartTime = 0;
    /** Current action name, used to select a frame strip. */
    private String currentAction = "idle";

    /**
     * Creates an animated monster by slicing a sprite sheet into action strips.
     */
    public AnimatedMonster(Context context, String monsterType, int hp, int atk, int def, int frameCount, long frameLength,
                           int spriteResId, int moveSoundId, int attackSoundId, int defendSoundId) {
        super(monsterType, hp, atk, def, "");

        this.frameCount = frameCount;
        this.frameLength = frameLength;

        Bitmap spriteSheet = decodeSpriteSheet(context, spriteResId, frameCount);
        if (spriteSheet == null) {
            this.frameWidth = TARGET_DISPLAY_SIZE;
            this.frameHeight = TARGET_DISPLAY_SIZE;
            idleFrames = new Bitmap[0];
            moveFrames = new Bitmap[0];
            attackFrames = new Bitmap[0];
            defendFrames = new Bitmap[0];
            return;
        }

        this.frameWidth = Math.max(1, spriteSheet.getWidth() / frameCount);
        this.frameHeight = Math.max(1, spriteSheet.getHeight() / 4);

        idleFrames = new Bitmap[frameCount];
        moveFrames = new Bitmap[frameCount];
        attackFrames = new Bitmap[frameCount];
        defendFrames = new Bitmap[frameCount];

        try {
            for (int i = 0; i < frameCount; i++) {
                idleFrames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
                moveFrames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, frameHeight, frameWidth, frameHeight);
                attackFrames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, 2 * frameHeight, frameWidth, frameHeight);
                defendFrames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, 3 * frameHeight, frameWidth, frameHeight);
            }
            // Recycle the original sheet to free memory once strips are created.
            if (spriteSheet != null && !spriteSheet.isRecycled()) {
                spriteSheet.recycle();
            }
        } catch (IllegalArgumentException | OutOfMemoryError error) {
            Log.w(TAG, "Unable to slice monster sprite sheet", error);
            idleFrames = new Bitmap[0];
            moveFrames = new Bitmap[0];
            attackFrames = new Bitmap[0];
            defendFrames = new Bitmap[0];
        }
    }

    private Bitmap decodeSpriteSheet(Context context, int resId, int frameCount) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, bounds);

        // We target high resolution (e.g., 256x256 per frame if assets allow)
        // or fall back to native size if it's already smaller.
        int targetWidth = frameCount * TARGET_DISPLAY_SIZE;
        int targetHeight = 4 * TARGET_DISPLAY_SIZE;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds, targetWidth, targetHeight);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        
        try {
            return BitmapFactory.decodeResource(context.getResources(), resId, options);
        } catch (OutOfMemoryError error) {
            Log.w(TAG, "Unable to decode monster sprite sheet - OOM", error);
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

    /**
     * Updates the current action and triggers the action SFX.
     */
    public void setAction(String action, Context context, boolean playSound) {
        if (!currentAction.equals(action)) {
            currentAction = action;
            currentFrame = 0;
            lastFrameChangeTime = System.currentTimeMillis();
            actionStartTime = lastFrameChangeTime;

            if (playSound) {
                SoundManager.playForMonster(getMonsterType(), action);
            }
        }
    }

    public Bitmap getCurrentFrame() {
        long now = System.currentTimeMillis();
        if (actionStartTime == 0) actionStartTime = now;

        // Reset to idle after animation delay.
        if (!"idle".equals(currentAction) && now - actionStartTime > 800) {
            currentAction = "idle";
            currentFrame = 0;
            actionStartTime = now;
        }

        Bitmap[] frames;
        switch (currentAction) {
            case "move": frames = moveFrames; break;
            case "attack": frames = attackFrames; break;
            case "defend": frames = defendFrames; break;
            default: frames = idleFrames; break;
        }

        if (frames == null || frames.length == 0) return null;

        int frameIndex = (int) (((now - actionStartTime) / frameLength) % frameCount);
        return frames[Math.min(frameIndex, frames.length - 1)];
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public void reset() {
        currentAction = "idle";
        currentFrame = 0;
        actionStartTime = 0;
    }
}
