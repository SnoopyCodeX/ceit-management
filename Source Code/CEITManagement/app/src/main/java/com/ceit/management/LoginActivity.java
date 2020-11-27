package com.ceit.management;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.ceit.management.model.ServerResponse;
import com.ceit.management.util.Constants;
import com.ceit.management.util.PreferenceUtil;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.ceit.management.auth.LoginCredentials;
import com.ceit.management.net.InternetReceiver.OnInternetConnectionChangedListener;
import com.ceit.management.api.UserAPI;
import com.ceit.management.util.DialogUtil;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("All")
public class LoginActivity extends AppCompatActivity implements OnInternetConnectionChangedListener
{
    private TextInputLayout inputUsername, inputPassword;
    private AppCompatButton btnLogin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        AppInstance.hookUpConnectivityListener(this);
        AppInstance.checkSelfPermission(this);

        if(PreferenceUtil.getBoolean(Constants.KEY_LOGGEDIN, false))
            proceed();

        if(!AppInstance.isConnected(this))
            DialogUtil.warningDialog(this, "Network Error", "You are not connected to an active network!", false);
        else
            DialogUtil.dismissDialog();
    }

    @Override
    public void onInternetConnectionChanged(boolean isConnected)
    {
        if(!isConnected)
            DialogUtil.warningDialog(this, "Network Error", "You are not connected to an active network!", false);
        else
            DialogUtil.dismissDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
       boolean allPermitted = false;
       switch (requestCode)
       {
           case Constants.CODE_PERMISSIONS:
               for(int i = 0; i < permissions.length; i++)
               {
                   String permission = permissions[i];
                   int result = grantResults[i];

                   switch (permission)
                   {
                       case Manifest.permission.ACCESS_NETWORK_STATE:
                       case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                           allPermitted = result == PackageManager.PERMISSION_GRANTED;
                           break;
                   }
               }
               break;
       }

       if(!allPermitted)
            DialogUtil.warningDialog(this, "Warning", "Please grant all the permissions or the app might fail to work as intended", false);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupViews()
    {
        inputUsername = findViewById(R.id.input_username);
        inputPassword = findViewById(R.id.input_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String username = inputUsername.getEditText().getText().toString();
                String password = inputPassword.getEditText().getText().toString();

                if(TextUtils.isEmpty(username) && !TextUtils.isEmpty(password))
                    inputUsername.setError("Username must not be empty!");
                else if(!TextUtils.isEmpty(username) && TextUtils.isEmpty(password))
                    inputPassword.setError("Password must not be empty!");
                else if(TextUtils.isEmpty(username) && TextUtils.isEmpty(password))
                {
                    inputUsername.setError("Username must not be empty!");
                    inputPassword.setError("Password must not be empty!");
                }
                else
                    login(LoginCredentials.newUser(username, password));
            }
        });
    }

    private void login(LoginCredentials credentials)
    {
        if(!AppInstance.isConnected(this))
        {
            DialogUtil.warningDialog(this, "Network Error", "You are not connected to an active network!", false);
            return;
        }

        DialogUtil.progressDialog(this, "Logging in...", getResources().getColor(R.color.themeColor), false);
        UserAPI api = AppInstance.retrofit().create(UserAPI.class);
        Call<ServerResponse> call = api.login(credentials);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call, @NotNull Response<ServerResponse> response)
            {
                DialogUtil.dismissDialog();
                ServerResponse model = response.body();

                if(model != null)
                {
                    String message = model.message;
                    boolean failed = model.hasError;

                    if(failed)
                    {
                        if(message.toLowerCase().contains("username") && !message.toLowerCase().contains("password"))
                            inputUsername.setError(message);
                        else if(message.toLowerCase().contains("password") && !message.toLowerCase().contains("username"))
                            inputPassword.setError(message);
                        else if(message.toLowerCase().contains("username") && message.toLowerCase().contains("password"))
                        {
                            inputUsername.setError(message);
                            inputPassword.setError(message);
                        }

                        DialogUtil.warningDialog(LoginActivity.this, "Login Failed", message, true);
                        return;
                    }

                    PreferenceUtil.putBoolean(Constants.KEY_LOGGEDIN, !failed);
                    proceed();
                    return;
                }

                DialogUtil.errorDialog(LoginActivity.this, "Login Failed", "Server error occurred while logging in", true);
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call, @NotNull Throwable t)
            {
                DialogUtil.dismissDialog();
                DialogUtil.errorDialog(LoginActivity.this, "Login Failed", t.getMessage(), "Okay", false);
                Log.e(LoginActivity.class.getSimpleName(), t.getMessage());
            }
        });
    }

    private void proceed()
    {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
