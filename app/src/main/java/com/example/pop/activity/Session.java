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
    public String getFirstName(){ return preferences.getString("firstNameKey", ""); }
    public String getLastName(){ return preferences.getString("lastNameKey", ""); }
    public String getEmail(){ return preferences.getString("emailKey", ""); }
    public String getCurrentFolder(){ return preferences.getString("currentFolderKey", ""); }

    //Setters
    public void setLogin(String login){
        preferences.edit().putString("loginKey", login).apply();
    }
    public void setUserId(int userId){ preferences.edit().putInt("userIdKey", userId).apply(); }
    public void setFirstName(String fName){ preferences.edit().putString("firstNameKey", fName).apply(); }
    public void setLastName(String lName){ preferences.edit().putString("lastNameKey", lName).apply(); }
    public void setEmail(String email){ preferences.edit().putString("emailKey", email).apply(); }
    public void setCurrentFolder(String currentFolder){ preferences.edit().putString("currentFolderKey", currentFolder).apply(); }
}
