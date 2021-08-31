package com.wokconns.wokconns.ui.adapter;

/**
 * Created by VARUN on 01/01/19.
 */

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wokconns.wokconns.databinding.MyJobsWorkAdapterBinding;
import com.wokconns.wokconns.dto.ArtistJobsDTO;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.R;

import java.util.ArrayList;


public class MyJobsWorkAdapter extends RecyclerView.Adapter<MyJobsWorkAdapter.MyViewHolder> {
    Context context;

    LayoutInflater layoutInflater;
    private ArrayList<ArtistJobsDTO> artistJobsDTOArrayList;
    private SharedPrefs prefrence;
    MyJobsWorkAdapterBinding binding;

    public MyJobsWorkAdapter(Context context, ArrayList<ArtistJobsDTO> artistJobsDTOArrayList) {
        this.context = context;
        this.artistJobsDTOArrayList = artistJobsDTOArrayList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        prefrence = SharedPrefs.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.my_jobs_work_adapter, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).
                load(artistJobsDTOArrayList.get(position).getUserImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.IVartist);
        holder.binding.tvTitle.setText(artistJobsDTOArrayList.get(position).getTitle());
        holder.binding.tvDescription.setText(artistJobsDTOArrayList.get(position).getDescription());
        holder.binding.tvJobIdValue.setText(artistJobsDTOArrayList.get(position).getJob_id());
        holder.binding.tvPrice.setText(artistJobsDTOArrayList.get(position).getCurrency_symbol() + artistJobsDTOArrayList.get(position).getPrice());
        holder.binding.tvTime.setText(artistJobsDTOArrayList.get(position).getJob_date() +" "+artistJobsDTOArrayList.get(position).getTime());
        holder.binding.tvCategory.setText(artistJobsDTOArrayList.get(position).getCat_name());
        holder.binding.CTVBprevioususer.setText(artistJobsDTOArrayList.get(position).getUsername());
        holder.binding.tvAddress.setText(artistJobsDTOArrayList.get(position).getAddress());

    }

    @Override
    public int getItemCount() {
        return artistJobsDTOArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        MyJobsWorkAdapterBinding binding;

        public MyViewHolder(@NonNull MyJobsWorkAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}


