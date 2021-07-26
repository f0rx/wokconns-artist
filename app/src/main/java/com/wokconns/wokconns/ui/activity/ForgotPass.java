package com.wokconns.wokconns.ui.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;


import com.wokconns.wokconns.R;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.utils.CustomButton;
import com.wokconns.wokconns.utils.CustomEditText;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.util.HashMap;

public class ForgotPass extends AppCompatActivity {
    private Context mContext;
    private CustomEditText etEmail;
    private CustomButton btnSubmit;
    private HashMap<String, String> parms = new HashMap<>();
    private String TAG = ForgotPass.class.getSimpleName();
    private LinearLayout llBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        mContext = ForgotPass.this;
        setUiAction();
    }

    public void setUiAction() {
        llBack = findViewById(R.id.llBack);
        etEmail = findViewById(R.id.etEmail);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> submitForm());
        llBack.setOnClickListener(v -> finish());
    }

    public void submitForm() {
        if (!ValidateMobile()) {
            return;
        } else {
            if (NetworkManager.isConnectToInternet(mContext)) {
                updatepass();

            } else {
                ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
            }
        }
    }


    public boolean ValidateMobile() {
        if (!ProjectUtils.isEmailValid(etEmail.getText().toString().trim())) {
            etEmail.setError(getResources().getString(R.string.val_email));
            etEmail.requestFocus();
            return false;
        }
        return true;
    }

    public void updatepass() {
        parms.put(Consts.EMAIL_ID, ProjectUtils.getEditTextValue(etEmail));
        ProjectUtils.showProgressDialog(mContext, false, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.FORGET_PASSWORD_API, parms, mContext).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                ProjectUtils.showToast(mContext, msg);
                finish();
                overridePendingTransition(R.anim.anim_slide_in_left,
                        R.anim.anim_slide_out_left);
            } else {
                ProjectUtils.showToast(mContext, msg);
            }
        });
    }

}
