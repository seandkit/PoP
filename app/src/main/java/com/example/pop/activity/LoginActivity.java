package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static String STRING_EMPTY = "";

    private SQLiteDatabaseAdapter db;
    private EditText email;
    private EditText password;
    private TextView registerLink;
    private TextView errorMsg;
    private TextView attempts;
    private Button loginBtn;
    private int count = 4;

    private ProgressDialog pDialog;
    private int success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new SQLiteDatabaseAdapter(this);
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.regLink);
        //errorMsg = findViewById(R.id.loginErrorMsg);
        //attempts = findViewById(R.id.attempts);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    validateMatch();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();
                }
                //boolean loginDetailsMatch = validateMatch();
                //if(loginDetailsMatch){
                //   Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                //    startActivity(intent);
                //}

            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void validateMatch() {
        if(!STRING_EMPTY.equals(email.getText().toString())&&
                !STRING_EMPTY.equals(password.getText().toString())){
            new validateMatchAsyncTask().execute();
        }
        else {
            Toast.makeText(LoginActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();

        }
    }

    private void failedAttempt() {
        count--;

        //errorMsg.setText("Wrong email or password!");
        //attempts.setText("No of attempts remaining: " + count);

        if(count == 0)
        {
            loginBtn.setEnabled(false);
        }
    }
    private class validateMatchAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display proggress bar
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Checking Database. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DBConstants.EMAIL, email.getText().toString());
            httpParams.put(DBConstants.PASSWORD, password.getText().toString());
            JSONObject jsonObject = httpJsonParser.makeHttpRequest("http://10.108.159.16/login2.php", "POST", httpParams);
            try {
                success = jsonObject.getInt("success");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(LoginActivity.this,
                                "Movie Added", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);

                        //Finish ths activity and go back to listing activity
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this,
                                "No Match",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}
