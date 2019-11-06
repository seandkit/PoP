package com.example.pop.handler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pop.Constants;
import com.example.pop.model.User;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    public SQLiteDatabaseHandler(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        User testUser =  new User("admin","D00191063@student.dkit.ie","Password!1");
        db.execSQL("CREATE TABLE "+Constants.USERDATA +" ( "+Constants.USERID +" INTEGER PRIMARY KEY, "+Constants.USERNAME +" TEXT,"+Constants.EMAIL +" TEXT, "+Constants.PASSWORD +" TEXT)");
        ContentValues values = new ContentValues();
        values.put(Constants.USERNAME, testUser.getName());
        values.put(Constants.EMAIL, testUser.getEmail());
        values.put(Constants.PASSWORD, testUser.getPassword());
        db.insert(Constants.USERDATA, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " +Constants.USERDATA);
        onCreate(db);
    }

    public void addUserHandler(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.USERNAME, user.getName());
        values.put(Constants.EMAIL, user.getEmail());
        values.put(Constants.PASSWORD, user.getPassword());
        db.insert(Constants.USERDATA, null, values);
        db.close();
    }

    public User findAccountHandler(String email, String password){
        String query = "Select "+Constants.USERNAME +", "+Constants.EMAIL +", " +Constants.PASSWORD +" FROM " + Constants.USERDATA + " WHERE " + Constants.EMAIL + " = " + "'" + email + "'" + " AND " + Constants.PASSWORD + " = '" + password + "'";
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

    public boolean checkUsernameExist(String username){
        String query = "Select count(*) FROM " + Constants.USERDATA + " WHERE " + Constants.USERNAME + " = '" + username + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCount= db.rawQuery(query, null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        if (count == 0) {
            db.close();
            return false;
        } else {
            db.close();
            return true;
        }
    }

    public boolean checkEmailExist(String email){
        String query = "Select count(*) FROM " + Constants.USERDATA + " WHERE " + Constants.EMAIL + " = '" + email + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCount= db.rawQuery(query, null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        if (count == 0) {
            db.close();
            return false;
        } else {
            db.close();
            return true;
        }
    }
}
