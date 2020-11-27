package com.ceit.management.auth;

import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class LoginCredentials
{
    @SerializedName("request")
    @Expose(serialize = true, deserialize = false)
    public String request;

    public LoginCredentials(String username, String password)
    {
        username = Base64.encodeToString(username.getBytes(), Base64.DEFAULT);
        password = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
        this.request = username + '.' + password;
    }

    public static LoginCredentials newUser(String username, String password)
    {
        return new LoginCredentials(username, password);
    }
}
