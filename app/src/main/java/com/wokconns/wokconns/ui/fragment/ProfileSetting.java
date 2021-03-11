package com.wokconns.wokconns.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.databinding.FragmentProfileSettingBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.activity.LanguageSelection;
import com.wokconns.wokconns.ui.activity.SignInActivity;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.ui.activity.WebViewCommon;
import com.wokconns.wokconns.utils.CustomEditText;
import com.wokconns.wokconns.utils.CustomTextViewBold;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class ProfileSetting extends Fragment implements View.OnClickListener {
    private Dialog dialog_pass;
    private CustomTextViewBold tvYesPass, tvNoPass;
    private CustomEditText etOldPassD, etNewPassD, etConfrimPassD;
    private ImageView ivClose;
    private HashMap<String, String> params;
    private HashMap<String, String> paramsLogout = new HashMap<>();
    private HashMap<String, File> paramsFile = new HashMap<>();
    private SharedPrefrence prefrence;
    private UserDTO userDTO;
    private String TAG = ProfileSetting.class.getSimpleName();
    private View view;
    private BaseActivity baseActivity;
    private DialogInterface dd;
    private LinearLayout llChangePass, llLogout;
    FragmentProfileSettingBinding binding;
    String baseURL = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_setting, container, false);
        view = binding.getRoot();
        prefrence = SharedPrefrence.getInstance(getActivity());
        userDTO = prefrence.getParentUser(Consts.USER_DTO);

        baseActivity.headerNameTV.setText(getResources().getString(R.string.settings));
        setUiAction();
        return view;
    }

    public void setUiAction() {
        binding.llChangePass.setOnClickListener(this);
        binding.llLanguage.setOnClickListener(this);
        binding.llPrivacy.setOnClickListener(this);
        binding.llFaq.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llChangePass:
                if (NetworkManager.isConnectToInternet(getActivity())) {
                    dialogPassword();
                } else {
                    ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                }
                break;
            case R.id.ll_language:
                Intent intent = new Intent(baseActivity, LanguageSelection.class);
                intent.putExtra(Consts.TYPE, "1");
                startActivity(intent);
                break;
            case R.id.ll_privacy:
                baseURL = Consts.PRIVACY_URL;
                getURLForWebView();
                break;
            case R.id.ll_faq:
                baseURL = Consts.FAQ_URL;
                getURLForWebView();
                break;
        }
    }


    public void dialogPassword() {
        dialog_pass = new Dialog(getActivity()/*, android.R.style.Theme_Dialog*/);
        dialog_pass.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_pass.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_pass.setContentView(R.layout.dailog_password);

        ivClose = (ImageView) dialog_pass.findViewById(R.id.iv_close);
        etOldPassD = (CustomEditText) dialog_pass.findViewById(R.id.etOldPassD);
        etNewPassD = (CustomEditText) dialog_pass.findViewById(R.id.etNewPassD);
        etConfrimPassD = (CustomEditText) dialog_pass.findViewById(R.id.etConfrimPassD);

        etOldPassD.setTransformationMethod(new PasswordTransformationMethod());
        etNewPassD.setTransformationMethod(new PasswordTransformationMethod());
        etConfrimPassD.setTransformationMethod(new PasswordTransformationMethod());

        tvYesPass = (CustomTextViewBold) dialog_pass.findViewById(R.id.tvYesPass);
        dialog_pass.show();
        dialog_pass.setCancelable(false);

        tvYesPass.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        params = new HashMap<>();
                        params.put(Consts.USER_ID, userDTO.getUser_id());
                        params.put(Consts.PASSWORD, ProjectUtils.getEditTextValue(etOldPassD));
                        params.put(Consts.NEW_PASSWORD, ProjectUtils.getEditTextValue(etNewPassD));

                        if (NetworkManager.isConnectToInternet(getActivity())) {
                            Submit();

                        } else {
                            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                        }
                    }
                });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_pass.dismiss();
            }
        });
    }


    public void updateProfile() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.UPDATE_PROFILE_API, params, paramsFile, getActivity()).imagePost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {
                    try {
                        ProjectUtils.showToast(getActivity(), msg);

                        userDTO = new Gson().fromJson(response.getJSONObject("data").toString(), UserDTO.class);
                        prefrence.setParentUser(userDTO, Consts.USER_DTO);
                        baseActivity.showImage();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    ProjectUtils.showToast(getActivity(), msg);
                }


            }
        });
    }

    public void logout() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.ARTIST_LOGOUT_API, paramsLogout, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {
                    ProjectUtils.showToast(getActivity(), msg);

                    dd.dismiss();
                    prefrence.clearAllPreferences();
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    baseActivity.finish();
                } else {
                    ProjectUtils.showToast(getActivity(), msg);
                }


            }
        });
    }

    public void confirmLogout() {
        try {
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(getResources().getString(R.string.app_name))
                    .setMessage(getResources().getString(R.string.logout_msg))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dd = dialog;
                            logout();

                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getURLForWebView() {
        if (prefrence.getValue(Consts.LANGUAGE_SELECTION).equalsIgnoreCase("")) {
            prefrence.setValue(Consts.LANGUAGE_SELECTION, "en");
        }
        new HttpsRequest(baseURL, baseActivity).stringGet(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                if (flag) {
                    try {
                        if (baseURL.equalsIgnoreCase(Consts.PRIVACY_URL)) {
                            Intent intent1 = new Intent(baseActivity, WebViewCommon.class);
                            intent1.putExtra(Consts.URL, msg);
                            intent1.putExtra(Consts.HEADER, getResources().getString(R.string.privacy_policy));
                            startActivity(intent1);
                        } else if (baseURL.equalsIgnoreCase(Consts.FAQ_URL)) {
                            Intent intent3 = new Intent(baseActivity, WebViewCommon.class);
                            intent3.putExtra(Consts.URL, msg);
                            intent3.putExtra(Consts.HEADER, getResources().getString(R.string.faq));
                            startActivity(intent3);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ProjectUtils.showToast(baseActivity, msg);
                }
            }
        });
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }

    private void Submit() {
        if (!passwordValidation()) {
            return;
        } else if (!checkpass()) {
            return;
        } else {
            if (NetworkManager.isConnectToInternet(getActivity())) {
                updateProfile();
                dialog_pass.dismiss();
            } else {
                ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
            }

        }
    }

    public boolean passwordValidation() {
        if (!ProjectUtils.isPasswordValid(etOldPassD.getText().toString().trim())) {
            etOldPassD.setError(getResources().getString(R.string.val_pass_c));
            etOldPassD.requestFocus();
            return false;
        } else if (!ProjectUtils.isPasswordValid(etNewPassD.getText().toString().trim())) {
            etNewPassD.setError(getResources().getString(R.string.val_pass_c));
            etNewPassD.requestFocus();
            return false;
        } else
            return true;

    }

    private boolean checkpass() {
        if (etNewPassD.getText().toString().trim().equals("")) {
            etNewPassD.setError(getResources().getString(R.string.val_new_pas));
            return false;
        } else if (etConfrimPassD.getText().toString().trim().equals("")) {
            etConfrimPassD.setError(getResources().getString(R.string.val_c_pas));
            return false;
        } else if (!etNewPassD.getText().toString().trim().equals(etConfrimPassD.getText().toString().trim())) {
            etConfrimPassD.setError(getResources().getString(R.string.val_n_c_pas));
            return false;
        }
        return true;
    }
}
