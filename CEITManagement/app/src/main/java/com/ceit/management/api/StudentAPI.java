package com.ceit.management.api;

import com.ceit.management.model.ServerResponse;
import com.ceit.management.pojo.StudentItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StudentAPI
{
    @GET("students")
    Call<ServerResponse<StudentItem>> getAllStudents();

    @GET("students/removed")
    Call<ServerResponse<StudentItem>> getAllRemovedStudents();

    @GET("students/{id}")
    Call<ServerResponse<StudentItem>> getStudent(@Path("id") int id);

    @POST("students/new")
    Call<ServerResponse<StudentItem>> addNewStudent(@Body StudentItem student);

    @POST("students/{id}/delete")
    Call<ServerResponse<StudentItem>> deleteStudent(@Path("id") int id);

    @POST("students/{id}/delete/permanent")
    Call<ServerResponse<StudentItem>> permanentDeleteStudent(@Path("id") int id);

    @POST("students/{id}/restore")
    Call<ServerResponse<StudentItem>> restoreStudent(@Path("id") int id);

    @POST("students/{id}/update")
    Call<ServerResponse<StudentItem>> updateStudent(@Path("id") int id,
                                                    @Body StudentItem student);
}
