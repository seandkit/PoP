package com.example.pop.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

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

public class Fragment_SearchByTag extends Fragment{



    private RecyclerView mRecyclerView;
    private ReceiptListAdapter mAdapter;
    private SearchView searchView;

    private Context context;
    private Session session;
    private int success;

    public List<Receipt> mReceiptList = new ArrayList<>();
    public List<Receipt> mReceiptListTemp = new ArrayList<>();

    private String tag = "";

    public Fragment_SearchByTag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search_tag, container, false);
        //Toolbar toolbar = v.findViewById(R.id.toolbar);
        //((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        context = getActivity().getApplicationContext();
        session = new Session(context);

        setHasOptionsMenu(true);

        if (CheckNetworkStatus.isNetworkAvailable(context)) {
            new Fragment_SearchByTag.FetchReceiptsAsyncTask().execute();
        }

        // Get a handle to the RecyclerView.
        mRecyclerView = v.findViewById(R.id.receiptList);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new ReceiptListAdapter(context, mReceiptList);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        searchView = v.findViewById(R.id.tagInput);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterReceipts(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return v;
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

    //Search Specific classes
    private void filterReceipts(String s){
        boolean vendorTag = false;
        for(Receipt r: mReceiptList){
            if(s.equalsIgnoreCase(r.getVendorName())){
                vendorTag = true;

                //==========================
                //Do something
                //==========================
            }
        }
        if(!vendorTag){
            tag = tag.concat(s+"@");
            new FetchFilteredReceiptsAsyncTask().execute();
        }
    }

    private class FetchFilteredReceiptsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DBConstants.USER_ID, String.valueOf(session.getUserId()));
            httpParams.put("tags", tag);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "getReceiptsByItems.php", "POST", httpParams);


            try {
                success = jsonObject.getInt("success");
                JSONArray receipts;
                if (success == 1) {
                    mReceiptListTemp = new ArrayList<>();
                    receipts = jsonObject.getJSONArray("data");

                    //Iterate through the response and populate receipt list
                    for (int i = 0; i < receipts.length(); i++) {
                        JSONObject receipt = receipts.getJSONObject(i);
                        int receiptId = receipt.getInt(DBConstants.RECEIPT_ID);
                        String receiptDate = receipt.getString(DBConstants.DATE);
                        String receiptVendor = receipt.getString(DBConstants.VENDOR);
                        double receiptTotal = receipt.getDouble(DBConstants.RECEIPT_TOTAL);

                        mReceiptListTemp.add(new Receipt(receiptId,receiptDate,receiptVendor,receiptTotal, session.getUserId()));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            mAdapter = new ReceiptListAdapter(context, mReceiptListTemp);
            // Connect the adapter with the RecyclerView.
            mRecyclerView.setAdapter(mAdapter);
            // Give the RecyclerView a default layout manager.
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }
}