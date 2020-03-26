package com.example.pop.activity.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.FolderActivity;
import com.example.pop.activity.FragmentHolder;
import com.example.pop.activity.Fragment_Popup_Folders;
import com.example.pop.activity.ReceiptActivity;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Receipt;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderReceiptListAdapter extends RecyclerView.Adapter<FolderReceiptListAdapter.ReceiptListItemHolder> implements Filterable {

    private List<Receipt> mReceiptList;
    private List<Receipt> mReceiptListFull;
    private LayoutInflater mInflater;

    private Context context;

    private int receiptId;
    private int recyclerListId;
    private int success;
    private String message;

    @NonNull
    @Override
    public ReceiptListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.receipt_list_item, parent, false);
        return new ReceiptListItemHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(final ReceiptListItemHolder holder, int position) {
        final Receipt receipt = mReceiptList.get(position);
        holder.receiptDateView.setText(receipt.getDate());
        holder.receiptShopView.setText(receipt.getVendorName());
        holder.receiptTotalView.setText(String.format("%.2f", receipt.getReceiptTotal()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckNetworkStatus.isNetworkAvailable(context)) {
                    Intent intent = new Intent(context, ReceiptActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("receiptID", receipt.getId());
                    context.startActivity(intent);
                }
                else {
                    Toast.makeText(context,"No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CardView cardView = v.findViewById(R.id.receiptListCardView);

                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, cardView);
                //inflating menu from xml resource
                popup.inflate(R.menu.folder_receipt_list_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.viewReceipt:
                                if (CheckNetworkStatus.isNetworkAvailable(context)) {
                                    Intent intent = new Intent(context, ReceiptActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("receiptID", receipt.getId());
                                    context.startActivity(intent);
                                }
                                else {
                                    Toast.makeText(context,"No internet connection", Toast.LENGTH_LONG).show();
                                }
                                return true;

                            case R.id.removeReceipt:
                                receiptId = receipt.getId();
                                new unlinkReceiptAsyncTask().execute();

                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popup.show();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mReceiptList.size();
    }

    public FolderReceiptListAdapter(Context context, List<Receipt> receiptList) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mReceiptListFull = new ArrayList<>(receiptList);
        this.mReceiptList = receiptList;
    }

    class ReceiptListItemHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public final TextView receiptDateView;
        public final TextView receiptShopView;
        public final TextView receiptTotalView;
        public final CardView cardView;
        final FolderReceiptListAdapter mAdapter;

        public ReceiptListItemHolder(View itemView, FolderReceiptListAdapter adapter) {
            super(itemView);

            receiptDateView = itemView.findViewById(R.id.receiptDate);
            receiptShopView = itemView.findViewById(R.id.receiptShop);
            receiptTotalView = itemView.findViewById(R.id.receiptTotal);

            cardView = itemView.findViewById(R.id.receiptListCardView);
            itemView.setOnCreateContextMenuListener(this);

            this.mAdapter = adapter;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Select The Action");
            contextMenu.add(0, view.getId(), 0, "Call");//groupId, itemId, order, title
            contextMenu.add(0, view.getId(), 0, "SMS");
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

    private class unlinkReceiptAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("receipt_id", String.valueOf(receiptId));//'1' has to be changed to a user chosen receipt id
            httpParams.put("folder_id", String.valueOf(FolderActivity.folderId));//'1' has to be changed to a user chosen receipt id
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "deleteReceiptFolderRelation.php", "POST", httpParams);
            try {
                success = jsonObject.getInt("success");
                if (success == 0) {
                    message = jsonObject.getString("message");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            if(success == 1){
                mReceiptList.remove(recyclerListId);
                notifyDataSetChanged();
            }
        }
    }
}