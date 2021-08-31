package com.wokconns.wokconns.interfacess;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.databinding.DataBindingUtil;

import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.DialogDisclaimerBindingImpl;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.ui.activity.WebViewCommon;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public interface DisclaimerWarning {
    default void showDisclaimerDialog(Context mContext, Intent intent) {
        if (mContext == null) throw new AssertionError("Context cannot be null");

        if (!(mContext instanceof Activity)) throw new AssertionError(
                "Interface methods can only be called within an Activity");
        try {
            Dialog dialog = new Dialog(mContext);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            final DialogDisclaimerBindingImpl binding1 = DataBindingUtil.inflate(
                    LayoutInflater.from(mContext), R.layout.dialog_disclaimer, null, false
            );
            dialog.setContentView(binding1.getRoot());

            dialog.show();
            dialog.setCancelable(true);

            AtomicBoolean hasAcceptedTerms = new AtomicBoolean(binding1.termsCheck.isChecked());

            binding1.tvTerms.setOnClickListener(v -> getURLForWebView(Const.TERMS_URL, mContext));

            binding1.tvPrivacy.setOnClickListener(v -> getURLForWebView(Const.PRIVACY_URL, mContext));

            binding1.cancelButton.setOnClickListener(v -> dialog.dismiss());

            binding1.termsCheck.setOnCheckedChangeListener((v, isChecked) ->
                    hasAcceptedTerms.set(isChecked));

            binding1.consentButton.setOnClickListener(v -> {
                if (hasAcceptedTerms.get()) {
                    if (intent != null)
                        mContext.startActivity(intent);
                    dialog.dismiss();
                } else ProjectUtils.showToast(mContext, "Please accept Terms and Condition!");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    default void getURLForWebView(String baseURL, Context mContext) {
        if (mContext == null) throw new AssertionError("Context cannot be null");

        if (!(mContext instanceof Activity)) throw new AssertionError(
                "Interface methods can only be called within an Activity");

        new HttpsRequest(baseURL, mContext)
                .stringGet("DisclaimerWarningTag", (flag, msg, response) -> {
                    if (flag) {
                        try {
                            if (baseURL.equalsIgnoreCase(Const.PRIVACY_URL)) {
                                Intent intent1 = new Intent(mContext, WebViewCommon.class);
                                intent1.putExtra(Const.URL, msg);
                                intent1.putExtra(Const.HEADER, mContext.getResources().getString(R.string.privacy_policy));
                                mContext.startActivity(intent1);
                            } else if (baseURL.equalsIgnoreCase(Const.TERMS_URL)) {
                                Intent intent3 = new Intent(mContext, WebViewCommon.class);
                                intent3.putExtra(Const.URL, msg);
                                intent3.putExtra(Const.HEADER, mContext.getResources().getString(R.string.terms_of_use));
                                mContext.startActivity(intent3);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ProjectUtils.showToast(mContext, msg);
                    }
                });
    }
}
