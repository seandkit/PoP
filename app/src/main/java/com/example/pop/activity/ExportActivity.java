package com.example.pop.activity;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.R;
import com.example.pop.adapter.ItemListAdapter;
import com.example.pop.helper.ScreenCapture;
import com.example.pop.helper.Utils;
import com.example.pop.model.Item;
import com.example.pop.model.Receipt;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExportActivity extends Activity {
    Bitmap receiptBitmap;
    Receipt receipt;
    List<Item> mItemList;
    boolean permission;

    private ConstraintLayout relativeLayout;
    Context context;

    private View v;

    private RecyclerView mItemRecyclerView;
    private ItemListAdapter mItemAdapter;
    private TextView total;
    private TextView cash;
    private TextView vendor;
    private TextView location;
    private TextView date;
    private TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7),(int)(height*.45));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);

        context = getApplicationContext();

        receiptBitmap = getIntent().getParcelableExtra("ReceiptBitmap");

        relativeLayout = findViewById(R.id.linearTest);

        Button btn_export_png = findViewById(R.id.jpgExport);
        Button btn_export_pdf = findViewById(R.id.pdf_btn);
        Button btn_export_csv = findViewById(R.id.csv_btn);
        Button btn_back = findViewById(R.id.back_btn);

        Intent intent = getIntent();
        receipt = intent.getParcelableExtra("receiptData");
        mItemList = (List<Item>) intent.getSerializableExtra("itemList");
        permission = intent.getBooleanExtra("permission",false);

        btn_export_png.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ask = permission;

                if(!ask) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    v = inflater.inflate(R.layout.activity_receipt, null);

                    v.findViewById(R.id.itemList).requestLayout();
                    v.findViewById(R.id.itemList).getLayoutParams().height = 75 * mItemList.size();
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

                    vendor.setText(receipt.getVendorName());
                    location.setText(receipt.getLocation());
                    String[] separated = receipt.getDate().split("-");
                    String dateOrdered = separated[2] + "-" + separated[1] + "-" + separated[0];
                    date.setText(dateOrdered);
                    time.setText(receipt.getTime());
                    total.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));
                    cash.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));

                    mItemAdapter = new ItemListAdapter(context, mItemList);
                    mItemRecyclerView.setAdapter(mItemAdapter);
                    mItemRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                    Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(bitmap);
                    v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                    v.draw(c);

                    bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), 1450 + (mItemList.size() * 75), Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bitmap);
                    relativeLayout.draw((canvas));

                    ScreenCapture.insertImage(getContentResolver(), bitmap,System.currentTimeMillis() +".jpg", "All Receipts",  "All Receipts");
                    Toast.makeText(getApplicationContext(), "PNG file saved to: Albums /All Receipts", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_export_pdf.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(context);
                v = inflater.inflate(R.layout.activity_receipt, null);

                relativeLayout = v.findViewById(R.id.receiptLayout);
                v.findViewById(R.id.itemList).requestLayout();
                v.findViewById(R.id.itemList).getLayoutParams().height = 75 * mItemList.size();
                ConstraintLayout pageLayout = v.findViewById(R.id.receiptPageContainer);
                pageLayout.removeView(v.findViewById(R.id.export_btn));
                pageLayout.removeView(v.findViewById(R.id.toolbar));

                v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                vendor = v.findViewById(R.id.receiptLocation);
                location = v.findViewById(R.id.storeAddress);
                total = v.findViewById(R.id.receiptTotalText);
                cash = v.findViewById(R.id.receiptCash);
                date = v.findViewById(R.id.receiptDate);
                time = v.findViewById(R.id.receiptTime);

                mItemRecyclerView = v.findViewById(R.id.itemList);

                vendor.setText(receipt.getVendorName());
                location.setText(receipt.getLocation());
                String[] separated = receipt.getDate().split("-");
                String dateOrdered = separated[2] + "-" + separated[1] + "-" + separated[0];
                date.setText(dateOrdered);
                time.setText(receipt.getTime());
                total.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));
                cash.setText("€" + String.format("%.2f", receipt.getReceiptTotal()));

                mItemAdapter = new ItemListAdapter(context, mItemList);
                mItemRecyclerView.setAdapter(mItemAdapter);
                mItemRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bitmap);
                v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                v.draw(c);

                bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), 1450 + (mItemList.size() * 75), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                relativeLayout.draw((canvas));

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/Pop Receipts/PDF Receipts");
                myDir.mkdirs();
                String fname = "Receipt_"+ System.currentTimeMillis() +".pdf";

                Utils.createPdf(context, bitmap, myDir, fname);
                Toast.makeText(getApplicationContext(), "PDF file Created in: /Pop Receipts/PDF Receipts", Toast.LENGTH_SHORT).show();
            }
        });

        btn_export_csv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder data = new StringBuilder();
                data.append("Location,Date,Time,Store,Cashier,Item,Quantity,Price,Total");
                data.append("\n"+receipt.getVendorName()  + "," +receipt.getDate() + "," + receipt.getTime() + "," +receipt.getId() + "," +receipt.getCashier()+ ","
                        + mItemList.get(0).getName() + "," + mItemList.get(0).getQuantity() + "," + mItemList.get(0).getPrice() + "," +
                        receipt.getReceiptTotal());

                for(int i = 1; i<mItemList.size(); i++){
                    data.append("\n"+ ", , , , ," + mItemList.get(i).getName() + "," + mItemList.get(i).getQuantity() + "," + mItemList.get(i).getPrice() + ",");
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

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}