package com.wokconns.wokconns.ui.adapter;

/**
 * Created by VARUN on 01/01/19.
 */

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wokconns.wokconns.dto.AllJobsDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.AdapterAllJobsBinding;
import com.wokconns.wokconns.databinding.DailogApplyJobBinding;
import com.wokconns.wokconns.databinding.ItemSectionBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.fragment.AllJobsFrag;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class AllJobsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String TAG = AllJobsAdapter.class.getSimpleName();
    private HashMap<String, String> params = new HashMap<>();
    private Dialog dialogApplyJob;
    private Context mContext;
    private AllJobsFrag allJobsFrag;
    private ArrayList<AllJobsDTO> objects = null;
    private ArrayList<AllJobsDTO> allJobsDTOList;
    private UserDTO userDTO;
    private LayoutInflater inflater;
    private SharedPrefrence prefrence;

    private final int VIEW_ITEM = 1;
    private final int VIEW_SECTION = 0;
    DailogApplyJobBinding dailogApplyJobBinding;

    AdapterAllJobsBinding allJobsBinding;
    ItemSectionBinding itemSectionBinding;

    public AllJobsAdapter(AllJobsFrag allJobsFrag, ArrayList<AllJobsDTO> objects, UserDTO userDTO, LayoutInflater inflater) {
        this.allJobsFrag = allJobsFrag;
        this.mContext = allJobsFrag.getActivity();
        this.objects = objects;
        this.allJobsDTOList = new ArrayList<>();
        this.allJobsDTOList.addAll(objects);
        this.userDTO = userDTO;
        this.inflater = inflater;
        prefrence = SharedPrefrence.getInstance(mContext);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_all_jobs, parent, false);
            allJobsBinding = DataBindingUtil.inflate(inflater, R.layout.adapter_all_jobs, parent, false);
            vh = new MyViewHolder(allJobsBinding);
        } else {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
            itemSectionBinding = DataBindingUtil.inflate(inflater, R.layout.item_section, parent, false);
            vh = new MyViewHolderSection(itemSectionBinding);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderMain, final int position) {
        if (holderMain instanceof MyViewHolder) {
            MyViewHolder holder = (MyViewHolder) holderMain;
            //0 = pending, 1 = confirm , 2 = complete, 3 =reject

            holder.allJobsBinding.tvJobId.setText(objects.get(position).getJob_id());
            holder.allJobsBinding.tvTitle.setText(objects.get(position).getTitle());
            holder.allJobsBinding.tvDescription.setText(objects.get(position).getDescription());
            holder.allJobsBinding.tvCategory.setText(objects.get(position).getCategory_name());
            holder.allJobsBinding.tvAddress.setText(objects.get(position).getAddress());
            holder.allJobsBinding.tvName.setText(objects.get(position).getUser_name());
            holder.allJobsBinding.tvPrice.setText(objects.get(position).getCurrency_symbol() + objects.get(position).getPrice());
            holder.allJobsBinding.tvDate.setText(ProjectUtils.changeDateFormatAll(objects.get(position).getJob_date()) + " | " + objects.get(position).getTime());

            Glide.with(mContext).
                    load(objects.get(position).getAvtar())
                    .placeholder(R.drawable.dummyuser_image)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.allJobsBinding.ivImage);
            holder.allJobsBinding.llApply.setOnClickListener(v -> {

                params.put(Consts.USER_ID, objects.get(position).getUser_id());
                params.put(Consts.JOB_ID, objects.get(position).getJob_id());
                params.put(Consts.ARTIST_ID, userDTO.getUser_id());
                dialogAbout(objects.get(position).getCurrency_symbol());
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

        AdapterAllJobsBinding allJobsBinding;

        public MyViewHolder(AdapterAllJobsBinding allJobsBinding) {
            super(allJobsBinding.getRoot());
            this.allJobsBinding = allJobsBinding;
        }
    }

    public static class MyViewHolderSection extends RecyclerView.ViewHolder {
        ItemSectionBinding itemSectionBinding;

        public MyViewHolderSection(ItemSectionBinding itemSectionBinding) {
            super(itemSectionBinding.getRoot());
            this.itemSectionBinding = itemSectionBinding;
        }
    }

    public void dialogAbout(String currency) {
        dialogApplyJob = new Dialog(mContext/*, android.R.style.Theme_Dialog*/);
        dialogApplyJob.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogApplyJob.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dailogApplyJobBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dailog_apply_job, null, false);
        dialogApplyJob.setContentView(dailogApplyJobBinding.getRoot());

        dailogApplyJobBinding.ctvbCurrency.setText("("+currency+")");
        dailogApplyJobBinding.etPriceD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1 && s.toString().startsWith("0")) {
                    s.clear();
                }
            }
        });

        dialogApplyJob.show();
        dialogApplyJob.setCancelable(false);

        dailogApplyJobBinding.ivClose.setOnClickListener(v -> dialogApplyJob.dismiss());
        dailogApplyJobBinding.tvSubmit.setOnClickListener(
                v -> {
                    params.put(Consts.DESCRIPTION, ProjectUtils.getEditTextValue(dailogApplyJobBinding.etAboutD));
                    params.put(Consts.PRICE, ProjectUtils.getEditTextValue(dailogApplyJobBinding.etPriceD));
                    submitPersonalProfile();

                });
    }

    public void applyJob() {

        new HttpsRequest(Consts.APPLIED_JOB_API, params, mContext).stringPost(TAG, (flag, msg, response) -> {
            dialogApplyJob.dismiss();
            if (flag) {
                ProjectUtils.showToast(mContext, msg);

                allJobsFrag.getjobs();
            } else {
                ProjectUtils.showToast(mContext, msg);
            }


        });
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        objects.clear();
        if (charText.length() == 0) {
            objects.addAll(allJobsDTOList);
        } else {
            for (AllJobsDTO allJobsDTO : allJobsDTOList) {
                if (allJobsDTO.getTitle().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    objects.add(allJobsDTO);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void submitPersonalProfile() {
        if (!validation(dailogApplyJobBinding.etAboutD, mContext.getResources().getString(R.string.val_des))) {
            return;
        } else if (!validation(dailogApplyJobBinding.etPriceD, mContext.getResources().getString(R.string.val_price))) {
            return;
        } else {
            if (NetworkManager.isConnectToInternet(mContext)) {
                applyJob();
            } else {
                ProjectUtils.showToast(mContext, mContext.getResources().getString(R.string.internet_concation));
            }
        }
    }

    public boolean validation(EditText editText, String msg) {
        if (!ProjectUtils.isEditTextFilled(editText)) {
            ProjectUtils.showLong(mContext, msg);
            return false;
        } else {
            return true;
        }
    }

}