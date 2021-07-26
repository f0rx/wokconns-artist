package com.wokconns.wokconns.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.wokconns.wokconns.dto.GalleryDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.AdapterGalleryBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.ui.activity.ImageGallery;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by VARUN on 01/01/19.
 */

public class AdapterGallery extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    LayoutInflater mLayoutInflater;
    private ArrayList<GalleryDTO> gallery;
    private Context context;
    private HashMap<String, String> parms = new HashMap<>();
    private String TAG = AdapterGallery.class.getSimpleName();
    private DialogInterface dialog_book;
    AdapterGalleryBinding binding;
    String type = "";

    public AdapterGallery(Context context, ArrayList<GalleryDTO> gallery, String type) {
        this.context = context;
        this.gallery = gallery;
        this.type = type;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.adapter_gallery, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        MyViewHolder myViewHolder = (MyViewHolder) holder;

        Glide
                .with(context)
                .load(gallery.get(position).getImage())
                .placeholder(R.drawable.bg)
                .into(myViewHolder.binding.ivBottomFoster);

        if (type.equalsIgnoreCase("gallery")) {
            myViewHolder.binding.llDeletePhoto.setVisibility(View.VISIBLE);
        } else if (type.equalsIgnoreCase("home")) {
            myViewHolder.binding.llDeletePhoto.setVisibility(View.GONE);
        }

        myViewHolder.binding.ivBottomFoster.setOnClickListener(v -> {
            if (type.equalsIgnoreCase("gallery")) {
                ((ImageGallery) context).showImg(gallery.get(position).getImage());
            }
        });

        myViewHolder.binding.ivDelete.setOnClickListener(v -> {
            parms.put(Consts.ID, gallery.get(position).getId());
            parms.put(Consts.USER_ID, gallery.get(position).getUser_id());
            deleteDialog();
        });
    }

    @Override
    public int getItemCount() {
        return gallery.size();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        AdapterGalleryBinding binding;

        public MyViewHolder(@NonNull AdapterGalleryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void deleteDialog() {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(context.getResources().getString(R.string.delete_gallery))
                    .setMessage(context.getResources().getString(R.string.delete_gallery_msg))
                    .setCancelable(false)
                    .setPositiveButton(context.getResources().getString(R.string.yes), (dialog, which) -> {
                        dialog_book = dialog;
                        deleteGallery();

                    })
                    .setNegativeButton(context.getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteGallery() {
        new HttpsRequest(Consts.DELETE_GALLERY_API, parms, context).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                ProjectUtils.showToast(context, msg);
                if (type.equalsIgnoreCase("gallery")) {
                    ((ImageGallery) context).getParentData();
                }
            } else {
                ProjectUtils.showLong(context, msg);
            }
        });
    }

}