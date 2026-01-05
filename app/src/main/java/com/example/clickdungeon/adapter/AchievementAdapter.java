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

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {
    private final List<Achievement> achievements;

    public AchievementAdapter(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, descriptionView;
        TextView statusView;

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
