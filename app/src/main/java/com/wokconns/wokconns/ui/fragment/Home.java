package com.wokconns.wokconns.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.wokconns.wokconns.dto.GalleryDTO;
import com.wokconns.wokconns.dto.HistoryDTO;
import com.wokconns.wokconns.dto.HomeBannerDTO;
import com.wokconns.wokconns.dto.HomeDataDTO;
import com.wokconns.wokconns.dto.HomeNearByJobsDTO;
import com.wokconns.wokconns.dto.HomeRecomendedDTO;
import com.wokconns.wokconns.dto.ProductDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.application.GlobalState;
import com.wokconns.wokconns.databinding.FragmentHomeBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.activity.ImageGallery;
import com.wokconns.wokconns.ui.activity.Services;
import com.wokconns.wokconns.ui.adapter.AdapterGallery;
import com.wokconns.wokconns.ui.adapter.AdapterInvoice;
import com.wokconns.wokconns.ui.adapter.AdapterNearBy;
import com.wokconns.wokconns.ui.adapter.AdapterRecommended;
import com.wokconns.wokconns.ui.adapter.AdapterServices;
import com.wokconns.wokconns.ui.adapter.HomeBannerPagerAdapter;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.util.ArrayList;
import java.util.HashMap;


public class Home extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private View view;
    private String TAG = Home.class.getSimpleName();
    private SharedPrefs preference;
    private UserDTO userDTO;

    HashMap<String, String> params = new HashMap<>();
    private BaseActivity baseActivity;
    FragmentHomeBinding binding;
    HomeDataDTO homeDataDTO;
    GlobalState globalState;

    ArrayList<HomeBannerDTO> bannerDTOArrayList = new ArrayList<>();
    HomeBannerPagerAdapter homeBannerPagerAdapter;

    AdapterNearBy nearByAdapter;
    LinearLayoutManager linearLayoutManager;
    ArrayList<HomeNearByJobsDTO> nearByJobsDTOArrayList = new ArrayList<>();

    LinearLayoutManager linearLayoutManager1;
    AdapterRecommended recommendedAdapter;
    ArrayList<HomeRecomendedDTO> recomendedDTOArrayList = new ArrayList<>();

    LinearLayoutManager linearLayoutManager2;
    AdapterInvoice invoiceAdapter;
    ArrayList<HistoryDTO> invoiceDTOArrayList = new ArrayList<>();

    LinearLayoutManager linearLayoutManager3;
    AdapterServices serviceAdapter;
    ArrayList<ProductDTO> servicesDTOArrayList = new ArrayList<>();

    LinearLayoutManager linearLayoutManager4;
    AdapterGallery galleryAdapter;
    ArrayList<GalleryDTO> galleryDTOArrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        view = binding.getRoot();
        baseActivity.headerNameTV.setText(getResources().getString(R.string.app_name));
        preference = SharedPrefs.getInstance(getActivity());
        userDTO = preference.getParentUser(Const.USER_DTO);

        setUiAction();
        return view;
    }

    public void setUiAction() {
        globalState = GlobalState.getInstance();
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvNearBy.setLayoutManager(linearLayoutManager);
        linearLayoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvRecommended.setLayoutManager(linearLayoutManager1);
        linearLayoutManager2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvLastInvoice.setLayoutManager(linearLayoutManager2);
        linearLayoutManager3 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvServices.setLayoutManager(linearLayoutManager3);
        linearLayoutManager4 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvGallery.setLayoutManager(linearLayoutManager4);

        if (globalState.getHomeData() != null) {
            homeDataDTO = globalState.getHomeData();
            setData();
        }

        binding.swipeRefreshLayout.setOnRefreshListener(this);
        binding.ivMenu.setOnClickListener(this);
        binding.tvSeeAll.setOnClickListener(this);
        binding.tvSeeAll1.setOnClickListener(this);
        binding.tvSeeAll2.setOnClickListener(this);
        binding.tvSeeAll3.setOnClickListener(this);
        binding.tvSeeAll4.setOnClickListener(this);

        params.put(Const.USER_ID, userDTO.getUser_id());
        params.put(Const.LATITUDE, ""+preference.getValue(Const.LATITUDE));
        params.put(Const.LONGITUDE, ""+preference.getValue(Const.LONGITUDE));
        params.put(Const.DISTANCE, "50");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llAddMoney:
                break;
            case R.id.iv_menu:
                if (baseActivity.drawer.isDrawerVisible(GravityCompat.START)) {
                    baseActivity.drawer.closeDrawer(GravityCompat.START);
                } else {
                    baseActivity.drawer.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.tv_see_all:
            case R.id.tv_see_all1:
                try {
                    baseActivity.ivSearch.setVisibility(View.VISIBLE);
                    baseActivity.rlheader.setVisibility(View.VISIBLE);

                    BaseActivity.navItemIndex = 1;
                    BaseActivity.CURRENT_TAG = BaseActivity.TAG_MAIN;
                    baseActivity.loadHomeFragment(new JobsFrag(), BaseActivity.CURRENT_TAG);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tv_see_all2:
                try {
                    baseActivity.ivSearch.setVisibility(View.GONE);
                    baseActivity.rlheader.setVisibility(View.VISIBLE);

                    BaseActivity.navItemIndex = 9;
                    BaseActivity.CURRENT_TAG = BaseActivity.TAG_HISTORY;
                    baseActivity.loadHomeFragment(new HistoryFragment(), BaseActivity.CURRENT_TAG);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tv_see_all3:
                Intent intent1 = new Intent(baseActivity, Services.class);
                baseActivity.startActivity(intent1);
                break;
            case R.id.tv_see_all4:
                Intent intent2 = new Intent(baseActivity, ImageGallery.class);
                baseActivity.startActivity(intent2);
                break;

        }
    }

    public void getHomeData() {
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Const.ARTIST_HOME_DATA, params, getActivity()).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            binding.swipeRefreshLayout.setRefreshing(false);
            if (flag) {
                binding.tvNo.setVisibility(View.GONE);
                try {
                    homeDataDTO = new Gson().fromJson(response.getJSONObject("data").toString(), HomeDataDTO.class);
                    globalState.setHomeData(homeDataDTO);
                    setData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                binding.tvNo.setVisibility(View.VISIBLE);
                binding.rvNearBy.setVisibility(View.GONE);
                binding.rvRecommended.setVisibility(View.GONE);
                binding.rvLastInvoice.setVisibility(View.GONE);
                binding.rvServices.setVisibility(View.GONE);
                binding.rvGallery.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.swipeRefreshLayout.post(() -> {

            Log.e("Runnable", "FIRST");
            if (NetworkManager.isConnectToInternet(getActivity())) {
                binding.swipeRefreshLayout.setRefreshing(true);
                getHomeData();

            } else {
                ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
            }
        }
        );

    }

    public void setData() {
        bannerDTOArrayList = homeDataDTO.getBanner();
        nearByJobsDTOArrayList = homeDataDTO.getNear_by_jobs();
        recomendedDTOArrayList = homeDataDTO.getRecomended();
        invoiceDTOArrayList = homeDataDTO.getInvoice();
        servicesDTOArrayList = homeDataDTO.getServices();
        galleryDTOArrayList = homeDataDTO.getGallery();

        if (bannerDTOArrayList.size() > 0) {
            homeBannerPagerAdapter = new HomeBannerPagerAdapter(Home.this, baseActivity, bannerDTOArrayList);
            binding.mViewPager.setAdapter(homeBannerPagerAdapter);
            binding.mViewPager.setCurrentItem(0);
            binding.tabDots.setViewPager(binding.mViewPager);
            binding.mViewPager.setNestedScrollingEnabled(false);
        }

        if (nearByJobsDTOArrayList.size() > 0) {
            binding.rlNearBy.setVisibility(View.VISIBLE);
            nearByAdapter = new AdapterNearBy(baseActivity, nearByJobsDTOArrayList, baseActivity);
            binding.rvNearBy.setAdapter(nearByAdapter);
            binding.rvNearBy.setNestedScrollingEnabled(false);
        } else {
            binding.rlNearBy.setVisibility(View.GONE);
        }

        if (recomendedDTOArrayList.size() > 0) {
            binding.rlRecommended.setVisibility(View.VISIBLE);
            recommendedAdapter = new AdapterRecommended(baseActivity, recomendedDTOArrayList, baseActivity);
            binding.rvRecommended.setAdapter(recommendedAdapter);
            binding.rvRecommended.setNestedScrollingEnabled(false);
        } else {
            binding.rlRecommended.setVisibility(View.GONE);
        }

        if (invoiceDTOArrayList.size() > 0) {
            binding.rlLastInvoice.setVisibility(View.VISIBLE);
            invoiceAdapter = new AdapterInvoice(
                    baseActivity, invoiceDTOArrayList, LayoutInflater.from(getActivity())
            );
            binding.rvLastInvoice.setAdapter(invoiceAdapter);
            binding.rvLastInvoice.setNestedScrollingEnabled(false);
        } else {
            binding.rlLastInvoice.setVisibility(View.GONE);
        }

        if (servicesDTOArrayList.size() > 0) {
            binding.rlServices.setVisibility(View.VISIBLE);
            serviceAdapter = new AdapterServices(baseActivity, servicesDTOArrayList, "home");
            binding.rvServices.setAdapter(serviceAdapter);
            binding.rvServices.setNestedScrollingEnabled(false);
        } else {
            binding.rlServices.setVisibility(View.GONE);
        }

        if (galleryDTOArrayList.size() > 0) {
            binding.rlGallery.setVisibility(View.VISIBLE);
            galleryAdapter = new AdapterGallery(baseActivity, galleryDTOArrayList, "home");
            binding.rvGallery.setAdapter(galleryAdapter);
            binding.rvGallery.setNestedScrollingEnabled(false);
        } else {
            binding.rlGallery.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        Log.e("ONREFREST_Firls", "FIRS");
        getHomeData();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        baseActivity = (BaseActivity) context;
    }
}
