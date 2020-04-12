package com.example.pop.activity.popup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pop.R;
import com.example.pop.activity.FragmentHolder;
import com.example.pop.adapter.FolderListAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class Popup_Folder_List extends BottomSheetDialogFragment {

    Context context;

    private RecyclerView mPopupFolderRecyclerView;
    private FolderListAdapter mPopupFolderAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_popup_folders, container,false);

        context = getActivity().getApplicationContext();

        mPopupFolderRecyclerView = v.findViewById(R.id.popupFolderList);
        mPopupFolderAdapter = new FolderListAdapter(context, FragmentHolder.folderList);
        mPopupFolderRecyclerView.setAdapter(mPopupFolderAdapter);
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
