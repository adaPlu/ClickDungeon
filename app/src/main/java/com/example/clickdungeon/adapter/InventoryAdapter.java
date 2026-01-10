package com.example.clickdungeon.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.InventoryItem;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    private final List<InventoryItem> items;
    private final OnItemClickListener listener;

    public InventoryAdapter(List<InventoryItem> items) {
        this(items, null);
    }

    public InventoryAdapter(List<InventoryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textItemName);
            quantity = itemView.findViewById(R.id.textItemQuantity);
        }

        void bind(InventoryItem item, OnItemClickListener listener) {
            name.setText(item.getName());
            quantity.setText(itemView.getContext()
                    .getString(R.string.inventory_quantity_format, item.getQuantity()));
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(item));
            } else {
                itemView.setOnClickListener(null);
            }
        }
    }

    @NonNull
    @Override
    public InventoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
