package com.example.pop.activity;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pop.R;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReceiptFragment extends Fragment {

    private SQLiteDatabaseAdapter db;
    private RecyclerView mRecyclerView;
    private ReceiptListAdapter mAdapter;

    private List<Receipt> mReceiptList = new ArrayList<>();

    public ReceiptFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_receipt, container, false);

        Context context = v.getContext();
        db = new SQLiteDatabaseAdapter(context);
        populateReceipts();

        // Get a handle to the RecyclerView.
        mRecyclerView = v.findViewById(R.id.receiptList);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new ReceiptListAdapter(context, mReceiptList);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        return v;
    }

    private void populateReceipts(){
        mReceiptList = db.findAllReceiptsForDisplayOnRecentTransaction(1);
    }
}
