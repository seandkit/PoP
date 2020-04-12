package com.example.pop.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.example.pop.DBConstants;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LinkReceiptAsyncTask extends AsyncTask<String, String, Integer> {

    private Context mContext;
    private String mReceiptUuidphp;

    public LinkReceiptAsyncTask(Context context, String receiptUuidphp){
        mContext = context;
        mReceiptUuidphp = receiptUuidphp;
    }

    private Session session;
    private SQLiteDatabaseAdapter db;
    private int success;
    private int newID;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        session = new Session(mContext);
        db = new SQLiteDatabaseAdapter(mContext);
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("uuid", mReceiptUuidphp + "@");
        httpParams.put("user_id", String.valueOf(session.getUserId()));
        httpParams.put("folder_id", String.valueOf(session.getCurrentFolder()));
        JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "uuidNULL.php", "POST", httpParams);

        try {
            success = jsonObject.getInt("success");
            if (success == 1) {
                newID = jsonObject.getInt("receipt_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newID;
    }

    protected void onPostExecute(Integer result) {
        String[] uuids = mReceiptUuidphp.split("@");

        for(String uuid : uuids) {
            db.dropUnlinkedReceipt(uuid);
        }
    }
}
