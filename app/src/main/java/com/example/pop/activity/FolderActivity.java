package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.adapter.FolderReceiptListAdapter;
import com.example.pop.adapter.ItemListAdapter;
import com.example.pop.asynctasks.FetchFolderReceiptsAsyncTask;
import com.example.pop.asynctasks.FetchFoldersAsyncTask;
import com.example.pop.asynctasks.SetCurrentFolderAsyncTask;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.ScreenCapture;
import com.example.pop.helper.Session;
import com.example.pop.helper.Utils;
import com.example.pop.model.Folder;
import com.example.pop.model.Item;
import com.example.pop.model.Receipt;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FolderActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context context;
    Session session;

    public static NavigationView navigationView;
    private DrawerLayout drawer;

    public static RecyclerView mRecyclerView;
    public static FolderReceiptListAdapter mAdapter;
    public static List<Receipt> folderReceiptList = new ArrayList<>();

    public static int folderId;
    public static String folderName;

    private int receiptId;
    private Receipt receipt;

    private Button btn_export;

    //Export Variable
    private View v;
    private ConstraintLayout relativeLayout;
    private Button exportBtn;
    private RecyclerView mItemRecyclerView;
    private ItemListAdapter mItemAdapter;
    public List<Item> mItemList = new ArrayList<>();
    private TextView total; //Can currently be got from db
    private TextView cash;
    private TextView change;
    private TextView vendor;
    private TextView location;
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
        navigationView.setNavigationItemSelectedListener(this);

        if (CheckNetworkStatus.isNetworkAvailable(context)) {
            Utils.updateFolderMenu(navigationView, context);

            FetchFolderReceiptsAsyncTask fetchFolderReceiptsAsyncTask = new FetchFolderReceiptsAsyncTask(FolderActivity.this, context, folderId);
            fetchFolderReceiptsAsyncTask.execute();
        }

        mRecyclerView = findViewById(R.id.receiptList);
        mAdapter = new FolderReceiptListAdapter(context, folderReceiptList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        btn_export = findViewById(R.id.export_btn);
        btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ask = Utils.requestStoragePermission(FolderActivity.this);

                if(!ask) {
                    new ExportAsyncTask().execute();
                    Toast.makeText(FolderActivity.this, "Exporting to gallery", Toast.LENGTH_LONG).show();
                }
            }
        });
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
                SetCurrentFolderAsyncTask setCurrentFolderAsyncTask = new SetCurrentFolderAsyncTask(FolderActivity.this, navigationView, context, folderId);
                setCurrentFolderAsyncTask.execute();
                return true;

            case R.id.deleteFolder:
                Utils.deleteFolderPopUp(FolderActivity.this, navigationView, folderId);
                return true;
        }
        return true;
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
                FragmentHolder.globalFolderList = new ArrayList<>();
                folderReceiptList = new ArrayList<>();
                finish();
                break;

            case R.id.nav_folder_add_new:
                Utils.newFolderPopUp(navigationView, FolderActivity.this);
                break;
        }

        return true;
    }

    private class ExportAsyncTask extends AsyncTask<String, String, String> {

        int success;
        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            for(Receipt r : folderReceiptList) {
                LayoutInflater inflater = LayoutInflater.from(context);
                v = inflater.inflate(R.layout.activity_receipt, null);

                relativeLayout = v.findViewById(R.id.receiptLayout);
                ConstraintLayout pageLayout = v.findViewById(R.id.receiptPageContainer);
                pageLayout.removeView(v.findViewById(R.id.export_btn));

                v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                vendor = v.findViewById(R.id.receiptLocation);
                location = v.findViewById(R.id.storeAddress);
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
                            double lat = receiptInfo.getDouble("lat");
                            double lng = receiptInfo.getDouble("lng");

                            String location = receiptInfo.getString("location");
                            String barcode = receiptInfo.getString("barcode");
                            String cashier = receiptInfo.getString("cashier");
                            double cash = receiptInfo.getDouble("cash_given");
                            int transactionType = receiptInfo.getInt("transaction_type");

                            receipt = new Receipt(receiptId, receiptDate, receiptTime, receiptVendor, receiptTotal, barcode, transactionType, cashier, cash, location, lat, lng, session.getUserId());
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
                    vendor.setText(receipt.getVendorName());
                    location.setText(receipt.getLocation());
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
}
