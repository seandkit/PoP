package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Item;
import com.example.pop.model.Receipt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiptActivity extends AppCompatActivity {

    private TextView total; //Can currently be got from db
    private TextView cash;
    private TextView change;
    private TextView location; //Temporarily Vendor
    private TextView date; //Can currently be got from db
    private TextView time; //Can currently be got from db
    private TextView barcodeNumber;
    private TextView otherNumber;

    int receiptId;
    private int success;
    private String message;

    private Context context;
    private Session session;

    public Receipt receipt;
    public List<Item> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        context = getApplicationContext();
        session = new Session(context);

        Intent intent = getIntent();
        receiptId = intent.getIntExtra("receiptID",0);

        System.out.println(receiptId);

        new FetchReceiptsInfoAsyncTask().execute();

        total = findViewById(R.id.receiptTotal);
        cash = findViewById(R.id.receiptCash);
        change = findViewById(R.id.receiptChangeDue);
        location = findViewById(R.id.receiptLocation);
        date = findViewById(R.id.receiptDate);
        time = findViewById(R.id.receiptTime);
        barcodeNumber = findViewById(R.id.receiptBarcodeNumber);
        otherNumber = findViewById(R.id.receiptOtherNumber);
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

                        receipt = new Receipt(receiptId, receiptDate, receiptTime, receiptVendor, receiptTotal);
                    }

                    for (int i = 0; i < itemData.length(); i++) {
                        JSONObject item = itemData.getJSONObject(i);
                        int itemId = item.getInt(DBConstants.ITEM_ID);
                        String itemName = item.getString("name");
                        double itemPrice = item.getDouble(DBConstants.PRICE);
                        int itemQuantity = item.getInt(DBConstants.QUANTITY);

                        //Populate a list of items to be displayed on receipt
                        itemList.add(new Item(itemId,itemName,itemPrice,itemQuantity));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            //?? populate xml with receipt and itemList??

            System.out.println("receiptild: "+receipt.getId());
            System.out.println("receiptDate: "+receipt.getDate());
            System.out.println("receiptTime: "+receipt.getTime());
            System.out.println("receiptVendor: "+receipt.getVendorName());
            System.out.println("receiptTotal: "+receipt.getReceiptTotal());
            for(Item i: itemList){
                System.out.println("itemID: "+i.getId() + " | itemName: "+i.getName()+" | itemPrice: "+i.getPrice()+" | itemQuantity: "+i.getQuantity());
            }
        }
    }
}
