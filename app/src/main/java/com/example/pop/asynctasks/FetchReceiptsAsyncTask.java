package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.FragmentHolder;
import com.example.pop.activity.Fragment_Receipt;
import com.example.pop.adapter.ReceiptListAdapter;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.model.Receipt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FetchReceiptsAsyncTask extends AsyncTask<String, String, String> {

    private Activity mAcc;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    public FetchReceiptsAsyncTask(Activity acc, Context context){
        mAcc = acc;
        mContext = context;
    }

    private Session session;
    private ProgressDialog pDialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        session = new Session(mContext);

        pDialog = new ProgressDialog(mAcc);
        pDialog.setMessage("Getting Receipts. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
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

    protected void onPostExecute(String result) {
        if(FragmentHolder.mReceiptList.size() == 0){
            ImageView iv = mAcc.findViewById(R.id.emptyListImg);
            iv.setVisibility(View.VISIBLE);
        } else {
            mAcc.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Fragment_Receipt.mAdapter = new ReceiptListAdapter(mContext, FragmentHolder.mReceiptList);
                    Fragment_Receipt.mRecyclerView.setAdapter(Fragment_Receipt.mAdapter);
                    Fragment_Receipt.mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                }
            });
        }

        pDialog.dismiss();
    }
}
