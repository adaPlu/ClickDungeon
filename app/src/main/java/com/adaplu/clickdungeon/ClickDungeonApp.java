package com.adaplu.clickdungeon;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.adaplu.clickdungeon.util.SoundManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * ClickDungeonApp is the main application class for the ClickDungeon game.
 * It initializes global services such as {@link SoundManager} and routes lifecycle callbacks so
 * audio pauses/resumes alongside the host activities.
 */
public class ClickDungeonApp extends Application {

    private final ActivityLifecycleCallbacks audioLifecycleCallbacks = new SimpleAudioLifecycleCallbacks();

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the sound manager at app startup to ensure assets are ready.
        SoundManager.init(this);
        // Ensure the initial mute state respects user preferences.
        SoundManager.syncMuteFromSettings(this);
        // Register lifecycle callbacks to handle sound pausing/resuming globally.
        registerActivityLifecycleCallbacks(audioLifecycleCallbacks);
        FirebaseCrashlytics.getInstance()
                .setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
    }

    @Override
    public void onTerminate() {
        // Unregister to prevent leaks and release sound resources.
        unregisterActivityLifecycleCallbacks(audioLifecycleCallbacks);
        SoundManager.release();
        super.onTerminate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // Pause all sounds when the UI is no longer visible to the user.
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            SoundManager.pauseAll();
        }
    }

    /**
     * SimpleAudioLifecycleCallbacks pauses and resumes the SoundManager based on
     * the visibility of the activities.
     */
    private static final class SimpleAudioLifecycleCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            // Re-sync mute state and resume playback when any activity comes to the foreground.
            SoundManager.syncMuteFromSettings(activity);
            SoundManager.resumeAll();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            // Pause sounds when an activity is losing focus to save CPU and battery.
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
