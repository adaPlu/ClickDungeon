package com.example.clickdungeon.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.clickdungeon.util.SoundManager;

public class AnimatedMonster extends Monster {

    private Bitmap[] idleFrames, moveFrames, attackFrames, defendFrames;
    private int frameCount;
    private int frameWidth, frameHeight;
    private int currentFrame = 0;
    private long lastFrameChangeTime = 0;
    private final long frameLength; // in ms
    private long actionStartTime = 0;
    private String currentAction = "idle";

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

    public void setAction(String action, Context context) {
        if (!currentAction.equals(action)) {
            currentAction = action;
            currentFrame = 0;
            lastFrameChangeTime = System.currentTimeMillis();
            actionStartTime = lastFrameChangeTime;

            // Use SoundManager
            SoundManager.playForMonster(getMonsterType(), action);
        }
    }

    public Bitmap getCurrentFrame() {
        long now = System.currentTimeMillis();

        // Automatically reset action back to idle after short delay
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

    public String getCurrentAction() {
        return currentAction;
    }

    public void reset() {
        currentAction = "idle";
        currentFrame = 0;
        lastFrameChangeTime = 0;
        actionStartTime = 0;
    }

}
