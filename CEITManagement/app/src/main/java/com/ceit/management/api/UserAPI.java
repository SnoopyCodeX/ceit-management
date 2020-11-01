package com.ceit.management.api;

import com.ceit.management.auth.LoginCredentials;
import com.ceit.management.model.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserAPI
{
    @POST("user/login")
    Call<ServerResponse> login(@Body LoginCredentials credentials);
}
