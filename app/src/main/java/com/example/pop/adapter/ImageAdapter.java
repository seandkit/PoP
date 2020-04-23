package com.example.pop.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.pop.R;

public class ImageAdapter extends PagerAdapter {
    private Context mContext;
    private int[] mImageIds = new int[] {R.drawable.view_all_receipts, R.drawable.view_search_by_date, R.drawable.view_search_by_tag,
            R.drawable.open_navigation_drawer, R.drawable.create_new_folder, R.drawable.open_folder_menu,
            R.drawable.open_receipt,
            R.drawable.open_export_receipt_options, R.drawable.export_receipt,
            R.drawable.open_map_of_location, R.drawable.map,
            R.drawable.log_out};

    public ImageAdapter(Context context) {
        mContext = context;
    }
    @Override
    public int getCount() {
        return mImageIds.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(mContext);

        imageView.setImageResource(mImageIds[position]);
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}
