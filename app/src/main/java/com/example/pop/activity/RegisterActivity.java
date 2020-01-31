package com.example.pop.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
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

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RegisterActivity extends AppCompatActivity {

    private SQLiteDatabaseAdapter db;
    private EditText name;
    private EditText email;
    private EditText pass;
    private EditText confirmPass;
    private TextView loginLink;
    private Button registerBtn;

    private int success;
    private User user = new User();
    private String message;
    private String lastName = "lastname";

    private static final String ALGORITHM = "AES";
    private static final String KEY = "F0C101355CD00EF098BD78C3D85E141C";

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new SQLiteDatabaseAdapter(this);
        name = findViewById(R.id.regName);
        email = findViewById(R.id.regEmail);
        pass = findViewById(R.id.regPassword);
        confirmPass = findViewById(R.id.regConfirmPassword);
        loginLink = findViewById(R.id.loginLink);
        registerBtn = findViewById(R.id.regBtn);

        context = this;

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().length() == 0 || email.getText().toString().length() == 0 ||
                        pass.getText().toString().length() == 0 || confirmPass.getText().toString().length() == 0) {
                    Toast.makeText(RegisterActivity.this,"Some fields left empty", Toast.LENGTH_LONG).show();
                }
                else{
                    if(checkEmail(email.getText().toString())) {
                        if(checkPassword(pass.getText().toString(), confirmPass.getText().toString())){
                            if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                                createUser();
                            } else {
                                Toast.makeText(RegisterActivity.this,"Unable to connect to internet", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
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

    private void createUser() {
        if(!DBConstants.STRING_EMPTY.equals(email.getText().toString())){
            new createUserAsyncTask().execute();
        }
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
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String encryptedPassword = encrypt(pass.getText().toString());

                HttpJsonParser httpJsonParser = new HttpJsonParser();
                Map<String, String> httpParams = new HashMap<>();
                httpParams.put(DBConstants.FIRST_NAME, name.getText().toString());
                httpParams.put(DBConstants.LAST_NAME, lastName);
                httpParams.put(DBConstants.EMAIL, email.getText().toString());
                httpParams.put(DBConstants.PASSWORD, encryptedPassword);
                JSONObject jsonObject = httpJsonParser.makeHttpRequest(DBConstants.BASE_URL+"register.php", "POST", httpParams);

                try {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        Session session = new Session(getApplicationContext());
                        session.setLogin("Login");
                        session.setUserId(user.getId());
                        session.setName(user.getFirstName());
                        session.setEmail(user.getEmail());

                        Intent i = new Intent(RegisterActivity.this, FragmentHolder.class);
                        startActivity(i);

                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public static String encrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
        return encryptedValue64;
    }

    public static String decrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.decode(value, Base64.DEFAULT);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        String decryptedValue = new String(decryptedByteValue,"utf-8");
        return decryptedValue;
    }

    private static Key generateKey() {
        Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        return key;
    }
}
