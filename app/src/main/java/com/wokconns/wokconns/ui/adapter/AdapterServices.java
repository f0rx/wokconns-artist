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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wokconns.wokconns.databinding.AdapterServicesHomeBinding;
import com.wokconns.wokconns.dto.ProductDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.AdapterServicesBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.ui.activity.Services;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by VARUN on 01/01/19.
 */

public class AdapterServices extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    LayoutInflater mLayoutInflater;
    private ArrayList<ProductDTO> productDTOList;
    private Context context;
    private HashMap<String, String> parms = new HashMap<>();
    private String TAG = AdapterServices.class.getSimpleName();
    private DialogInterface dialog_book;
    AdapterServicesBinding binding;
    AdapterServicesHomeBinding bindingHome;
    private String type = "";
    private final int VIEW_SERVICE = 1;
    private final int VIEW_HOME = 0;

    public AdapterServices(Context context, ArrayList<ProductDTO> productDTOList, String type) {
        this.context = context;
        this.productDTOList = productDTOList;
        this.type = type;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_HOME) {
            bindingHome = DataBindingUtil.inflate(mLayoutInflater, R.layout.adapter_services_home, parent, false);
            vh = new AdapterServices.MyViewHolderHomeService(bindingHome);
        } else {
            binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.adapter_services, parent, false);
            vh = new AdapterServices.MyViewHolder(binding);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterServices.MyViewHolderHomeService) {
            MyViewHolderHomeService myViewHolder = (MyViewHolderHomeService) holder;

            myViewHolder.binding.CTVproductname.setText(productDTOList.get(position).getProduct_name());
            myViewHolder.binding.CTVproductprice.setText(productDTOList.get(position).getCurrency_type() + productDTOList.get(position).getPrice());

            Glide.with(context).
                    load(productDTOList.get(position).getProduct_image())
                    .placeholder(R.drawable.bg)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(myViewHolder.binding.IVproduct);
        } else {
            MyViewHolder myViewHolder = (MyViewHolder) holder;

            myViewHolder.binding.CTVproductname.setText(productDTOList.get(position).getProduct_name());
            myViewHolder.binding.CTVproductprice.setText(productDTOList.get(position).getCurrency_type() + productDTOList.get(position).getPrice());

            Glide.with(context).
                    load(productDTOList.get(position).getProduct_image())
                    .placeholder(R.drawable.bg)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(myViewHolder.binding.IVproduct);

            myViewHolder.binding.ivDelete.setOnClickListener(v -> {
                parms.put(Consts.PRODUCT_ID, productDTOList.get(position).getId());
                deleteDialog();
            });
        }
    }

    @Override
    public int getItemCount() {
        return productDTOList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        AdapterServicesBinding binding;

        public MyViewHolder(@NonNull AdapterServicesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class MyViewHolderHomeService extends RecyclerView.ViewHolder {
        AdapterServicesHomeBinding binding;

        public MyViewHolderHomeService(AdapterServicesHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int local = VIEW_HOME;
        if (type.equalsIgnoreCase("home")) {
            local = VIEW_HOME;
        } else if (type.equalsIgnoreCase("services")) {
            local = VIEW_SERVICE;
        }
        return local;
    }

    public void deleteDialog() {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(context.getResources().getString(R.string.delete_service))
                    .setMessage(context.getResources().getString(R.string.delete_service_msg))
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
        new HttpsRequest(Consts.DELETE_PRODUCT_API, parms, context).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                if (type.equalsIgnoreCase("services")) {
                    ((Services) context).getArtist();
                }
            } else {
                ProjectUtils.showLong(context, msg);
            }
        });
    }

}