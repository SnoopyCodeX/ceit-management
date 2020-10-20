package com.ceit.management.auth;

public final class LoginCredentials
{
    public String username;
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
