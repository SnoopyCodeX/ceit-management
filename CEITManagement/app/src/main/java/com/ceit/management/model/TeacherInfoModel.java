package com.ceit.management.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TeacherInfoModel
{
    @SerializedName("photoUrl")
    @Expose
    public String photoUrl;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("position")
    @Expose
    public String position;

    @SerializedName("rank")
    @Expose
    public String rank;
}
