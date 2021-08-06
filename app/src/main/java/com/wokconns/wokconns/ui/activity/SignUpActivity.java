package com.wokconns.wokconns.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.utils.CustomButton;
import com.wokconns.wokconns.utils.CustomEditText;
import com.wokconns.wokconns.utils.CustomTextView;
import com.wokconns.wokconns.utils.CustomTextViewBold;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private CustomEditText CETfirstname, CETMobileNumber, CETemailadd, CETenterpassword, CETenterpassagain, etReferal;
    private CustomButton CBsignup;
    private CustomTextView CTVsignin;
    private final String TAG = SignUpActivity.class.getSimpleName();
    private RelativeLayout RRsncbar;
    private SharedPrefrence prefrence;
    private UserDTO userDTO;
    private SharedPreferences firebase;
    private CustomTextViewBold tvTerms, tvPrivacy;
    private CheckBox termsCB;
    private ImageView ivEnterShow, ivReEnterShow;
    private boolean isHide = false;
    String baseURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProjectUtils.Fullscreen(SignUpActivity.this);
        setContentView(R.layout.activity_sign_up);
        mContext = SignUpActivity.this;
        prefrence = SharedPrefrence.getInstance(mContext);
        firebase = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Log.e("tokensss", firebase.getString(Consts.DEVICE_TOKEN, ""));
        setUiAction();
    }

    public void setUiAction() {
        RRsncbar = findViewById(R.id.RRsncbar);
        termsCB = findViewById(R.id.termsCB);
        etReferal = findViewById(R.id.etReferal);
        CETfirstname = findViewById(R.id.CETfirstname);
        CETemailadd = findViewById(R.id.CETemailadd);
        CETMobileNumber = findViewById(R.id.mobileNumber);
        CETenterpassword = findViewById(R.id.CETenterpassword);
        CETenterpassagain = findViewById(R.id.CETenterpassagain);
        CBsignup = findViewById(R.id.CBsignup);
        CTVsignin = findViewById(R.id.CTVsignin);
        tvTerms = findViewById(R.id.tvTerms);
        tvPrivacy = findViewById(R.id.tvPrivacy);

        CBsignup.setOnClickListener(this);
        CTVsignin.setOnClickListener(this);
        tvTerms.setOnClickListener(this);
        tvPrivacy.setOnClickListener(this);

        ivEnterShow = findViewById(R.id.ivEnterShow);
        ivReEnterShow = findViewById(R.id.ivReEnterShow);
        ivReEnterShow.setOnClickListener(this);
        ivEnterShow.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.CBsignup:
                clickForSubmit();
                break;
            case R.id.CTVsignin:
                startActivity(new Intent(mContext, SignInActivity.class));
                finish();
                break;
            case R.id.tvTerms:
                baseURL = Consts.TERMS_URL;
                getURLForWebView();
                break;
            case R.id.tvPrivacy:
                baseURL = Consts.PRIVACY_URL;
                getURLForWebView();
                break;
            case R.id.ivEnterShow:
                if (isHide) {
                    ivEnterShow.setImageResource(R.drawable.ic_pass_visible);
                    CETenterpassword.setTransformationMethod(null);
                    CETenterpassword.setSelection(CETenterpassword.getText().length());
                    isHide = false;
                } else {
                    ivEnterShow.setImageResource(R.drawable.ic_pass_invisible);
                    CETenterpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    CETenterpassword.setSelection(CETenterpassword.getText().length());
                    isHide = true;
                }
                break;
            case R.id.ivReEnterShow:
                if (isHide) {
                    ivReEnterShow.setImageResource(R.drawable.ic_pass_visible);
                    CETenterpassagain.setTransformationMethod(null);
                    CETenterpassagain.setSelection(CETenterpassagain.getText().length());
                    isHide = false;
                } else {
                    ivReEnterShow.setImageResource(R.drawable.ic_pass_invisible);
                    CETenterpassagain.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    CETenterpassagain.setSelection(CETenterpassagain.getText().length());
                    isHide = true;
                }
                break;
        }
    }

    public void register() {
        ProjectUtils.showProgressDialog(mContext, false, getResources().getString(R.string.please_wait));

        new HttpsRequest(Consts.REGISTER_API, getparm(), mContext).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();

            if (flag) {
                try {
                    ProjectUtils.showToast(mContext, String.format("%s %s!",
                            getResources().getString(R.string.registration_success_msg),
                            ProjectUtils.getEditTextValue(CETfirstname)));

                    Intent in = new Intent(mContext, OTPVerificationActivity.class);
                    in.putExtra(Consts.EMAIL, CETemailadd.getText().toString());
                    in.putExtra(Consts.MOBILE, CETMobileNumber.getText().toString());

                    startActivity(in);
                    finish();

                    overridePendingTransition(R.anim.anim_slide_in_left,
                            R.anim.anim_slide_out_left);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ProjectUtils.showToast(mContext, msg);
            }
        });
    }

    public void clickForSubmit() {
        if (!validation(CETfirstname, getResources().getString(R.string.val_name))) {
            return;
        } else if (!ProjectUtils.isEmailValid(CETemailadd.getText().toString().trim())) {
            showSickbar(getResources().getString(R.string.val_email));
        } else if (!validation(CETMobileNumber, getResources().getString(R.string.val_phone))) {
            showSickbar(getResources().getString(R.string.val_phone));
        } else if (!ProjectUtils.isPasswordValid(CETenterpassword.getText().toString().trim())) {
            showSickbar(getResources().getString(R.string.val_pass));
        } else if (!checkpass()) {
            return;
        } else if (!validateTerms()) {
            return;
        } else {
            if (NetworkManager.isConnectToInternet(mContext)) {
                register();
            } else {
                ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
            }
        }
    }

    private boolean validateTerms() {
        if (termsCB.isChecked()) {
            return true;
        } else {
            showSickbar(getResources().getString(R.string.trms_acc));
            ProjectUtils.showLong(mContext, getResources().getString(R.string.trms_acc));
            return false;
        }
    }

    private boolean checkpass() {
        if (CETenterpassword.getText().toString().trim().equals("")) {
            showSickbar(getResources().getString(R.string.val_pass));
            return false;
        } else if (CETenterpassagain.getText().toString().trim().equals("")) {
            showSickbar(getResources().getString(R.string.val_pass1));
            return false;
        } else if (!CETenterpassword.getText().toString().trim().equals(CETenterpassagain.getText().toString().trim())) {
            showSickbar(getResources().getString(R.string.val_pass3));
            return false;
        }
        return true;
    }

    public HashMap<String, String> getparm() {
        HashMap<String, String> parms = new HashMap<>();
        parms.put(Consts.NAME, ProjectUtils.getEditTextValue(CETfirstname));
        parms.put(Consts.EMAIL_ID, ProjectUtils.getEditTextValue(CETemailadd));
        parms.put(Consts.MOBILE, ProjectUtils.getEditTextValue(CETMobileNumber));
        parms.put(Consts.PASSWORD, ProjectUtils.getEditTextValue(CETenterpassword));
        parms.put(Consts.REFERRAL_CODE, ProjectUtils.getEditTextValue(etReferal));
        parms.put(Consts.ROLE, "1");
        parms.put(Consts.DEVICE_ID, "12345");
        parms.put(Consts.DEVICE_TYPE, "ANDROID");
        parms.put(Consts.DEVICE_TOKEN, firebase.getString(Consts.DEVICE_TOKEN, ""));
        return parms;
    }

    public void showSickbar(String msg) {
        Snackbar snackbar = Snackbar.make(RRsncbar, msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    public boolean validation(EditText editText, String msg) {
        if (!ProjectUtils.isEditTextFilled(editText)) {
            Snackbar snackbar = Snackbar.make(RRsncbar, msg, Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            snackbar.show();
            return false;
        } else {
            return true;
        }
    }

    private void getURLForWebView() {
        if (prefrence.getValue(Consts.LANGUAGE_SELECTION).equalsIgnoreCase(""))
            prefrence.setValue(Consts.LANGUAGE_SELECTION, "en");

        new HttpsRequest(baseURL, mContext).stringGet(TAG, (flag, msg, response) -> {
            if (flag) {
                try {
                    if (baseURL.equalsIgnoreCase(Consts.PRIVACY_URL)) {
                        Intent intent1 = new Intent(mContext, WebViewCommon.class);
                        intent1.putExtra(Consts.URL, msg);
                        intent1.putExtra(Consts.HEADER, getResources().getString(R.string.privacy_policy));
                        startActivity(intent1);
                    } else if (baseURL.equalsIgnoreCase(Consts.TERMS_URL)) {
                        Intent intent3 = new Intent(mContext, WebViewCommon.class);
                        intent3.putExtra(Consts.URL, msg);
                        intent3.putExtra(Consts.HEADER, getResources().getString(R.string.terms_of_use));
                        startActivity(intent3);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ProjectUtils.showToast(mContext, msg);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        startActivity(new Intent(mContext, SignInActivity.class));
        finish();
    }
}
