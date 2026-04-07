package com.example.clickdungeon.model;

/**
 * Represents a single achievement entry with display metadata and unlock state.
 */
public class Achievement {
    /** Short display name shown in the achievements list. */
    private final String title;
    /** Human-readable description of the requirement or reward. */
    private final String description;
    /** Tracks whether the player has already earned this achievement. */
    private boolean isUnlocked;

    /**
     * Creates a new achievement entry.
     *
     * @param title the label displayed in the UI
     * @param description the explanatory text for the achievement
     * @param isUnlocked whether the achievement is already earned
     */
    public Achievement(String title, String description, boolean isUnlocked) {
        this.title = title;
        this.description = description;
        this.isUnlocked = isUnlocked;
    }

    /** Returns the display title shown in the UI list. */
    public String getTitle() {
        return title;
    }

    /** Returns the descriptive subtitle for this achievement. */
    public String getDescription() {
        return description;
    }

    /** Returns true when the player has earned this achievement. */
    public boolean isUnlocked() {
        return isUnlocked;
    }

    /** Updates the unlocked state as the player completes requirements. */
    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
}
