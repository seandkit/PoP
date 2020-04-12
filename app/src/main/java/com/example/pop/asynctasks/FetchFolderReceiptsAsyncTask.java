package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pop.DBConstants;
import com.example.pop.activity.FolderActivity;
import com.example.pop.adapter.FolderReceiptListAdapter;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.model.Receipt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FetchFolderReceiptsAsyncTask extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private int mFolderId;

    public FetchFolderReceiptsAsyncTask(Context context, int folderId){
        mContext = context;
        mFolderId = folderId;
    }

    Session session;

    int success;
    String message;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        session = new Session(mContext);
    }

    @Override
    protected String doInBackground(String... params) {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("folder_id", String.valueOf(mFolderId));
        JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllReceiptsFromFolder.php", "POST", httpParams);

        try {
            success = jsonObject.getInt("success");
            JSONArray receipts;
            if (success == 1) {
                FolderActivity.folderReceiptList = new ArrayList<>();
                receipts = jsonObject.getJSONArray("data");

                for (int i = 0; i < receipts.length(); i++) {
                    JSONObject receipt = receipts.getJSONObject(i);
                    int receiptId = receipt.getInt(DBConstants.RECEIPT_ID);
                    String receiptDate = receipt.getString(DBConstants.DATE);
                    String receiptVendor = receipt.getString(DBConstants.VENDOR);
                    double receiptTotal = receipt.getDouble(DBConstants.RECEIPT_TOTAL);

                    FolderActivity.folderReceiptList.add(new Receipt(receiptId,receiptDate,receiptVendor,receiptTotal, session.getUserId()));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {
        FolderActivity.mAdapter = new FolderReceiptListAdapter(mContext, FolderActivity.folderReceiptList);
        FolderActivity.mRecyclerView.setAdapter(FolderActivity.mAdapter);
        FolderActivity.mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }
}