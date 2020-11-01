package com.ceit.management.api;

import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.StudentItem;
import com.ceit.management.pojo.TeacherItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TeacherAPI
{
    @GET("teachers")
    Call<ServerResponse<TeacherItem>> getAllTeachers();

    @GET("teachers/removed")
    Call<ServerResponse<TeacherItem>> getAllRemovedTeachers();

    @GET("teachers/{id}")
    Call<ServerResponse<StudentItem>> getTeacher(@Path("id") String id);

    @POST("teachers/new")
    Call<ServerResponse<TeacherItem>> addNewTeacher(@Body TeacherItem teacher);

    @POST("teachers/{id}/delete")
    Call<ServerResponse<TeacherItem>> deleteTeacher(@Path("id") String id);

    @POST("teachers/{id}/restore")
    Call<ServerResponse<TeacherItem>> restoreTeacher(@Path("id") String id);

    @POST("teachers/{id}/update")
    Call<ServerResponse<TeacherItem>> updateTeacher(@Path("id") String id,
                                                    @Body TeacherItem teacher);
}
