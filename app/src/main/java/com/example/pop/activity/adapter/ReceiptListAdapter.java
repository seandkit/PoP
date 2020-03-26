package com.example.pop.activity.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.view.ContextMenu;
import android.view.Gravity;
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

import static android.content.Context.WINDOW_SERVICE;

public class ReceiptListAdapter extends RecyclerView.Adapter<ReceiptListAdapter.ReceiptListItemHolder> implements Filterable {

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
                popup.inflate(R.menu.receipt_list_menu);
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

                            case R.id.addToFolder:
                                FragmentHolder.addToFolder_ReceiptId = receipt.getId();
                                Fragment_Popup_Folders fragment_popup_folders = new Fragment_Popup_Folders();
                                FragmentActivity activity = (FragmentActivity)(context);
                                FragmentManager fm = activity.getSupportFragmentManager();
                                fragment_popup_folders.show(fm,"Add To Folder Popup");

                                return true;

                            case R.id.deleteReceipt:

                                deleteReceiptPopUp(receipt.getId());

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

    public void deleteReceiptPopUp(final int deleteReceiptId) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialoglayout = inflater.inflate(R.layout.delete_receipt_pop_up, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(dialoglayout.getRootView().getContext());

        builder.setTitle("Delete Receipt");
        builder.setView(dialoglayout);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context,"Deleted",Toast.LENGTH_LONG).show();

                //Delete Single receipt
                for(int i = 0; i < mReceiptList.size(); i++){
                    if(mReceiptList.get(i).getId() == deleteReceiptId){
                        recyclerListId = i;
                        receiptId = deleteReceiptId;
                        new deleteReceiptAsyncTask().execute();
                    }
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context,"Cancel",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        //alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return mReceiptList.size();
    }

    public ReceiptListAdapter(Context context, List<Receipt> receiptList) {
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
        final ReceiptListAdapter mAdapter;

        public ReceiptListItemHolder(View itemView, ReceiptListAdapter adapter) {
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

    private class deleteReceiptAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("receipt_id", String.valueOf(receiptId));//'1' has to be changed to a user chosen receipt id
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "deleteReceipt.php", "POST", httpParams);
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