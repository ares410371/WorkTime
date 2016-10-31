package cz.droidboy.worktime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_SSID = "pref_ssid";
    public static final String KEY_PREF_BSSID = "pref_mac";
    public static final String KEY_PREF_CHANNELS = "pref_channels";

    private static final String CHANNELS_FORMAT = "([\\d]{1,2}(,|, ){0,1})+";

    private AppCompatAutoCompleteTextView vSsid;
    private AppCompatEditText vBssid;
    private AppCompatEditText vChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        vSsid = (AppCompatAutoCompleteTextView) findViewById(R.id.settings_ssid);
        vBssid = (AppCompatEditText) findViewById(R.id.settings_bssid);
        vChannels = (AppCompatEditText) findViewById(R.id.settings_channels);
        final View moreLayout = findViewById(R.id.settings_more_layout);
        final AppCompatButton more = (AppCompatButton) findViewById(R.id.settings_more_button);
        AppCompatButton save = (AppCompatButton) findViewById(R.id.settings_button);

        vChannels.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.save || id == EditorInfo.IME_NULL) {
                    attemptSave();
                    return true;
                }
                return false;
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moreLayout.getVisibility() == View.VISIBLE) {
                    moreLayout.setVisibility(View.GONE);
                    more.setText(R.string.more);
                } else {
                    moreLayout.setVisibility(View.VISIBLE);
                    more.setText(R.string.less);
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSave();
            }
        });

        if (savedInstanceState == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String ssid = prefs.getString(KEY_PREF_SSID, null);
            String bssid = prefs.getString(KEY_PREF_BSSID, null);
            String channels = prefs.getString(KEY_PREF_CHANNELS, null);

            if (ssid != null) {
                vSsid.setText(ssid);
            }
            if (bssid != null) {
                vBssid.setText(bssid);
            }
            if (channels != null) {
                vChannels.setText(channels);
            }
        }

        //fill autocomplete adapter
        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
        List<String> autoCompleteList;
        if (configurations != null) {
            final int size = configurations.size();
            Timber.d("configurations size " + size);
            autoCompleteList = new ArrayList<>(size);
            if (!configurations.isEmpty()) {
                for (int i = 0; i < size; i++) {
                    autoCompleteList.add(configurations.get(i).SSID.replaceAll("\"", ""));
                }
            }
        } else {
            Timber.d("configurations null");
            autoCompleteList = new ArrayList<>();
        }
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, autoCompleteList);
        vSsid.setAdapter(autoCompleteAdapter);
    }

    private boolean isSsidValid(String ssid) {
        return ssid.length() <= 32;
    }

    private boolean isBsidValid(String bsid) {
        return bsid.length() <= 17;
    }

    private boolean isChannelsValid(String channels) {
        return channels.matches(CHANNELS_FORMAT);
    }

    private void attemptSave() {
        // Reset errors.
        vSsid.setError(null);
        vBssid.setError(null);
        vChannels.setError(null);

        String ssid = vSsid.getText().toString();
        String bssid = vBssid.getText().toString();
        String channels = vChannels.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(ssid) && !isSsidValid(ssid)) {
            vSsid.setError(getString(R.string.error_ssid));
            focusView = vSsid;
            cancel = true;
        }

        if (!TextUtils.isEmpty(bssid) && !isBsidValid(bssid)) {
            vBssid.setError(getString(R.string.error_bssid));
            focusView = vBssid;
            cancel = true;
        }

        if (!TextUtils.isEmpty(channels) && !isChannelsValid(channels)) {
            vChannels.setError(getString(R.string.error_channels));
            focusView = vBssid;
            cancel = true;
        }

        if (TextUtils.isEmpty(ssid) && TextUtils.isEmpty(bssid) && TextUtils.isEmpty(channels)) {
            vSsid.setError(getString(R.string.error_nothing_specified));
            vBssid.setError(getString(R.string.error_nothing_specified));
            vChannels.setError(getString(R.string.error_nothing_specified));
            focusView = vSsid;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (ssid.isEmpty()) {
                ssid = null;
            }
            if (bssid.isEmpty()) {
                bssid = null;
            }
            if (channels.isEmpty()) {
                channels = null;
            }
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putString(KEY_PREF_SSID, ssid)
                    .putString(KEY_PREF_BSSID, bssid)
                    .putString(KEY_PREF_CHANNELS, channels)
                    .apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        }
    }

}
