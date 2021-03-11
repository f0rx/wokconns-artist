package com.wokconns.wokconns.ui.adapter;

/**
 * Created by VARUN on 01/01/19.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.AdapterMyBookingsWorkBinding;
import com.wokconns.wokconns.dto.ArtistBookingDTO;
import com.wokconns.wokconns.preferences.SharedPrefrence;

import java.util.ArrayList;

public class AdapterMyBookingsWork extends RecyclerView.Adapter<AdapterMyBookingsWork.MyViewHolder> {
    Context context;

    LayoutInflater layoutInflater;
    private ArrayList<ArtistBookingDTO> artistJobsDTOArrayList;
    private SharedPrefrence prefrence;
    AdapterMyBookingsWorkBinding binding;

    public AdapterMyBookingsWork(Context context, ArrayList<ArtistBookingDTO> artistJobsDTOArrayList) {
        this.context = context;
        this.artistJobsDTOArrayList = artistJobsDTOArrayList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        prefrence = SharedPrefrence.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.adapter_my_bookings_work, parent, false);
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

        holder.binding.ratingbar.setRating(Float.parseFloat(artistJobsDTOArrayList.get(position).getRating()));
        holder.binding.tvTitle.setText(artistJobsDTOArrayList.get(position).getTitle());
        holder.binding.tvDescription.setText(artistJobsDTOArrayList.get(position).getDescription());
        holder.binding.tvPrice.setText(artistJobsDTOArrayList.get(position).getCurrency_type() + artistJobsDTOArrayList.get(position).getPrice());
        holder.binding.tvTime.setText(artistJobsDTOArrayList.get(position).getBooking_date() +" "+artistJobsDTOArrayList.get(position).getBooking_time());
        holder.binding.CTVBprevioususer.setText(artistJobsDTOArrayList.get(position).getUsername());
    }

    @Override
    public int getItemCount() {
        return artistJobsDTOArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        AdapterMyBookingsWorkBinding binding;

        public MyViewHolder(@NonNull AdapterMyBookingsWorkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}


