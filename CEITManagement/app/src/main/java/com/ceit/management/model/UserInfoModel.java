package com.ceit.management.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserInfoModel
{
    @SerializedName("username")
    @Expose
    public String username;
}
