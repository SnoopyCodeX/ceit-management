package com.ceit.management.api;

import com.ceit.management.model.ServerResponse;
import com.ceit.management.model.TeacherInfoModel;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface TeacherAPI
{
    @GET("teacher")
    Call<List<TeacherInfoModel>> getAllTeachers();

    @GET("teacher/{name}")
    Call<TeacherInfoModel> getTeacherInfo(@Path("name") String name);

    @Multipart
    @POST("teacher/new")
    Call<ServerResponse> addNewTeacher(@Part MultipartBody.Part photo,
                                       @Part("name") RequestBody name,
                                       @Part("position") RequestBody position,
                                       @Part("rank") RequestBody rank);
}
