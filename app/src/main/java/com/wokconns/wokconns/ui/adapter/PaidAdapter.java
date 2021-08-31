package com.wokconns.wokconns.ui.adapter;

/**
 * Created by VARUN on 01/01/19.
 */
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wokconns.wokconns.dto.HistoryDTO;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.ui.activity.ViewInvoice;
import com.wokconns.wokconns.utils.CustomTextView;
import com.wokconns.wokconns.utils.CustomTextViewBold;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.utils.CustomTextViewSemiBold;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class PaidAdapter extends RecyclerView.Adapter<PaidAdapter.MyViewHolder> {

    Context mContext;
    ArrayList<HistoryDTO> objects =null;
    ArrayList<HistoryDTO> historyDTOList;
    private SharedPrefs prefrence;
    private LayoutInflater inflater;

    public PaidAdapter(Context mContext, ArrayList<HistoryDTO> objects, LayoutInflater inflater) {
        this.mContext = mContext;
        this.objects = objects;
        this.historyDTOList = new ArrayList<>();
        this.historyDTOList.addAll(objects);
        this.inflater = inflater;
        prefrence = SharedPrefs.getInstance(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater
                .inflate(R.layout.adapter_paid, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.CTVBservice.setText(mContext.getResources().getString(R.string.service)+" " + objects.get(position).getInvoice_id());
        try {
            holder.CTVdate.setText(ProjectUtils.convertTimestampDateToTime(ProjectUtils.correctTimestamp(Long.parseLong(objects.get(position).getCreated_at()))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.CTVprice.setText(objects.get(position).getCurrency_type() + objects.get(position).getFinal_amount());
        holder.CTVServicetype.setText(objects.get(position).getCategoryName());
        holder.CTVwork.setText(objects.get(position).getCategoryName());
        holder.CTVname.setText(ProjectUtils.getFirstLetterCapital(objects.get(position).getUserName()));

        Glide.with(mContext).
                load(objects.get(position).getUserImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.IVprofile);
        if (objects.get(position).getFlag().equalsIgnoreCase("0")) {
            holder.tvStatus.setText(mContext.getResources().getString(R.string.unpaid));
            holder.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_orange));
        } else if (objects.get(position).getFlag().equalsIgnoreCase("1")) {
            holder.tvStatus.setText(mContext.getResources().getString(R.string.paid));
            holder.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_green));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("mm.ss");

        try {
            Date dt = sdf.parse(objects.get(position).getWorking_min());
            sdf = new SimpleDateFormat("HH:mm:ss");

            holder.CTVTime.setText(mContext.getResources().getString(R.string.duration)+" " + sdf.format(dt));

        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.tvView.setOnClickListener(v -> {
            Intent in = new Intent(mContext, ViewInvoice.class);
            in.putExtra(Const.HISTORY_DTO, objects.get(position));
            mContext.startActivity(in);

        });

    }

    @Override
    public int getItemCount() {

        return objects.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public CustomTextViewBold CTVBservice;
        public CustomTextView CTVdate, CTVServicetype, CTVwork, CTVname, CTVTime, tvStatus,tvView;
        public CustomTextViewSemiBold CTVprice;
        public ImageView IVprofile;
        public LinearLayout llStatus;

        public MyViewHolder(View view) {
            super(view);

            CTVBservice = view.findViewById(R.id.CTVBservice);
            CTVdate = view.findViewById(R.id.CTVdate);
            CTVprice = view.findViewById(R.id.CTVprice);
            CTVServicetype = view.findViewById(R.id.CTVServicetype);
            CTVwork = view.findViewById(R.id.CTVwork);
            CTVname = view.findViewById(R.id.CTVname);
            IVprofile = view.findViewById(R.id.IVprofile);
            CTVTime = view.findViewById(R.id.CTVTime);
            llStatus = view.findViewById(R.id.llStatus);
            tvStatus = view.findViewById(R.id.tvStatus);
            tvView = view.findViewById(R.id.tvView);

        }
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        objects.clear();
        if (charText.length() == 0) {
            objects.addAll(historyDTOList);
        } else {
            for (HistoryDTO historyDTO : historyDTOList) {
                if (historyDTO.getInvoice_id().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    objects.add(historyDTO);
                }
            }
        }
        notifyDataSetChanged();
    }

}