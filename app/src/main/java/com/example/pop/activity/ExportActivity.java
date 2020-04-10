package com.example.pop.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.R;
import com.example.pop.activity.adapter.ItemListAdapter;
import com.example.pop.model.Item;
import com.example.pop.model.Receipt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportActivity extends Activity {


    Button save;
    Bitmap receiptBitmap;
    private ConstraintLayout relativeLayout;
    private ConstraintLayout relativeLayout2;
    LinearLayout linearLayout;
    Context context;

    private Bitmap bitmap;
    private Button btn_export_png;
    private Button btn_back;
    private Button btn_export_csv;
    private Button btn_export_pdf;
    private Button btn_export;
    private View v;



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


        receiptBitmap = (Bitmap) getIntent().getParcelableExtra("ReceiptBitmap");

        relativeLayout = findViewById(R.id.linearTest);
        save = findViewById(R.id.jpgExport);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("This is the first log", "It gets to here");
                Canvas canvas = new Canvas(receiptBitmap);
                relativeLayout.draw((canvas));

                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File file = new File(directory, "A09popImageReceipt2nick" + ".jpg");
                if (!file.exists()) {
                    Log.d("path", file.toString());
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                        receiptBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }


                }
            }


        });


        btn_export_png = (Button) findViewById(R.id.jpgExport);
        btn_export_pdf = (Button) findViewById(R.id.pdf_btn);
        btn_export_csv = (Button) findViewById(R.id.csv_btn);
        btn_back = (Button) findViewById(R.id.back_btn);

        Intent intent = getIntent();
        final Receipt receipt = intent.getParcelableExtra("receiptData");
        final List<Item> mItemList = (List<Item>) intent.getSerializableExtra("itemList");
        final boolean permission = intent.getBooleanExtra("permission",false);



        btn_export_png.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean ask = permission;

                if(!ask) {

                    LayoutInflater inflater = LayoutInflater.from(context);
                    v = inflater.inflate(R.layout.activity_receipt, null);

                    relativeLayout = v.findViewById(R.id.receiptLayout);
                    ConstraintLayout pageLayout = v.findViewById(R.id.receiptPageContainer);
                    pageLayout.removeView(v.findViewById(R.id.export_btn));
                    //pageLayout.removeView(v.findViewById(R.id.export_btn_pdf));
                    //pageLayout.removeView(v.findViewById(R.id.export_btn_csv));

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
                ConstraintLayout pageLayout = v.findViewById(R.id.receiptPageContainer);
                pageLayout.removeView(v.findViewById(R.id.export_btn));
                pageLayout.removeView(v.findViewById(R.id.toolbar));
                //pageLayout.removeView(v.findViewById(R.id.export_btn_csv));

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

                bitmap = Bitmap.createBitmap(relativeLayout.getWidth(), relativeLayout.getHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                relativeLayout.draw((canvas));

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/Pop Receipts/PDF Receipts");
                myDir.mkdirs();
                String fname = "Receipt_"+ System.currentTimeMillis() +".pdf";
                File file = new File(myDir, fname);


                createPdf(bitmap, myDir, fname);
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





}
