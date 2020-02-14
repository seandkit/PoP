package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.pop.R;

import java.io.File;
import java.io.FileOutputStream;

public class ExportActivity extends Activity {


    Button save;
    Bitmap receiptBitmap;
    LinearLayout relativeLayout;

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
    }
}
