package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecentTransactionsActivity extends AppCompatActivity {

    private SQLiteDatabaseAdapter db;
    private RecyclerView mRecyclerView;
    private ReceiptListAdapter mAdapter;

    private List<Receipt> mReceiptList = new ArrayList<>();

    private RegisterActivity r;
    private LoginActivity l;
    private int userID;

    private ProgressDialog pDialog;
    private int success;
    private String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_recent_transactions);

        new FetchReceiptsAsyncTask().execute();

        //db = new SQLiteDatabaseAdapter(this);
        //populateReceipts();

    }

//    private void populateReceipts(){
//        if(r.getUserID() > -1){
//            userID = r.getUserID();
//        }
//        else{
//            userID = l.getUserID();
//        }
//        mReceiptList = db.findAllReceiptsForDisplayOnRecentTransaction(userID);
//    }

    private class FetchReceiptsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(RecentTransactionsActivity.this);
            pDialog.setMessage("Loading receipts. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DBConstants.USER_ID, String.valueOf(userID));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllReceipts.php", "POST", null);
            try {
                success = jsonObject.getInt("success");
                JSONArray receipts;
                if (success == 1) {
                    mReceiptList = new ArrayList<>();
                    receipts = jsonObject.getJSONArray("data");
                    //Iterate through the response and populate receipt list
                    for (int i = 0; i < receipts.length(); i++) {
                        JSONObject receipt = receipts.getJSONObject(i);
                        int receiptId = receipt.getInt(DBConstants.RECEIPTID);
                        String receiptDate = receipt.getString(DBConstants.DATE);
                        String receiptVendor = receipt.getString(DBConstants.VENDOR);
                        double receiptTotal = receipt.getDouble(DBConstants.RECEIPT_TOTAL);
                        System.out.println(receiptVendor);
                        mReceiptList.add(new Receipt(receiptId,receiptDate,receiptVendor,receiptTotal));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    populateReceiptList();
                    System.out.println("Successful receipts load");
                }
            });
        }

    }
    private void populateReceiptList(){
        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.receiptList);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new ReceiptListAdapter(this, mReceiptList);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
