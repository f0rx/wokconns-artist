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
import com.wokconns.wokconns.databinding.AdapterPayoutHistoryBinding;
import com.wokconns.wokconns.dto.PayoutDTO;
import com.wokconns.wokconns.preferences.SharedPrefrence;

import java.util.ArrayList;

public class AdapterPayoutHistory extends RecyclerView.Adapter<AdapterPayoutHistory.MyViewHolder> {
    Context context;

    LayoutInflater layoutInflater;
    private ArrayList<PayoutDTO> payoutDTOArrayList;
    private SharedPrefrence prefrence;
    AdapterPayoutHistoryBinding binding;

    public AdapterPayoutHistory(Context context, ArrayList<PayoutDTO> payoutDTOArrayList) {
        this.context = context;
        this.payoutDTOArrayList = payoutDTOArrayList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        prefrence = SharedPrefrence.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.adapter_payout_history, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).
                load(payoutDTOArrayList.get(position).getImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.IVartist);

        holder.binding.tvTitle.setText(payoutDTOArrayList.get(position).getTitle());
        holder.binding.tvDescription.setText(payoutDTOArrayList.get(position).getDescription());
        holder.binding.tvPrice.setText(payoutDTOArrayList.get(position).getCurrency_type() + payoutDTOArrayList.get(position).getAmount());
        holder.binding.tvTime.setText(payoutDTOArrayList.get(position).getCreated_date());
        holder.binding.tvRefIdValue.setText(payoutDTOArrayList.get(position).getReference_id());
    }

    @Override
    public int getItemCount() {
        return payoutDTOArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        AdapterPayoutHistoryBinding binding;

        public MyViewHolder(@NonNull AdapterPayoutHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}


