package com.example.pop.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pop.DBConstants;
import com.example.pop.TEST;
import com.example.pop.model.Receipt;
import com.example.pop.model.User;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SQLiteDatabaseAdapter {

    SQLiteDatabaseHelper dbHelper;
    public SQLiteDatabaseAdapter(Context context)
    {
        dbHelper = new SQLiteDatabaseHelper(context);
    }

    public void addUserHandler(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBConstants.USERNAME, user.getName());
        values.put(DBConstants.EMAIL, user.getEmail());
        values.put(DBConstants.PASSWORD, user.getPassword());
        db.insert(DBConstants.USERDATA, null, values);
    }


    public User findAccountHandler(String email, String password){
        String query = "Select "+ DBConstants.USERNAME +", "+ DBConstants.EMAIL +", " + DBConstants.PASSWORD +" FROM "
                + DBConstants.USERDATA + " WHERE "
                + DBConstants.EMAIL + " = " + "'" + email + "'"
                + " AND " + DBConstants.PASSWORD + " = '" + password + "'";
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
                + DBConstants.USERDATA + " WHERE "
                + DBConstants.USERNAME + " = '" + username + "'";
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
                + DBConstants.USERDATA + " WHERE "
                + DBConstants.EMAIL + " = '" + email + "'";
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
        String query = "Select * FROM " + DBConstants.RECEIPTDATA
                + "WHERE " + DBConstants.ID + " = " + id;
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

    public List<Receipt> findAllReceiptsForDisplayOnRecentTransaction(int id){
        String query = "Select * FROM " + DBConstants.RECEIPTDATA
                +" WHERE " + DBConstants.USERID+ " = "+id;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<Receipt> receiptList = new ArrayList<>();
        if(cursor != null) {
            cursor.moveToNext();
            for (int i = 0; i < cursor.getCount(); i++) {
                Receipt receipt = new Receipt(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                                              cursor.getInt(3), cursor.getFloat(4), cursor.getInt(5));
                receiptList.add(receipt);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return receiptList;
    }

    static class SQLiteDatabaseHelper  extends SQLiteOpenHelper{

        private Context context;

        public SQLiteDatabaseHelper(Context context) {
            super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION12);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            User testUser = new User(1, "admin", "D00191063@student.dkit.ie", "Password!1");
            db.execSQL("CREATE TABLE " + DBConstants.USERDATA + " ( "
                    + DBConstants.ID + " INTEGER PRIMARY KEY, "
                    + DBConstants.USERNAME + " TEXT,"
                    + DBConstants.EMAIL + " TEXT, "
                    + DBConstants.PASSWORD + " TEXT)");
            ContentValues userValues = new ContentValues();
            userValues.put(DBConstants.USERNAME, testUser.getName());
            userValues.put(DBConstants.EMAIL, testUser.getEmail());
            userValues.put(DBConstants.PASSWORD, testUser.getPassword());
            db.insert(DBConstants.USERDATA, null, userValues);
            db.execSQL("CREATE TABLE " + DBConstants.RECEIPTDATA + " ( "
                    + DBConstants.ID + " INTEGER PRIMARY KEY, "
                    + DBConstants.DATE + " TEXT,"
                    + DBConstants.VENDORNAME + " TEXT, "
                    + DBConstants.CARDTRANS + " INTEGER,"
                    + DBConstants.RECEIPTTOTAL + " REAL,"
                    + DBConstants.USERID + " INTEGER NOT NULL REFERENCES " + DBConstants.USERDATA + "(" + DBConstants.ID + "))");
            db.execSQL("CREATE TABLE "+ DBConstants.ITEMDATA + " ("
                    + DBConstants.ID + " INTEGER PRIMARY KEY, "
                    + DBConstants.ITEMNAME + " TEXT)");
            db.execSQL("CREATE TABLE " + DBConstants.RECEIPTITEMDATA + "( "
                    + DBConstants.RECEIPTITEMPK + " INTEGER PRIMARY KEY, "
                    + DBConstants.RECEIPTID + " INTEGER NOT NULL REFERENCES " + DBConstants.RECEIPTDATA + "('" + DBConstants.ID +"'), "
                    + DBConstants.ITEMID + " INTEGER NOT NULL REFERENCES " + DBConstants.ITEMDATA + "('" + DBConstants.ID +"'), "
                    + DBConstants.PRICE + " INTEGER, "
                    + DBConstants.QUANTITY+ " INTEGER)");
            TEST.sampleTestReceipts(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.USERDATA + ";");
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.RECEIPTDATA+ ";");
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.ITEMDATA+ ";");
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.RECEIPTITEMDATA+ ";");
            onCreate(db);
        }
    }
}
