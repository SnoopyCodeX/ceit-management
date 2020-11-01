package com.ceit.management.api;

import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.ClassItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ClassAPI
{
    @GET("classes")
    Call<ServerResponse<ClassItem>> getAllClasses();

    @GET("classes/removed")
    Call<ServerResponse<ClassItem>> getAllRemovedClasses();

    @GET("classes/{id}")
    Call<ServerResponse<ClassItem>> getClass(@Path("id") String id);

    @POST("classes/new")
    Call<ServerResponse<ClassItem>> addNewClass(@Body ClassItem classItem);

    @POST("classes/{id}/delete")
    Call<ServerResponse<ClassItem>> deleteClass(@Path("id") String id);

    @POST("classes/{classId}/remove/{studentId}")
    Call<ServerResponse<ClassItem>> removeStudentFromClass(@Path("classId") String classId,
                                                           @Path("studentId") String studentId);

    @POST("classes/{id}/restore")
    Call<ServerResponse<ClassItem>> restoreClass(@Path("id") String id);

    @POST("classes/{id}/update")
    Call<ServerResponse<ClassItem>> updateClass(@Path("id") String id,
                                                @Body ClassItem classItem);
}
