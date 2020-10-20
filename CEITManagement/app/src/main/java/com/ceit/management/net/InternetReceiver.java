package com.ceit.management.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetReceiver extends BroadcastReceiver
{
    private OnInternetConnectionChangedListener listener;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
            return;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        boolean isConnected = (info != null) && ((info.getType() == ConnectivityManager.TYPE_MOBILE ||
                info.getType() == ConnectivityManager.TYPE_WIFI ||
                info.getType() == ConnectivityManager.TYPE_ETHERNET) && info.isConnected());

        if(listener != null)
            listener.onInternetConnectionChanged(isConnected);
    }

    public static boolean isConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        return (info != null) && ((info.getType() == ConnectivityManager.TYPE_MOBILE ||
                info.getType() == ConnectivityManager.TYPE_WIFI ||
                info.getType() == ConnectivityManager.TYPE_ETHERNET) && info.isConnected());
    }

    public void setOnInternetConnectionChangedListener(OnInternetConnectionChangedListener listener)
    {
        this.listener = listener;
    }

    public static interface OnInternetConnectionChangedListener
    {
        public void onInternetConnectionChanged(boolean isConnected);
    }
}