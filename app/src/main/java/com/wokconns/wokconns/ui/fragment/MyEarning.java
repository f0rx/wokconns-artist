package com.wokconns.wokconns.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wokconns.wokconns.dto.CurrencyDTO;
import com.wokconns.wokconns.dto.EarningDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.FragmentMyEarningBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.activity.PayoutHistory;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyEarning extends Fragment {
    private View view;
    private final String TAG = MyEarning.class.getSimpleName();
    private EarningDTO earningDTO;
    private ArrayList<EarningDTO.ChartData> chartDataList;
    private final HashMap<String, String> params = new HashMap<>();
    private final HashMap<String, String> paramsRequest = new HashMap<>();
    private SharedPrefrence prefrence;
    private UserDTO userDTO;
    private BaseActivity baseActivity;

    List<String> list = new ArrayList<>();
    String[] stringArray;
    private DialogInterface dialog_book;
    FragmentMyEarningBinding binding;
    private ArrayList<CurrencyDTO> currencyDTOArrayList = new ArrayList<>();
    String currencyCode = "";
    private final HashMap<String, String> paramsUpdate = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_earning, container, false);
        view = binding.getRoot();

        baseActivity.headerNameTV.setText(getResources().getString(R.string.my_earnings));
        prefrence = SharedPrefrence.getInstance(getActivity());
        userDTO = prefrence.getParentUser(Consts.USER_DTO);
        params.put(Consts.ARTIST_ID, "14");
//        params.put(Consts.ARTIST_ID, userDTO.getUser_id());
        paramsRequest.put(Consts.USER_ID, userDTO.getUser_id());

        setUiAction();
        return view;
    }

    public void setUiAction() {

        getCurrencyValue();

        binding.etCurrency.setOnClickListener(v -> binding.etCurrency.showDropDown());

        binding.etCurrency.setOnItemClickListener((parent, view, position, id) -> {
            binding.etCurrency.showDropDown();
            CurrencyDTO currencyDTO = (CurrencyDTO) parent.getItemAtPosition(position);
            Log.e(TAG, "onItemClick: " + currencyDTO.getCode());

            currencyCode = currencyDTO.getCode();
            params.put(Consts.CURRENCY_CODE, currencyCode);

            try {
                getEarning();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.tvPayoutHistory.setOnClickListener(v -> {
            if (NetworkManager.isConnectToInternet(getActivity())) {
                Intent intent = new Intent(baseActivity, PayoutHistory.class);
                startActivity(intent);
            } else {
                ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (NetworkManager.isConnectToInternet(getActivity())) {
            getEarning();
        } else {
            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
        }
    }

    public void getEarning() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.MY_EARNING1_API, params, getActivity()).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                try {
                    earningDTO = new Gson().fromJson(response.getJSONObject("data").toString(), EarningDTO.class);
                    showData();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                ProjectUtils.showLong(getActivity(), msg);
            }
        });

    }

    public void requestPayment() {

        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.WALLET_REQUEST_API, paramsRequest, getActivity()).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                ProjectUtils.showLong(getActivity(), msg);
                dialog_book.dismiss();
            } else {
                ProjectUtils.showLong(getActivity(), msg);
            }
        });

    }

    public void getCurrencyValue() {
        new HttpsRequest(Consts.GET_CURRENCY_API, baseActivity).stringGet(TAG, (flag, msg, response) -> {
            if (flag) {
                try {
                    currencyDTOArrayList = new ArrayList<>();
                    Type getCurrencyDTO = new TypeToken<List<CurrencyDTO>>() {
                    }.getType();
                    currencyDTOArrayList = new Gson().fromJson(response.getJSONArray("data").toString(), getCurrencyDTO);

                    try {
                        ArrayAdapter<CurrencyDTO> currencyAdapter = new ArrayAdapter<>(baseActivity, android.R.layout.simple_list_item_1, currencyDTOArrayList);
                        binding.etCurrency.setAdapter(currencyAdapter);
                        binding.etCurrency.setCursorVisible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                ProjectUtils.showToast(baseActivity, msg);
            }
        });
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }

    public void showData() {

        binding.tvOnlineEarning.setText(earningDTO.getCurrency_symbol() + earningDTO.getOnlineEarning());
        binding.tvCashEarning.setText(earningDTO.getCurrency_symbol() + earningDTO.getCashEarning());
//        binding.tvWalletAmount.setText(earningDTO.getCurrency_symbol() + earningDTO.getWalletAmount());
        binding.tvTotalEarning.setText(earningDTO.getCurrency_symbol() + earningDTO.getTotalEarning());
        binding.tvJobDone.setText(earningDTO.getJobDone());
        binding.tvTotalJob.setText(earningDTO.getTotalJob());
        binding.tvCompletePercentages.setText(earningDTO.getCompletePercentages() + " %");


        chartDataList = new ArrayList<>();
        chartDataList = earningDTO.getChartData();

        for (int i = 0; i < chartDataList.size(); i++) {

            list.add(chartDataList.get(i).getDay());
        }

        stringArray = list.toArray(new String[0]);


        binding.chart1.setDrawBarShadow(false);
        binding.chart1.setDrawValueAboveBar(true);

        binding.chart1.getDescription().setEnabled(false);

        binding.chart1.setMaxVisibleValueCount(60);

        binding.chart1.setPinchZoom(false);

        binding.chart1.setDrawGridBackground(false);


        XAxis xAxis = binding.chart1.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter((value, axis) -> stringArray[(int) value % stringArray.length]);

        YAxis leftAxis = binding.chart1.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = binding.chart1.getAxisRight();
        rightAxis.setEnabled(false);
        Legend l = binding.chart1.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        setData(chartDataList);

    }


    private void setData(ArrayList<EarningDTO.ChartData> charts) {

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        for (int i = 0; i < charts.size(); i++) {

            yVals1.add(new BarEntry(i, Float.parseFloat(charts.get(i).getCount())));
        }


        BarDataSet set1;

        if (binding.chart1.getData() != null &&
                binding.chart1.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) binding.chart1.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            binding.chart1.getData().notifyDataChanged();
            binding.chart1.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, getResources().getString(R.string.earning_graph));

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            binding.chart1.setData(data);
            binding.chart1.invalidate();
        }
    }

    public void bookDailog() {
        try {
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(getResources().getString(R.string.payment))
                    .setMessage(getResources().getString(R.string.process_payment))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                        dialog_book = dialog;
                        requestPayment();

                    })
                    .setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
