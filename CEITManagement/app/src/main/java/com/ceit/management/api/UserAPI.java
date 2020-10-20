package com.ceit.management.api;

import com.ceit.management.auth.LoginCredentials;
import com.ceit.management.model.ResponseModel;
import com.ceit.management.model.UserInfoModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserAPI
{
    @POST("user/login")
    Call<ResponseModel> login(@Body LoginCredentials creds);

    @GET("user/{username}")
    Call<UserInfoModel> getUserInfo(@Path("username") String username);
}
