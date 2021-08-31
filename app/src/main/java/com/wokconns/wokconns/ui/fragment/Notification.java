package com.wokconns.wokconns.ui.fragment;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wokconns.wokconns.dto.NotificationDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.adapter.NotificationAdapter;
import com.wokconns.wokconns.utils.CustomTextViewBold;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Notification extends Fragment {
    private String TAG = Notification.class.getSimpleName();
    private RecyclerView RVnotification;
    private NotificationAdapter notificationAdapter;
    private ArrayList<NotificationDTO> notificationDTOlist;
    private LinearLayoutManager mLayoutManager;
    private SharedPrefs prefrence;
    private UserDTO userDTO;
    private CustomTextViewBold tvNo;
    private View view;
    private BaseActivity baseActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_notification, container, false);
        prefrence = SharedPrefs.getInstance(getActivity());
        userDTO = prefrence.getParentUser(Const.USER_DTO);
        baseActivity.headerNameTV.setText(getResources().getString(R.string.notifications));
        setUiAction(view);
        return view;
    }

    public void setUiAction(View v) {
        tvNo = v.findViewById(R.id.tvNo);
        RVnotification = v.findViewById(R.id.RVnotification);

        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        RVnotification.setLayoutManager(mLayoutManager);
    }



    @Override
    public void onResume() {
        super.onResume();
        if (NetworkManager.isConnectToInternet(getActivity())) {
            getNotification();

        } else {
            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
        }
    }

    public void getNotification() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Const.GET_NOTIFICATION_API, getparm(), getActivity()).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {

                tvNo.setVisibility(View.GONE);
                RVnotification.setVisibility(View.VISIBLE);
                try {
                    notificationDTOlist = new ArrayList<>();
                    Type getpetDTO = new TypeToken<List<NotificationDTO>>() {
                    }.getType();
                    notificationDTOlist = (ArrayList<NotificationDTO>) new Gson().fromJson(response.getJSONArray("my_notifications").toString(), getpetDTO);
                    showData();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                tvNo.setVisibility(View.VISIBLE);
                RVnotification.setVisibility(View.GONE);
            }
        });
    }

    public HashMap<String, String> getparm() {
        HashMap<String, String> parms = new HashMap<>();
        parms.put(Const.USER_ID, userDTO.getUser_id());
        return parms;
    }

    public void showData() {
        notificationAdapter = new NotificationAdapter(getActivity(), notificationDTOlist);
        RVnotification.setAdapter(notificationAdapter);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }
}
