package com.ceit.management.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseModel
{
    @SerializedName("username")
    @Expose
    public String username;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("hasError")
    @Expose
    public boolean hasError;
}
