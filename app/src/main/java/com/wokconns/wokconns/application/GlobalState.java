package com.wokconns.wokconns.application;

import androidx.multidex.MultiDexApplication;

import com.wokconns.wokconns.dto.HomeDataDTO;
import com.wokconns.wokconns.preferences.SharedPrefs;


public class GlobalState extends MultiDexApplication {

    private static GlobalState mInstance;
    HomeDataDTO homeData;
    SharedPrefs sharedPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        sharedPrefs = SharedPrefs.getInstance(this);
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
