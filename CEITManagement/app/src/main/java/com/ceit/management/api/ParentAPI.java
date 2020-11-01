package com.ceit.management.api;

import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.ParentItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ParentAPI
{
    @GET("parents")
    Call<ServerResponse<ParentItem>> getAllParents();

    @GET("parents/removed")
    Call<ServerResponse<ParentItem>> getAllRemovedParents();

    @GET("parents/{id}")
    Call<ServerResponse<ParentItem>> getParent(@Path("id") String id);

    @POST("parents/new")
    Call<ServerResponse<ParentItem>> addNewParent(@Body ParentItem parent);

    @POST("parents/{id}/delete")
    Call<ServerResponse<ParentItem>> deleteParent(@Path("id") String id);

    @POST("parents/{id}/restore")
    Call<ServerResponse<ParentItem>> restoreParent(@Path("id") String id);

    @POST("parents/{id}/update")
    Call<ServerResponse<ParentItem>> updateParent(@Path("id") String id,
                                                  @Body ParentItem parent);
}
