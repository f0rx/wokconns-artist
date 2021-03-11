package com.wokconns.wokconns.application;

import androidx.multidex.MultiDexApplication;

import com.wokconns.wokconns.dto.HomeDataDTO;
import com.wokconns.wokconns.preferences.SharedPrefrence;


public class GlobalState extends MultiDexApplication {

    private static GlobalState mInstance;
    HomeDataDTO homeData;
    SharedPrefrence sharedPrefrence;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        sharedPrefrence = SharedPrefrence.getInstance(this);
    }


    public static synchronized GlobalState getInstance() {
        return mInstance;
    }

    public HomeDataDTO getHomeData() {
        return homeData;
    }

    public void setHomeData(HomeDataDTO homeData) {
        this.homeData = homeData;
    }
}
