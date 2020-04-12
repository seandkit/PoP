package com.example.pop.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.AsyncTask;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Receipt;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fragment_SearchByDate extends Fragment {

    private String endSearchByDate = "";
    private String startSearchByDate = "";
    private DrawerLayout drawer;
    private TextView mDisplayDateFrom;
    private TextView mDisplayDateTo;
    private DatePickerDialog.OnDateSetListener mDateSetListenerFrom;
    private DatePickerDialog.OnDateSetListener mDateSetListenerTo;

    public static RecyclerView mRecyclerView;
    public static RecyclerView.Adapter mAdapter;
    public static ImageView mImageView;

    private RecyclerView.LayoutManager mLayoutManager;
    private Object Receipt;

    private Session session;
    private Context context;
    private int success;
    public List<Receipt> mReceiptList = new ArrayList<>();
    public List<Receipt> mReceiptListTemp = new ArrayList<>();

    public static List<Receipt> mEmptyList = new ArrayList<>();
    private ReceiptListAdapter mEmptyAdapter;


    String[] listOfMonths = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    String[] listOfMonthsDigits = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    public Fragment_SearchByDate() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        context = getActivity().getApplicationContext();
        session = new Session(context);

        mRecyclerView = v.findViewById(R.id.receiptList);
        mAdapter = new ReceiptListAdapter(context, FragmentHolder.mReceiptList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mImageView = v.findViewById(R.id.emptyListImg);

        mDisplayDateFrom = (TextView) v.findViewById(R.id.tvDateFrom);
        mDisplayDateTo = (TextView) v.findViewById(R.id.tvDateTo);




        mDisplayDateFrom.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Black, mDateSetListenerFrom, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
                dialog.show();
                //updateRecyclerView();
            }
        });

        mDateSetListenerFrom = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                String displayDate = day + " " + listOfMonths[month] + " " + year;

                String dateValue = year + "-" + listOfMonthsDigits[month] + "-" + day;
                mDisplayDateFrom.setText(displayDate);
                startSearchByDate = dateValue;



                try {
                    updateSearchList(mReceiptList, startSearchByDate, endSearchByDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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


                String displayDate = day + " " + listOfMonths[month] + " " + year;
                String dateValue = year + "-" + listOfMonthsDigits[month]  + "-" + day;
                mDisplayDateTo.setText(displayDate);
                endSearchByDate = dateValue;

                try {
                    updateSearchList(mReceiptList, startSearchByDate, endSearchByDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        };

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter = new ReceiptListAdapter(context, FragmentHolder.mReceiptList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        Toast.makeText(getActivity(), String.valueOf(mReceiptList.size()),
                Toast.LENGTH_LONG).show();

        if( mAdapter.getItemCount() != 0 ){
            mImageView.setVisibility(View.GONE);
        }
    }

    public void updateSearchList(List<Receipt> receiptList, String startSearchByDate, String endSearchByDate) throws ParseException {
        ArrayList<Receipt> updatedReceiptList = new ArrayList<>();

        String dtStart = startSearchByDate;
        String dtEnd = endSearchByDate;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStart = format.parse(dtStart);
        Date dateEnd = format.parse(dtEnd);




        if(dtStart.length() > 0 && dtEnd.length() > 0)
        {

            for (Receipt receipt : receiptList) {
                Date tempDate = format.parse(receipt.getDate());

                if(!(tempDate.before(dateStart) || tempDate.after(dateEnd)))
                {
                    updatedReceiptList.add(receipt);
                }

            }
        }



        // Create an adapter and supply the data to be displayed.
        mAdapter = new ReceiptListAdapter(context, updatedReceiptList);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
}
