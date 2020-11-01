package com.ceit.management.auth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class LoginCredentials
{
    @SerializedName("username")
    @Expose
    public String username;

    @SerializedName("password")
    @Expose
    public String password;

    public LoginCredentials(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public static LoginCredentials newUser(String username, String password)
    {
        return new LoginCredentials(username, password);
    }
}
