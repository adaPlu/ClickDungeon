package com.example.clickdungeon.adapter;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clickdungeon.R;
import com.example.clickdungeon.model.ShopItem;
import java.util.List;

public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ShopItem item);
    }

    private final List<ShopItem> shopItems;
    private final OnItemClickListener listener;

    public ShopItemAdapter(List<ShopItem> shopItems, OnItemClickListener listener) {
        this.shopItems = shopItems;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, priceView;

        public ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.textItemName);
            priceView = view.findViewById(R.id.textItemPrice);
        }

        @SuppressLint("DefaultLocale")
        public void bind(final ShopItem item, final OnItemClickListener listener) {
            nameView.setText(item.getName());
            String stockLabel = item.getStock() <= 0 ? "(Out of stock)" : "Stock: " + item.getStock();
            priceView.setText(String.format("Price: %d  %s", item.getPrice(), stockLabel));

            itemView.setAlpha(item.getStock() <= 0 ? 0.5f : 1f);
            itemView.setEnabled(item.getStock() > 0);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @NonNull
    @Override
    public ShopItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(shopItems.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return shopItems.size();
    }
}
