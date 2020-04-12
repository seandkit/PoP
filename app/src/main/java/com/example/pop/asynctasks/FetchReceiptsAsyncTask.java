package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.example.pop.DBConstants;
import com.example.pop.activity.FragmentHolder;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.model.Receipt;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FetchReceiptsAsyncTask extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    public FetchReceiptsAsyncTask(Context context){
        mContext = context;
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
        httpParams.put(DBConstants.USER_ID, String.valueOf(session.getUserId()));
        JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllReceipts.php", "POST", httpParams);

        try {
            int success = jsonObject.getInt("success");
            JSONArray receipts;
            if (success == 1) {
                FragmentHolder.mReceiptList = new ArrayList<>();
                receipts = jsonObject.getJSONArray("data");

                for (int i = 0; i < receipts.length(); i++) {
                    JSONObject receipt = receipts.getJSONObject(i);
                    int receiptId = receipt.getInt(DBConstants.RECEIPT_ID);
                    String receiptDate = receipt.getString(DBConstants.DATE);
                    String receiptVendor = receipt.getString(DBConstants.VENDOR);
                    double receiptTotal = receipt.getDouble(DBConstants.RECEIPT_TOTAL);

                    FragmentHolder.mReceiptList.add(new Receipt(receiptId,receiptDate,receiptVendor,receiptTotal, session.getUserId()));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {}
}
