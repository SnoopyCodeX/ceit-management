package com.ceit.management.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class PreferenceUtil
{
    private static SharedPreferences sharedPreferences;
    private static Context context;

    public static final void bindWith(Context ctx)
    {
        context = ctx;
    }

    public static final SharedPreferences getPreference(String name, int mode)
    {
        return (sharedPreferences = context.getSharedPreferences(name, mode));
    }

    public static final SharedPreferences getPreference()
    {
        return (sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static final String getString(String key, String defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getString(key, defaultValue);
    }

    public static final boolean getBoolean(String key, boolean defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static final float getFloat(String key, float defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getFloat(key, defaultValue);
    }

    public static final long getLong(String key, long defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getLong(key, defaultValue);
    }

    public static final int getInt(String key, int defaultValue)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        return sharedPreferences.getInt(key, defaultValue);
    }

    public static final boolean putString(String key, String value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        return edit.commit();
    }

    public static final boolean putBoolean(String key, boolean value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(key, value);
        return edit.commit();
    }

    public static final boolean putInt(String key, int value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(key, value);
        return edit.commit();
    }

    public static final boolean putFloat(String key, float value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putFloat(key, value);
        return edit.commit();
    }

    public static final boolean putLong(String key, long value)
    {
        if(sharedPreferences == null)
            sharedPreferences = getPreference();

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong(key, value);
        return edit.commit();
    }
}
