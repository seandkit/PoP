package com.example.pop.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.FragmentHolder;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Folder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.FolderListItemHolder> {

    private List<Folder> mFolderList;
    private LayoutInflater mInflater;

    private Context context;

    private int receiptId;
    private int folderId;

    private int success;
    private String message;

    private OnItemClicked onClick;

    public interface OnItemClicked {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public FolderListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.layout_listener_add_to_folder, parent, false);
        return new FolderListItemHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(final FolderListItemHolder holder, final int position) {
        final Folder folder = mFolderList.get(position);
        holder.folderNameView.setText(folder.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiptId = FragmentHolder.addToFolder_ReceiptId;
                folderId = folder.getId();

                new addReceiptToFolderAsyncTask().execute();

                onClick.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFolderList.size();
    }

    public void setOnClick(OnItemClicked onClick)
    {
        this.onClick=onClick;
    }

    public FolderListAdapter(Context context, List<Folder> folderList) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.mFolderList = folderList;
    }

    static class FolderListItemHolder extends RecyclerView.ViewHolder {
        final TextView folderNameView;
        final FolderListAdapter mAdapter;

        FolderListItemHolder(View itemView, FolderListAdapter adapter) {
            super(itemView);

            folderNameView = itemView.findViewById(R.id.popupFolderName);
            this.mAdapter = adapter;
        }
    }

    private class addReceiptToFolderAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("receipt_id", String.valueOf(receiptId));
            httpParams.put("folder_id", String.valueOf(folderId));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "addReceiptToFolder.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");
                message = jsonObject.getString("message");
            } catch (JSONException e) {
                Toast.makeText(context,"FolderListAdapter: " + message, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            if(success == 1){
                Toast.makeText(context,"Added to folder", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(context,"Already in folder", Toast.LENGTH_LONG).show();
            }
        }
    }
}