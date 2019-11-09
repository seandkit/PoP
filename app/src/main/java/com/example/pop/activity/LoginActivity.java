package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pop.R;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

public class LoginActivity extends AppCompatActivity {

    private SQLiteDatabaseAdapter db;
    private EditText email;
    private EditText password;
    private TextView registerLink;
    private TextView errorMsg;
    private TextView attempts;
    private Button loginBtn;
    private int count = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new SQLiteDatabaseAdapter(this);
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.regLink);
        errorMsg = findViewById(R.id.loginErrorMsg);
        attempts = findViewById(R.id.attempts);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean loginDetailsMatch = validateMatch(email.getText().toString(), password.getText().toString());
                if(loginDetailsMatch){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }

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

    private boolean validateMatch(String email, String userPassword) {
        try {
            if (!db.findAccountHandler(email, userPassword).equals(null)) {
                return true;
            } else {
                failedAttempt();
                return false;
            }
        }catch(NullPointerException e){
            failedAttempt();
            return false;
        }
    }

    private void failedAttempt() {
        count--;

        errorMsg.setText("Wrong email or password!");
        attempts.setText("No of attempts remaining: " + count);

        if(count == 0)
        {
            loginBtn.setEnabled(false);
        }
    }
}
