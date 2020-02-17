package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.material.navigation.NavigationView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FragmentHolder extends AppCompatActivity implements NfcAdapter.ReaderCallback, NavigationView.OnNavigationItemSelectedListener {

    FrameLayout frameLayout;
    BottomNavigationView bottomNavigationView;

    private DrawerLayout drawer;

    private NfcAdapter nfcAdapter = null;
    private SQLiteDatabaseAdapter db;

    //fragments:
    private Fragment_Receipt receiptFragment;
    private Fragment_SearchByDate searchFragment;
    private Fragment_SearchByTag tagFragment;

    private Context context;
    private Session session;
    private String receiptUuidphp = "";

    private int success;
    private String message;
    private int receiptID;

    private String currentDate;
    private String vendor;
    private Double  total;
    private String uuid;

    Receipt newReceipt;

    private String unlinkedReceiptUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_holder);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        context = getApplicationContext();
        session = new Session(context);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        db = new SQLiteDatabaseAdapter(this);

        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
            if(db.getUnlinkedReceipts().size() != 0) {
                List<Receipt> receipts = db.getUnlinkedReceipts();

                System.out.println("SIZE = " + receipts.size());
                System.out.println("FIRST UUID = " + receipts.get(0).getUuid());
                System.out.println("FIRST NAME = " + receipts.get(0).getVendorName());
                System.out.println("FIRST TOTAL = " + receipts.get(0).getReceiptTotal());

                Toast.makeText(FragmentHolder.this,"Found unlinked receipts", Toast.LENGTH_LONG).show();

                for (Receipt r : receipts) {
                    unlinkedReceiptUuid = r.getUuid();
                    receiptUuidphp = receiptUuidphp.concat(r.getUuid() + "@");
                }

                new linkReceiptAsyncTask().execute();
            }
        }

        //Fragment initialization
        receiptFragment = new Fragment_Receipt();
        searchFragment = new Fragment_SearchByDate();
        tagFragment = new Fragment_SearchByTag();

        InitializeFragment(receiptFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.nav_logOut:
                session.setLogin("");
                session.setUserId(0);
                session.setFirstName("");
                session.setLastName("");
                session.setEmail("");

                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

        currentDate = responseArray[0];
        vendor = responseArray[1];
        total = Double.valueOf(responseArray[2]);
        uuid = responseArray[3];

        newReceipt = new Receipt(currentDate, vendor, total, session.getUserId(), uuid);

        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
            receiptUuidphp = receiptUuidphp.concat(newReceipt.getUuid()+"@");
            try {
                String result = new linkReceiptAsyncTask().execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{
            db.addUnlinkedReceipt(newReceipt);
        }

        runOnUiThread(new Runnable()
        {
            public void run()
            {
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

                if (success == 1) {
                    receiptID = jsonObject.getInt("receipt_id");
                    receiptUuidphp = "";
                    newReceipt = new Receipt(receiptID, currentDate, vendor, total, session.getUserId());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            boolean answer = db.dropUnlinkedReceipt(unlinkedReceiptUuid);
        }
    }

    private class addFolderAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("folder_name", "PoP-Placeholder");//'PoP-Placeholder' needs to be changed to some user inputted variable
            httpParams.put("user_id", String.valueOf(session.getUserId()));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "addFolder.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");

                if (success == 1) {
                    int folderid = jsonObject.getInt("data");//returned folder id to attach to some folder object
                }
                else{
                    message = jsonObject.getString("message");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            if (success == 0) {
                Toast.makeText(FragmentHolder.this, message, Toast.LENGTH_LONG).show();
            }
            else{
                //update folder display list with new folder object
            }
        }
    }

    private class addReceiptToFolderAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("receipt_id", "1");//'1' needs to be changed to some user chosen receipt id
            httpParams.put("folder_id", "1");//'1' needs to be changed to some user chosen folder id
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "addReceiptToFolder.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");
                message = jsonObject.getString("message");
                //Can choose to set values in success '1'- means added successfully, '0'- is otherwise
                if (success == 1) {

                }
                else{

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            Toast.makeText(FragmentHolder.this, message, Toast.LENGTH_LONG).show();
            //Can choose execute something in success '1'- means added successfully, '0'- is otherwise
            if (success == 0) {

            }
            else{

            }
        }
    }
}
