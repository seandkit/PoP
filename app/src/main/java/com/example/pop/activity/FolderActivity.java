package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.accounts.Account;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.adapter.FolderReceiptListAdapter;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Folder;
import com.example.pop.model.Receipt;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FolderActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    Context context;
    Session session;

    boolean firstTime = false;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    public static List<Folder> folderList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private FolderReceiptListAdapter mAdapter;
    public List<Receipt> mReceiptList = new ArrayList<>();

    public static int folderId;
    public static String folderName;

    private int success;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        Intent intent = getIntent();
        folderId = intent.getIntExtra("folderId", 0);
        folderName = intent.getStringExtra("folderName");

        Toolbar toolbar = findViewById(R.id.folder_toolbar);
        toolbar.setTitle(folderName);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.folder_drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        context = getApplicationContext();
        session = new Session(context);

        navigationView = findViewById(R.id.navigation_view);

        try {
            String str_result = new FolderActivity.fetchFoldersAsyncTask().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (CheckNetworkStatus.isNetworkAvailable(context)) {
            new FetchFolderReceiptsAsyncTask().execute();
        }

        //Move below code block into populateReceiptList()
        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.receiptList);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new FolderReceiptListAdapter(context, mReceiptList);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.folder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.setCurrent:
                new setCurrentFolderAsyncTask().execute();
                return true;

            case R.id.deleteFolder:
                deleteFolderPopUp();
                return true;
        }
        return true;
    }

    public void deleteFolderPopUp() {
        final LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.delete_folder_pop_up, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete Folder");
        builder.setView(dialoglayout);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(FolderActivity.this,"Accept",Toast.LENGTH_LONG).show();
                new deleteFolderAsyncTask().execute();

                Intent intent = new Intent(context, FragmentHolder.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(FolderActivity.this,"Cancel",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private class deleteFolderAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("folder_id", String.valueOf(folderId));//'1' needs to be changed to some user chosen folder id
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
                Toast.makeText(FolderActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class FetchFolderReceiptsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("folder_id", String.valueOf(folderId));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllReceiptsFromFolder.php", "POST", httpParams);

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

        protected void onPostExecute(String result) {
            mAdapter = new FolderReceiptListAdapter(context, mReceiptList);
            // Connect the adapter with the RecyclerView.
            mRecyclerView.setAdapter(mAdapter);
            // Give the RecyclerView a default layout manager.
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
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
                Toast.makeText(FolderActivity.this, message, Toast.LENGTH_LONG).show();
            }
            else{
                //If success update xml
                for(Folder folder: folderList){
                    addNewItem(folder.getId(), folder.getName());
                }
            }
        }
    }

    private class setCurrentFolderAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DBConstants.USER_ID, String.valueOf(session.getUserId()));
            httpParams.put("folder_id", String.valueOf(folderId));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "assignCurrentFolder.php", "POST", httpParams);

            try {
                success = jsonObject.getInt("success");
                if (success == 1) {
                    session.setCurrentFolder(String.valueOf(folderId));
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
                Toast.makeText(FolderActivity.this, message, Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(FolderActivity.this, "Set To Current", Toast.LENGTH_LONG).show();

                MenuItem myMoveGroupItem = navigationView.getMenu().getItem(1);
                SubMenu subMenu = myMoveGroupItem.getSubMenu();
                subMenu.clear();
                for(Folder folder: folderList){
                    addNewItem(folder.getId(), folder.getName());
                }
            }
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
}
