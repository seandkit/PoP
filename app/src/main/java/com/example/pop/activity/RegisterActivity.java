package com.example.pop.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HashingFunctions;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.helper.Session;
import com.example.pop.helper.Utils;
import com.example.pop.model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText fName;
    private EditText lName;
    private EditText email;
    private EditText pass;
    private EditText confirmPass;

    private ProgressDialog pDialog;
    private int success;
    private User user = new User();
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fName = findViewById(R.id.regFirstName);
        lName = findViewById(R.id.regLastName);
        email = findViewById(R.id.regEmail);
        pass = findViewById(R.id.regPassword);
        confirmPass = findViewById(R.id.regConfirmPassword);

        findViewById(R.id.regBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fName.getText().toString().length() == 0
                        || lName.getText().toString().length() == 0
                        || email.getText().toString().length() == 0
                        || pass.getText().toString().length() == 0
                        || confirmPass.getText().toString().length() == 0) {
                    Toast.makeText(RegisterActivity.this,"Some fields left empty", Toast.LENGTH_LONG).show();
                }
                else{
                    if(checkEmail(email.getText().toString())
                            && checkPassword(pass.getText().toString(), confirmPass.getText().toString())
                            && CheckNetworkStatus.isNetworkAvailable(getApplicationContext())
                            && !DBConstants.STRING_EMPTY.equals(email.getText().toString())) {
                        new createUserAsyncTask().execute();
                    } else {
                        Toast.makeText(RegisterActivity.this,"Unable to connect to internet", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        findViewById(R.id.loginLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean checkEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;

        if(pattern.matcher(email).matches() == true){
            return true;
        }
        else{
            Toast.makeText(RegisterActivity.this,"Invalid Email", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean checkPassword(String pass, String confirmPass) {
        Pattern passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$");

        if(!(passwordPattern.matcher(pass).matches())) {
            Toast.makeText(RegisterActivity.this,"Password is too weak\nRequires: uppercase, lowercase and to be at least 8 chars long", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!pass.equals(confirmPass)) {
            Toast.makeText(RegisterActivity.this,"Passwords don't match", Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            return true;
        }
    }

    private class createUserAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Creating Account, Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String encryptedPassword = HashingFunctions.encrypt(pass.getText().toString());

                HttpJsonParser httpJsonParser = new HttpJsonParser();
                Map<String, String> httpParams = new HashMap<>();
                httpParams.put(DBConstants.FIRST_NAME, fName.getText().toString());
                httpParams.put(DBConstants.LAST_NAME, lName.getText().toString());
                httpParams.put(DBConstants.EMAIL, email.getText().toString());
                httpParams.put(DBConstants.PASSWORD, encryptedPassword);
                JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL+"register.php", "POST", httpParams);

                success = jsonObject.getInt("success");
                if(success == 0) {
                    message = jsonObject.getString("message");
                }
                else {
                    JSONArray userObjects = jsonObject.getJSONArray("data");
                    JSONObject userObject = userObjects.getJSONObject(0);
                    user.setId(userObject.getInt("user_id"));
                    user.setFirstName(userObject.getString("first_name"));
                    user.setLastName(userObject.getString("last_name"));
                    user.setEmail(userObject.getString("email"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            if (success == 1) {
                Session session = new Session(getApplicationContext());
                session.setLogin("Login");
                session.setUserId(user.getId());
                session.setFirstName(user.getFirstName());
                session.setLastName(user.getLastName());
                session.setEmail(user.getEmail());

                Intent i = new Intent(RegisterActivity.this, FragmentHolder.class);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
