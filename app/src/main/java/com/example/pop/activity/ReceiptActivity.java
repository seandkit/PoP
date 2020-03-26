package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import com.example.pop.activity.adapter.ItemListAdapter;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Folder;
import com.example.pop.model.Item;
import com.example.pop.model.Receipt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ReceiptActivity extends AppCompatActivity {

    private TextView total; //Can currently be got from db
    private TextView cash;
    private TextView change;
    private TextView location; //Temporarily Vendor
    private TextView date; //Can currently be got from db
    private TextView time; //Can currently be got from db
    private TextView barcodeNumber;
    private TextView otherNumber;

    private RecyclerView mRecyclerView;
    private ItemListAdapter mAdapter;
    public List<Item> mItemList = new ArrayList<>();

    int receiptId;
    private int success;
    private String message;

    private Context context;
    private Session session;

    public Receipt receipt;

    private Bitmap bitmap;
    private Button btn_export_png;
    private Button btn_export_csv;
    private Button btn_export_pdf;

    public static List<Folder> folderList = new ArrayList<>();

    ConstraintLayout relativeLayout;

    private int STORAGE_PERMISSION_CODE = 1;

    //private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        //toolbar = findViewById(R.id.receiptToolbar);
        //setSupportActionBar(toolbar);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });*/

        relativeLayout = findViewById(R.id.receiptLayout);

        btn_export_png = (Button) findViewById(R.id.export_btn_png);
        btn_export_pdf = (Button) findViewById(R.id.export_btn_pdf);
        btn_export_csv = (Button) findViewById(R.id.export_btn_csv);

        btn_export_png.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestStoragePermission();

                bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                relativeLayout.draw((canvas));

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/PopReceipts");
                myDir.mkdirs();
                String fname = "Receipt_"+ System.currentTimeMillis() +".jpg";
                File file = new File(myDir, fname);

                if (!file.exists()) {
                    Log.d("path", file.toString());

                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        Toast.makeText(getApplicationContext(), "Receipt successfully exported to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        fos.flush();
                        fos.close();
                    } catch (java.io.IOException e) {
                        Toast.makeText(getApplicationContext(), "Problem exporting receipt", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });


        btn_export_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestStoragePermission();

                bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                relativeLayout.draw((canvas));

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/PopReceipts");
                myDir.mkdirs();
                String fname = "Receipt_"+ System.currentTimeMillis() +".png";
                File file = new File(myDir, fname);

                if (!file.exists()) {
                    Log.d("path", file.toString());

                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        Toast.makeText(getApplicationContext(), "Receipt successfully exported to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        fos.flush();
                        fos.close();
                    } catch (java.io.IOException e) {
                        Toast.makeText(getApplicationContext(), "Problem exporting receipt", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });



        btn_export_csv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                StringBuilder data = new StringBuilder();
                data.append("Item,Quantity,Total");

                for(int i = 0; i<mAdapter.getItemCount(); i++){
                    data.append("\n"+String.valueOf(i)+","+String.valueOf(i*i));

                }
                try {
                    FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
                    out.write((data.toString()).getBytes());
                    out.close();

                    Context context = getApplicationContext();
                    File filelocation = new File(getFilesDir(), "data.csv");
                    Uri path = FileProvider.getUriForFile(context, "com.example.pop.fileprovider", filelocation);
                    Intent fileIntent = new Intent(Intent.ACTION_SEND);
                    fileIntent.setType("text/csv");
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(fileIntent, "Send mail"));
                }
                catch (Exception e){
                    e.printStackTrace();

                }

            }
        });

        context = this;

        session = new Session(context);

        Intent intent = getIntent();
        receiptId = intent.getIntExtra("receiptID",0);

        System.out.println("RECEIPT ID: " + receiptId);

        total = findViewById(R.id.receiptTotalText);
        cash = findViewById(R.id.receiptCash);
        change = findViewById(R.id.receiptChangeDue);
        location = findViewById(R.id.receiptLocation);
        date = findViewById(R.id.receiptDate);
        time = findViewById(R.id.receiptTime);
        otherNumber = findViewById(R.id.receiptOtherNumber);




        new FetchReceiptsInfoAsyncTask().execute();



    }


    public void onClick(View v) {
        Double markerLat = 53.9979;
        Double markerLong = -6.406;
        String markerTitle = "Tesco";
        String markerSnippet = "Dublin Rd, Townparks, Dundalk, Co. Louth";
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

        folderList = new ArrayList<>();
        try {
            String result = new FetchAllFoldersWithReceiptAsyncTask().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(folderList.isEmpty()){
            subMenu.add(Menu.NONE, 1, Menu.NONE, "Empty");
        }
        else {
            for(Folder f : folderList){
                subMenu.add(Menu.NONE, f.getId(), Menu.NONE, f.getName()).setOnMenuItemClickListener(folderOnClickListener);
            }
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

    private void requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(context)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to export your receipts.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(ReceiptActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class FetchReceiptsInfoAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

                        String location = receiptInfo.getString("location");
                        String barcode = receiptInfo.getString("barcode");
                        String cashier = receiptInfo.getString("cashier");
                        double cash = receiptInfo.getDouble("cash_given");
                        int transactionType = receiptInfo.getInt("transaction_type");

                        receipt = new Receipt(receiptId, receiptDate, receiptTime, receiptVendor, receiptTotal, barcode, transactionType, cashier, cash, location, session.getUserId());
                    }

                    for (int i = 0; i < itemData.length(); i++) {
                        JSONObject item = itemData.getJSONObject(i);
                        int itemId = item.getInt(DBConstants.ITEM_ID);
                        String itemName = item.getString("name");
                        double itemPrice = item.getDouble(DBConstants.PRICE);
                        int itemQuantity = item.getInt(DBConstants.QUANTITY);

                        //Populate a list of items to be displayed on receipt
                        mItemList.add(new Item(itemId,itemName,itemPrice,itemQuantity));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            if(success == 1)
            {
                location.setText(receipt.getVendorName());
                String[] separated = receipt.getDate().split("-");
                String dateOrdered = separated[2] + "-" + separated[1] + "-" + separated[0];
                date.setText(dateOrdered);
                time.setText(receipt.getTime());
                total.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));
                cash.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));

                System.out.println(receipt.getBarcode());
                System.out.println(receipt.getCash());
                System.out.println(receipt.getCashier());
                System.out.println(receipt.getLocation());
                System.out.println(receipt.getTransactionType());
            }
            else{
                Toast.makeText(ReceiptActivity.this,"Empty", Toast.LENGTH_LONG).show();
            }

            mRecyclerView = findViewById(R.id.itemList);
            mAdapter = new ItemListAdapter(context, mItemList);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }

    private class FetchAllFoldersWithReceiptAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("receipt_id", String.valueOf(receiptId));
            httpParams.put("user_id", String.valueOf(session.getUserId()));
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllFoldersWithReceipt.php", "POST", httpParams);

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
        }
    }
}
