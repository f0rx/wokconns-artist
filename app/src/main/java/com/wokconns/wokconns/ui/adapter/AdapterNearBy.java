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
import com.wokconns.wokconns.dto.HomeNearByJobsDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.AdapterNearByBinding;

import java.util.ArrayList;

public class AdapterNearBy extends RecyclerView.Adapter<AdapterNearBy.MyViewHolder> {

    Context mContext;
    ArrayList<HomeNearByJobsDTO> nearByJobsDTOArrayList;
    AdapterNearByBinding binding;
    LayoutInflater layoutInflater;

    public AdapterNearBy(Context mContext, ArrayList<HomeNearByJobsDTO> nearByJobsDTOArrayList) {
        this.mContext = mContext;
        this.nearByJobsDTOArrayList = nearByJobsDTOArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.adapter_near_by, parent, false);
        View itemView = binding.getRoot();
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.binding.setNearByDTO(nearByJobsDTOArrayList.get(position));

        holder.binding.tvPrice.setText(nearByJobsDTOArrayList.get(position).getCurrency_symbol()+" "+nearByJobsDTOArrayList.get(position).getPrice());

        Glide.with(mContext).
                load(nearByJobsDTOArrayList.get(position).getUserImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.ivImage);
    }

    @Override
    public int getItemCount() {
        return nearByJobsDTOArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        AdapterNearByBinding binding;

        public MyViewHolder(AdapterNearByBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}