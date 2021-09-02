package com.wokconns.wokconns.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.utils.CustomButton;
import com.wokconns.wokconns.utils.CustomEditText;
import com.wokconns.wokconns.utils.CustomTextView;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.util.HashMap;

public class AddMoney extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = AddMoney.class.getSimpleName();
    private Context mContext;
    private CustomEditText etAddMoney;
    private CustomTextView tv1000, tv1500, tv2000;
    private CustomButton cbAdd;
    float rs = 0;
    float rs1 = 0;
    float final_rs = 0;
   private final HashMap<String, String> parmas = new HashMap<>();
    private SharedPrefs prefrence;
    private UserDTO userDTO;
    private String amt = "";
    private String currency = "";
    private CustomTextView tvWallet;
    private ImageView ivBack;
    private Dialog dialog;
    private LinearLayout paystackButton, flutterwaveButton, llCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);
        mContext = AddMoney.this;
        prefrence = SharedPrefs.getInstance(mContext);
        userDTO = prefrence.getParentUser(Const.USER_DTO);
         parmas.put(Const.USER_ID, userDTO.getUser_id());
        setUiAction();
    }

    public void setUiAction() {
        tvWallet = findViewById(R.id.tvWallet);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        if (getIntent().hasExtra(Const.AMOUNT)) {
            amt = getIntent().getStringExtra(Const.AMOUNT);
            currency = getIntent().getStringExtra(Const.CURRENCY);

            tvWallet.setText(String.format("%s %s", currency, amt));
        }

        cbAdd = findViewById(R.id.cbAdd);
        cbAdd.setOnClickListener(this);

        etAddMoney = findViewById(R.id.etAddMoney);
        etAddMoney.setSelection(etAddMoney.getText().length());

        tv1000 = findViewById(R.id.tv1000);
        tv1000.setOnClickListener(this);

        tv1500 = findViewById(R.id.tv1500);
        tv1500.setOnClickListener(this);

        tv2000 = findViewById(R.id.tv2000);
        tv2000.setOnClickListener(this);

        tv1000.setText(String.format("+ %s 1000", currency));
        tv1500.setText(String.format("+ %s 1500", currency));
        tv2000.setText(String.format("+ %s 2000", currency));
    }

    @Override
    public void onClick(View v) {
        if (etAddMoney.getText().toString().trim().equalsIgnoreCase("")) {
            rs1 = 0;

        } else {
            rs1 = Float.parseFloat(etAddMoney.getText().toString().trim());

        }

        switch (v.getId()) {
            case R.id.tv1000:
                rs = 1000;
                final_rs = rs1 + rs;
                etAddMoney.setText(final_rs + "");
                etAddMoney.setSelection(etAddMoney.getText().length());
                break;
            case R.id.tv1500:
                rs = 1500;
                final_rs = rs1 + rs;
                etAddMoney.setText(final_rs + "");
                etAddMoney.setSelection(etAddMoney.getText().length());
                break;
            case R.id.tv2000:
                rs = 2000;
                final_rs = rs1 + rs;
                etAddMoney.setText(final_rs + "");
                etAddMoney.setSelection(etAddMoney.getText().length());
                break;
            case R.id.cbAdd:
                if (etAddMoney.getText().toString().length() > 0 && Float.parseFloat(etAddMoney.getText().toString().trim())>0) {
                    if (NetworkManager.isConnectToInternet(mContext)) {
                        parmas.put(Const.AMOUNT, ProjectUtils.getEditTextValue(etAddMoney));
                        dialogPayment();


                    } else {
                        ProjectUtils.showLong(mContext, getResources().getString(R.string.internet_concation));
                    }
                } else {
                    ProjectUtils.showLong(mContext, getResources().getString(R.string.val_money));
                }
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (prefrence.getValue(Const.SURL).equalsIgnoreCase(Const.PAYMENT_SUCCESS)) {
            prefrence.clearPreferences(Const.SURL);
            finish();
        } else if (prefrence.getValue(Const.FURL).equalsIgnoreCase(Const.PAYMENT_FAIL)) {
            prefrence.clearPreferences(Const.FURL);
            finish();
        }else if (prefrence.getValue(Const.SURL).equalsIgnoreCase(Const.PAYMENT_SUCCESS_paypal)) {
            prefrence.clearPreferences(Const.SURL);
            addMoney();
        }else if (prefrence.getValue(Const.FURL).equalsIgnoreCase(Const.PAYMENT_FAIL_Paypal)) {
            prefrence.clearPreferences(Const.FURL);
            finish();
        }
    }


    public void addMoney() {
        new HttpsRequest(Const.ADD_MONEY_API, parmas, mContext).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                ProjectUtils.showLong(mContext, msg);
                finish();
            } else {
                ProjectUtils.showLong(mContext, msg);
            }
        });
    }




    public void dialogPayment() {
        dialog = new Dialog(mContext/*, android.R.style.Theme_Dialog*/);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dailog_payment_option);


        ///dialog.getWindow().setBackgroundDrawableResource(R.color.black);
        paystackButton = dialog.findViewById(R.id.paystackButton);
        flutterwaveButton = dialog.findViewById(R.id.flutterwaveButton);
        llCancel = dialog.findViewById(R.id.llCancel);

        dialog.show();
        dialog.setCancelable(false);
        llCancel.setOnClickListener(v -> dialog.dismiss());
        paystackButton.setOnClickListener(v -> {
            Intent in2 = new Intent(mContext, PaymentWeb.class);
            in2.putExtra(Const.USER_DTO, userDTO);
            in2.putExtra(Const.AMOUNT, amt);
            in2.putExtra(Const.CURRENCY, currency);
            AddMoney.this.startActivity(in2);
            dialog.dismiss();
        });
        flutterwaveButton.setOnClickListener(v -> {
            Intent in2 = new Intent(mContext, PaymentWeb.class);
            in2.putExtra(Const.USER_DTO, userDTO);
            in2.putExtra(Const.AMOUNT, amt);
            in2.putExtra(Const.CURRENCY, currency);
            AddMoney.this.startActivity(in2);
            dialog.dismiss();
        });

    }

}
