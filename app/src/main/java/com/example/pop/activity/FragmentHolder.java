package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pop.R;
import com.example.pop.activity.popup.Popup_Blur;
import com.example.pop.adapter.ReceiptListAdapter;
import com.example.pop.asynctasks.AddFolderAsyncTask;
import com.example.pop.asynctasks.FetchFoldersAsyncTask;
import com.example.pop.asynctasks.FetchReceiptsAsyncTask;
import com.example.pop.asynctasks.LinkReceiptAsyncTask;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.Session;
import com.example.pop.helper.Utils;
import com.example.pop.model.Folder;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FragmentHolder extends AppCompatActivity implements NfcAdapter.ReaderCallback, NavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;

    public static NavigationView navigationView;
    public static DrawerLayout drawer;

    public static List<Folder> folderList = new ArrayList<>();

    private NfcAdapter nfcAdapter = null;
    private SQLiteDatabaseAdapter db;

    //Initialize fragments
    private Fragment_Receipt receiptFragment;
    private Fragment_SearchByDate searchFragment;
    private Fragment_SearchByTag tagFragment;

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private Session session;

    public static int addToFolder_ReceiptId;

    public static List<Receipt> mReceiptList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_holder);

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

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
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
    protected void onPostResume() {
        super.onPostResume();
        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
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
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableReaderMode(this);
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
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                FetchFoldersAsyncTask fetchFoldersAsyncTask = new FetchFoldersAsyncTask(navigationView, context);
                String wait = fetchFoldersAsyncTask.execute().get();
                FetchReceiptsAsyncTask fetchReceiptsAsyncTask = new FetchReceiptsAsyncTask(context);
                String wait2 = fetchReceiptsAsyncTask.execute().get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateDrawerInfo(){
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
                Utils.newFolderPopUp(navigationView, FragmentHolder.this);
                break;

            case R.id.nav_tutorial:
                Intent tutorial = new Intent(this, Tutorial.class);
                startActivity(tutorial);
        }

        return true;
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

        stringResponse = new String(response, StandardCharsets.UTF_8);

        final String finalStringResponse = stringResponse;

        String[] responseArray = finalStringResponse.split(",");

        String currentDate = responseArray[0];
        String vendor = responseArray[1];
        double total = Double.parseDouble(responseArray[2]);
        String uuid = responseArray[3];

        Receipt newReceipt = new Receipt(currentDate, vendor, total, session.getUserId(), uuid);

        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
            try {
                LinkReceiptAsyncTask linkReceiptAsyncTask = new LinkReceiptAsyncTask(context, newReceipt.getUuid());
                int newReceiptID = linkReceiptAsyncTask.execute().get();

                newReceipt = new Receipt(newReceiptID, currentDate, vendor, total, session.getUserId());
                Utils.showNotification(context,"NFC_Channel", "Receipt Received", "Tap to view", newReceipt.getId());
            } catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
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
}