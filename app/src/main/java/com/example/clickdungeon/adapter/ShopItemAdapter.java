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

/**
 * RecyclerView adapter for shop inventory rows.
 */
public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.ViewHolder> {

    /** Callback for shop item taps. */
    public interface OnItemClickListener {
        void onItemClick(ShopItem item);
    }

    /** Backing list for shop rows. */
    private final List<ShopItem> shopItems;
    /** Listener for purchase tap events. */
    private final OnItemClickListener listener;

    /** Creates an adapter bound to shop items and a click listener. */
    public ShopItemAdapter(List<ShopItem> shopItems, OnItemClickListener listener) {
        this.shopItems = shopItems;
        this.listener = listener;
    }

    /**
     * ViewHolder for a shop row (name + price/stock).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, priceView;

        /** Binds view references for reuse. */
        public ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.textItemName);
            priceView = view.findViewById(R.id.textItemPrice);
        }

        /** Binds the item label, price, and stock state. */
        @SuppressLint("DefaultLocale")
        public void bind(final ShopItem item, final OnItemClickListener listener) {
            nameView.setText(item.getName());
            android.content.Context ctx = itemView.getContext();
            String stockLabel = item.getStock() <= 0
                    ? ctx.getString(R.string.shop_item_out_of_stock)
                    : ctx.getString(R.string.shop_item_in_stock, item.getStock());
            priceView.setText(ctx.getString(R.string.shop_price_gold, item.getPrice(), stockLabel));
            itemView.setContentDescription(ctx.getString(R.string.shop_item_desc,
                    item.getName(), item.getPrice(), stockLabel));

            itemView.setAlpha(item.getStock() <= 0 ? 0.5f : 1f);
            itemView.setEnabled(item.getStock() > 0);
            itemView.setOnClickListener(listener != null ? v -> listener.onItemClick(item) : null);
        }
    }

    @NonNull
    @Override
    public ShopItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ViewHolder(view);
    }

    /** Binds the shop item for the row at the given position. */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(shopItems.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return shopItems.size();
    }
}
