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

public class RecentTransactionsActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private SQLiteDatabaseAdapter db;
    private RecyclerView mRecyclerView;
    private ReceiptListAdapter mAdapter;

    private List<Receipt> mReceiptList = new ArrayList<>();

    private NfcAdapter nfcAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_recent_transactions);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        db = new SQLiteDatabaseAdapter(this);
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
        mReceiptList = db.findAllReceiptsForDisplayOnRecentTransaction(1);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcAdapter.disableReaderMode(this);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] response = new byte[0];
        try {
            response = isoDep.transceive(Utils.hexStringToByteArray("00A4040007A0000002471001"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String stringResponse = "";

        try {
            stringResponse = new String(response, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String finalStringResponse = stringResponse;

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                TextView v = findViewById(R.id.pageTitle);
                v.append("\nCard Response: " + finalStringResponse);
            }
        });
        System.out.println("\nCard Response: " + Utils.toHex(response));

        try {
            isoDep.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
