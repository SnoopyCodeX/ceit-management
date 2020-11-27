package com.ceit.management.api;

import androidx.annotation.Keep;

import com.ceit.management.auth.LoginCredentials;
import com.ceit.management.auth.UpdateCredentials;
import com.ceit.management.model.ServerResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

@Keep
public interface UserAPI
{
    @POST("user/login")
    Call<ServerResponse> login(@Body LoginCredentials credentials);

    @POST("user/update")
    Call<ServerResponse> update(@Body UpdateCredentials credentials);
}
