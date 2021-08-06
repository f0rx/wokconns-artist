package com.wokconns.wokconns.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.gson.Gson;
import com.wokconns.wokconns.dto.ArtistDetailsDTO;
import com.wokconns.wokconns.dto.ProductDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ActivityServicesBinding;
import com.wokconns.wokconns.databinding.DailogArProductBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.adapter.AdapterServices;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Services extends AppCompatActivity implements View.OnClickListener {
    private String TAG = Services.class.getSimpleName();
    ActivityServicesBinding binding;
    Context context;
    private View view;
    private ArtistDetailsDTO artistDetailsDTO;
    private ArrayList<ProductDTO> productDTOList;
    private AdapterServices adapterServices;
    private Bundle bundle;
    private GridLayoutManager gridLayoutManager;
    private HashMap<String, String> paramsUpdate;
    private Dialog dialogEditProduct;
    private HashMap<String, File> paramsFile;
    private UserDTO userDTO;
    private SharedPrefrence prefrence;
    BottomSheet.Builder builder;
    private HashMap<String, String> parms = new HashMap<>();
    File file;
    DailogArProductBinding binding1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        binding = DataBindingUtil.setContentView(this, R.layout.activity_services);
        context = Services.this;
        binding.llServicesAdd.setOnClickListener(this);
        prefrence = SharedPrefrence.getInstance(context);
        userDTO = prefrence.getParentUser(Consts.USER_DTO);
        parms.put(Consts.ARTIST_ID, userDTO.getUser_id());
        parms.put(Consts.USER_ID, userDTO.getUser_id());

        bundle = getIntent().getExtras();
        if (bundle != null) {
            artistDetailsDTO = (ArtistDetailsDTO) bundle.getSerializable(Consts.ARTIST_DTO);
        }
        showUiAction();
    }

    public void showUiAction() {
        binding.llBack.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getArtist();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_services_add:
                Intent intent = new Intent(Services.this, AddServiceActivity.class);
                startActivity(intent);
                break;
            case R.id.llBack:
                finish();
                break;
        }
    }

    public void showData() {
        gridLayoutManager = new GridLayoutManager(context, 2);
        productDTOList = new ArrayList<>();
        productDTOList = artistDetailsDTO.getProducts();

        binding.tvNotFound.setVisibility(View.GONE);
        binding.rlView.setVisibility(View.VISIBLE);
        adapterServices = new AdapterServices(Services.this, productDTOList, "services");
        binding.rvServices.setLayoutManager(gridLayoutManager);
        binding.rvServices.setAdapter(adapterServices);

    }

    public void addServices() {
        dialogProduct();
    }

    public void dialogProduct() {
        paramsUpdate = new HashMap<>();
        paramsFile = new HashMap<>();
        dialogEditProduct = new Dialog(context/*, android.R.style.Theme_Dialog*/);
        dialogEditProduct.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogEditProduct.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding1 = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dailog_ar_product, null, false);

        dialogEditProduct.setContentView(binding1.getRoot());

        dialogEditProduct.show();
        dialogEditProduct.setCancelable(false);


        binding1.etImageD.setOnClickListener(v -> builder.show());
        binding1.tvNoPro.setOnClickListener(v -> dialogEditProduct.dismiss());
        binding1.tvYesPro.setOnClickListener(
                v -> {
                    paramsUpdate.put(Consts.USER_ID, userDTO.getUser_id());
                    paramsUpdate.put(Consts.PRODUCT_NAME, ProjectUtils.getEditTextValue(binding1.etProNameD));
                    paramsUpdate.put(Consts.PRICE, ProjectUtils.getEditTextValue(binding1.etRateProD));
                    paramsFile.put(Consts.PRODUCT_IMAGE, file);

                    if (NetworkManager.isConnectToInternet(context)) {
                        if (!validation(binding1.etImageD, getResources().getString(R.string.val_iamg_ad))) {
                            return;
                        } else if (!validation(binding1.etProNameD, getResources().getString(R.string.val_namepro))) {
                            return;
                        } else if (!validation(binding1.etRateProD, getResources().getString(R.string.val_rate))) {
                            return;
                        } else {
                            addProduct();
                        }
                    } else {
                        ProjectUtils.showToast(context, getResources().getString(R.string.internet_concation));
                    }
                });

    }

    public boolean validation(EditText editText, String msg) {
        if (!ProjectUtils.isEditTextFilled(editText)) {
            ProjectUtils.showLong(context, msg);
            return false;
        } else {
            return true;
        }
    }

    public void getArtist() {
        new HttpsRequest(Consts.GET_ARTIST_BY_ID_API, parms, context).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                try {

                    artistDetailsDTO = new Gson().fromJson(response.getJSONObject("data").toString(), ArtistDetailsDTO.class);
                    showData();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
            }
        });
    }

    public void addProduct() {
        ProjectUtils.showProgressDialog(context, true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.ADD_PRODUCT_API, paramsUpdate, paramsFile, context).imagePost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                ProjectUtils.showToast(context, msg);
//                    parentFrag.getArtist();
                dialogEditProduct.dismiss();
            } else {
                ProjectUtils.showToast(context, msg);
            }
        });
    }

}
