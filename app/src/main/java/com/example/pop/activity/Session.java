package com.example.pop.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Session {
    private SharedPreferences preferences;

    public Session(Context context){
         preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //Getters
    public String getLogin(){ return preferences.getString("loginKey", ""); }
    public int getUserId(){ return preferences.getInt("userIdKey", 0); }
    public String getName(){ return preferences.getString("nameKey", ""); }
    public String getEmail(){ return preferences.getString("emailKey", ""); }
    public int getChosenReceiptId(){ return preferences.getInt("receiptIdKey", 0); }

    //Setters
    public void setLogin(String login){
        preferences.edit().putString("loginKey", login).apply();
    }
    public void setUserId(int userId){ preferences.edit().putInt("userIdKey", userId).apply(); }
    public void setName(String name){ preferences.edit().putString("nameKey", name).apply(); }
    public void setEmail(String email){ preferences.edit().putString("emailKey", email).apply(); }
    public void setChosenReceiptId(int receiptId){ preferences.edit().putInt("receiptIdKey", receiptId).apply(); }
}
