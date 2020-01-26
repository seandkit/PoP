package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pop.R;

public class ReceiptActivity extends AppCompatActivity {

    private TextView total;
    private TextView cash;
    private TextView change;
    private TextView location;
    private TextView date;
    private TextView time;
    private TextView barcodeNumber;
    private TextView otherNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Intent intent = getIntent();
        int receiptID = intent.getIntExtra("receiptID", 0);

        total = findViewById(R.id.receiptTotal);
        cash = findViewById(R.id.receiptCash);
        change = findViewById(R.id.receiptChangeDue);
        location = findViewById(R.id.receiptLocation);
        date = findViewById(R.id.receiptDate);
        time = findViewById(R.id.receiptTime);
        barcodeNumber = findViewById(R.id.receiptBarcodeNumber);
        otherNumber = findViewById(R.id.receiptOtherNumber);
    }
}
