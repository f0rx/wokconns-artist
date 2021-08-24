package com.wokconns.wokconns.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.schibstedspain.leku.LocationPickerActivity;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.FragmentAddBankBinding;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.schibstedspain.leku.LocationPickerActivityKt.LATITUDE;
import static com.schibstedspain.leku.LocationPickerActivityKt.LONGITUDE;


public class AddBank extends Fragment implements View.OnClickListener {
    private View view;
    private String TAG = AddBank.class.getSimpleName();
    private SharedPrefrence prefrence;
    private UserDTO userDTO;
    private String status = "";

    HashMap<String, String> paramsGetAccount = new HashMap<>();
    HashMap<String, String> paramsAddAccount = new HashMap<>();
    private BaseActivity baseActivity;
    FragmentAddBankBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_bank, container, false);
        view = binding.getRoot();

        prefrence = SharedPrefrence.getInstance(getActivity());
        userDTO = prefrence.getParentUser(Consts.USER_DTO);

        setUiAction();
        return view;
    }

    public void setUiAction() {
        binding.back.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
//        binding.etBranchAddress.setOnClickListener(this);

        paramsGetAccount.put(Consts.ARTIST_ID, userDTO.getUser_id());

        getAccount();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                clickForSubmit();
                break;
            case R.id.back:
                if (baseActivity.drawer.isDrawerVisible(GravityCompat.START)) {
                    baseActivity.drawer.closeDrawer(GravityCompat.START);
                } else {
                    baseActivity.drawer.openDrawer(GravityCompat.START);
                }
                break;
//            case R.id.et_branch_address:
//                findPlace();
//                break;
        }
    }

    public void getAccount() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.GET_ACCOUNT_DETAIL, paramsGetAccount, getActivity()).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                try {
                    binding.etBankName.setText(response.getJSONObject("data").getString("bank_name"));
                    binding.etAccountNumber.setText(response.getJSONObject("data").getString("account_no"));
//                    binding.etBranchCode.setText(response.getJSONObject("data").getString("ifsc_code"));
                    binding.etNameCard.setText(response.getJSONObject("data").getString("account_holder_name"));
//                        binding.etBranchAddress.setText(response.getJSONObject("data").getString("bank_address"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void clickForSubmit() {
        if (!validateValue(binding.etBankName, getResources().getString(R.string.enter_bank_name))) {
            return;
        } else if (!validateValue(binding.etAccountNumber, getResources().getString(R.string.enter_bank_account))) {
            return;
        } else if (!validateValue(binding.etNameCard, getResources().getString(R.string.enter_name_on_card))) {
            return;
        } else {
            if (NetworkManager.isConnectToInternet(baseActivity)) {
                addAccount();
            } else {
                ProjectUtils.showToast(baseActivity, getResources().getString(R.string.internet_concation));
            }
        }
    }

    public void addAccount() {
        paramsAddAccount.put(Consts.ARTIST_ID, userDTO.getUser_id());
        paramsAddAccount.put(Consts.BANK_NAME, ProjectUtils.getEditTextValue(binding.etBankName));
        paramsAddAccount.put(Consts.ACCOUNT_NUMBER, ProjectUtils.getEditTextValue(binding.etAccountNumber));
//        paramsAddAccount.put(Consts.IFSC_CODE, ProjectUtils.getEditTextValue(binding.etBranchCode));
        paramsAddAccount.put(Consts.ACCOUNT_HOLDER_NAME, ProjectUtils.getEditTextValue(binding.etNameCard));
//        paramsAddAccount.put(Consts.BANK_ADDRESS, ProjectUtils.getEditTextValue(binding.etBranchAddress));

        new HttpsRequest(Consts.ADD_ACCOUNT_DETAIL, paramsAddAccount, getActivity()).stringPost(TAG,
                (flag, msg, response) -> {
                    ProjectUtils.pauseProgressDialog();
                    if (flag) {
                        try {
                            ProjectUtils.showToast(baseActivity, msg);
                            getAccount();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                    }
                });
    }


    public boolean validateValue(EditText editText, String message) {
        if (!ProjectUtils.isEditTextFilled(editText)) {
            editText.setError(message);
            editText.requestFocus();
            return false;
        } else {
            editText.setError(null);
            editText.clearFocus();
            return true;
        }
    }

    private void findPlace() {
        Intent locationPickerIntent = new LocationPickerActivity.Builder()
                .withGooglePlacesEnabled()
                //.withLocation(41.4036299, 2.1743558)
                .build(baseActivity);

        startActivityForResult(locationPickerIntent, 101);
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(baseActivity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
            Log.e("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

//            binding.etBranchAddress.setText(obj.getAddressLine(0));

//            lats = lat;
//            longs = lng;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                try {
                    getAddress(data.getDoubleExtra(LATITUDE, 0.0), data.getDoubleExtra(LONGITUDE, 0.0));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        baseActivity = (BaseActivity) context;
    }

}
