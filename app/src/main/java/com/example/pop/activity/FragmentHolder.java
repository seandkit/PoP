package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.popup.Popup_Blur;
import com.example.pop.adapter.ReceiptListAdapter;
import com.example.pop.asynctasks.AddFolderAsyncTask;
import com.example.pop.asynctasks.FetchFoldersAsyncTask;
import com.example.pop.asynctasks.LinkReceiptAsyncTask;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.helper.Utils;
import com.example.pop.model.Folder;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FragmentHolder extends AppCompatActivity implements NfcAdapter.ReaderCallback, NavigationView.OnNavigationItemSelectedListener {

    FrameLayout frameLayout;
    BottomNavigationView bottomNavigationView;

    public static NavigationView navigationView;

    public static DrawerLayout drawer;

    public static List<Folder> folderList = new ArrayList<>();
    private String newFolderName;
    private int newFolderId;

    private NfcAdapter nfcAdapter = null;
    private SQLiteDatabaseAdapter db;

    //fragments:
    private Fragment_Receipt receiptFragment;
    private Fragment_SearchByDate searchFragment;
    private Fragment_SearchByTag tagFragment;

    private static Context context;
    private Session session;

    public static int addToFolder_ReceiptId;

    Receipt newReceipt;

    public static List<Receipt> mReceiptList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_holder);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        context = getApplicationContext();
        session = new Session(context);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        db = new SQLiteDatabaseAdapter(this);

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        populateDrawerInfo();

        receiptFragment = new Fragment_Receipt();
        searchFragment = new Fragment_SearchByDate();
        tagFragment = new Fragment_SearchByTag();

        InitializeFragment(receiptFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_home :
                        InitializeFragment(receiptFragment);
                        return true;

                    case R.id.nav_search :
                        InitializeFragment(searchFragment);
                        return true;

                    case R.id.nav_data :
                        InitializeFragment(tagFragment);
                        return true;
                }
                return false;
            }
        });
        setUp();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setUp();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            final Intent intent = new Intent(this, Popup_Blur.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

        updateReceiptListUI();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void setUp(){
        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
            if(db.getUnlinkedReceipts().size() != 0) {
                Toast.makeText(FragmentHolder.this,"Found unlinked receipts", Toast.LENGTH_LONG).show();

                List<Receipt> receipts = db.getUnlinkedReceipts();

                String unlinkedReceiptUUIDPhp = "";
                for (Receipt r : receipts) {
                    unlinkedReceiptUUIDPhp = unlinkedReceiptUUIDPhp +  r.getUuid() + "@";
                }

                try {
                    LinkReceiptAsyncTask linkReceiptAsyncTask = new LinkReceiptAsyncTask(context, unlinkedReceiptUUIDPhp);
                    int newID = linkReceiptAsyncTask.execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                FetchFoldersAsyncTask fetchFoldersAsyncTask = new FetchFoldersAsyncTask(navigationView, context);
                String wait = fetchFoldersAsyncTask.execute().get();
                String wait2 = new fetchReceiptsAsyncTask().execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void populateDrawerInfo(){
        View headerView = navigationView.getHeaderView(0);
        TextView drawerUsername = headerView.findViewById(R.id.drawer_username);
        String drawerUsernameValue = session.getFirstName() + " " + session.getLastName();
        drawerUsername.setText(drawerUsernameValue.toUpperCase());
        TextView drawerEmail = headerView.findViewById(R.id.drawer_email);
        drawerEmail.setText(session.getEmail());
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

                drawer.closeDrawer(GravityCompat.START);

                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                folderList = new ArrayList<>();
                mReceiptList = new ArrayList<>();
                finish();
                break;

            case R.id.nav_folder_add_new:
                newFolderPopUp();
                break;

            case R.id.nav_tutorial:
                Intent tutorial = new Intent(this, Tutorial.class);
                startActivity(tutorial);
        }

        return true;
    }

    public void newFolderPopUp() {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.new_folder_pop_up, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Create New Folder");
        builder.setView(dialoglayout);

        final EditText userInput = dialoglayout.findViewById(R.id.newFolderInput);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newFolderName = userInput.getText().toString();
                boolean exists = false;
                for(Folder f : folderList){
                    if (f.getName().equalsIgnoreCase(newFolderName)) {
                        exists = true;
                    }
                }
                if(!exists){
                    AddFolderAsyncTask addFolderAsyncTask = new AddFolderAsyncTask(navigationView, context, newFolderName);
                    addFolderAsyncTask.execute();
                }
                else {
                    Toast.makeText(FragmentHolder.this,"Folder already exists",Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static MenuItem.OnMenuItemClickListener drawerFolderClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            drawer.closeDrawer(GravityCompat.START);

            Intent intent = new Intent(context, FolderActivity.class);

            for(Folder f : folderList){
                if(f.getName().equalsIgnoreCase(String.valueOf(menuItem.getTitle()))){
                    intent.putExtra("folderId", f.getId());
                    intent.putExtra("folderName", f.getName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
            }

            context.startActivity(intent);

            return false;
        }
    };

    private void InitializeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
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
        } catch (IOException e) { e.printStackTrace(); }

        byte[] response = new byte[0];
        try {
            response = isoDep.transceive(Utils.hexStringToByteArray("00A4040007A0000002471002"));
        } catch (IOException e) { e.printStackTrace(); }

        String stringResponse = "";

        try {
            stringResponse = new String(response, "UTF-8");
        } catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        final String finalStringResponse = stringResponse;

        String[] responseArray = finalStringResponse.split(",");

        String currentDate = responseArray[0];
        String vendor = responseArray[1];
        double total = Double.valueOf(responseArray[2]);
        String uuid = responseArray[3];

        newReceipt = new Receipt(currentDate, vendor, total, session.getUserId(), uuid);

        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
            try {
                LinkReceiptAsyncTask linkReceiptAsyncTask = new LinkReceiptAsyncTask(context, newReceipt.getUuid());
                int newReceiptID = linkReceiptAsyncTask.execute().get();

                newReceipt = new Receipt(newReceiptID, currentDate, vendor, total, session.getUserId());
                Utils.showNotification(context,"NFC_Channel", "Receipt Received", "Tap to view", newReceipt.getId());
            } catch (ExecutionException e) { e.printStackTrace(); }
              catch (InterruptedException e) { e.printStackTrace(); }
        }
        else{
            db.addUnlinkedReceipt(newReceipt);
            Utils.showNotification(context,"NFC_Channel", "Receipt Received", "Connect to the internet to view", -1);
        }

        mReceiptList.add(newReceipt);

        try {
            isoDep.close();
        } catch (IOException e) { e.printStackTrace(); }

        updateReceiptListUI();
    }

    private void updateReceiptListUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment_Receipt.mAdapter = new ReceiptListAdapter(FragmentHolder.this, FragmentHolder.mReceiptList);
                Fragment_Receipt.mRecyclerView.setAdapter(Fragment_Receipt.mAdapter);
                Fragment_Receipt.mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
        });
    }

    private class fetchReceiptsAsyncTask extends AsyncTask<String, String, String> {

        int success;
        String message;

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

        protected void onPostExecute(String result) {}
    }
}
