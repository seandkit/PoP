package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.pop.R;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.handler.SQLiteDatabaseHandler;

import java.util.LinkedList;

public class RecentTransactionsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ReceiptListAdapter mAdapter;

    private LinkedList<String[]> mReceiptList = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_recent_transactions);

        populateReceipts();

        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.receiptList);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new ReceiptListAdapter(this, mReceiptList);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void populateReceipts(){
        SQLiteDatabaseHandler db = new SQLiteDatabaseHandler(this);
        mReceiptList = db.findAllReceiptsForDisplayOnRecentTransaction();
    }
}
