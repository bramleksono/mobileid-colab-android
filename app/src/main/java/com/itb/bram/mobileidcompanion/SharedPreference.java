package com.itb.bram.mobileidcompanion;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * class to store value on sharedprererence
 */
public class SharedPreference {
    static final String TAG = "MobileID Companion";
    //sharedpreference string
    private static final String PROPERTY_GCMID = "GCMID";
    private static final String PROPERTY_IDNUMBER = "UserIdNumber";
    private static String PREF_NAME = "prefs";

    private SharedPreferences getAppPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
    }

    public void deleteGCM(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_GCMID);
        editor.commit();
        Log.i(TAG, "GCM ID deleted");
    }

    public void deleteIDNumber(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_IDNUMBER);
        editor.commit();
        Log.i(TAG, "ID number deleted");
    }

    public void storeGCM(Context context, String gcmid) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCMID, gcmid);
        editor.commit();
        Log.i(TAG, "Storing GCM ID");
    }

    public String getGCM(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String gcmid = prefs.getString(PROPERTY_GCMID, "");
        if (gcmid.isEmpty()) {
            Log.i(TAG, "There's no GCM ID in preferences");
            return null;
        }
        return gcmid;
    }

    public String getIDNumber(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String Content = prefs.getString(PROPERTY_IDNUMBER, "");
        if (Content.isEmpty()) {
            Log.i(TAG, "There's no ID Number in preferences");
            return null;
        }
        return Content;
    }
}
