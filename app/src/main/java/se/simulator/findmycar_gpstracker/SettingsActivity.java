package se.simulator.findmycar_gpstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (tm.getSimState() == tm.SIM_STATE_READY) {
            editor.putString(getString(R.string.saved_country_iso), tm.getSimCountryIso().toUpperCase());
        }
        else{
            editor.putString(getString(R.string.saved_country_iso), getString(R.string.default_country_iso));
        }
        editor.commit();

        getFragmentManager().beginTransaction()
                .replace(R.id.frame_settings,new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName(getString(R.string.pref_file_key));
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);
        }


    }
}
