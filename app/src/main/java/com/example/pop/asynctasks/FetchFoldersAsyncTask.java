package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
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

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private NavigationView mNavigationView;

    public FetchFoldersAsyncTask(NavigationView navigationView, Context context){
        mNavigationView = navigationView;
        mContext = context;
    }

    private Session session;

    private int success;
    private String message;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        session = new Session(mContext);
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
                FragmentHolder.folderList = new ArrayList<>();
                for (int i = 0; i < folders.length(); i++) {
                    JSONObject folder = folders.getJSONObject(i);
                    FragmentHolder.folderList.add(new Folder(folder.getInt("folder_id"), folder.getString("folder_name")));
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
        if (success == 0) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
        else{
            //If success update xml
            MenuItem myMoveGroupItem = FragmentHolder.navigationView.getMenu().getItem(1);
            SubMenu subMenu = myMoveGroupItem.getSubMenu();
            subMenu.clear();
            for(Folder folder: FragmentHolder.folderList){
                Utils.addDrawerFolder(mNavigationView, mContext, folder.getId(), folder.getName());
            }
        }
    }
}
