package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.activity.FolderActivity;
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

public class SetCurrentFolderAsyncTask extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private  NavigationView mNavigationView;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private int mFolderId;

    public SetCurrentFolderAsyncTask(NavigationView navigationView, Context context, int folderId){
        mNavigationView = navigationView;
        mContext = context;
        mFolderId = folderId;
    }

    Session session;

    int success;
    String message;

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
        httpParams.put("folder_id", String.valueOf(mFolderId));
        JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "assignCurrentFolder.php", "POST", httpParams);

        try {
            success = jsonObject.getInt("success");
            if (success == 1) {
                session.setCurrentFolder(String.valueOf(mFolderId));
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
            Toast.makeText(mContext, "Set To Current", Toast.LENGTH_LONG).show();

            MenuItem myMoveGroupItem = mNavigationView.getMenu().getItem(1);
            SubMenu subMenu = myMoveGroupItem.getSubMenu();
            subMenu.clear();
            for(Folder folder: FragmentHolder.folderList){
                Utils.addDrawerFolder(mNavigationView, mContext, folder.getId(), folder.getName());
            }
        }
    }
}
