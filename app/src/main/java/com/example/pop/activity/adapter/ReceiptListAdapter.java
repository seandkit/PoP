package com.example.pop.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pop.R;
import com.example.pop.model.Receipt;

import java.util.LinkedList;
import java.util.List;

public class ReceiptListAdapter extends RecyclerView.Adapter<ReceiptListAdapter.ReceiptListItemHolder> {

    private List<Receipt> mReceiptList;
    private LayoutInflater mInflater;

    @NonNull
    @Override
    public ReceiptListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.receipt_list_item, parent, false);
        return new ReceiptListItemHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(ReceiptListItemHolder holder, int position) {
        Receipt receipt = mReceiptList.get(position);
        holder.receiptDateView.setText(receipt.getDate());
        holder.receiptShopView.setText(receipt.getVendorName());
        holder.receiptTotalView.setText(""+receipt.getReceiptTotal());
    }

    @Override
    public int getItemCount() {
        return mReceiptList.size();
    }

    public ReceiptListAdapter(Context context, List<Receipt> receiptList) {
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


