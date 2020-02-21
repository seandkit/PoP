package com.example.pop.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pop.R;
import com.example.pop.activity.adapter.FolderListAdapter;
import com.example.pop.model.Folder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class Fragment_Popup_Folders extends BottomSheetDialogFragment {

    Context context;

    private RecyclerView mPopupFolderRecyclerView;
    private FolderListAdapter mPopupFolderAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_popup_folders, container,false);

        context = getActivity().getApplicationContext();

        mPopupFolderRecyclerView = v.findViewById(R.id.popupFolderList);
        // Create an adapter and supply the data to be displayed.
        mPopupFolderAdapter = new FolderListAdapter(context, FragmentHolder.folderList);
        // Connect the adapter with the RecyclerView.
        mPopupFolderRecyclerView.setAdapter(mPopupFolderAdapter);
        // Give the RecyclerView a default layout manager.
        mPopupFolderRecyclerView.setLayoutManager(new LinearLayoutManager(context));


        mPopupFolderAdapter.setOnClick(new FolderListAdapter.OnItemClicked() {
            @Override
            public void onItemClick(int position) {
                dismiss();
            }
        });

        return v;
    }
}
