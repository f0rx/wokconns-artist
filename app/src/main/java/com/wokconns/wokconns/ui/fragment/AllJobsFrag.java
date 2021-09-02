package com.wokconns.wokconns.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.DailogFilterJobBinding;
import com.wokconns.wokconns.dto.AllJobsDTO;
import com.wokconns.wokconns.dto.CategoryDTO;
import com.wokconns.wokconns.dto.CurrencyDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.adapter.AllJobsAdapter;
import com.wokconns.wokconns.utils.CustomTextViewBold;
import com.wokconns.wokconns.utils.ProjectUtils;
import com.wokconns.wokconns.utils.SpinnerDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AllJobsFrag extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private final String TAG = AllJobsFrag.class.getSimpleName();
    private RecyclerView RVhistorylist;
    private AllJobsAdapter allJobsAdapter;
    private ArrayList<AllJobsDTO> allJobsDTOList;
    private ArrayList<AllJobsDTO> allJobsDTOListSection;
    private ArrayList<AllJobsDTO> allJobsDTOListSection1;
    private LinearLayoutManager mLayoutManager;
    private SharedPrefs prefrence;
    private UserDTO userDTO;
    private CustomTextViewBold tvNo;
    private LayoutInflater myInflater;
    private SearchView svSearch;
    private RelativeLayout rlSearch;
    private BaseActivity baseActivity;
    HashMap<String, String> parms = new HashMap<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    private final HashMap<String, String> params = new HashMap<>();
    private Dialog dialogFilterJob;
    DailogFilterJobBinding dailogFilterJobBinding;

    private final HashMap<String, String> parmsCategory = new HashMap<>();
    private ArrayList<CategoryDTO> categoryDTOS = new ArrayList<>();
    private ArrayList<CurrencyDTO> currencyDTOArrayList = new ArrayList<>();
    private SpinnerDialog spinnerDialogCate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_all_jobs, container, false);
        prefrence = SharedPrefs.getInstance(requireActivity());
        userDTO = prefrence.getParentUser(Const.USER_DTO);
        myInflater = LayoutInflater.from(requireActivity());
        parms.put(Const.ARTIST_ID, userDTO.getUser_id());
        parmsCategory.put(Const.USER_ID, userDTO.getUser_id());
        setUiAction(view);
        return view;
    }

    public void setUiAction(View v) {
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh_layout);
        rlSearch = v.findViewById(R.id.rlSearch);
        svSearch = v.findViewById(R.id.svSearch);
        tvNo = v.findViewById(R.id.tvNo);
        RVhistorylist = v.findViewById(R.id.RVhistorylist);
        mLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        RVhistorylist.setLayoutManager(mLayoutManager);

        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    if (newText.length() > 0) {
                        allJobsAdapter.filter(newText);
                    }
                } catch (Exception e) {

                }
                return false;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(() -> {

                    Log.e("Runnable", "FIRST");
                    if (NetworkManager.isConnectToInternet(requireActivity())) {
                        swipeRefreshLayout.setRefreshing(true);
                        getjobs();

                    } else {
                        ProjectUtils.showToast(requireActivity(), getResources().getString(R.string.internet_concation));
                    }
                }
        );
        baseActivity.ivSearch.setOnClickListener(v1 -> {

            if (NetworkManager.isConnectToInternet(requireActivity())) {
                if (rlSearch.getVisibility() == View.VISIBLE) {
                    baseActivity.ivSearch.setImageResource(R.drawable.ic_search_white);
                    rlSearch.setVisibility(View.GONE);
                } else {

                    baseActivity.ivSearch.setImageResource(R.drawable.ic_close_circle);
                    rlSearch.setVisibility(View.VISIBLE);

                }
            } else {
                ProjectUtils.showToast(requireActivity(), getString(R.string.internet_concation));
            }

        });

    }

    @Override
    public void onResume() {
        super.onResume();
        getCategory();
        getCurrencyValue();
    }

    public void getjobs() {
        new HttpsRequest(Const.GET_ALL_JOB_API, parms, requireActivity()).stringPost(TAG, (flag, msg, response) -> {
            swipeRefreshLayout.setRefreshing(false);
            if (flag) {
                tvNo.setVisibility(View.GONE);
                RVhistorylist.setVisibility(View.VISIBLE);
                baseActivity.ivSearch.setVisibility(View.VISIBLE);
                try {
                    allJobsDTOList = new ArrayList<>();
                    Type getpetDTO = new TypeToken<List<AllJobsDTO>>() {
                    }.getType();
                    allJobsDTOList = new Gson().fromJson(response.getJSONArray("data").toString(), getpetDTO);
                    showData();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                tvNo.setVisibility(View.VISIBLE);
                RVhistorylist.setVisibility(View.GONE);
                baseActivity.ivSearch.setVisibility(View.GONE);
            }
        });
    }

    public void showData() {
        allJobsAdapter = new AllJobsAdapter(AllJobsFrag.this, allJobsDTOList, userDTO, myInflater);
        RVhistorylist.setAdapter(allJobsAdapter);
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }

    @Override
    public void onRefresh() {
        getjobs();
        rlSearch.setVisibility(View.GONE);
    }

    public void setSection() {
        HashMap<String, ArrayList<AllJobsDTO>> has = new HashMap<>();
        allJobsDTOListSection = new ArrayList<>();
        for (int i = 0; i < allJobsDTOList.size(); i++) {


            if (has.containsKey(ProjectUtils.changeDateFormate1(allJobsDTOList.get(i).getJob_date()))) {
                allJobsDTOListSection1 = new ArrayList<>();
                allJobsDTOListSection1 = has.get(ProjectUtils.changeDateFormate1(allJobsDTOList.get(i).getJob_date()));
                allJobsDTOListSection1.add(allJobsDTOList.get(i));
                has.put(ProjectUtils.changeDateFormate1(allJobsDTOList.get(i).getJob_date()), allJobsDTOListSection1);


            } else {
                allJobsDTOListSection1 = new ArrayList<>();
                allJobsDTOListSection1.add(allJobsDTOList.get(i));
                has.put(ProjectUtils.changeDateFormate1(allJobsDTOList.get(i).getJob_date()), allJobsDTOListSection1);
            }
        }

        for (String key : has.keySet()) {
            AllJobsDTO allJobsDTO = new AllJobsDTO();
            allJobsDTO.setSection(true);
            allJobsDTO.setSection_name(key);
            allJobsDTOListSection.add(allJobsDTO);
            allJobsDTOListSection.addAll(has.get(key));

        }
        showData();
    }

    public void dialogAbout() {
        dialogFilterJob = new Dialog(baseActivity/*, android.R.style.Theme_Dialog*/);
        dialogFilterJob.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogFilterJob.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dailogFilterJobBinding = DataBindingUtil.inflate(LayoutInflater.from(baseActivity), R.layout.dailog_filter_job, null, false);
        dialogFilterJob.setContentView(dailogFilterJobBinding.getRoot());

        dailogFilterJobBinding.etCategoryD.setOnClickListener(v -> {
            if (NetworkManager.isConnectToInternet(baseActivity)) {
                if (categoryDTOS.size() > 0)
                    spinnerDialogCate.showSpinerDialog();
            } else {
                ProjectUtils.showToast(baseActivity, getResources().getString(R.string.internet_concation));
            }
        });

        try {
            if (currencyDTOArrayList.size() > 0) {
                ArrayAdapter<CurrencyDTO> currencyAdapter = new ArrayAdapter<>(baseActivity, android.R.layout.simple_list_item_1, currencyDTOArrayList);
                dailogFilterJobBinding.etCurrencyD.setAdapter(currencyAdapter);
                dailogFilterJobBinding.etCurrencyD.setCursorVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dailogFilterJobBinding.etCurrencyD.setOnClickListener(v -> dailogFilterJobBinding.etCurrencyD.showDropDown());

        dailogFilterJobBinding.etCurrencyD.setOnItemClickListener((parent, view, position, id) -> {
            dailogFilterJobBinding.etCurrencyD.showDropDown();
            CurrencyDTO currencyDTO = (CurrencyDTO) parent.getItemAtPosition(position);
            Log.e(TAG, "onItemClick: " + currencyDTO.getCurrency_symbol());
            params.put(Const.CURRENCY, currencyDTO.getCurrency_symbol());
        });

        dialogFilterJob.show();
        dialogFilterJob.setCancelable(false);

        dailogFilterJobBinding.tvCancel.setOnClickListener(v -> dialogFilterJob.dismiss());
        dailogFilterJobBinding.tvSubmit.setOnClickListener(
                v -> {
                    Log.e(TAG, "onClick: " + dailogFilterJobBinding.seekBar.getProgress());
                    filteredList();
                });
    }

    public void filteredList() {
        params.put(Const.PRICE, "" + dailogFilterJobBinding.seekBar.getProgress());
        new HttpsRequest(Const.JOB_FILTER, params, baseActivity).imagePost(TAG, (flag, msg, response) -> {
            dialogFilterJob.dismiss();
            if (flag) {

                try {
                    allJobsDTOList = new ArrayList<>();
                    Type getJobDTO = new TypeToken<List<AllJobsDTO>>() {
                    }.getType();
                    allJobsDTOList = new Gson().fromJson(response.getJSONArray("data").toString(), getJobDTO);
                    showData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ProjectUtils.showToast(baseActivity, msg);
            }
        });
    }

    public void getCategory() {
        new HttpsRequest(Const.GET_ALL_CATEGORY_API, parmsCategory, requireActivity()).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                try {
                    categoryDTOS = new ArrayList<>();
                    Type getpetDTO = new TypeToken<List<CategoryDTO>>() {
                    }.getType();
                    categoryDTOS = new Gson().fromJson(response.getJSONArray("data").toString(), getpetDTO);

                    spinnerDialogCate = new SpinnerDialog(baseActivity, categoryDTOS, getResources().getString(R.string.select_cate));// With 	Animation
                    spinnerDialogCate.bindOnSpinerListener((item, id, position) -> {
                        dailogFilterJobBinding.etCategoryD.setText(item);
                        params.put(Const.CATEGORY_ID, id);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
            }
        });
    }

    public void getCurrencyValue() {
        new HttpsRequest(Const.GET_CURRENCY_API, baseActivity).stringGet(TAG, (flag, msg, response) -> {
            if (flag) {
                try {
                    currencyDTOArrayList = new ArrayList<>();
                    Type getCurrencyDTO = new TypeToken<List<CurrencyDTO>>() {
                    }.getType();
                    currencyDTOArrayList = new Gson().fromJson(response.getJSONArray("data").toString(), getCurrencyDTO);

                    CurrencyDTO naira = null;

                    for (CurrencyDTO el : currencyDTOArrayList)
                        if (el.getCode().equalsIgnoreCase("NGN")) naira = el;

                    CurrencyDTO finalNaira = naira;
                    dailogFilterJobBinding.etCurrencyD.postDelayed(() -> {
                        dailogFilterJobBinding.etCurrencyD.setText(String.format("%s", finalNaira), false);

                        dailogFilterJobBinding.etCurrencyD.setSelection(
                                dailogFilterJobBinding.etCurrencyD.getText().length());
                    }, 500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ProjectUtils.showToast(baseActivity, msg);
            }
        });
    }

}
