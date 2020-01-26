package com.example.pop.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.pop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class receiptTransaction extends AppCompatActivity {

    FrameLayout frameLayout;
    BottomNavigationView navigation;

    //fragments:
    private Fragment_Receipt receiptFragment;
    private Fragment_SearchByDate searchFragment;
    private Data dataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_transaction2);
        navigation = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frameLayout);

        //Fragment initialization
        receiptFragment = new Fragment_Receipt();
        searchFragment = new Fragment_SearchByDate();
        dataFragment = new Data();


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
                        InitializeFragment(dataFragment);
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
}
