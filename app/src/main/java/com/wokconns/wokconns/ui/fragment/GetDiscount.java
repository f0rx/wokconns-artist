package com.wokconns.wokconns.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.wokconns.wokconns.dto.DiscountDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.utils.CustomButton;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.utils.CustomTextView;
import com.wokconns.wokconns.utils.CustomTextViewBold;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.util.HashMap;

public class GetDiscount extends Fragment implements View.OnClickListener {
    private String TAG = GetDiscount.class.getSimpleName();
    private CustomTextView CTVdescription;
    private CustomTextViewBold CTVBcode, CTVBcopy;
    private CustomButton CBinvitefriend;
    private HashMap<String, String> parms = new HashMap<>();
    private DiscountDTO discountDTO;
    private String code = "";
    private ClipboardManager myClipboard;
    private ClipData myClip;
    private SharedPrefrence prefrence;
    private UserDTO userDTO;
    private View view;
    private BaseActivity baseActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_get_discount, container, false);

        prefrence = SharedPrefrence.getInstance(getActivity());

        userDTO = prefrence.getParentUser(Consts.USER_DTO);
        myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        parms.put(Consts.USER_ID, userDTO.getUser_id());
        baseActivity.headerNameTV.setText(getResources().getString(R.string.get_discount));

        setUiAction(view);
        return view;
    }


    public void setUiAction(View v) {
        CTVdescription = v.findViewById(R.id.CTVdescription);
        CTVBcode = v.findViewById(R.id.CTVBcode);
        CTVBcopy = v.findViewById(R.id.CTVBcopy);
        CBinvitefriend = v.findViewById(R.id.CBinvitefriend);
        CTVBcopy.setOnClickListener(this);
        CBinvitefriend.setOnClickListener(this);
        if (NetworkManager.isConnectToInternet(getActivity())) {
            getCode();
        } else {
            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.CTVBcopy:
                String text;
                text = CTVBcode.getText().toString().trim();
                myClip = ClipData.newPlainText("text", text);
                myClipboard.setPrimaryClip(myClip);
                ProjectUtils.showLong(getActivity(), getResources().getString(R.string.code_copy));
                break;
            case R.id.CBinvitefriend:
                share();
                break;

        }
    }

    public void getCode() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.GET_REFERRAL_CODE_API, parms, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {
                    try {
                        ProjectUtils.showToast(getActivity(), msg);

                        discountDTO = new Gson().fromJson(response.getJSONObject("my_referral_code").toString(), DiscountDTO.class);
                        showData();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        });
    }

    public void showData() {
        CTVdescription.setText(discountDTO.getDescription());
        CTVBcode.setText(discountDTO.getCode());

        code = discountDTO.getCode();
    }

    public void share() {
        try {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.referral_msg)+" " + code);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.app_name)));
        } catch (Exception e) {
            e.printStackTrace();
            ProjectUtils.showLong(getActivity(), getResources().getString(R.string.opss_share));
        }


    }
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }


}
