package com.example.pop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pop.R;
import com.example.pop.model.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListItemHolder> {
    private List<Item> mItemList;
    private List<Item> mItemListFull;
    private LayoutInflater mInflater;

    private Context context;

    @NonNull
    @Override
    public ItemListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.item_list_item, parent, false);
        context = parent.getContext();
        return new ItemListItemHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(ItemListItemHolder holder, int position) {
        final Item item = mItemList.get(position);
        holder.itemNameView.setText(item.getName());
        holder.itemQuantityView.setText(String.valueOf(item.getQuantity()));
        holder.itemPriceView.setText(String.format("%.2f", item.getPrice()));
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public ItemListAdapter(Context context, List<Item> itemList) {
        mInflater = LayoutInflater.from(context);

        mItemListFull = new ArrayList<>(itemList);
        this.mItemList = itemList;
    }

    class ItemListItemHolder extends RecyclerView.ViewHolder {
        public final TextView itemNameView;
        public final TextView itemQuantityView;
        public final TextView itemPriceView;
        final ItemListAdapter mAdapter;

        public ItemListItemHolder(View itemView, ItemListAdapter adapter) {
            super(itemView);
            itemNameView = itemView.findViewById(R.id.itemName);
            itemQuantityView = itemView.findViewById(R.id.itemQuantity);
            itemPriceView = itemView.findViewById(R.id.itemPrice);
            this.mAdapter = adapter;
        }
    }
}