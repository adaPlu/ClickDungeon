package com.example.clickdungeon.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.clickdungeon.util.SoundManager;

/**
 * Monster subclass that manages sprite-sheet animation frames and action state.
 */
public class AnimatedMonster extends Monster {

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
     *
     * @param context context used to decode resources
     * @param monsterType stable type key used for SFX and animation lookup
     * @param hp starting hit points
     * @param atk base attack value
     * @param def base defense value
     * @param frameCount number of frames in each action strip
     * @param frameLength time between frames in milliseconds
     * @param spriteResId sprite sheet resource with 4 rows (idle/move/attack/defend)
     * @param moveSoundId unused legacy parameter for move SFX
     * @param attackSoundId unused legacy parameter for attack SFX
     * @param defendSoundId unused legacy parameter for defend SFX
     */
    public AnimatedMonster(Context context, String monsterType, int hp, int atk, int def, int frameCount, long frameLength,
                           int spriteResId, int moveSoundId, int attackSoundId, int defendSoundId) {
        super(monsterType, hp, atk, def, ""); // no image emoji needed

        this.frameCount = frameCount;
        this.frameLength = frameLength;

        Bitmap spriteSheet = BitmapFactory.decodeResource(context.getResources(), spriteResId);
        this.frameWidth = spriteSheet.getWidth() / frameCount;
        this.frameHeight = spriteSheet.getHeight() / 4;

        idleFrames = new Bitmap[frameCount];
        moveFrames = new Bitmap[frameCount];
        attackFrames = new Bitmap[frameCount];
        defendFrames = new Bitmap[frameCount];

        for (int i = 0; i < frameCount; i++) {
            idleFrames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
            moveFrames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, frameHeight, frameWidth, frameHeight);
            attackFrames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, 2 * frameHeight, frameWidth, frameHeight);
            defendFrames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, 3 * frameHeight, frameWidth, frameHeight);
        }
    }

    /**
     * Updates the current action and plays an SFX cue by default.
     */
    public void setAction(String action, Context context) {
        setAction(action, context, true);
    }

    /**
     * Updates the current action and optionally triggers the action SFX.
     */
    public void setAction(String action, Context context, boolean playSound) {
        if (!currentAction.equals(action)) {
            currentAction = action;
            currentFrame = 0;
            lastFrameChangeTime = System.currentTimeMillis();
            actionStartTime = lastFrameChangeTime;

            // Use SoundManager when allowed
            if (playSound) {
                SoundManager.playForMonster(getMonsterType(), action);
            }
        }
    }

    /**
     * Returns the bitmap for the current animation frame, advancing when needed.
     */
    public Bitmap getCurrentFrame() {
        long now = System.currentTimeMillis();

        // Automatically reset action back to idle after a short animation window.
        if (!"idle".equals(currentAction) && now - actionStartTime > 600) {
            currentAction = "idle";
            currentFrame = 0;
            lastFrameChangeTime = now;
        }

        Bitmap[] frames;
        switch (currentAction) {
            case "move":
                frames = moveFrames;
                break;
            case "attack":
                frames = attackFrames;
                break;
            case "defend":
                frames = defendFrames;
                break;
            default:
                frames = idleFrames;
                break;
        }

        if (now - lastFrameChangeTime > frameLength) {
            currentFrame = (currentFrame + 1) % frameCount;
            lastFrameChangeTime = now;
        }

        return frames[currentFrame];
    }

    /** Returns the active action string ("idle", "move", "attack", "defend"). */
    public String getCurrentAction() {
        return currentAction;
    }

    /** Resets animation state to idle and clears frame timers. */
    public void reset() {
        currentAction = "idle";
        currentFrame = 0;
        lastFrameChangeTime = 0;
        actionStartTime = 0;
    }

}
