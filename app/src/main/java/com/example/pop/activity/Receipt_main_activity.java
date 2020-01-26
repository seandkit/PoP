package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.pop.R;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Receipt_main_activity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    FrameLayout frameLayout;
    BottomNavigationView navigation;

    private NfcAdapter nfcAdapter = null;
    private SQLiteDatabaseAdapter db;

    //fragments:
    private Fragment_Receipt receiptFragment;
    private Fragment_SearchByDate searchFragment;
    private Fragment_SearchByTag tagFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_main_activity);
        navigation = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        db = new SQLiteDatabaseAdapter(this);

        //Fragment initialization
        receiptFragment = new Fragment_Receipt();
        searchFragment = new Fragment_SearchByDate();
        tagFragment = new Fragment_SearchByTag();

        InitializeFragment(receiptFragment);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //switch to fragment

                switch(menuItem.getItemId()){
                    case R.id.nav_home :
                        //Code to be executed when item 1 selected.
                        InitializeFragment(receiptFragment);
                        return true;

                    case R.id.nav_search :
                        //Code to be executed when item 1 selected.
                        InitializeFragment(searchFragment);
                        return true;

                    case R.id.nav_data :
                        //Code to be executed when item 1 selected.
                        InitializeFragment(tagFragment);
                        return true;
                }
                return false;
            }
        });
    }

    private void InitializeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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

        String[] responseArray = finalStringResponse.split(",");

        String currentDate = responseArray[0];
        String vendor = responseArray[1];
        Double  total = Double.valueOf(responseArray[2]);

        final Receipt newReceipt = new Receipt(currentDate, vendor, total, 1);

        //======================================================================================================
        //This is the receipt (newReceipt) that will contain a uuid that needs to be searched for in the cloud
        //======================================================================================================

        runOnUiThread(new Runnable()
        {
            public void run() {
                addItem(newReceipt);
            }
        });

        try {
            isoDep.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addItem(Receipt receipt){
        receiptFragment.addItemToList(this, receipt);
    }
}
