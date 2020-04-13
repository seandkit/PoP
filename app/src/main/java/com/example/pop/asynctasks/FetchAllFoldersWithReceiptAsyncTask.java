package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.pop.DBConstants;
import com.example.pop.activity.ReceiptActivity;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.model.Folder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FetchAllFoldersWithReceiptAsyncTask extends AsyncTask<String, String, String> {

    private Activity mAcc;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private int mReceiptId;

    public FetchAllFoldersWithReceiptAsyncTask(Activity acc, Context context, int receiptId){
        mAcc = acc;
        mContext = context;
        mReceiptId = receiptId;
    }

    private Session session;
    private ProgressDialog pDialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        session = new Session(mContext);

        pDialog = new ProgressDialog(mAcc);
        pDialog.setMessage("Getting Related Folders. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("receipt_id", String.valueOf(mReceiptId));
        httpParams.put("user_id", String.valueOf(session.getUserId()));
        JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllFoldersWithReceipt.php", "POST", httpParams);

        try {
            int success = jsonObject.getInt("success");
            JSONArray folders;
            if (success == 1) {
                folders = jsonObject.getJSONArray("data");
                ReceiptActivity.receiptFoldersList = new ArrayList<>();
                for (int i = 0; i < folders.length(); i++) {
                    JSONObject folder = folders.getJSONObject(i);
                    ReceiptActivity.receiptFoldersList.add(new Folder(folder.getInt("folder_id"), folder.getString("folder_name")));
                }
            }
            else{
                String message = jsonObject.getString("message");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {
        pDialog.dismiss();
    }
}
