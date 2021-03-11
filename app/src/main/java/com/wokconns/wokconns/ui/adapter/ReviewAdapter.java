package com.wokconns.wokconns.ui.adapter;

/**
 * Created by VARUN on 01/01/19.
 */

import android.content.Context;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wokconns.wokconns.databinding.AdapterReviewBinding;
import com.wokconns.wokconns.dto.ReviewsDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    Context mContext;
    ArrayList<ReviewsDTO> reviewsDTOList;
    AdapterReviewBinding binding;
    private LayoutInflater inflater;

    public ReviewAdapter(Context mContext, ArrayList<ReviewsDTO> reviewsDTOList) {
        this.mContext = mContext;
        this.reviewsDTOList = reviewsDTOList;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(inflater, R.layout.adapter_review, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.binding.tvName.setText(reviewsDTOList.get(position).getName());
        holder.binding.tvRating.setText("(" + reviewsDTOList.get(position).getRating() + "/5)");
        holder.binding.tvComment.setText(reviewsDTOList.get(position).getComment());
        try {
            holder.binding.tvTime.setText(ProjectUtils.convertTimestampToFormatDate(ProjectUtils.correctTimestamp(Long.parseLong(reviewsDTOList.get(position).getCreated_at()))));
        }catch (Exception e){
            e.printStackTrace();
        }

        holder.binding.ratingbar.setRating(Float.parseFloat(reviewsDTOList.get(position).getRating()));
        Glide.with(mContext).
                load(reviewsDTOList.get(position).getImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.ivArtist);
    }

    @Override
    public int getItemCount() {

        return reviewsDTOList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        AdapterReviewBinding binding;

        public MyViewHolder(AdapterReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}