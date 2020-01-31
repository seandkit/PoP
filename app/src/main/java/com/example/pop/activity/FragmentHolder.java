package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentHolder extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    FrameLayout frameLayout;
    BottomNavigationView navigation;

    private NfcAdapter nfcAdapter = null;
    private SQLiteDatabaseAdapter db;

    //fragments:
    private Fragment_Receipt receiptFragment;
    private Fragment_SearchByDate searchFragment;
    private Fragment_SearchByTag tagFragment;

    private Context context;
    private Session session;
    private String receiptUuidphp;

    int success;

    Receipt newReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_holder);
        navigation = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        context = getApplicationContext();
        session = new Session(context);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        db = new SQLiteDatabaseAdapter(this);

        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
            if(db.getUnlinkedReceipts() != null) {
                List<Receipt> receipts = db.getUnlinkedReceipts();
                for(Receipt r: receipts){
                    receiptUuidphp = receiptUuidphp.concat(r.getUuid()+"@");
                }
                new linkReceiptAsyncTask().execute();
            }
        }

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
            response = isoDep.transceive(Utils.hexStringToByteArray("00A4040007A0000002471002"));
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
        String uuid = responseArray[3];

        newReceipt = new Receipt(currentDate, vendor, total, session.getUserId(), uuid);

        db.addUnlinkedReceipt(uuid, vendor,currentDate, String.valueOf(total));

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
        receiptFragment.addItemToList(receipt);
    }

    private class linkReceiptAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("uuid", receiptUuidphp);
            httpParams.put("user_id", String.valueOf(session.getUserId()));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "uuidNULL.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");
                JSONArray data;
                if (success == 1) {
                    Toast.makeText(FragmentHolder.this,
                            "User Linked",
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            //?? populate xml with receipt and itemList??


        }
    }
}
