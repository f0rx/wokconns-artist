package com.wokconns.wokconns.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wokconns.wokconns.databinding.FragmentMyBookingsWorkBinding;
import com.wokconns.wokconns.dto.ArtistBookingDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.activity.PreviousWork;
import com.wokconns.wokconns.ui.adapter.AdapterMyBookingsWork;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.util.ArrayList;

public class MyBookingsWorkFrag extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private String TAG = MyBookingsWorkFrag.class.getSimpleName();
    private AdapterMyBookingsWork myBookingsWorkAdapter;
    private ArrayList<ArtistBookingDTO> artistBookingDTOList;
    private LinearLayoutManager mLayoutManager;
    private SharedPrefrence preference;
    private UserDTO userDTO;
    private LayoutInflater myInflater;
    private PreviousWork context;
    FragmentMyBookingsWorkBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_bookings_work, container, false);
        view = binding.getRoot();
        preference = SharedPrefrence.getInstance(getActivity());
        userDTO = preference.getParentUser(Consts.USER_DTO);
        myInflater = LayoutInflater.from(getActivity());
        setUiAction(view);
        return view;
    }

    public void setUiAction(View v) {
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        binding.rvBookingsWork.setLayoutManager(mLayoutManager);

        artistBookingDTOList = (context).artistDetailsDTO.getArtist_booking();
        showData();

        binding.swipeRefreshLayout.setOnRefreshListener(this);
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("Runnable", "FIRST");
                                        if (NetworkManager.isConnectToInternet(getActivity())) {
                                        } else {
                                            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                                        }
                                    }
                                }
        );

    }


    public void showData() {
        if (artistBookingDTOList.size() > 0) {
            binding.tvNo.setVisibility(View.GONE);
            binding.rvBookingsWork.setVisibility(View.VISIBLE);
            myBookingsWorkAdapter = new AdapterMyBookingsWork(context, artistBookingDTOList);
            binding.rvBookingsWork.setAdapter(myBookingsWorkAdapter);
        } else {
            binding.tvNo.setVisibility(View.VISIBLE);
            binding.rvBookingsWork.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        Log.e("ONREFREST_Firls", "FIRS");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (PreviousWork) context;
    }


    public void gotos() {
        Intent in = new Intent(getActivity(), BaseActivity.class);
        in.putExtra(Consts.SCREEN_TAG, Consts.START_BOOKING_ARTIST_NOTIFICATION);
        startActivity(in);
        context.finish();

    }
}
