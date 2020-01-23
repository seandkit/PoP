package com.example.pop.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pop.R;
import com.example.pop.activity.LoginActivity;
import com.example.pop.activity.ReceiptActivity;
import com.example.pop.activity.ReceiptFragment;
import com.example.pop.activity.Receipt_main_activity;
import com.example.pop.activity.RecentTransactionsActivity;
import com.example.pop.activity.RegisterActivity;
import com.example.pop.model.Receipt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ReceiptListAdapter extends RecyclerView.Adapter<ReceiptListAdapter.ReceiptListItemHolder> implements Filterable {

    private List<Receipt> mReceiptList;
    private List<Receipt> mReceiptListFull;
    private LayoutInflater mInflater;

    private Context context;

    @NonNull
    @Override
    public ReceiptListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.receipt_list_item, parent, false);
        context = parent.getContext();
        return new ReceiptListItemHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(ReceiptListItemHolder holder, int position) {
        Receipt receipt = mReceiptList.get(position);
        holder.receiptDateView.setText(receipt.getDate());
        holder.receiptShopView.setText(receipt.getVendorName());
        holder.receiptTotalView.setText(""+receipt.getReceiptTotal());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(context, ReceiptActivity.class);
                //context.startActivity(intent);
                System.out.println("Click");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mReceiptList.size();
    }

    public ReceiptListAdapter(Context context, List<Receipt> receiptList) {
        mInflater = LayoutInflater.from(context);

        mReceiptListFull = new ArrayList<>(receiptList);
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

    @Override
    public Filter getFilter() {
        return receiptFilter;
    }

    private Filter receiptFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Receipt> filteredList = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0){

                filteredList.addAll(mReceiptListFull);
            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Receipt receipt : mReceiptListFull)
                {
                    if(receipt.getVendorName().toLowerCase().contains(filterPattern)){
                        filteredList.add(receipt);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            mReceiptList.clear();
            mReceiptList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };
}



