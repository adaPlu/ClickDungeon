package com.example.clickdungeon.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clickdungeon.R;
import com.example.clickdungeon.model.Achievement;

import java.util.List;

/**
 * RecyclerView adapter that binds achievement data to list rows.
 */
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {
    /** Backing list of achievements to display. */
    private final List<Achievement> achievements;

    /**
     * Creates an adapter for the provided achievement list.
     */
    public AchievementAdapter(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    /**
     * ViewHolder for a single achievement row.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, descriptionView;
        TextView statusView;

        /** Binds row views for reuse during scrolling. */
        public ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.textAchievementTitle);
            descriptionView = view.findViewById(R.id.textAchievementDescription);
            statusView = view.findViewById(R.id.textAchievementStatus);
        }
    }

    @NonNull
    @Override
    public AchievementAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    /** Binds achievement text and unlock styling to the row. */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.titleView.setText(achievement.getTitle());
        holder.descriptionView.setText(achievement.getDescription());
        boolean unlocked = achievement.isUnlocked();
        holder.statusView.setText(unlocked ? "Completed" : "Locked");
        holder.itemView.setAlpha(unlocked ? 1f : 0.65f);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }
}
