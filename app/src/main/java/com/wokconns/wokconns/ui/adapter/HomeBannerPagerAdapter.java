package com.wokconns.wokconns.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.wokconns.wokconns.dto.HomeBannerDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ViewpagerHomeBannerBinding;
import com.wokconns.wokconns.ui.fragment.Home;

import java.util.ArrayList;


public class HomeBannerPagerAdapter extends PagerAdapter {

    private Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<HomeBannerDTO> bannerDTOArrayList;
    Home homeFragment;
    ViewpagerHomeBannerBinding binding;

    public HomeBannerPagerAdapter(Home homeFragment, Context mContext, ArrayList<HomeBannerDTO> bannerDTOArrayList) {
        this.mContext = mContext;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.bannerDTOArrayList = bannerDTOArrayList;
        this.homeFragment = homeFragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.viewpager_home_banner, container, false);
        View itemView = binding.getRoot();

        binding.tvTitle.setText(bannerDTOArrayList.get(position).getTitle());
        binding.tvDescription.setText(bannerDTOArrayList.get(position).getDescription());

        Glide.with(mContext)
                .load(bannerDTOArrayList.get(position).getImage())
                .apply(new RequestOptions())
                .placeholder(R.drawable.ic_placeholder_white)
                .into(binding.ivImage);

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return bannerDTOArrayList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}