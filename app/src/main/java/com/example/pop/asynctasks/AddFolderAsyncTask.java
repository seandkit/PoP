package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.activity.FragmentHolder;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.helper.Utils;
import com.example.pop.model.Folder;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddFolderAsyncTask extends AsyncTask<String, String, String> {

    private Activity mAcc;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private NavigationView mNavigationView;
    private String mNewFolderName;

    public AddFolderAsyncTask(Activity acc, NavigationView navigationView, String newFolderName){
        mAcc = acc;
        mNavigationView = navigationView;
        mContext = acc.getApplicationContext();
        mNewFolderName = newFolderName;
    }

    private Session session;
    private ProgressDialog pDialog;

    private int mNewFolderId;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        session = new Session(mContext);

        pDialog = new ProgressDialog(mAcc);
        pDialog.setMessage("Creating Folder. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("folder_name", mNewFolderName);
        httpParams.put("user_id", String.valueOf(session.getUserId()));

        JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "addFolder.php", "POST", httpParams);

        try {
            int success = jsonObject.getInt("success");
            if (success == 1) {
                mNewFolderId = jsonObject.getInt("data");
                FragmentHolder.globalFolderList.add(new Folder(mNewFolderId, mNewFolderName));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {
        Utils.addDrawerFolder(mNavigationView, mContext, mNewFolderId, mNewFolderName);
        pDialog.dismiss();
        Toast.makeText(mContext,"Folder created",Toast.LENGTH_LONG).show();
    }
}
