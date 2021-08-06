package com.wokconns.wokconns.https;

import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.interfacess.Consts;

import okhttp3.MultipartBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiClientFacade {

    @Headers({"Content-Type: application/json", Consts.LANGUAGE + ":" + "en"})
    @PUT(Consts.UPDATE_PROFILE_ARTIST_API)
    @FormUrlEncoded
    Call<UserDTO> updateArtistProfile(@Field(Consts.CATEGORY_ID) String categoryId,
                                      @Field(Consts.ID) String currencyId,
                                      @Field(Consts.USER_ID) String userid,
                                      @Field(Consts.NAME) String name,
                                      @Field(Consts.BIO) String bio,
                                      @Field(Consts.ABOUT_US) String aboutUs,
                                      @Field(Consts.CITY) String city,
                                      @Field(Consts.COUNTRY) String country,
                                      @Field(Consts.LOCATION) String location,
                                      @Field(Consts.PRICE) String price,
                                      @Field(Consts.LATITUDE) String latitude,
                                      @Field(Consts.LONGITUDE) String longitude);

    @Multipart
    @POST(Consts.ARTIST_IMAGE_API)
    Call<Response> uploadProfileImage(@Part MultipartBody.Part image,
                                      @Part MultipartBody.Part userId);
}
