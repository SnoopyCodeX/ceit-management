package com.ceit.management.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServerResponse<E>
{
    @SerializedName("hasError")
    @Expose
    public boolean hasError;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("data")
    @Expose
    public List<E> data;
}
