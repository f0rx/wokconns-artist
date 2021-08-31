package com.wokconns.wokconns.ui.adapter;

/**
 * Created by VARUN on 01/01/19.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wokconns.wokconns.dto.AppliedJobDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.AdapterAppliedJobBinding;
import com.wokconns.wokconns.databinding.ItemSectionBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.ui.fragment.AppliedJobsFrag;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AppliedJobAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private String TAG = AppliedJobAdapter.class.getSimpleName();
    private HashMap<String, String> params;
    private HashMap<String, String> paramsStart;
    private DialogInterface dialog_book;
    private AppliedJobsFrag appliedJobsFrag;
    private ArrayList<AppliedJobDTO> objects = new ArrayList<>();
    private ArrayList<AppliedJobDTO> appliedJobDTOSList;
    private UserDTO userDTO;
    private Context mContext;
    private LayoutInflater inflater;
    SimpleDateFormat sdf1, timeZone;
    private Date date;
    int CALL_PERMISSION = 101;
    private final int VIEW_ITEM = 1;
    private final int VIEW_SECTION = 0;

    AdapterAppliedJobBinding appliedJobBinding;
    ItemSectionBinding itemSectionBinding;


    public AppliedJobAdapter(AppliedJobsFrag appliedJobsFrag, ArrayList<AppliedJobDTO> objects, UserDTO userDTO, LayoutInflater inflater) {
        this.appliedJobsFrag = appliedJobsFrag;
        this.objects = objects;
        this.appliedJobDTOSList = new ArrayList<>();
        this.appliedJobDTOSList.addAll(objects);
        this.userDTO = userDTO;
        this.mContext = appliedJobsFrag.getActivity();
        this.inflater = inflater;
        sdf1 = new SimpleDateFormat(Const.DATE_FORMATE_SERVER, Locale.ENGLISH);
        timeZone = new SimpleDateFormat(Const.DATE_FORMATE_TIMEZONE, Locale.ENGLISH);

        date = new Date();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_applied_job, parent, false);
            appliedJobBinding = DataBindingUtil.inflate(inflater, R.layout.adapter_applied_job, parent, false);
            vh = new MyViewHolder(appliedJobBinding);
        } else {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
            itemSectionBinding = DataBindingUtil.inflate(inflater, R.layout.item_section, parent, false);
            vh = new MyViewHolderSection(itemSectionBinding);
        }
        return vh;
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderMain, final int position) {
        if (holderMain instanceof MyViewHolder) {
            MyViewHolder holder = (MyViewHolder) holderMain;

            holder.appliedJobBinding.tvDate.setText(ProjectUtils.changeDateFormatAll(objects.get(position).getJob_date()) + " | " + objects.get(position).getTime());
            holder.appliedJobBinding.tvJobId.setText(objects.get(position).getJob_id());
            holder.appliedJobBinding.tvName.setText(objects.get(position).getUser_name());
            holder.appliedJobBinding.tvDescription.setText(objects.get(position).getDescription());
            holder.appliedJobBinding.tvAddress.setText(objects.get(position).getUser_address());
            holder.appliedJobBinding.tvEmail.setText(objects.get(position).getUser_email());
            holder.appliedJobBinding.tvMobile.setText(objects.get(position).getUser_mobile());

            holder.appliedJobBinding.tvCategory.setText(objects.get(position).getCategory_name());
            holder.appliedJobBinding.tvTitle.setText(objects.get(position).getTitle());
            holder.appliedJobBinding.tvPrice.setText(objects.get(position).getCurrency_symbol() + objects.get(position).getPrice());

            holder.appliedJobBinding.tvMobile.setOnClickListener(v -> {
                if (ProjectUtils.hasPermissionInManifest(mContext, CALL_PERMISSION, Manifest.permission.CALL_PHONE)) {
                    if (objects.get(position).getUser_mobile().equalsIgnoreCase("")) {
                        ProjectUtils.showToast(mContext, mContext.getResources().getString(R.string.mobile_no_not));
                    } else {
                        try {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + objects.get(position).getUser_mobile()));
                            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            appliedJobsFrag.startActivity(callIntent);

                        } catch (Exception e) {
                            Log.e("Exception", "" + e);
                        }
                    }
                }
            });
            Glide.with(mContext).
                    load(objects.get(position).getUser_image())
                    .placeholder(R.drawable.dummyuser_image)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.appliedJobBinding.ivProfile);

            if (objects.get(position).getStatus().equalsIgnoreCase("0")) {
                holder.appliedJobBinding.llDecline.setVisibility(View.VISIBLE);
                holder.appliedJobBinding.llStart.setVisibility(View.GONE);
                holder.appliedJobBinding.tvStatus.setText(mContext.getResources().getString(R.string.pending));
                holder.appliedJobBinding.llInpro.setVisibility(View.GONE);
                holder.appliedJobBinding.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_orange));
            } else if (objects.get(position).getStatus().equalsIgnoreCase("1")) {
                holder.appliedJobBinding.llDecline.setVisibility(View.GONE);
                holder.appliedJobBinding.llStart.setVisibility(View.VISIBLE);
                holder.appliedJobBinding.llInpro.setVisibility(View.GONE);
                holder.appliedJobBinding.tvStatus.setText(mContext.getResources().getString(R.string.confirm));
                holder.appliedJobBinding.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_yellow));
            } else if (objects.get(position).getStatus().equalsIgnoreCase("2")) {
                holder.appliedJobBinding.llDecline.setVisibility(View.GONE);
                holder.appliedJobBinding.llStart.setVisibility(View.GONE);
                holder.appliedJobBinding.llInpro.setVisibility(View.GONE);
                holder.appliedJobBinding.tvStatus.setText(mContext.getResources().getString(R.string.com));
                holder.appliedJobBinding.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_green));
            } else if (objects.get(position).getStatus().equalsIgnoreCase("3")) {
                holder.appliedJobBinding.llDecline.setVisibility(View.GONE);
                holder.appliedJobBinding.llStart.setVisibility(View.GONE);
                holder.appliedJobBinding.llInpro.setVisibility(View.GONE);
                holder.appliedJobBinding.tvStatus.setText(mContext.getResources().getString(R.string.rej));
                holder.appliedJobBinding.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_dark_red));
            } else if (objects.get(position).getStatus().equalsIgnoreCase("5")) {
                holder.appliedJobBinding.llDecline.setVisibility(View.GONE);
                holder.appliedJobBinding.llStart.setVisibility(View.GONE);
                holder.appliedJobBinding.llInpro.setVisibility(View.VISIBLE);
                holder.appliedJobBinding.tvStatus.setText(mContext.getResources().getString(R.string.inprogres));
                holder.appliedJobBinding.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_dark_red));
            }


            holder.appliedJobBinding.llDecline.setOnClickListener(v -> {
                params = new HashMap<>();
                params.put(Const.AJ_ID, objects.get(position).getAj_id());
                params.put(Const.STATUS, "3");
                rejectDialog(mContext.getResources().getString(R.string.reject), mContext.getResources().getString(R.string.reject_msg));
            });

            holder.appliedJobBinding.llStart.setOnClickListener(v -> {
                paramsStart = new HashMap<>();
                paramsStart.put(Const.USER_ID, objects.get(position).getUser_id());
                paramsStart.put(Const.ARTIST_ID, objects.get(position).getArtist_id());
                paramsStart.put(Const.DATE_STRING, sdf1.format(date).toString().toUpperCase());
                paramsStart.put(Const.TIMEZONE, timeZone.format(date));
                paramsStart.put(Const.PRICE, objects.get(position).getPrice());
                paramsStart.put(Const.JOB_ID, objects.get(position).getJob_id());
                startDialog(mContext.getResources().getString(R.string.start), mContext.getResources().getString(R.string.start_app));
            });

        } else {
            MyViewHolderSection view = (MyViewHolderSection) holderMain;
            view.itemSectionBinding.tvSection.setText(objects.get(position).getSection_name());
        }

    }

    @Override
    public int getItemViewType(int position) {
        return this.objects.get(position).isSection() ? VIEW_SECTION : VIEW_ITEM;
    }

    @Override
    public int getItemCount() {

        return objects.size();
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder {
        AdapterAppliedJobBinding appliedJobBinding;

        public MyViewHolder(AdapterAppliedJobBinding appliedJobBinding) {
            super(appliedJobBinding.getRoot());
            this.appliedJobBinding = appliedJobBinding;
        }
    }

    public static class MyViewHolderSection extends RecyclerView.ViewHolder {
        ItemSectionBinding itemSectionBinding;

        public MyViewHolderSection(ItemSectionBinding itemSectionBinding) {
            super(itemSectionBinding.getRoot());
            this.itemSectionBinding = itemSectionBinding;
        }
    }

    public void reject() {

        new HttpsRequest(Const.JOB_STATUS_ARTIST_API, params, mContext).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                ProjectUtils.showToast(mContext, msg);
                dialog_book.dismiss();
                appliedJobsFrag.getjobs();
            } else {
                ProjectUtils.showToast(mContext, msg);
            }
        });
    }

    public void startJob() {
        new HttpsRequest(Const.START_JOB_API, paramsStart, mContext).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                ProjectUtils.showToast(mContext, msg);
                dialog_book.dismiss();
                appliedJobsFrag.gotos();
            } else {
                ProjectUtils.showToast(mContext, msg);
            }


        });
    }

    public void rejectDialog(String title, String msg) {
        try {
            new AlertDialog.Builder(mContext)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(title)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton(mContext.getResources().getString(R.string.yes), (dialog, which) -> {
                        dialog_book = dialog;
                        reject();

                    })
                    .setNegativeButton(mContext.getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDialog(String title, String msg) {
        try {
            new AlertDialog.Builder(mContext)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(title)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton(mContext.getResources().getString(R.string.yes), (dialog, which) -> {
                        dialog_book = dialog;
                        startJob();

                    })
                    .setNegativeButton(mContext.getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        objects.clear();
        if (charText.length() == 0) {
            objects.addAll(appliedJobDTOSList);
        } else {
            for (AppliedJobDTO appliedJobDTO : appliedJobDTOSList) {
                if (appliedJobDTO.getUser_name().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    objects.add(appliedJobDTO);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    objects = appliedJobDTOSList;
                } else {
                    ArrayList<AppliedJobDTO> filteredList = new ArrayList<>();
                    for (AppliedJobDTO row : appliedJobDTOSList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getUser_name().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    objects = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = objects;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                objects = (ArrayList<AppliedJobDTO>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}