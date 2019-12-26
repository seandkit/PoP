package com.example.pop.activity;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.R;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.model.Receipt;
import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Search extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private TextView mDisplayDateFrom;
    private TextView mDisplayDateTo;
    private DatePickerDialog.OnDateSetListener mDateSetListenerFrom;
    private DatePickerDialog.OnDateSetListener mDateSetListenerTo;
    private Button btn_export;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public Search() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        drawer = v.findViewById(R.id.drawer_layout);
        NavigationView navigationView = v.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener((toggle));
        toggle.syncState();

        ArrayList<Receipt> receiptArrayList = new ArrayList<>(10);
        receiptArrayList.add(new Receipt(1, "24/12/2019", "Tesco", 1, 54.99, 1));
        receiptArrayList.add(new Receipt(2, "24/12/2019", "Argos", 12, 24.99, 1));
        receiptArrayList.add(new Receipt(3, "24/12/2019", "Dunnes", 17, 154.99, 1));
        receiptArrayList.add(new Receipt(4, "24/12/2019", "Argos", 19, 20.05, 1));
        receiptArrayList.add(new Receipt(5, "24/12/2019", "Argos", 22, 19.99, 1));
        receiptArrayList.add(new Receipt(6, "24/12/2019", "Dunnes", 25, 4.99, 1));
        receiptArrayList.add(new Receipt(7, "24/12/2019", "Tesco", 45, 104.99, 1));
        receiptArrayList.add(new Receipt(8, "24/12/2019", "Tesco", 60, 2054.99, 1));
        receiptArrayList.add(new Receipt(9, "24/12/2019", "Tesco", 61, 1254.99, 1));



        mRecyclerView = v.findViewById(R.id.receiptList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new ReceiptListAdapter(getContext(), receiptArrayList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mDisplayDateFrom = (TextView) v.findViewById(R.id.tvDateFrom);
        mDisplayDateTo = (TextView) v.findViewById(R.id.tvDateTo);
        btn_export = (Button) v.findViewById(R.id.export_btn);

        mDisplayDateFrom.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                                                                android.R.style.Theme_Black,
                                                                mDateSetListenerFrom,
                                                                year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                dialog.show();
            }
        });

        mDateSetListenerFrom = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month+1;
                String date = month + "/" + day + "/" + year;
                mDisplayDateFrom.setText(date);
            }
        };

        mDisplayDateTo.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                        android.R.style.Theme_Black,
                        mDateSetListenerTo,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                dialog.show();
            }
        });

        mDateSetListenerTo = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month+1;
                String date = month + "/" + day + "/" + year;
                mDisplayDateTo.setText(date);
            }
        };


        btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ExportActivity.class);
                startActivity(i);
            }
        });


        return v;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()){
            case R.id.nav_search:
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Search()).commit();
                Log.d("This is clicked", this.toString());
                break;
            case R.id.nav_searchTag:
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchByTag()).commit();
                Log.d("This is clicked", this.toString());
                break;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
