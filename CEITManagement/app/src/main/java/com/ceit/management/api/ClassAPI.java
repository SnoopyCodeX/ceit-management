package com.ceit.management.api;

import androidx.annotation.Keep;

import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.ClassItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

@Keep
public interface ClassAPI
{
    @GET("classes")
    Call<ServerResponse<ClassItem>> getAllClasses();

    @GET("classes/removed")
    Call<ServerResponse<ClassItem>> getAllRemovedClasses();

    @GET("classes/{id}")
    Call<ServerResponse<ClassItem>> getClass(@Path("id") int id);

    @POST("classes/new")
    Call<ServerResponse<ClassItem>> addNewClass(@Body ClassItem classItem);

    @POST("classes/{id}/delete")
    Call<ServerResponse<ClassItem>> deleteClass(@Path("id") int id);

    @POST("classes/{id}/delete/permanent")
    Call<ServerResponse<ClassItem>> permanentDeleteClass(@Path("id") int id);

    @POST("classes/{classId}/remove/{studentId}")
    Call<ServerResponse<ClassItem>> removeStudentFromClass(@Path("classId") int classId,
                                                           @Path("studentId") String studentId);

    @POST("classes/{id}/restore")
    Call<ServerResponse<ClassItem>> restoreClass(@Path("id") int id);

    @POST("classes/{id}/update")
    Call<ServerResponse<ClassItem>> updateClass(@Path("id") int id,
                                                @Body ClassItem classItem);
}
