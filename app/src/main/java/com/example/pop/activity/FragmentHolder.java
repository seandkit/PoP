package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.adapter.FolderListAdapter;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
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
import java.util.concurrent.Executor;

public class FragmentHolder extends AppCompatActivity implements NfcAdapter.ReaderCallback, NavigationView.OnNavigationItemSelectedListener {

    FrameLayout frameLayout;
    BottomNavigationView bottomNavigationView;
    NavigationView navigationView;

    private DrawerLayout drawer;

    public static List<Folder> folderList = new ArrayList<>();
    private String newFolderName;
    private int newFolderId;

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

    public static int addToFolder_ReceiptId;

    private String currentDate;
    private String vendor;
    private Double  total;
    private String uuid;
    Receipt newReceipt;
    private String unlinkedReceiptUuid;

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

        setUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent intent = new Intent(this, BlurActivity.class);
        startActivity(intent);

        Fragment_Receipt.mAdapter = new ReceiptListAdapter(this, FragmentHolder.mReceiptList);
        Fragment_Receipt.mRecyclerView.setAdapter(Fragment_Receipt.mAdapter);
        Fragment_Receipt.mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    public void setUp(){
        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
            try {
                String wait1 = new fetchFoldersAsyncTask().execute().get();
                String wait2 = new fetchReceiptsAsyncTask().execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(db.getUnlinkedReceipts().size() != 0) {
                List<Receipt> receipts = db.getUnlinkedReceipts();

                Toast.makeText(FragmentHolder.this,"Found unlinked receipts", Toast.LENGTH_LONG).show();

                for (Receipt r : receipts) {
                    unlinkedReceiptUuid = r.getUuid();
                    receiptUuidphp = receiptUuidphp.concat(r.getUuid() + "@");
                }

                new linkReceiptAsyncTask().execute();
            }
        }
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
                break;

            case R.id.nav_folder_add_new:
                newFolderPopUp();
                break;
        }

        return true;
    }

    public void newFolderPopUp() {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.new_folder_pop_up, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Create New Folder");
        builder.setView(dialoglayout);

        final EditText userInput = (EditText) dialoglayout.findViewById(R.id.newFolderInput);


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
                    new addFolderAsyncTask().execute();
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

    public boolean addNewItem(int itemId, String itemName){
        MenuItem myMoveGroupItem = navigationView.getMenu().getItem(1);
        SubMenu subMenu = myMoveGroupItem.getSubMenu();

        if(session.getCurrentFolder().equalsIgnoreCase(String.valueOf(itemId))){
            subMenu.add(Menu.NONE, itemId, Menu.NONE, itemName).setIcon(R.drawable.baseline_folder_open_24).setOnMenuItemClickListener(folderOnClickListener);
        }
        else{
            subMenu.add(Menu.NONE, itemId, Menu.NONE, itemName).setIcon(R.drawable.ic_folder_black_24dp).setOnMenuItemClickListener(folderOnClickListener);
        }

        return true;
    }

    private MenuItem.OnMenuItemClickListener folderOnClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            drawer.closeDrawer(GravityCompat.START);

            Intent intent = new Intent(context, FolderActivity.class);

            for(Folder f : folderList){
                if(f.getName().equalsIgnoreCase(String.valueOf(menuItem.getTitle()))){
                    intent.putExtra("folderId", f.getId());
                    intent.putExtra("folderName", f.getName());
                }
            }

            startActivity(intent);

            return false;
        }
    };

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

        mReceiptList.add(newReceipt);

        try {
            isoDep.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            httpParams.put("folder_id", String.valueOf(session.getCurrentFolder()));
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
            httpParams.put("folder_name", newFolderName);
            httpParams.put("user_id", String.valueOf(session.getUserId()));

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "addFolder.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");
                if (success == 1) {
                    newFolderId = jsonObject.getInt("data");
                    folderList.add(new Folder(newFolderId, newFolderName));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            addNewItem(newFolderId, newFolderName);
            Toast.makeText(FragmentHolder.this,"Folder created",Toast.LENGTH_LONG).show();
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

    //This delete method calls a php function which deletes all ReceiptFolder and Folder objects with the same given folder id chosen by the user
    private class deleteFolderAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("folder_id", String.valueOf(newFolderId));//'1' needs to be changed to some user chosen folder id
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "deleteFolder.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");
                //Can choose to set values in success '1'- means added successfully, '0'- is otherwise
                //success 1 means deleted in this case
                if (success == 1) {
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
            //Can choose execute something in success '1'- means added successfully, '0'- is otherwise
            if (success == 0) {
                Toast.makeText(FragmentHolder.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class fetchFoldersAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DBConstants.USER_ID, String.valueOf(session.getUserId()));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllFolders.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");
                JSONArray folders;
                if (success == 1) {
                    folders = jsonObject.getJSONArray("data");
                    //Iterate through the response and populate receipt list
                    folderList = new ArrayList<>();
                    for (int i = 0; i < folders.length(); i++) {
                        JSONObject folder = folders.getJSONObject(i);
                        folderList.add(new Folder(folder.getInt("folder_id"), folder.getString("folder_name")));
                    }
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
                //If success update xml
                MenuItem myMoveGroupItem = navigationView.getMenu().getItem(1);
                SubMenu subMenu = myMoveGroupItem.getSubMenu();
                subMenu.clear();
                for(Folder folder: folderList){
                    addNewItem(folder.getId(), folder.getName());
                }
            }
        }
    }

    private class fetchReceiptsAsyncTask extends AsyncTask<String, String, String> {
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

    @Override
    protected void onResume() {
        super.onResume();

        try {
            String str_result = new fetchFoldersAsyncTask().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
