package com.example.pop.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
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
public class Fragment_Receipt extends Fragment {

    private SQLiteDatabaseAdapter db;
    private RecyclerView mRecyclerView;
    private ReceiptListAdapter mAdapter;

    public List<Receipt> mReceiptList = new ArrayList<>();

    public Fragment_Receipt() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_receipt, container, false);

        Context context = v.getContext();
        db = new SQLiteDatabaseAdapter(context);

        mReceiptList = db.findAllReceiptsForDisplayOnRecentTransaction(1);

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

    void addItemToList(Context context, Receipt receipt){
        mReceiptList.add(receipt);
        mAdapter.notifyItemInserted(mReceiptList.size() - 1);
    }
}
