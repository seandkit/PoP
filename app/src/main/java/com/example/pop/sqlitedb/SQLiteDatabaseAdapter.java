package com.example.pop.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pop.Constants;
import com.example.pop.model.Receipt;
import com.example.pop.model.User;

import java.util.LinkedList;

public class SQLiteDatabaseAdapter {

    SQLiteDatabaseHelper dbHelper;
    public SQLiteDatabaseAdapter(Context context)
    {
        dbHelper = new SQLiteDatabaseHelper(context);
    }

    public void addUserHandler(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.USERNAME, user.getName());
        values.put(Constants.EMAIL, user.getEmail());
        values.put(Constants.PASSWORD, user.getPassword());
        db.insert(Constants.USERDATA, null, values);
    }


    public User findAccountHandler(String email, String password){
        String query = "Select "+Constants.USERNAME +", "+Constants.EMAIL +", " +Constants.PASSWORD +" FROM "
                +Constants.USERDATA + " WHERE "
                +Constants.EMAIL + " = " + "'" + email + "'"
                + " AND " + Constants.PASSWORD + " = '" + password + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        User user = new User();
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            user.setName(cursor.getString(0));
            user.setEmail(cursor.getString(1));
            user.setPassword(cursor.getString(2));

        } else {
            user = null;
        }
        return user;
    }

    public boolean checkUsernameExist(String username){
        String query = "Select count(*) FROM "
                +Constants.USERDATA + " WHERE "
                +Constants.USERNAME + " = '" + username + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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

    public boolean checkEmailExist(String email){
        String query = "Select count(*) FROM "
                +Constants.USERDATA + " WHERE "
                +Constants.EMAIL + " = '" + email + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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

    public Receipt findReceiptById(int id){
        String query = "Select * FROM " + Constants.RECEIPTDATA
                + "WHERE " + Constants.ID + " = " + id;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        Receipt foundReceipt = new Receipt();
        foundReceipt.setId(cursor.getInt(0));
        foundReceipt.setDate(cursor.getString(1));
        foundReceipt.setVendorName(cursor.getString(2));
        foundReceipt.setCardTrans(cursor.getInt(3));
        foundReceipt.setReceiptTotal(cursor.getDouble(4));
        cursor.close();
        return foundReceipt;
    }

    public LinkedList<String[]> findAllReceiptsForDisplayOnRecentTransaction(int id){
        String query = "Select "+Constants.DATE +", "
                +Constants.VENDORNAME +", "
                +Constants.RECEIPTTOTAL
                +" FROM " +Constants.RECEIPTDATA
                +" WHERE " +Constants.USERID+ " = "+id;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        LinkedList<String[]> receiptList = new LinkedList<>();
        if(cursor != null) {
            cursor.moveToNext();
            for (int i = 0; i < cursor.getCount(); i++) {
                String[] receipts = {cursor.getString(0), cursor.getString(1), String.valueOf(cursor.getFloat(2))};
                receiptList.add(receipts);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return receiptList;
    }

    static class SQLiteDatabaseHelper  extends SQLiteOpenHelper{

        private Context context;

        public SQLiteDatabaseHelper(Context context) {
            super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION7);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            User testUser = new User(1, "admin", "D00191063@student.dkit.ie", "Password!1");
            Receipt testReceipt1 = new Receipt("2019-10-03","Tesco", 1, 103.59,1);
            Receipt testReceipt2 = new Receipt("2019-10-11","Tesco", 0, 4.08,1);
            Receipt testReceipt3 = new Receipt("2019-10-15","Tesco", 1, 55.55,1);
            Receipt testReceipt4 = new Receipt("2019-10-20","Tesco", 0, 23.00,1);
            Receipt testReceipt5 = new Receipt("2019-10-24","Tesco", 1, 77.531,1);
            Receipt testReceipt6 = new Receipt("2019-10-29","Tesco", 1, 60.311,1);
            Receipt testReceipt7 = new Receipt("2019-11-01","Tesco", 0, 12.961,1);
            Receipt testReceipt8 = new Receipt("2019-11-03","Tesco", 1, 33.33,1);
            Receipt testReceipt9 = new Receipt("2019-11-07","Tesco", 0, 201.68,1);
            db.execSQL("CREATE TABLE " + Constants.USERDATA + " ( "
                    + Constants.ID + " INTEGER PRIMARY KEY, "
                    + Constants.USERNAME + " TEXT,"
                    + Constants.EMAIL + " TEXT, "
                    + Constants.PASSWORD + " TEXT)");
            ContentValues userValues = new ContentValues();
            userValues.put(Constants.USERNAME, testUser.getName());
            userValues.put(Constants.EMAIL, testUser.getEmail());
            userValues.put(Constants.PASSWORD, testUser.getPassword());
            db.insert(Constants.USERDATA, null, userValues);
            db.execSQL("CREATE TABLE " + Constants.RECEIPTDATA + " ( "
                    + Constants.ID + " INTEGER PRIMARY KEY, "
                    + Constants.DATE + " TEXT,"
                    + Constants.VENDORNAME + " TEXT, "
                    + Constants.CARDTRANS + " INTEGER,"
                    + Constants.RECEIPTTOTAL + " REAL,"
                    + Constants.USERID + " INTEGER NOT NULL REFERENCES " + Constants.USERDATA + "(" + Constants.ID + "))");

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

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Constants.USERDATA);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.RECEIPTDATA);
            onCreate(db);
        }

        private void insertReceiptToDB(SQLiteDatabase db, Receipt r){
            ContentValues receiptValues = new ContentValues();
            receiptValues.put(Constants.DATE, r.getDate());
            receiptValues.put(Constants.VENDORNAME, r.getVendorName());
            receiptValues.put(Constants.CARDTRANS, r.isCardTrans());
            receiptValues.put(Constants.RECEIPTTOTAL, r.getReceiptTotal());
            receiptValues.put(Constants.USERID, r.getUserId());
            db.insert(Constants.RECEIPTDATA, null, receiptValues);
        }
    }
}
