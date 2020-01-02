package com.example.pop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pop.model.Receipt;

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

        total = findViewById(R.id.receiptTotal);
        cash = findViewById(R.id.receiptCash);
        change = findViewById(R.id.receiptChangeDue);
        location = findViewById(R.id.receiptLocation);
        date = findViewById(R.id.receiptDate);
        time = findViewById(R.id.receiptTime);
        barcodeNumber = findViewById(R.id.receiptBarcodeNumber);
        otherNumber = findViewById(R.id.receiptOtherNumber);

        //Receipt receipt = findReceiptById(id);

        //total.setText(Double.toString(receipt.getReceiptTotal()));
        //cash.setText(Double.toString(receipt.getReceiptCash()));
        //change.setText(Double.toString(receipt.getReceiptChange()));
        //location.setText(receipt.getReceiptLocation());
        //date.setText(receipt.getReceiptDate());
        //time.setText(receipt.getReceiptTime());
        //barcodeNumber.setText(receipt.getBarcodeNumber());


    }
}
