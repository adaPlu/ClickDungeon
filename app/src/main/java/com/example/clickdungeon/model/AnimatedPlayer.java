package com.example.clickdungeon.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.example.clickdungeon.R;
import com.example.clickdungeon.util.SoundManager;

import java.util.HashMap;
import java.util.Map;

public class AnimatedPlayer {
    private final Map<String, Bitmap[]> animations = new HashMap<>();
    private final int frameWidth;
    private final int frameHeight;
    private final int frameCount;
    private final long frameDuration; // duration in ms per frame
    private long lastUpdateTime = 0;
    private int currentFrameIndex = 0;
    private long actionStartTime = 0;

    private String currentAction = "idle";
    private final PlayerClass playerClass;

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

    private int getSpriteResourceForClass(PlayerClass playerClass) {
        switch (playerClass) {
            case KNIGHT: return R.drawable.knight_sprite_sheet;
            case THIEF:  return R.drawable.thief_sprite_sheet;
            case WIZARD: return R.drawable.wizard_sprite_sheet;
            default:     return R.drawable.knight_sprite_sheet;
        }
    }

    public Bitmap getCurrentFrame() {
        long now = System.currentTimeMillis();

        // Automatically reset to idle after short animation delay
        if (!"idle".equals(currentAction) && now - actionStartTime > 600) {
            setAction("idle");
        }

        Bitmap[] frames = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            frames = animations.getOrDefault(currentAction, animations.get("idle"));
        }
        if (frames == null || frames.length == 0) {
            return null;
        }

        if (now - lastUpdateTime >= frameDuration) {
            currentFrameIndex = (currentFrameIndex + 1) % frames.length;
            lastUpdateTime = now;
        }

        return frames[currentFrameIndex];
    }

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

    public void reset() {
        currentFrameIndex = 0;
        lastUpdateTime = 0;
        currentAction = "idle";
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }
}
