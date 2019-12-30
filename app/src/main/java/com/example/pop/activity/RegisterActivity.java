package com.example.pop.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.User;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private SQLiteDatabaseAdapter db;
    private EditText username;
    private EditText email;
    private EditText pass;
    private EditText confirmPass;
    private TextView loginLink;
    private TextView errorMsg;
    private Button registerBtn;

    private ProgressDialog pDialog;
    private int success;
    private User user = new User();
    private String message;
    private String lastName = "lastname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new SQLiteDatabaseAdapter(this);
        username = findViewById(R.id.regUsername);
        email = findViewById(R.id.regEmail);
        pass = findViewById(R.id.regPassword);
        confirmPass = findViewById(R.id.regConfirmPassword);
        loginLink = findViewById(R.id.loginLink);
        //errorMsg = findViewById(R.id.regErrorMsg);
        registerBtn = findViewById(R.id.regBtn);

        username.addTextChangedListener(inputWatcher);
        email.addTextChangedListener(inputWatcher);
        pass.addTextChangedListener(inputWatcher);
        confirmPass.addTextChangedListener(inputWatcher);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPassword(pass.getText().toString(), confirmPass.getText().toString())) {
                    if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                        createUser();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Unable to connect to internet",
                                Toast.LENGTH_LONG).show();
                    }
                } else{
                    Toast.makeText(RegisterActivity.this,
                            "Bad password match",
                            Toast.LENGTH_LONG).show();
                }
        //        boolean validEmail = checkEmail(email.getText().toString());
        //        boolean validPassword = checkPassword(pass.getText().toString(), confirmPass.getText().toString());

        //        if(validEmail && validPassword) {
        //            addValidUser(username.getText().toString(), email.getText().toString(),confirmPass.getText().toString());
        //            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        //            startActivity(intent);
        //        }
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private final TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) {
            if (username.getText().toString().length() == 0 || email.getText().toString().length() == 0 ||
                    pass.getText().toString().length() == 0 || confirmPass.getText().toString().length() == 0) {
                registerBtn.setEnabled(false);
            } else {
                registerBtn.setEnabled(true);
            }
        }
    };

    private void createUser() {
        if(!DBConstants.STRING_EMPTY.equals(email.getText().toString())){
            new createUserAsyncTask().execute();
        }
        else {
            Toast.makeText(RegisterActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();

        }
    }

//    private boolean checkEmail(String email) {
//
//        //Check if email is taken
//        if(db.checkEmailExist(email)) {
//            Toast toast = Toast.makeText(getApplicationContext(), "Email already taken!", Toast.LENGTH_LONG);
//            toast.show();
//            return false;
//        }
//        else {
//            return true;
//        }
//    }

    private boolean checkPassword(String pass, String confirmPass) {

        Pattern passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$");

        if(!(passwordPattern.matcher(pass).matches())) {
            Toast toast = Toast.makeText(getApplicationContext(), "Password is too weak \n Requires: lowercase, uppercase, number and special char", Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
        else if(!pass.equals(confirmPass)) {
            System.out.println("Pass: " + pass);
            System.out.println("Confirm Pass: " + confirmPass);

            Toast toast = Toast.makeText(getApplicationContext(), "Passwords don't match!", Toast.LENGTH_LONG);
            toast.show();
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
            //Display proggress bar
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Checking Database. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(DBConstants.FIRST_NAME, username.getText().toString());
            httpParams.put(DBConstants.LAST_NAME, lastName);
            httpParams.put(DBConstants.EMAIL, email.getText().toString());
            httpParams.put(DBConstants.PASSWORD, pass.getText().toString());
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL+"addUser.php", "POST", httpParams);
            try {
                success = jsonObject.getInt("success");
                if(success == 0){
                    message = jsonObject.getString("message");
                }
                else{
                    JSONArray userObjects = jsonObject.getJSONArray("data");
                    JSONObject userObject = userObjects.getJSONObject(0);
                    user.setId(userObject.getInt("user_id"));
                    user.setFirstName(userObject.getString("first_name"));
                    user.setLastName(userObject.getString("last_name"));
                    user.setEmail(userObject.getString("email"));
                }
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
                        System.out.println("User added");
                        System.out.println("id:"+user.getId()+" fname:" +user.getFirstName()+" lname:"+user.getLastName() + " email:" + user.getEmail());

                        //Finish ths activity and go back to listing activity
                        finish();

                    } else {
                        Toast.makeText(RegisterActivity.this,
                                message,
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }

//    private void addValidUser(String username, String email, String password){
//        User user = new User(username, email, password);
//        db.addUserHandler(user);
//    }
    public int getUserID(){
        return user.getId();
    }
}
