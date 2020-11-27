package com.ceit.management;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ceit.management.net.InternetReceiver;
import com.ceit.management.util.Constants;
import com.ceit.management.util.PreferenceUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppInstance extends Application
{
    private static Retrofit retrofit;
    private static InternetReceiver internetReceiver;

    @Override
    public void onCreate()
    {
        super.onCreate();

        if(retrofit == null)
        {
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .baseUrl(Constants.BASE_URL)
                    .build();
        }

        if(internetReceiver == null)
            internetReceiver = new InternetReceiver();

        PreferenceUtil.bindWith(this);
        IntentFilter internetFilter = new IntentFilter();
        internetFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetReceiver, internetFilter);
    }

    public static void hookUpConnectivityListener(InternetReceiver.OnInternetConnectionChangedListener listener)
    {
        if(internetReceiver != null)
            internetReceiver.setOnInternetConnectionChangedListener(listener);
    }

    public static boolean isConnected(Context context)
    {
        return InternetReceiver.isConnected(context);
    }

    public static final Retrofit retrofit()
    {
        return retrofit;
    }

    public static final void checkSelfPermission(Context context)
    {
        ArrayList<String> permissions = new ArrayList<>();

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        String[] denied = new String[permissions.size()];
        permissions.toArray(denied);

        if(denied.length > 0)
            ActivityCompat.requestPermissions((AppCompatActivity) context, denied, Constants.CODE_PERMISSIONS);
    }
}
