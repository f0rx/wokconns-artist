package com.wokconns.wokconns.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wokconns.wokconns.dto.AppointmentDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.adapter.AdapterAppointmnet;
import com.wokconns.wokconns.utils.CustomTextViewBold;
import com.wokconns.wokconns.utils.ProjectUtils;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppointmentFrag extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private View view;
    private String TAG = HistoryFragment.class.getSimpleName();
    private RecyclerView RVhistorylist;
    private AdapterAppointmnet adapterAppointmnet;
    private ArrayList<AppointmentDTO> appointmentDTOSList;
    private LinearLayoutManager mLayoutManager;
    private SharedPrefs prefrence;
    private UserDTO userDTO;
    private CustomTextViewBold tvNo;
    private BaseActivity baseActivity;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_appointment, container, false);
        baseActivity.headerNameTV.setText(getResources().getString(R.string.my_appoin));
        prefrence = SharedPrefs.getInstance(getActivity());
        userDTO = prefrence.getParentUser(Const.USER_DTO);

        setUiAction(view);
        return view;
    }

    public void setUiAction(View v) {
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        tvNo = v.findViewById(R.id.tvNo);
        RVhistorylist = v.findViewById(R.id.RVhistorylist);

        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        RVhistorylist.setLayoutManager(mLayoutManager);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(() -> {

            Log.e("Runnable", "FIRST");
            if (NetworkManager.isConnectToInternet(getActivity())) {
                swipeRefreshLayout.setRefreshing(true);
                getHistroy();

            } else {
                ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
            }
        }
        );
    }



    public void getHistroy() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Const.GET_APPOINTMENT_API, getparm(), getActivity()).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            swipeRefreshLayout.setRefreshing(false);
            if (flag) {
                tvNo.setVisibility(View.GONE);
                RVhistorylist.setVisibility(View.VISIBLE);
                try {
                    appointmentDTOSList = new ArrayList<>();
                    Type getpetDTO = new TypeToken<List<AppointmentDTO>>() {
                    }.getType();
                    appointmentDTOSList = (ArrayList<AppointmentDTO>) new Gson().fromJson(response.getJSONArray("data").toString(), getpetDTO);
                    showData();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                tvNo.setVisibility(View.VISIBLE);
                RVhistorylist.setVisibility(View.GONE);
            }
        });
    }

    public HashMap<String, String> getparm() {
        HashMap<String, String> parms = new HashMap<>();
        parms.put(Const.USER_ID, userDTO.getUser_id());
        parms.put(Const.ROLE, "1");
        return parms;
    }

    public void showData() {
        adapterAppointmnet = new AdapterAppointmnet(AppointmentFrag.this, appointmentDTOSList,userDTO);
        RVhistorylist.setAdapter(adapterAppointmnet);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }


    @Override
    public void onRefresh() {
        Log.e("ONREFREST_Firls", "FIRS");
        getHistroy();
    }
}
