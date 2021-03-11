package com.wokconns.wokconns.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ActivityPayoutHistoryBinding;
import com.wokconns.wokconns.dto.PayoutDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.adapter.AdapterPayoutHistory;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PayoutHistory extends AppCompatActivity implements View.OnClickListener {
    private String TAG = PayoutHistory.class.getSimpleName();
    Context context;
    ActivityPayoutHistoryBinding binding;
    private LinearLayoutManager mLayoutManager;
    private SharedPrefrence preference;
    private UserDTO userDTO;
    private AdapterPayoutHistory payoutHistoryAdapter;
    private ArrayList<PayoutDTO> payoutDTOList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payout_history);
        context = PayoutHistory.this;
        setUiAction();
    }

    public void setUiAction() {
        preference = SharedPrefrence.getInstance(context);
        userDTO = preference.getParentUser(Consts.USER_DTO);

        binding.llBack.setOnClickListener(this);

        mLayoutManager = new LinearLayoutManager(context);
        binding.rvPayout.setLayoutManager(mLayoutManager);

        if (NetworkManager.isConnectToInternet(context)) {
            getPayoutHistory();
        } else {
            ProjectUtils.showToast(context, getResources().getString(R.string.internet_concation));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
        }
    }

    public void showData() {
        if (payoutDTOList.size() > 0) {
            binding.tvNo.setVisibility(View.GONE);
            binding.rvPayout.setVisibility(View.VISIBLE);
            payoutHistoryAdapter = new AdapterPayoutHistory(context, payoutDTOList);
            binding.rvPayout.setAdapter(payoutHistoryAdapter);
        } else {
            binding.tvNo.setVisibility(View.VISIBLE);
            binding.rvPayout.setVisibility(View.GONE);
        }
    }

    public void getPayoutHistory() {
        ProjectUtils.showProgressDialog(context, true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.GET_PAYOUT_DATA, getparam(), context).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {

                    binding.tvNo.setVisibility(View.GONE);
                    binding.rvPayout.setVisibility(View.VISIBLE);
                    try {
                        payoutDTOList = new ArrayList<>();
                        Type getPayout = new TypeToken<List<PayoutDTO>>() {
                        }.getType();
                        payoutDTOList = (ArrayList<PayoutDTO>) new Gson().fromJson(response.getJSONObject("data").getJSONArray("payout").toString(), getPayout);
                        showData();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    binding.tvNo.setVisibility(View.VISIBLE);
                    binding.rvPayout.setVisibility(View.GONE);
                }
            }
        });
    }

    public HashMap<String, String> getparam() {
        HashMap<String, String> params = new HashMap<>();
        params.put(Consts.USER_ID, userDTO.getUser_id());
//        parms.put(Consts.USER_ID, "14");
        return params;
    }


}