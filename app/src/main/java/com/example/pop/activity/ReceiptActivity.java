package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

                boolean ask = requestStoragePermission();

                if(!ask) {
                    bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    relativeLayout.draw((canvas));
                    Toast.makeText(ReceiptActivity.this, "Exporting to gallery", Toast.LENGTH_LONG).show();

                    //saveImage(bitmap, "PHOTO");

                    //MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "yourTitle" , "yourDescription");
                    ScreenCapture.insertImage(getContentResolver(), bitmap, System.currentTimeMillis() + ".jpg", "All Receipts", "All Receipts");

                    /*if (!file.exists()) {
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
                    }*/
                }
            }
        });


        btn_export_pdf.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {

                requestStoragePermission();

                bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                relativeLayout.draw((canvas));

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/Pop Receipts");
                myDir.mkdirs();
                String fname = "Receipt_"+ System.currentTimeMillis() +".pdf";
                File file = new File(myDir, fname);


                createPdf(bitmap, myDir, fname);

                /*if (!file.exists()) {
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
                }*/
            }
        });



        btn_export_csv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StringBuilder data = new StringBuilder();
                data.append("Item,Quantity,Total");

                for(int i = 0; i < mItemList.size(); i++){
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

    private void createPdf(Bitmap bitmap, File myDir, String fileName){
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0 , null);
        document.finishPage(page);


        // write the document content
        File filePath = new File(myDir, fileName);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();
    }

    private void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        return answer;
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
        }
    }

    private class FetchAllFoldersWithReceiptAsyncTask extends AsyncTask<String, String, String> {

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
