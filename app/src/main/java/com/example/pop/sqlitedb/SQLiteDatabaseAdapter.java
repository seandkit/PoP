package com.example.pop.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pop.DBConstants;
import com.example.pop.TEST;
import com.example.pop.model.Item;
import com.example.pop.model.Receipt;
import com.example.pop.model.User;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabaseAdapter {

    SQLiteDatabaseHelper dbHelper;
    public SQLiteDatabaseAdapter(Context context)
    {
        dbHelper = new SQLiteDatabaseHelper(context);
    }


    public void addUnlinkedReceipt(String uuid, String vendor, String date, String total) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uuid", uuid);
        values.put("vendor", vendor);
        values.put("date", date);
        values.put("total", total);
        db.insert("unlinkedReceipts", null, values);
    }

    public List<Receipt> getUnlinkedReceipts() {
        String query = SQLiteQueries.getUnlinkedReceiptsString();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<Receipt> receipts = new ArrayList<>();
        if(cursor != null){
            cursor.moveToNext();
            for(int i = 0; i < cursor.getCount(); i++){
                Receipt receipt = new Receipt(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3),cursor.getDouble(4));
                receipts.add(receipt);
                cursor.moveToNext();
            }
            return receipts;
        }
        else{
            return null;
        }
    }

    static class SQLiteDatabaseHelper  extends SQLiteOpenHelper{

        private Context context;

        public SQLiteDatabaseHelper(Context context) {
            super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION12);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQLiteQueries.createUnlinkedReceiptsTableString());

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.USERDATA + ";");
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.RECEIPTDATA+ ";");
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.ITEMDATA+ ";");
            db.execSQL("DROP TABLE IF EXISTS " + DBConstants.RECEIPTITEMDATA+ ";");
            db.execSQL("DROP TABLE IF EXISTS unlinkedReceipts;");
            onCreate(db);
        }
    }
}
