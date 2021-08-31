package com.wokconns.wokconns.ui.adapter;

/**
 * Created by VARUN on 01/01/19.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.wokconns.wokconns.dto.AppointmentDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.ui.fragment.AppointmentFrag;
import com.wokconns.wokconns.utils.CustomTextView;
import com.wokconns.wokconns.utils.CustomTextViewBold;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterAppointmnet extends RecyclerView.Adapter<AdapterAppointmnet.MyViewHolder> {
    private String TAG = AdapterAppointmnet.class.getSimpleName();
    private AppointmentFrag appointmentFrag;
    private Context mContext;
    private ArrayList<AppointmentDTO> appointmentDTOSList;
    private UserDTO userDTO;
    SimpleDateFormat sdf1, timeZone;
    private HashMap<String, String> paramBookAppointment = new HashMap<>();
    private HashMap<String, String> paramDeclineAppointment = new HashMap<>();
    private DialogInterface dialog_book;
    public AdapterAppointmnet(AppointmentFrag appointmentFrag, ArrayList<AppointmentDTO> appointmentDTOSList, UserDTO userDTO) {
        this.appointmentFrag = appointmentFrag;
        mContext = appointmentFrag.getActivity();
        this.appointmentDTOSList = appointmentDTOSList;
        this.userDTO = userDTO;
        sdf1 = new SimpleDateFormat(Const.DATE_FORMATE_SERVER, Locale.ENGLISH);
        timeZone = new SimpleDateFormat(Const.DATE_FORMATE_TIMEZONE, Locale.ENGLISH);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_appointment, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        Glide.with(mContext).
                load(appointmentDTOSList.get(position).getUserImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivArtist);

        holder.tvWork.setText(appointmentDTOSList.get(position).getCategory_name());
        holder.tvLocation.setText(appointmentDTOSList.get(position).getUserAddress());
        holder.tvName.setText(appointmentDTOSList.get(position).getUserName());
        holder.tvDate.setText(appointmentDTOSList.get(position).getDate_string());
        holder.tvEmail.setText(appointmentDTOSList.get(position).getUserEmail());
        holder.tvMobile.setText(appointmentDTOSList.get(position).getUserMobile());
        if (appointmentDTOSList.get(position).getStatus().equalsIgnoreCase("0")) {
            holder.llACDE.setVisibility(View.VISIBLE);
            holder.tvEdit.setVisibility(View.VISIBLE);
            holder.llComplete.setVisibility(View.GONE);
            holder.tvStatus.setText(mContext.getResources().getString(R.string.pending));
            holder.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_red));
        } else if (appointmentDTOSList.get(position).getStatus().equalsIgnoreCase("1")) {
            holder.llACDE.setVisibility(View.GONE);
            holder.tvEdit.setVisibility(View.VISIBLE);
            holder.llComplete.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(mContext.getResources().getString(R.string.acc));
            holder.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_yellow));
        } else if (appointmentDTOSList.get(position).getStatus().equalsIgnoreCase("2")) {
            holder.llACDE.setVisibility(View.GONE);
            holder.llComplete.setVisibility(View.GONE);
            holder.tvEdit.setVisibility(View.GONE);
            holder.tvStatus.setText(mContext.getResources().getString(R.string.rej));
            holder.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_dark_red));
        } else if (appointmentDTOSList.get(position).getStatus().equalsIgnoreCase("3")) {
            holder.llACDE.setVisibility(View.GONE);
            holder.llComplete.setVisibility(View.GONE);
            holder.tvEdit.setVisibility(View.GONE);
            holder.tvStatus.setText(mContext.getResources().getString(R.string.com));
            holder.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_green));
        } else if (appointmentDTOSList.get(position).getStatus().equalsIgnoreCase("4")) {
            holder.llACDE.setVisibility(View.GONE);
            holder.llComplete.setVisibility(View.GONE);
            holder.tvEdit.setVisibility(View.GONE);
            holder.tvStatus.setText(mContext.getResources().getString(R.string.dec));
            holder.llStatus.setBackground(mContext.getResources().getDrawable(R.drawable.rectangle_dark_blue));
        }


        holder.tvEdit.setOnClickListener(v -> {
            paramBookAppointment.put(Const.ARTIST_ID, userDTO.getUser_id());
            paramBookAppointment.put(Const.USER_ID, appointmentDTOSList.get(position).getUser_id());
            paramBookAppointment.put(Const.APPOINTMENT_ID, appointmentDTOSList.get(position).getId());
            clickScheduleDateTime();
        });
        holder.llDecline.setOnClickListener(v -> {
            paramDeclineAppointment.put(Const.USER_ID, userDTO.getUser_id());
            paramDeclineAppointment.put(Const.APPOINTMENT_ID, appointmentDTOSList.get(position).getId());
            paramDeclineAppointment.put(Const.REQUEST, "2");
            bookDailog(mContext.getResources().getString(R.string.dec),mContext.getResources().getString(R.string.dec_msg));

        });
        holder.llAccept.setOnClickListener(v -> {
            paramDeclineAppointment.put(Const.USER_ID, userDTO.getUser_id());
            paramDeclineAppointment.put(Const.APPOINTMENT_ID, appointmentDTOSList.get(position).getId());
            paramDeclineAppointment.put(Const.REQUEST, "1");
            bookDailog(mContext.getResources().getString(R.string.acc),mContext.getResources().getString(R.string.acc_msg));

        });
        holder.llComplete.setOnClickListener(v -> {
            paramDeclineAppointment.put(Const.USER_ID, userDTO.getUser_id());
            paramDeclineAppointment.put(Const.APPOINTMENT_ID, appointmentDTOSList.get(position).getId());
            paramDeclineAppointment.put(Const.REQUEST, "3");
            bookDailog(mContext.getResources().getString(R.string.comp),"Are you sure you want to Complete this appointment?");

        });

    }


    @Override
    public int getItemCount() {

        return appointmentDTOSList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView ivArtist;
        public CustomTextView  tvWork, tvLocation, tvDate, tvStatus,tvMobile,tvEmail;
        public LinearLayout llStatus,llAccept, llDecline, llACDE, llComplete;
        public CustomTextViewBold tvName;
        public CustomTextView  tvEdit;
        public MyViewHolder(View view) {
            super(view);
            ivArtist = view.findViewById(R.id.ivArtist);
            tvWork = view.findViewById(R.id.tvWork);
            tvName = view.findViewById(R.id.tvName);
            tvLocation = view.findViewById(R.id.tvLocation);
            tvDate = view.findViewById(R.id.tvDate);
            tvEdit = view.findViewById(R.id.tvEdit);
            llStatus = view.findViewById(R.id.llStatus);
            tvStatus = view.findViewById(R.id.tvStatus);
            llAccept = view.findViewById(R.id.llAccept);
            llDecline = view.findViewById(R.id.llDecline);
            llACDE = view.findViewById(R.id.llACDE);
            llComplete = view.findViewById(R.id.llComplete);
            tvEmail = view.findViewById(R.id.tvEmail);
            tvMobile = view.findViewById(R.id.tvMobile);
        }
    }

    public void clickScheduleDateTime() {
        new SingleDateAndTimePickerDialog.Builder(mContext)
                .bottomSheet()
                .curved()
                .mustBeOnFuture()
                .listener(date -> {
                    paramBookAppointment.put(Const.DATE_STRING, String.valueOf(sdf1.format(date).toString().toUpperCase()));
                    paramBookAppointment.put(Const.TIMEZONE, String.valueOf(timeZone.format(date)));
                    bookAppointment();
                })
                .display();
    }

    public void bookAppointment() {
        new HttpsRequest(Const.EDIT_APPOINTMENT_API, paramBookAppointment, mContext).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                ProjectUtils.showToast(mContext, msg);
                appointmentFrag.getHistroy();
            } else {
                ProjectUtils.showToast(mContext, msg);
            }


        });
    }
    public void declineAppointment() {

        new HttpsRequest(Const.APPOINTMENT_OPERATION_API, paramDeclineAppointment, mContext).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                ProjectUtils.showToast(mContext, msg);
                appointmentFrag.getHistroy();
            } else {
                ProjectUtils.showToast(mContext, msg);
            }


        });
    }

    public void bookDailog(String title , String msg) {
        try {
            new AlertDialog.Builder(mContext)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(title)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton(mContext.getResources().getString(R.string.yes), (dialog, which) -> {
                        dialog_book = dialog;
                        declineAppointment();

                    })
                    .setNegativeButton(mContext.getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}