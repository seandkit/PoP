package com.example.pop.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pop.DBConstants;
import com.example.pop.model.Receipt;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabaseAdapter {

    SQLiteDatabaseHelper dbHelper;
    public SQLiteDatabaseAdapter(Context context)
    {
        dbHelper = new SQLiteDatabaseHelper(context);
    }

    public void addUnlinkedReceipt(Receipt receipt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", receipt.getDate());
        values.put("total", receipt.getReceiptTotal());
        values.put("vendor", receipt.getVendorName());
        values.put("uuid", receipt.getUuid());

        db.insert("unlinkedreceipts", null, values);
    }

    public List<Receipt> getUnlinkedReceipts() {
        String query = SQLiteQueries.getUnlinkedReceiptString();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<Receipt> receipts = new ArrayList<>();

        if(cursor != null){
            cursor.moveToNext();
            for(int i = 0; i < cursor.getCount(); i++){
                Receipt receipt = new Receipt(cursor.getString(1), cursor.getString(2),
                        cursor.getDouble(3), cursor.getInt(4),cursor.getString(5));
                receipts.add(receipt);
                cursor.moveToNext();
            }
            return receipts;
        }
        else{
            return null;
        }
    }

    public boolean dropUnlinkedReceipt(String uuid){
        System.out.println("DB = " + uuid);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int delCount = db.delete(DBConstants.UNLINKEDRECEIPTS, "uuid = ?", new String[] {uuid});

        System.out.println(delCount);
        return false;
    }

    static class SQLiteDatabaseHelper  extends SQLiteOpenHelper{

        private Context context;

        public SQLiteDatabaseHelper(Context context) {
            super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQLiteQueries.createUnlinkedReceiptsTableString());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS '" + DBConstants.RECEIPTDATA + "'");
            db.execSQL("DROP TABLE IF EXISTS '" + DBConstants.ITEMDATA + "'");
            db.execSQL("DROP TABLE IF EXISTS '" + DBConstants.RECEIPTITEMDATA + "'");
            db.execSQL("DROP TABLE IF EXISTS '" + DBConstants.USERDATA + "'");
            db.execSQL("DROP TABLE IF EXISTS '" + DBConstants.UNLINKEDRECEIPTS + "'");
            onCreate(db);
        }
    }
}
