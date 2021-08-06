package com.wokconns.wokconns.https;

import android.util.Log;

import androidx.annotation.NonNull;

import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.interfacess.Consts;

import java.io.File;
import java.util.Map;

import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class ApiClientImpl {
    private static final String TAG = "ApiClientImpl";
    private final Map<String, String> params;
    private final Map<String, File> fileMapParams;
    private ApiClientFacade facade;

    public ApiClientImpl(Map<String, String> params) {
        this(params, null);
        facade = RetrofitClient.getInstance().facade();
    }

    public ApiClientImpl(Map<String, String> params, Map<String, File> fileMapParams) {
        this.params = params;
        this.fileMapParams = fileMapParams;
        facade = RetrofitClient.getInstance().facade();
    }

    public void updateArtisanProfile(final ApiClientResponse res) {
        String categoryId = params.get(Consts.CATEGORY_ID);
        String currencyId = params.get(Consts.ID);
        String userId = params.get(Consts.USER_ID);
        String name = params.get(Consts.NAME);
        String bio = params.get(Consts.BIO);
        String aboutUs = params.get(Consts.ABOUT_US);
        String city = params.get(Consts.CITY);
        String country = params.get(Consts.COUNTRY);
        String location = params.get(Consts.LOCATION);
        String price = params.get(Consts.PRICE);
        String latitude = params.get(Consts.LATITUDE);
        String longitude = params.get(Consts.LONGITUDE);

        Call<UserDTO> request = facade.updateArtistProfile(categoryId, currencyId,
                userId, name, bio, aboutUs, city, country,
                location, price, latitude, longitude);

        request.enqueue(new Callback<UserDTO>() {
            @Override
            public void onResponse(@NonNull Call<UserDTO> caller,
                                   @NonNull retrofit2.Response<UserDTO> response) {
                if (response.isSuccessful() && response.body() != null)
                    res.onResponse(response.isSuccessful(),
                            response.message(), response.body());
                else res.onResponse(false,
                        response.message(), response.body());
            }

            @Override
            public void onFailure(@NonNull Call<UserDTO> call, @NonNull Throwable t) {
                Log.i(TAG, "Server responded with failure ===>> " + t.getLocalizedMessage(), t);
            }
        });
    }
}
