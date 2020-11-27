package com.ceit.management.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.ceit.management.AppInstance;
import com.ceit.management.R;
import com.ceit.management.api.UserAPI;
import com.ceit.management.auth.UpdateCredentials;
import com.ceit.management.model.ServerResponse;
import com.ceit.management.util.DialogUtil;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("All")
public class SettingsFragment extends Fragment
{
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        TextInputLayout username = root.findViewById(R.id.input_username);
        TextInputLayout password = root.findViewById(R.id.input_password);
        TextInputLayout newUsername = root.findViewById(R.id.input_new_username);
        TextInputLayout newPassword = root.findViewById(R.id.input_new_password);
        AppCompatButton update = root.findViewById(R.id.btn_update);

        update.setOnClickListener(v -> {
            String str_username = username.getEditText().getText().toString();
            String str_password = password.getEditText().getText().toString();
            String str_newUsername = newUsername.getEditText().getText().toString();
            String str_newPassword = newPassword.getEditText().getText().toString();

            if(!AppInstance.isConnected(getContext()))
            {
                DialogUtil.errorDialog(getContext(), "Disconnected", "You are not connected to an active network", "Okay", false);
                return;
            }

            if(str_username.isEmpty())
                username.setError("Username is required");
            else if(str_password.isEmpty())
                password.setError("Password is required");
            else if(str_newUsername.isEmpty())
                newUsername.setError("New username is required");
            else if(str_newPassword.isEmpty())
                newPassword.setError("New password is required");
            else
            {
                DialogUtil.progressDialog(getContext(), "Updating credentials...", getContext().getResources().getColor(R.color.themeColor), false);
                UserAPI api = AppInstance.retrofit().create(UserAPI.class);
                Call<ServerResponse> call = api.update(UpdateCredentials.newUser(str_username, str_password, str_newUsername, str_newPassword));
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response)
                    {
                        DialogUtil.dismissDialog();
                        ServerResponse server = response.body();

                        if(server != null && !server.hasError) {
                            DialogUtil.successDialog(getContext(), "Update Success", "Your admin credentials has been updated successfully", "Okay", false);

                            username.getEditText().setText("");
                            password.getEditText().setText("");
                            newUsername.getEditText().setText("");
                            newPassword.getEditText().setText("");

                            username.getEditText().clearFocus();
                            password.getEditText().clearFocus();
                            newUsername.getEditText().clearFocus();
                            newPassword.getEditText().clearFocus();
                        }
                        else if(server != null && server.hasError)
                            DialogUtil.errorDialog(getContext(), "Update Failed", server.message, "Okay", false);
                        else
                            DialogUtil.errorDialog(getContext(), "Update Failed", "Server returned an unexpected response", "Okay", false);
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t)
                    {
                        DialogUtil.dismissDialog();
                        DialogUtil.errorDialog(getContext(), "Update Failed", t.getMessage(), "Okay", false);
                    }
                });
            }
        });

        return root;
    }
}
