package com.wokconns.wokconns.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.utils.ProjectUtils;
import com.wokconns.wokconns.utils.CustomButton;
import com.wokconns.wokconns.utils.CustomEditText;
import com.wokconns.wokconns.utils.CustomTextView;
import com.wokconns.wokconns.utils.CustomTextViewBold;

import java.util.HashMap;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private CustomEditText CETemailadd, CETenterpassword;
    private CustomButton CBsignIn;
    private CustomTextViewBold CTVBforgot;
    private CustomTextView CTVsignup;
    private String TAG = SignInActivity.class.getSimpleName();
    private RelativeLayout RRsncbar;
    private SharedPrefrence prefrence;
    private UserDTO userDTO;
    private SharedPreferences firebase;
    private ImageView ivEnterShow;
    private boolean isHide = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProjectUtils.Fullscreen(SignInActivity.this);
        setContentView(R.layout.activity_sign_in);
        mContext = SignInActivity.this;
        prefrence = SharedPrefrence.getInstance(mContext);
        firebase = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Log.e("tokensss", Objects.requireNonNull(firebase.getString(Consts.DEVICE_TOKEN, "")));
        setUiAction();
    }

    public void setUiAction() {
        RRsncbar = findViewById(R.id.RRsncbar);
        CETemailadd = findViewById(R.id.CETemailadd);
        CETenterpassword = findViewById(R.id.CETenterpassword);
        CBsignIn = findViewById(R.id.CBsignIn);
        CTVsignup = findViewById(R.id.CTVsignup);
        CTVBforgot = findViewById(R.id.CTVBforgot);

        CBsignIn.setOnClickListener(this);
        CTVBforgot.setOnClickListener(this);
        CTVsignup.setOnClickListener(this);

        ivEnterShow = findViewById(R.id.ivEnterShow);
        ivEnterShow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.CTVBforgot:
                startActivity(new Intent(mContext, ForgotPass.class));
                break;
            case R.id.CBsignIn:
                clickForSubmit();
                break;
            case R.id.CTVsignup:
                startActivity(new Intent(mContext, SignUpActivity.class));
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
        }
    }

    public void login() {
        if (prefrence.getValue(Consts.LANGUAGE_SELECTION).equalsIgnoreCase("")) {
            prefrence.setValue(Consts.LANGUAGE_SELECTION, "en");
        }

        ProjectUtils.showProgressDialog(mContext, true, getResources().getString(R.string.please_wait));

//        _mockSignIn();

        new HttpsRequest(Consts.LOGIN_API, getparm(), mContext).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();

            if (flag) {
                try {
                    ProjectUtils.showToast(mContext, msg);

                    userDTO = new Gson().fromJson(response.getJSONObject("data").toString(), UserDTO.class);

                    prefrence.setParentUser(userDTO, Consts.USER_DTO);

                    prefrence.setBooleanValue(Consts.IS_REGISTERED, true);

                    ProjectUtils.showToast(mContext, msg);

                    Intent in = new Intent(mContext, BaseActivity.class);

                    finish();

                    startActivity(in);

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
        if (!ProjectUtils.isEmailValid(CETemailadd.getText().toString().trim())) {
            showSickbar(getResources().getString(R.string.val_email));
        } else if (!ProjectUtils.isPasswordValid(CETenterpassword.getText().toString().trim())) {
            showSickbar(getResources().getString(R.string.val_pass));
        } else {
            if (NetworkManager.isConnectToInternet(mContext)) {
                login();
            } else {
                ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
            }
        }


    }

    public HashMap<String, String> getparm() {
        HashMap<String, String> parms = new HashMap<>();
        parms.put(Consts.EMAIL_ID, ProjectUtils.getEditTextValue(CETemailadd));
        parms.put(Consts.PASSWORD, ProjectUtils.getEditTextValue(CETenterpassword));
        parms.put(Consts.DEVICE_TYPE, "ANDROID");
        parms.put(Consts.DEVICE_TOKEN, firebase.getString(Consts.DEVICE_TOKEN, ""));
        parms.put(Consts.DEVICE_ID, "12345");
        parms.put(Consts.ROLE, "1");
        Log.e(TAG + " Login", parms.toString());
        return parms;
    }

    public void showSickbar(String msg) {
        Snackbar snackbar = Snackbar.make(RRsncbar, msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        clickDone();
    }

    public void clickDone() {
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.app_name))
                .setMessage(getResources().getString(R.string.close_msg))
                .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                    dialog.dismiss();
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_HOME);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void _mockSignIn() {
        userDTO = new UserDTO("123456", "User 402",
                "user@admin.com", "passworD12", "https://firebasestorage.googleapis.com/v0/b/smartlets-x.appspot.com/o/assets%2Fdefault-user.png?alt=media&token=82e08454-1786-4f0f-989a-03605e489a64",
                "72 Congress Road", "Lekki Phase 1", "",
                "", "1", "", "", "",
                "", "User_Ref341", "Male", "Lagos",
                "Nigeria", "", "", "", "",
                "", "",
                "", "NG", "",
                "Access Bank (Diamond)", "0182928122", "",
                "Ben Joe Cherish", 1, 1);
        prefrence.setParentUser(userDTO, Consts.USER_DTO);

        prefrence.setBooleanValue(Consts.IS_REGISTERED, true);

        ProjectUtils.showToast(mContext, "Login Success!!");

        Intent in = new Intent(mContext, BaseActivity.class);
        startActivity(in);
        finish();
        overridePendingTransition(R.anim.anim_slide_in_left,
                R.anim.anim_slide_out_left);
    }
}
