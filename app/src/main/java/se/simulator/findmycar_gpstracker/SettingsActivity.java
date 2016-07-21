package se.simulator.findmycar_gpstracker;

import android.content.Context;
import android.content.Intent;
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
import android.view.MenuItem;
import android.widget.Toast;


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
                .replace(R.id.frame_settings,new SettingsHeaderFragment())
                .commit();

    }

    @Override
    public void onBackPressed(){
        if (!getFragmentManager().popBackStackImmediate()){
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    public static class SettingsHeaderFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(getString(R.string.pref_file_key));
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_headers);
            // Add Configure Device Button
            findPreference("pref1").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_settings, new SettingsGeneralFragment())
                            .addToBackStack( SettingsGeneralFragment.class.getSimpleName())
                            .commit();
                    ((SettingsActivity) getActivity()).getSupportActionBar().setTitle("pref1");
                    return true;
                }
            });
        }

        @Override
        public void onResume(){
            super.onResume();
            ((SettingsActivity) getActivity()).getSupportActionBar().setTitle("Settings");
        }

    }

    public static class SettingsGeneralFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesName(getString(R.string.pref_file_key));
            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);

            // Add Configure Device Button
            Preference configureDeviceButton = findPreference(getString(R.string.pref_button_configure_device_key));
            configureDeviceButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
                @Override
                public boolean onPreferenceClick(Preference preference){
                    try {
                        Intent intent = new Intent(getActivity(), ConfigureDeviceActivity.class);
                        startActivity(intent);
                    }
                    catch (Exception e){
                        Log.e("Open Activity", "onPreferenceClick: Unable to start Activity - ConfigureDeviceActivity");
                    }
                    return true;
                }
            });
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key){
            if (key.equals(getString(R.string.pref_key_sms_message))){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.spinner_position_main),0);
                editor.commit();
            }
        }

        @Override
        public void onResume(){
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause(){
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

    }
}
