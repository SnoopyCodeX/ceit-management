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
    Call<ServerResponse<ParentItem>> getParent(@Path("id") int id);

    @POST("parents/new")
    Call<ServerResponse<ParentItem>> addNewParent(@Body ParentItem parent);

    @POST("parents/{id}/delete")
    Call<ServerResponse<ParentItem>> deleteParent(@Path("id") int id);

    @POST("parents/{id}/delete/permanent")
    Call<ServerResponse<ParentItem>> permanentDeleteParent(@Path("id") int id);

    @POST("parents/{id}/restore")
    Call<ServerResponse<ParentItem>> restoreParent(@Path("id") int id);

    @POST("parents/{id}/update")
    Call<ServerResponse<ParentItem>> updateParent(@Path("id") int id,
                                                  @Body ParentItem parent);
}
