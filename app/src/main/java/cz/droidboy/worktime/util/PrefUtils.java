package cz.droidboy.worktime.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cz.droidboy.worktime.SettingsActivity;

/**
 * @author Jonas Sevcik
 */
public class PrefUtils {

    public static boolean hasSavedFilterData(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return prefs.getString(SettingsActivity.KEY_PREF_SSID, null) != null || prefs.getString(SettingsActivity.KEY_PREF_BSSID, null) != null || prefs.getString(SettingsActivity.KEY_PREF_CHANNELS, null) != null;
    }
}
