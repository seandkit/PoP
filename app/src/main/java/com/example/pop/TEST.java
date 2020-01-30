package com.example.pop;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pop.model.Item;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteQueries;

import java.util.ArrayList;
import java.util.List;

public class TEST {
    public static void sampleTestReceipts(SQLiteDatabase db) {
        //Sample Test Items
        Item testItem1 = new Item(1,"TESCO 1LT MILK", 0.79,1);
        Item testItem2 = new Item(2,"TESCO WHITE PAN", 0.69,1);
        Item testItem3 = new Item(3,"ORANGE JUICE", 2.39,1);
        Item testItem4 = new Item(4,"ACTIMEL 12PK", 3.50,1);

        //Sample List of Items to be displays on virtual receipt
        List<Item> itemList0 = new ArrayList<>();
        itemList0.add(testItem1);
        itemList0.add(testItem2);
        itemList0.add(testItem3);
        itemList0.add(testItem4);

        //Sample Receipt Items
        Receipt testReceipt0 = new Receipt("2019/10/01", "Tesco", 1, 1, itemList0);
        Receipt testReceipt1 = new Receipt("2019/10/03", "Tesco", 1, 103.59, 1);
        Receipt testReceipt2 = new Receipt("2019/10/11", "Tesco", 0, 4.08, 1);
        Receipt testReceipt3 = new Receipt("2019/10/15", "Tesco", 1, 55.55, 1);
        Receipt testReceipt4 = new Receipt("2019/10/20", "Tesco", 0, 23.00, 1);
        Receipt testReceipt5 = new Receipt("2019/10/24", "Tesco", 1, 77.53, 1);
        Receipt testReceipt6 = new Receipt("2019/10/29", "Tesco", 1, 60.31, 1);
        Receipt testReceipt7 = new Receipt("2019/11/01", "Tesco", 0, 12.96, 1);
        Receipt testReceipt8 = new Receipt("2019/11/03", "Tesco", 1, 33.33, 1);
        Receipt testReceipt9 = new Receipt("2019/11/07", "Tesco", 0, 201.68, 1);

        insertReceiptToDB(db, testReceipt0);
        insertReceiptToDB(db, testReceipt1);
        insertReceiptToDB(db, testReceipt2);
        insertReceiptToDB(db, testReceipt3);
        insertReceiptToDB(db, testReceipt4);
        insertReceiptToDB(db, testReceipt5);
        insertReceiptToDB(db, testReceipt6);
        insertReceiptToDB(db, testReceipt7);
        insertReceiptToDB(db, testReceipt8);
        insertReceiptToDB(db, testReceipt9);
    }

    //Method to insert a receipt into localdb for testing - if there is an items list on the receipt, add the each item to the db and the receipt-item connection
    private static void insertReceiptToDB(SQLiteDatabase db, Receipt r) {
        ContentValues receiptValues = new ContentValues();
        receiptValues.put(DBConstants.DATE, r.getDate());
        receiptValues.put(DBConstants.VENDORNAME, r.getVendorName());
        receiptValues.put(DBConstants.CARDTRANS, r.isCardTrans());
        receiptValues.put(DBConstants.RECEIPTTOTAL, r.getReceiptTotal());
        receiptValues.put(DBConstants.USERID, r.getUserId());
        db.insert(DBConstants.RECEIPTDATA, null, receiptValues);
        if (r.getItems() != null) {
            for (Item i : r.getItems()) {
                if(isItemInDB(db,i.getName())) {
                    insertReceiptItemToDB(db, r, i);
                }
                else{
                    insertItemToDB(db, i);
                    insertReceiptItemToDB(db, r, i);
                }
            }
        }
    }
    //Method to add item to localdb
    private static void insertItemToDB(SQLiteDatabase db, Item i){
        ContentValues itemValues = new ContentValues();
        itemValues.put(DBConstants.ITEMNAME, i.getName());
        db.insert(DBConstants.ITEMDATA, null, itemValues);
    }
    //Method to add receiptItem connection and information to localdb
    private static void insertReceiptItemToDB(SQLiteDatabase db, Receipt r, Item i){
        ContentValues receiptItemValues = new ContentValues();
        receiptItemValues.put(DBConstants.RECEIPT_ID, r.getId());
        receiptItemValues.put(DBConstants.ITEM_ID, i.getId());
        receiptItemValues.put(DBConstants.PRICE, i.getPrice());
        receiptItemValues.put(DBConstants.QUANTITY, i.getQuantity());
        db.insert(DBConstants.RECEIPTITEMDATA, null, receiptItemValues);
    }
    //Method to check if item already in database database
    private static boolean isItemInDB(SQLiteDatabase db, String itemName){
        String query = SQLiteQueries.checkItemNameExistString(itemName);
        Cursor mCount= db.rawQuery(query, null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        if (count == 0) {
            return false;
        } else {
            return true;
        }
    }
}
