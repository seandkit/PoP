package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.activity.FragmentHolder;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.helper.Utils;
import com.example.pop.model.Folder;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FetchFoldersAsyncTask extends AsyncTask<String, String, String> {

    private Activity mAcc;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private NavigationView mNavigationView;

    public FetchFoldersAsyncTask(Activity acc, NavigationView navigationView, Context context){
        mAcc = acc;
        mNavigationView = navigationView;
        mContext = context;
    }

    private Session session;
    private ProgressDialog pDialog;

    private int success;
    private String message;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        session = new Session(mContext);

        pDialog = new ProgressDialog(mAcc);
        pDialog.setMessage("Getting Folders. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put(DBConstants.USER_ID, String.valueOf(session.getUserId()));
        JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "fetchAllFolders.php", "POST", httpParams);

        try {
            success = jsonObject.getInt("success");
            JSONArray folders;
            if (success == 1) {
                folders = jsonObject.getJSONArray("data");
                //Iterate through the response and populate receipt list
                FragmentHolder.globalFolderList = new ArrayList<>();
                for (int i = 0; i < folders.length(); i++) {
                    JSONObject folder = folders.getJSONObject(i);
                    FragmentHolder.globalFolderList.add(new Folder(folder.getInt("folder_id"), folder.getString("folder_name")));
                }
            }
            else{
                message = jsonObject.getString("message");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {
        pDialog.dismiss();

        if (success == 1) {
            MenuItem myMoveGroupItem = FragmentHolder.navigationView.getMenu().getItem(1);
            SubMenu subMenu = myMoveGroupItem.getSubMenu();
            subMenu.clear();
            for(Folder folder: FragmentHolder.globalFolderList){
                Utils.addDrawerFolder(mNavigationView, mContext, folder.getId(), folder.getName());
            }
        }
    }
}
