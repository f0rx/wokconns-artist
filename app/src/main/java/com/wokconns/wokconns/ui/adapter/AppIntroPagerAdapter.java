package com.wokconns.wokconns.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.wokconns.wokconns.R;
import com.wokconns.wokconns.ui.activity.AppIntro;
import com.wokconns.wokconns.utils.CustomTextView;

public class AppIntroPagerAdapter extends PagerAdapter {
    private final Context mContext;
    LayoutInflater mLayoutInflater;
    private final int[] mResources;
    private final AppIntro activity;


    public AppIntroPagerAdapter(AppIntro appIntroActivity, Context mContext, int[] mResources) {
        this.mContext = mContext;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mResources = mResources;
        this.activity = appIntroActivity;
    }

    @Override
    @NonNull
    public View instantiateItem(@NonNull ViewGroup container, final int position) {

        View itemView = mLayoutInflater.inflate(R.layout.appintropager_adapter, container, false);
        ImageView ivImage = itemView.findViewById(R.id.ivImage);

        CustomTextView ctvTextBottom = itemView.findViewById(R.id.ctvText);
        CustomTextView ctvDescription = itemView.findViewById(R.id.ctvTextdecrib);
        ivImage.setImageResource(mResources[position]);
        setDescText(position, ctvDescription, ctvTextBottom);


        container.addView(itemView);
        ctvTextBottom.setOnClickListener(v -> {
            int pos = position + 1;
            activity.scrollPage(pos);
        });
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public void setDescText(int pos, TextView ctvDescription, TextView ctvTextBottom) {
        switch (pos) {
            case 0:
                ctvDescription.setText(mContext.getString(R.string.intro_1));
                ctvTextBottom.setText(mContext.getString(R.string.intro_1_bottom));
                break;
            case 1:
                ctvDescription.setText(mContext.getString(R.string.intro_2));
                ctvTextBottom.setText(mContext.getString(R.string.intro_2_bottom));
                break;
            case 2:
                ctvDescription.setText(mContext.getString(R.string.intro_3));
                ctvTextBottom.setText(mContext.getString(R.string.intro_3_bottom));
                break;
        }
    }
}