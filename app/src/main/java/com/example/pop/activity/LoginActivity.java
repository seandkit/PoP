package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pop.R;
import com.example.pop.model.User;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

public class LoginActivity extends AppCompatActivity {

    private SQLiteDatabaseAdapter db;
    private EditText email;
    private EditText password;
    private TextView registerLink;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Session session = new Session(getApplicationContext());
        System.out.println(session.getLogin());
        checkLogin(session.getLogin());

        db = new SQLiteDatabaseAdapter(this);
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.regLink);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean loginDetailsMatch = validateMatch(email.getText().toString(), password.getText().toString());
                if(loginDetailsMatch){
                    //Make call to the db for user details.
                    User user = new User();

                    Session session = new Session(getApplicationContext());
                    session.setLogin("Login");
                    session.setUserId(user.getId());
                    session.setName(user.getName());
                    session.setEmail(user.getEmail());

                    Intent intent = new Intent(LoginActivity.this, FragmentHolder.class);
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

    private boolean validateMatch(String username, String userPassword) {
        if (!db.findAccountHandler(username, userPassword).equals(null)) {
            return true;
        } else {
            return false;
        }
    }

    private void checkLogin(String login) {
        if(login.equals("Login")) {
            Intent i = new Intent(LoginActivity.this, FragmentHolder.class);
            startActivity(i);
        }
    }
}
