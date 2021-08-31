package com.wokconns.wokconns.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.wokconns.wokconns.R;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.utils.CustomButton;
import com.wokconns.wokconns.utils.CustomEditText;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.util.HashMap;

public class ForgotPass extends AppCompatActivity {
    private Context mContext;
    private CustomEditText etMobile;
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
        etMobile = findViewById(R.id.etMobile);
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
        if (!ProjectUtils.isPhoneNumberValid(etMobile.getText().toString().trim())) {
            etMobile.setError(getResources().getString(R.string.valid_mobile_number));
            etMobile.requestFocus();
            return false;
        }
        return true;
    }

    public void updatepass() {
        parms.put(Const.MOBILE, ProjectUtils.getEditTextValue(etMobile));

        ProjectUtils.showProgressDialog(mContext, false, getResources().getString(R.string.please_wait));

        new HttpsRequest(Const.FORGET_PASSWORD_API, parms, mContext).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                if (msg == null || msg.isEmpty())
                    ProjectUtils.showToast(mContext, String.format(
                            "%s %s",
                            getResources().getString(R.string.forgot_pass_success_msg),
                            ProjectUtils.getEditTextValue(etMobile)
                    ));

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
