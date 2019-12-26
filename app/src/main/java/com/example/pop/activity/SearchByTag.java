package com.example.pop.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.pop.R;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.model.Receipt;

import java.util.ArrayList;


public class SearchByTag extends Fragment{


    private DrawerLayout drawer;
    private TextView mDisplayDateFrom;
    private TextView mDisplayDateTo;
    private DatePickerDialog.OnDateSetListener mDateSetListenerFrom;
    private DatePickerDialog.OnDateSetListener mDateSetListenerTo;
    private Button btn_export;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ReceiptListAdapter adapter;
    private SearchView searchView;

    public SearchByTag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search_tag, container, false);
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        setHasOptionsMenu(true);

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


        btn_export = (Button) v.findViewById(R.id.export_btn);


        searchView = (SearchView) v.findViewById(R.id.tagInput);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

        btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ExportActivity.class);
                startActivity(i);
            }
        });


        return v;
    }

}