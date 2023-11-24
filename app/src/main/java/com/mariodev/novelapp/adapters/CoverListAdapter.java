package com.mariodev.novelapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;

public class CoverListAdapter extends BaseAdapter {

    final Context context;
    final List<Integer> mList;

    public CoverListAdapter(Context context) {
        this.context = context;
        mList = new ArrayList<>();

        int[] coverImagesArray = {R.drawable.cover1, R.drawable.cover2, R.drawable.cover3, R.drawable.cover4, R.drawable.cover5, R.drawable.cover6, R.drawable.cover7, R.drawable.cover8, R.drawable.cover9};

        for (int value : coverImagesArray) {
            mList.add(value);
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View row = layoutInflater.inflate(R.layout.cover_img_item , viewGroup , false);

        ImageView imageView = row.findViewById(R.id.coverListImg);

        imageView.setImageResource(mList.get(i));

        return row;
    }
}
