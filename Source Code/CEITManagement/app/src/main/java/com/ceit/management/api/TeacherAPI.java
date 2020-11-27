package com.ceit.management.api;

import androidx.annotation.Keep;

import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.StudentItem;
import com.ceit.management.pojo.TeacherItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

@Keep
public interface TeacherAPI
{
    @GET("teachers")
    Call<ServerResponse<TeacherItem>> getAllTeachers();

    @GET("teachers/removed")
    Call<ServerResponse<TeacherItem>> getAllRemovedTeachers();

    @GET("teachers/{id}")
    Call<ServerResponse<TeacherItem>> getTeacher(@Path("id") int id);

    @POST("teachers/new")
    Call<ServerResponse<TeacherItem>> addNewTeacher(@Body TeacherItem teacher);

    @POST("teachers/{id}/delete")
    Call<ServerResponse<TeacherItem>> deleteTeacher(@Path("id") int id);

    @POST("teachers/{id}/delete/permanent")
    Call<ServerResponse<TeacherItem>> permanentDeleteTeacher(@Path("id") int id);

    @POST("teachers/{id}/restore")
    Call<ServerResponse<TeacherItem>> restoreTeacher(@Path("id") int id);

    @POST("teachers/{id}/update")
    Call<ServerResponse<TeacherItem>> updateTeacher(@Path("id") int id,
                                                    @Body TeacherItem teacher);
}
