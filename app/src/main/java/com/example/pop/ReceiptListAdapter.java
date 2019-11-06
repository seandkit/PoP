package com.example.pop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

public class ReceiptListAdapter extends RecyclerView.Adapter<ReceiptListAdapter.ReceiptListItemHolder> {

    private LinkedList<String[]> mReceiptList;
    private LayoutInflater mInflater;

    @NonNull
    @Override
    public ReceiptListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.receipt_list_item, parent, false);
        return new ReceiptListItemHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(ReceiptListItemHolder holder, int position) {
        String[] mCurrent = mReceiptList.get(position);
        holder.receiptDateView.setText(mCurrent[0]);
        holder.receiptShopView.setText(mCurrent[1]);
        holder.receiptTotalView.setText(mCurrent[2]);
    }

    @Override
    public int getItemCount() {
        return mReceiptList.size();
    }

    public ReceiptListAdapter(Context context, LinkedList<String[]> receiptList) {
        mInflater = LayoutInflater.from(context);
        this.mReceiptList = receiptList;
    }

    class ReceiptListItemHolder extends RecyclerView.ViewHolder {
        public final TextView receiptDateView;
        public final TextView receiptShopView;
        public final TextView receiptTotalView;
        final ReceiptListAdapter mAdapter;

        public ReceiptListItemHolder(View itemView, ReceiptListAdapter adapter) {
            super(itemView);
            receiptDateView = itemView.findViewById(R.id.receiptDate);
            receiptShopView = itemView.findViewById(R.id.receiptShop);
            receiptTotalView = itemView.findViewById(R.id.receiptTotal);
            this.mAdapter = adapter;
        }
    }
}


