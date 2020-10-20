package com.ceit.management.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerResponse
{
    @SerializedName("hasError")
    @Expose
    public boolean hasError;

    @SerializedName("message")
    @Expose
    public String message;
}
