package com.example.pop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pop.R;
import com.example.pop.model.User;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

import java.security.Key;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RegisterActivity extends AppCompatActivity {

    private SQLiteDatabaseAdapter db;
    private EditText username;
    private EditText email;
    private EditText pass;
    private EditText confirmPass;
    private TextView loginLink;
    private Button registerBtn;

    private String EText;
    private String DText;

    private static final String ALGORITHM = "AES";
    private static final String KEY = "F0C101355CD00EF098BD78C3D85E141C";

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
                String usernameText = username.getText().toString();
                String emailText = email.getText().toString();
                String passText = pass.getText().toString();
                String confirmPassText = confirmPass.getText().toString();

                boolean validUsername = checkUsername(usernameText);
                boolean validEmail = checkEmail(emailText);
                boolean validPassword = checkPassword(passText, confirmPassText);

                if(validUsername && validEmail && validPassword) {
                    User user = new User(usernameText, emailText, passText);
                    db.addUserHandler(user);

                    //Need to pull user information to populate session

                    Session session = new Session(getApplicationContext());
                    session.setLogin("Login");
                    //session.setUserId(userId);
                    session.setName(usernameText);
                    session.setEmail(emailText);

                    //Intent intent = new Intent(RegisterActivity.this, FragmentHolder.class);
                    //startActivity(intent);

                    try {
                        EText = encrypt(passText);
                        DText = decrypt(EText);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println(passText);
                    System.out.println("Encrypt: " + EText);
                    System.out.println("Decrypt: " + DText);
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

    private boolean checkUsername(String username) {

        //Check if username is taken
        if(db.checkUsernameExist(username)){
            Toast toast = Toast.makeText(getApplicationContext(), "Username already taken!", Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
        else{
            return true;
        }
    }

    private boolean checkEmail(String email) {

        //Check if email is taken
        if(db.checkEmailExist(email)) {
            Toast toast = Toast.makeText(getApplicationContext(), "Email already taken!", Toast.LENGTH_LONG);
            toast.show();
            return false;
        }
        else {
            return true;
        }
    }

    private boolean checkPassword(String pass, String confirmPass) {

        Pattern passwordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$");

        if(!(passwordPattern.matcher(pass).matches())) {
            Toast toast = Toast.makeText(getApplicationContext(), "Password is too weak \n Requires: lowercase, uppercase and to be at least 8 chars long", Toast.LENGTH_LONG);
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

    public static String encrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
        return encryptedValue64;
    }

    public static String decrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.decode(value, Base64.DEFAULT);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        String decryptedValue = new String(decryptedByteValue,"utf-8");
        return decryptedValue;
    }

    private static Key generateKey()
    {
        Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        return key;
    }
}
