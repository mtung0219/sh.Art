package com.qi.shart;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceData {

    static final String PREF_LOGGEDIN_USER_EMAIL = "shart_logged_in_email";
    static final String PREF_USER_LOGGEDIN_STATUS = "shart_logged_in_status";
    static final String PREF_LOGGEDIN_USER_NAME = "shart_logged_in_username";
    private static final String PREFERENCE_FILE_NAME = "shartfile";

    public SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setLoggedInUserEmail(Context ctx, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LOGGEDIN_USER_EMAIL,email);
        editor.apply();
    }
    public static void setUserLoggedInStatus(Context ctx, boolean status)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(PREF_USER_LOGGEDIN_STATUS, status);
        editor.apply();
    }
    public static void setLoggedInUsername(Context ctx, String username) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_LOGGEDIN_USER_NAME,username);
        editor.apply();
    }

    public static String getLoggedInEmailUser(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_LOGGEDIN_USER_EMAIL, "");
    }
    public static String getLoggedInUsername(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_LOGGEDIN_USER_NAME, "");
    }

    public static boolean getUserLoggedInStatus(Context ctx)
    {
        return getSharedPreferences(ctx).getBoolean(PREF_USER_LOGGEDIN_STATUS, false);
    }

    public static void clearLoggedInInfo(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_LOGGEDIN_USER_EMAIL);
        editor.remove(PREF_USER_LOGGEDIN_STATUS);
        editor.remove(PREF_LOGGEDIN_USER_NAME);
        editor.apply();
    }


}
