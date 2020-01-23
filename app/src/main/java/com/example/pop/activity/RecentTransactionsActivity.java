package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pop.R;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RecentTransactionsActivity extends AppCompatActivity {

    private SQLiteDatabaseAdapter db;
    private RecyclerView mRecyclerView;
    private ReceiptListAdapter mAdapter;

    private Session session;

    private List<Receipt> mReceiptList = new ArrayList<>();

    private NfcAdapter nfcAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_receipt_transaction2);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        db = new SQLiteDatabaseAdapter(this);

        session = new Session(getApplicationContext());

        populateReceipts(session.getUserId());

        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.receiptList);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new ReceiptListAdapter(this, mReceiptList);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void populateReceipts(int id){
        mReceiptList = db.findAllReceiptsForDisplayOnRecentTransaction(id);
    }
}
