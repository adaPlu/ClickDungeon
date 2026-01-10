package com.example.clickdungeon.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.PricedItem;

import java.util.List;

public class PricedItemAdapter extends RecyclerView.Adapter<PricedItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PricedItem item);
    }

    private final List<PricedItem> items;
    private final OnItemClickListener listener;

    public PricedItemAdapter(List<PricedItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView priceView;

        ViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textPricedItemName);
            priceView = itemView.findViewById(R.id.textPricedItemPrice);
        }

        void bind(PricedItem item, OnItemClickListener listener) {
            nameView.setText(item.getName());
            String label = itemView.getContext().getString(
                    R.string.shop_price_gold,
                    item.getPrice(),
                    "Qty: " + item.getQuantity());
            priceView.setText(label);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @NonNull
    @Override
    public PricedItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_priced, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PricedItemAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
