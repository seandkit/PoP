package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
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

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private int mReceiptId;

    public FetchAllFoldersWithReceiptAsyncTask(Context context, int receiptId){
        mContext = context;
        mReceiptId = receiptId;
    }

    private Session session;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        session = new Session(mContext);
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

    protected void onPostExecute(String result) {}
}
