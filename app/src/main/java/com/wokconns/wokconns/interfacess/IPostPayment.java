package com.wokconns.wokconns.interfacess;

import android.app.Activity;

import com.wokconns.wokconns.R;
import com.wokconns.wokconns.dto.HistoryDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.util.HashMap;

public interface IPostPayment {
    String _kTAG = IPostPayment.class.getName();

    default void updatePaymentStatus(Activity activity,
                                     SharedPrefs prefrence, HashMap<String, String> params,
                                     HistoryDTO history) {
        if (prefrence.getValue(Const.SURL).equalsIgnoreCase(Const.PAYMENT_SUCCESS)) {
            prefrence.clearPreferences(Const.SURL);
            sendPayment(activity, params, history);
        } else if (prefrence.getValue(Const.FURL).equalsIgnoreCase(Const.PAYMENT_FAIL)) {
            prefrence.clearPreferences(Const.FURL);
            activity.finish();
        }
    }

    default void sendPayment(Activity activity, HashMap<String, String> params, HistoryDTO history) {
        ProjectUtils.showProgressDialog(activity, true, activity.getResources().getString(R.string.please_wait));
        new HttpsRequest(Const.MAKE_PAYMENT, params, activity).stringPost(_kTAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                ProjectUtils.showToast(activity, msg);
//                Intent in = new Intent(activity, WriteReview.class);
//                in.putExtra(Consts.HISTORY_DTO, history);
//                activity.startActivity(in);
                activity.finish();
            } else {
                ProjectUtils.showToast(activity, msg);
            }
        });
    }
}
