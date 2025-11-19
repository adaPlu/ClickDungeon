package com.example.clickdungeon;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.example.clickdungeon.util.SoundManager;

/**
 * Initializes global services such as {@link SoundManager} and routes lifecycle callbacks so
 * audio pauses/resumes alongside the host activities.
 */
public class ClickDungeonApp extends Application {

    private final ActivityLifecycleCallbacks audioLifecycleCallbacks = new SimpleAudioLifecycleCallbacks();

    @Override
    public void onCreate() {
        super.onCreate();
        SoundManager.init(this);
        SoundManager.syncMuteFromSettings(this);
        registerActivityLifecycleCallbacks(audioLifecycleCallbacks);
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(audioLifecycleCallbacks);
        SoundManager.release();
        super.onTerminate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            SoundManager.pauseAll();
        }
    }

    private static final class SimpleAudioLifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            SoundManager.syncMuteFromSettings(activity);
            SoundManager.resumeAll();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            SoundManager.pauseAll();
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }
}
