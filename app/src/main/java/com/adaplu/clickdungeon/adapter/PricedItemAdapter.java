package com.adaplu.clickdungeon.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adaplu.clickdungeon.R;
import com.adaplu.clickdungeon.model.PricedItem;

import java.util.List;

/**
 * RecyclerView adapter for priced item rows used in shop sell/buyback lists.
 */
public class PricedItemAdapter extends RecyclerView.Adapter<PricedItemAdapter.ViewHolder> {

    /** Callback invoked when a priced item row is tapped. */
    public interface OnItemClickListener {
        void onItemClick(PricedItem item);
    }

    /** Backing list for adapter rows. */
    private final List<PricedItem> items;
    /** Listener for row tap events. */
    private final OnItemClickListener listener;

    /** Creates an adapter with a required click listener. */
    public PricedItemAdapter(List<PricedItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    /**
     * ViewHolder for a priced item row (name + price/quantity).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView priceView;

        /** Binds view references for reuse. */
        ViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textPricedItemName);
            priceView = itemView.findViewById(R.id.textPricedItemPrice);
        }

        /** Binds the item data and click listener. */
        void bind(PricedItem item, OnItemClickListener listener) {
            nameView.setText(item.getName());
            android.content.Context ctx = itemView.getContext();
            String qtyLabel = ctx.getString(R.string.inventory_quantity_format, item.getQuantity());
            String label = ctx.getString(R.string.shop_price_gold, item.getPrice(), qtyLabel);
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

    /** Binds the priced item for the row at the given position. */
    @Override
    public void onBindViewHolder(@NonNull PricedItemAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
