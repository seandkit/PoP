package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.adapter.ItemListAdapter;
import com.example.pop.asynctasks.FetchAllFoldersWithReceiptAsyncTask;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.helper.Utils;
import com.example.pop.model.Folder;
import com.example.pop.model.Item;
import com.example.pop.model.Receipt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ReceiptActivity extends AppCompatActivity {

    private TextView total; //Can currently be got from db
    private TextView cash;
    private TextView change;
    private TextView vendor;
    private TextView location;
    private TextView date; //Can currently be got from db
    private TextView time; //Can currently be got from db
    private TextView barcodeNumber;
    private TextView otherNumber;
    private double lat;
    private double lng;

    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    public List<Item> mItemList = new ArrayList<>();

    int receiptId;

    private Context context;
    private Session session;

    public Receipt receipt;

    private Button btn_export;

    public static List<Folder> receiptFoldersList = new ArrayList<>();

    ConstraintLayout relativeLayout;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        toolbar = findViewById(R.id.receiptToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        relativeLayout = findViewById(R.id.receiptLayout);

        btn_export = findViewById(R.id.export_btn);

        btn_export.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                boolean permission = Utils.requestStoragePermission(ReceiptActivity.this);

                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(ReceiptActivity.this, ExportActivity.class);
                    intent.putExtra("itemList", (Serializable) mItemList);
                    intent.putExtra("receiptData", receipt);
                    intent.putExtra("permission", permission);
                    startActivity(intent);
                }
            }
        });

        context = this;
        session = new Session(context);

        Intent intent = getIntent();
        receiptId = intent.getIntExtra("receiptID",0);

        total = findViewById(R.id.receiptTotalText);
        cash = findViewById(R.id.receiptCash);
        change = findViewById(R.id.receiptChangeDue);
        vendor = findViewById(R.id.receiptLocation);
        location = findViewById(R.id.storeAddress);
        date = findViewById(R.id.receiptDate);
        time = findViewById(R.id.receiptTime);
        otherNumber = findViewById(R.id.receiptOtherNumber);

        new FetchReceiptsInfoAsyncTask().execute();
    }

    public void onClick(View v) {
        double markerLat = receipt.getLat();
        double markerLong = receipt.getLng();
        String markerTitle = receipt.getVendorName();
        String markerSnippet = receipt.getLocation();

        Intent intent = new Intent(ReceiptActivity.this, Map_Location.class);
        intent.putExtra("title", markerTitle);
        intent.putExtra("snippet", markerSnippet);
        intent.putExtra("lat", markerLat);
        intent.putExtra("long", markerLong);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.receipt_menu, menu);

        SubMenu subMenu = menu.getItem(0).getSubMenu();

        receiptFoldersList = new ArrayList<>();

        FetchAllFoldersWithReceiptAsyncTask fetchAllFoldersWithReceiptAsyncTask = new FetchAllFoldersWithReceiptAsyncTask(ReceiptActivity.this, context, receiptId);
        fetchAllFoldersWithReceiptAsyncTask.execute();

        if(receiptFoldersList.isEmpty()){
            subMenu.add(Menu.NONE, 1, Menu.NONE, "Empty");
        }
        else {
            for(Folder f : receiptFoldersList){
                subMenu.add(Menu.NONE, f.getId(), Menu.NONE, f.getName()).setOnMenuItemClickListener(folderOnClickListener);
            }
        }

        return true;
    }

    private MenuItem.OnMenuItemClickListener folderOnClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            Intent intent = new Intent(context, FolderActivity.class);

            for(Folder f : receiptFoldersList){
                if(f.getName().equalsIgnoreCase(String.valueOf(menuItem.getTitle()))){
                    intent.putExtra("folderId", f.getId());
                    intent.putExtra("folderName", f.getName());
                }
            }

            startActivity(intent);
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class FetchReceiptsInfoAsyncTask extends AsyncTask<String, String, String> {

        ProgressDialog pDialog;
        int success;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ReceiptActivity.this);
            pDialog.setMessage("Checking Database. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
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

                    for (int i = 0; i < itemData.length(); i++) {
                        JSONObject item = itemData.getJSONObject(i);
                        int itemId = item.getInt(DBConstants.ITEM_ID);
                        String itemName = item.getString("name");
                        double itemPrice = item.getDouble(DBConstants.PRICE);
                        int itemQuantity = item.getInt(DBConstants.QUANTITY);

                        mItemList.add(new Item(itemId,itemName,itemPrice,itemQuantity));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();

            if(success == 1)
            {
                vendor.setText(receipt.getVendorName());
                String[] separated = receipt.getDate().split("-");
                String dateOrdered = separated[2] + "-" + separated[1] + "-" + separated[0];
                date.setText(dateOrdered);
                time.setText(receipt.getTime());
                location.setText(receipt.getLocation());
                total.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));
                cash.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));
            }
            else{
                Toast.makeText(ReceiptActivity.this,"Empty", Toast.LENGTH_LONG).show();
            }

            mRecyclerView = findViewById(R.id.itemList);
            mAdapter = new ItemListAdapter(context, mItemList);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            findViewById(R.id.itemList).requestLayout();
            findViewById(R.id.itemList).getLayoutParams().height = 75 * mItemList.size();
        }
    }
}