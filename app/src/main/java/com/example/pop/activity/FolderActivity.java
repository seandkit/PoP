package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Receipt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    Context context;
    Session session;

    private RecyclerView mRecyclerView;
    private ReceiptListAdapter mAdapter;
    public List<Receipt> mReceiptList = new ArrayList<>();

    private String folderName;

    int success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        Intent intent = getIntent();
        folderName = intent.getStringExtra("folderName");


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(folderName);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        context = getApplicationContext();
        session = new Session(context);

        if (CheckNetworkStatus.isNetworkAvailable(context)) {
            new FetchReceiptsAsyncTask().execute();
        }

        //Move below code block into populateReceiptList()
        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.receiptList);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new ReceiptListAdapter(context, mReceiptList);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    private class FetchReceiptsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DBConstants.USER_ID, String.valueOf(session.getUserId()));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllReceipts.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");
                JSONArray receipts;
                if (success == 1) {
                    mReceiptList = new ArrayList<>();
                    receipts = jsonObject.getJSONArray("data");
                    //Iterate through the response and populate receipt list
                    for (int i = 0; i < receipts.length(); i++) {
                        JSONObject receipt = receipts.getJSONObject(i);
                        int receiptId = receipt.getInt(DBConstants.RECEIPT_ID);
                        String receiptDate = receipt.getString(DBConstants.DATE);
                        String receiptVendor = receipt.getString(DBConstants.VENDOR);
                        double receiptTotal = receipt.getDouble(DBConstants.RECEIPT_TOTAL);

                        mReceiptList.add(new Receipt(receiptId,receiptDate,receiptVendor,receiptTotal, session.getUserId()));
                    }
                    //Collections.sort(mReceiptList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            mAdapter = new ReceiptListAdapter(context, mReceiptList);
            // Connect the adapter with the RecyclerView.
            mRecyclerView.setAdapter(mAdapter);
            // Give the RecyclerView a default layout manager.
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }
}
