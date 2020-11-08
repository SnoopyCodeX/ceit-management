package com.ceit.management.auth;

import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class UpdateCredentials
{
    @SerializedName("request")
    @Expose(serialize = true, deserialize = false)
    public String request;

    public UpdateCredentials(String oldUsername, String oldPassword, String newUsername, String newPassword)
    {
        oldUsername = Base64.encodeToString(oldUsername.getBytes(), Base64.DEFAULT);
        oldPassword = Base64.encodeToString(oldPassword.getBytes(), Base64.DEFAULT);
        newUsername = Base64.encodeToString(newUsername.getBytes(), Base64.DEFAULT);
        newPassword = Base64.encodeToString(newPassword.getBytes(), Base64.DEFAULT);

        this.request = oldUsername + '.' + oldPassword + '.' + newUsername + '.' + newPassword;
    }

    public static UpdateCredentials newUser(String oldUsername, String oldPassword, String newUsername, String newPassword)
    {
        return new UpdateCredentials(oldUsername, oldPassword, newUsername, newPassword);
    }
}
