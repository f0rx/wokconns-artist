package com.wokconns.wokconns.ui.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;

import com.google.gson.Gson;
import com.wokconns.wokconns.dto.ArtistBookingDTO;
import com.wokconns.wokconns.dto.ArtistDetailsDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ActivityPreviousWorkBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.adapter.MyJobsWorkAdapter;
import com.wokconns.wokconns.ui.fragment.MyBookingsWorkFrag;
import com.wokconns.wokconns.ui.fragment.MyJobsWorkFrag;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PreviousWork extends AppCompatActivity implements View.OnClickListener {
    private String TAG = PreviousWork.class.getSimpleName();
    ActivityPreviousWorkBinding binding;
    Context context;
    private View view;
    public ArtistDetailsDTO artistDetailsDTO;
    private MyJobsWorkAdapter myJobsWorkAdapter;
    private ArrayList<ArtistBookingDTO> artistBookingDTOList;
    private Bundle bundle;
    private LinearLayoutManager mLayoutManagerReview;
    private HashMap<String, String> parms = new HashMap<>();
    private UserDTO userDTO;
    private SharedPrefrence prefrence;
    private MyBookingsWorkFrag appliedWorksFrag = new MyBookingsWorkFrag();
    private MyJobsWorkFrag myJobsWorkFrag = new MyJobsWorkFrag();
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        binding = DataBindingUtil.setContentView(this, R.layout.activity_previous_work);
        context = PreviousWork.this;
        prefrence = SharedPrefrence.getInstance(context);
        userDTO = prefrence.getParentUser(Consts.USER_DTO);
        parms.put(Consts.ARTIST_ID, userDTO.getUser_id());
        parms.put(Consts.USER_ID, userDTO.getUser_id());

        bundle = getIntent().getExtras();
        if (bundle != null) {
            artistDetailsDTO = (ArtistDetailsDTO) bundle.getSerializable(Consts.ARTIST_DTO);
        }

        showUiAction();
    }

    public void showUiAction() {
        binding.llBack.setOnClickListener(this);
        fragmentManager = getSupportFragmentManager();

        binding.tvMyJobs.setOnClickListener(this);
        binding.tvMyBookings.setOnClickListener(this);

        fragmentManager.beginTransaction().add(R.id.frame, myJobsWorkFrag).commit();

//        mLayoutManagerReview = new LinearLayoutManager(context.getApplicationContext());
//        binding.rvPreviousWork.setLayoutManager(mLayoutManagerReview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getArtist();
    }

    public void showData() {
        artistBookingDTOList = new ArrayList<>();
        artistBookingDTOList = artistDetailsDTO.getArtist_booking();

//        if (artistBookingDTOList.size() > 0) {
//            binding.tvNotFound.setVisibility(View.GONE);
//            binding.rvPreviousWork.setVisibility(View.VISIBLE);
//            myJobsWorkAdapter = new MyJobsWorkAdapter(context, artistBookingDTOList);
//            binding.rvPreviousWork.setAdapter(myJobsWorkAdapter);
//        } else {
//            binding.tvNotFound.setVisibility(View.VISIBLE);
//            binding.rvPreviousWork.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvMyJobs:
                setSelected(true, false);
                fragmentManager.beginTransaction().replace(R.id.frame, myJobsWorkFrag).commit();
                break;
            case R.id.tvMyBookings:
                setSelected(false, true);
                fragmentManager.beginTransaction().replace(R.id.frame, appliedWorksFrag).commit();
                break;
        }
    }

    public void getArtist() {
        new HttpsRequest(Consts.GET_ARTIST_BY_ID_API, parms, context).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                if (flag) {
                    try {

                        artistDetailsDTO = new Gson().fromJson(response.getJSONObject("data").toString(), ArtistDetailsDTO.class);
                        showData();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        });
    }

    public void setSelected(boolean firstBTN, boolean secondBTN) {
        if (firstBTN) {
            binding.tvMyJobs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            binding.tvMyJobs.setTextColor(getResources().getColor(R.color.white));
            binding.tvMyBookings.setBackgroundColor(getResources().getColor(R.color.white));
            binding.tvMyBookings.setTextColor(getResources().getColor(R.color.gray));
        }
        if (secondBTN) {
            binding.tvMyJobs.setBackgroundColor(getResources().getColor(R.color.white));
            binding.tvMyJobs.setTextColor(getResources().getColor(R.color.gray));
            binding.tvMyBookings.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            binding.tvMyBookings.setTextColor(getResources().getColor(R.color.white));


        }
        binding.tvMyJobs.setSelected(firstBTN);
        binding.tvMyBookings.setSelected(secondBTN);
    }
}
