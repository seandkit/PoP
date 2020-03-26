package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accounts.Account;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.adapter.FolderReceiptListAdapter;
import com.example.pop.activity.adapter.ItemListAdapter;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Folder;
import com.example.pop.model.Item;
import com.example.pop.model.Receipt;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private int receiptId;
    private Receipt receipt;

    private Button btn_export;
    private int STORAGE_PERMISSION_CODE = 1;


    //Export Variable
    private View v;
    private ConstraintLayout relativeLayout;
    private RecyclerView mItemRecyclerView;
    private ItemListAdapter mItemAdapter;
    public List<Item> mItemList = new ArrayList<>();
    private TextView total; //Can currently be got from db
    private TextView cash;
    private TextView change;
    private TextView location; //Temporarily Vendor
    private TextView date; //Can currently be got from db
    private TextView time; //Can currently be got from db
    private TextView barcodeNumber;
    private TextView otherNumber;

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

        if (CheckNetworkStatus.isNetworkAvailable(context)) {
            try {
                String str_result = new FolderActivity.fetchFoldersAsyncTask().execute().get();
                String str_result2 = new FetchFolderReceiptsAsyncTask().execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mRecyclerView = findViewById(R.id.receiptList);
        mAdapter = new FolderReceiptListAdapter(context, mReceiptList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        btn_export = findViewById(R.id.export_btn);
        btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ask = requestStoragePermission();

                if(!ask) {
                    new ExportAsyncTask().execute();
                    Toast.makeText(FolderActivity.this, "Exporting to gallery", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean requestStoragePermission() {
        boolean answer = false;

        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            answer = true;

            new AlertDialog.Builder(context)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to export your receipts.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(FolderActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        return answer;
    }

    private class ExportAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            for(Receipt r : mReceiptList) {
                LayoutInflater inflater = LayoutInflater.from(context);
                v = inflater.inflate(R.layout.activity_receipt, null);

                relativeLayout = findViewById(R.id.receiptLayout);

                v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                location = v.findViewById(R.id.receiptLocation);
                total = v.findViewById(R.id.receiptTotalText);
                cash = v.findViewById(R.id.receiptCash);
                date = v.findViewById(R.id.receiptDate);
                time = v.findViewById(R.id.receiptTime);

                mItemRecyclerView = v.findViewById(R.id.itemList);

                receiptId = r.getId();

                HttpJsonParser httpJsonParser = new HttpJsonParser();
                Map<String, String> httpParams = new HashMap<>();
                httpParams.put("receipt_id", String.valueOf(receiptId));
                JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "getAllReceiptInfo.php", "POST", httpParams);

                try {
                    success = jsonObject.getInt("success");
                    JSONArray receiptData;
                    JSONArray itemData;

                    if (success == 1) {
                        receiptData = jsonObject.getJSONArray("receipt");
                        itemData = jsonObject.getJSONArray("items");
                        for (int i = 0; i < receiptData.length(); i++) {
                            JSONObject receiptInfo = receiptData.getJSONObject(i);

                            //Values that can currently be gotten from database and assigned to receipt
                            int receiptId = receiptInfo.getInt(DBConstants.RECEIPT_ID);
                            String receiptDate = receiptInfo.getString(DBConstants.DATE);
                            String receiptTime = receiptInfo.getString("time");
                            String receiptVendor = receiptInfo.getString(DBConstants.VENDOR);
                            double receiptTotal = receiptInfo.getDouble(DBConstants.RECEIPT_TOTAL);

                            String location = receiptInfo.getString("location");
                            String barcode = receiptInfo.getString("barcode");
                            String cashier = receiptInfo.getString("cashier");
                            double cash = receiptInfo.getDouble("cash_given");
                            int transactionType = receiptInfo.getInt("transaction_type");

                            receipt = new Receipt(receiptId, receiptDate, receiptTime, receiptVendor, receiptTotal, barcode, transactionType, cashier, cash, location, session.getUserId());
                        }

                        mItemList = new ArrayList<>();
                        for (int i = 0; i < itemData.length(); i++) {
                            JSONObject item = itemData.getJSONObject(i);
                            int itemId = item.getInt(DBConstants.ITEM_ID);
                            String itemName = item.getString("name");
                            double itemPrice = item.getDouble(DBConstants.PRICE);
                            int itemQuantity = item.getInt(DBConstants.QUANTITY);

                            //Populate a list of items to be displayed on receipt
                            mItemList.add(new Item(itemId, itemName, itemPrice, itemQuantity));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(success == 1)
                {
                    location.clearComposingText();
                    location.setText(receipt.getVendorName());
                    String[] separated = receipt.getDate().split("-");
                    String dateOrdered = separated[2] + "-" + separated[1] + "-" + separated[0];
                    date.setText(dateOrdered);
                    time.setText(receipt.getTime());
                    total.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));
                    cash.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));
                }

                mItemAdapter = new ItemListAdapter(context, mItemList);
                mItemRecyclerView.setAdapter(mItemAdapter);
                mItemRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bitmap);
                v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                v.draw(c);

                ScreenCapture.insertImage(getContentResolver(), bitmap,System.currentTimeMillis() +".jpg", folderName, folderName + " Receipts");
            }

            return null;
        }

        protected void onPostExecute(String result) { }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
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
        LayoutInflater inflater = (LayoutInflater) FolderActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialoglayout = inflater.inflate(R.layout.delete_folder_pop_up, null);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(dialoglayout.getRootView().getContext());

        builder.setTitle("Delete Folder");
        builder.setView(dialoglayout);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context,"Deleted",Toast.LENGTH_LONG).show();
                new deleteFolderAsyncTask().execute();

                Intent intent = new Intent(FolderActivity.this, FragmentHolder.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context,"Cancel",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        android.app.AlertDialog alertDialog = builder.create();
        //alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
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
