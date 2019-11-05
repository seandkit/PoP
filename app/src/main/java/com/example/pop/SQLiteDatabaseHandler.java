package com.example.pop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pop.model.User;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "PoP.db";

    private static final String TABLE_NAME = "userdata";

    private static final String COLUMN_USERID = "userid";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_USEREMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    public SQLiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        User testUser =  new User("admin","D00191063@student.dkit.ie","Password!1");
        db.execSQL("CREATE TABLE "+TABLE_NAME+" ( "+COLUMN_USERID+" INTEGER PRIMARY KEY, "+COLUMN_USERNAME+" TEXT,"+COLUMN_USEREMAIL+" TEXT, "+COLUMN_PASSWORD+" TEXT)");
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, testUser.getName());
        values.put(COLUMN_USEREMAIL, testUser.getEmail());
        values.put(COLUMN_PASSWORD, testUser.getPassword());
        db.insert(TABLE_NAME, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME);
        onCreate(db);
    }

    public void addUserHandler(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getName());
        values.put(COLUMN_USEREMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public User findAccountHandler(String email, String password){
        String query = "Select "+COLUMN_USERNAME+", "+COLUMN_USEREMAIL+", " +COLUMN_PASSWORD+" FROM " + TABLE_NAME + " WHERE " + COLUMN_USEREMAIL + " = " + "'" + email + "'" + " AND " + COLUMN_PASSWORD + " = '" + password + "'";
        SQLiteDatabase db = this.getReadableDatabase();
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
        db.close();
        return user;
    }
}
