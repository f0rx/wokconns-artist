package com.wokconns.wokconns.ui.adapter;

/**
 * Created by VARUN on 01/01/19.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wokconns.wokconns.dto.HomeRecomendedDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.AdapterRecommendedBinding;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.fragment.JobsFrag;

import java.util.ArrayList;

public class AdapterRecommended extends RecyclerView.Adapter<AdapterRecommended.MyViewHolder> {

    Context mContext;
    ArrayList<HomeRecomendedDTO> recomendedDTOArrayList;
    AdapterRecommendedBinding binding;
    LayoutInflater layoutInflater;
    BaseActivity baseActivity;

    public AdapterRecommended(Context mContext,
                              ArrayList<HomeRecomendedDTO> recomendedDTOArrayList,
                              BaseActivity baseActivity) {
        this.mContext = mContext;
        this.recomendedDTOArrayList = recomendedDTOArrayList;
        this.baseActivity = baseActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.adapter_recommended, parent, false);
        View itemView = binding.getRoot();

        itemView.setOnClickListener(v -> {
            baseActivity.ivSearch.setVisibility(View.VISIBLE);
            baseActivity.rlheader.setVisibility(View.VISIBLE);

            BaseActivity.navItemIndex = 1;
            BaseActivity.CURRENT_TAG = BaseActivity.TAG_MAIN;
            baseActivity.loadHomeFragment(new JobsFrag(), BaseActivity.CURRENT_TAG);
        });
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.binding.setHomeRecommendedDTO(recomendedDTOArrayList.get(position));

        Glide.with(mContext).
                load(recomendedDTOArrayList.get(position).getUserImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.cIvImage);
    }

    @Override
    public int getItemCount() {
        return recomendedDTOArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        AdapterRecommendedBinding binding;

        public MyViewHolder(AdapterRecommendedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}