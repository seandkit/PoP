package com.example.pop.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pop.DBConstants;
import com.example.pop.R;
import com.example.pop.activity.adapter.FolderListAdapter;
import com.example.pop.activity.adapter.ReceiptListAdapter;
import com.example.pop.helper.CheckNetworkStatus;
import com.example.pop.helper.HttpJsonParser;
import com.example.pop.model.Folder;
import com.example.pop.model.Receipt;
import com.example.pop.sqlitedb.SQLiteDatabaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_Receipt extends Fragment {

    private SQLiteDatabaseAdapter db;
    public static RecyclerView mRecyclerView;
    public static ReceiptListAdapter mAdapter;
    public static ImageView mImageView;

    private ProgressDialog pDialog;
    private int success;
    private String message;

    private Context context;
    private Session session;

    private int receiptId;
    private int recyclerListId;

    public static List<Receipt> mEmptyList = new ArrayList<>();
    private ReceiptListAdapter mEmptyAdapter;

    public Fragment_Receipt() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_receipt, container, false);

        context = getActivity();
        session = new Session(context);
        mRecyclerView = v.findViewById(R.id.receiptList);
        mImageView = v.findViewById(R.id.emptyListImg);

        db = new SQLiteDatabaseAdapter(context);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter = new ReceiptListAdapter(context, FragmentHolder.mReceiptList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        if( mAdapter.getItemCount() != 0 ){
            mImageView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        mEmptyAdapter = new ReceiptListAdapter(context, mEmptyList);
        mRecyclerView.setAdapter(mEmptyAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
}
