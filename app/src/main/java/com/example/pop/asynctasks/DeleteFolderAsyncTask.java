package com.example.pop.asynctasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.activity.FolderActivity;
import com.example.pop.helper.HttpJsonParser;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeleteFolderAsyncTask extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private int mFolderId;

    public DeleteFolderAsyncTask(Context context, int folderId){
        mContext = context;
        mFolderId = folderId;
    }

    int success;
    String message;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        HttpJsonParser httpJsonParser = new HttpJsonParser();
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("folder_id", String.valueOf(mFolderId));
        JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL + "deleteFolder.php", "POST", httpParams);

        try {
            success = jsonObject.getInt("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {
        if (success == 0) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }
}
