package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.pop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Receipt_main_activity extends AppCompatActivity {

    FrameLayout frameLayout;
    BottomNavigationView navigation;

    //fragments:
    private ReceiptFragment receiptFragment;
    private Search searchFragment;
    private SearchByTag tagFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_main_activity);
        navigation = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        //Fragment initialization
        receiptFragment = new ReceiptFragment();
        searchFragment = new Search();
        tagFragment = new SearchByTag();


        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //switch to fragment

                switch(menuItem.getItemId()){
                    case R.id.nav_home :
                        //Code to be executed when item 1 selected.
                        InitializeFragment(receiptFragment);
                        return true;

                    case R.id.nav_search :
                        //Code to be executed when item 1 selected.
                        InitializeFragment(searchFragment);
                        return true;

                    case R.id.nav_data :
                        //Code to be executed when item 1 selected.
                        InitializeFragment(tagFragment);
                        return true;

                }
                return false;
            }

        });
    }

    private void InitializeFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }
}
