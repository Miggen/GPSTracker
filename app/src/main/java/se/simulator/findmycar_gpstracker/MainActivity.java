package se.simulator.findmycar_gpstracker;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity{
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this,getString(R.string.pref_file_key),MODE_PRIVATE,R.xml.pref_general,false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.filter_message_received));
        registerReceiver(messageReceiver,filter);
        updateInformationFragment();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(messageReceiver);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInformationFragment();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_help:
                return true;

            case R.id.action_feedback:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getLocation(View view) {
        TextView buttonGetLocation = (TextView) findViewById(R.id.button_get_location);
        buttonGetLocation.setEnabled(false);
        if (checkPermissionSMS()) {
            sendSMS();
        }
    }

    private void sendSMS(){
        String sent = "SMS_SENT";
        String delivered = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this,0,new Intent(sent),0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this,0,new Intent(delivered),0);

        // when the SMS has been sent
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView textview = (TextView) findViewById(R.id.text1);
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        textview.setText("SMS sent");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        textview.setText("Radio off");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        textview.setText("Null PDU");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        textview.setText("No service");
                        break;
                    default:
                        textview.setText("Generic failure");
                        break;
                }
                TextView buttonGetLocation = (TextView) findViewById(R.id.button_get_location);
                buttonGetLocation.setEnabled(true);
            }
        },new IntentFilter(sent));

        // when the SMS has been delivered
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView textview = (TextView) findViewById(R.id.text1);
                switch(getResultCode())
                {
                    case Activity.RESULT_OK:
                        textview.setText("SMS delivered");
                        break;
                    default:
                        textview.setText("SMS not delivered");
                        break;
                }
            }
        },new IntentFilter(delivered));

        String[] preferenceKeys = {getString(R.string.pref_key_tracker_number), getString(R.string.pref_key_sms_message)};
        String[] preferences = readPreferences(preferenceKeys);

        SmsManager sm = SmsManager.getDefault();
        String to_number = preferences[0];
        String msg = preferences[1];
        sm.sendTextMessage(to_number,null,msg,sentPI,deliveredPI);
    }

    private String[] readPreferences(String[] preferenceKeys){
        String[] preferences = new String[preferenceKeys.length];
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);
        for (int i = 0; i < preferenceKeys.length; i++) {
            preferences[i] = sharedPref.getString(preferenceKeys[i],"");
        }
        return preferences;
    }

    private boolean checkPermissionSMS() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED ) {
            return true;
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},PERMISSION_REQUEST_SEND_SMS);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode) {
            case PERMISSION_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    sendSMS();
                }
                return;
            }
        }
    }

    public void viewLocation(View view) {
        // View location on map
    }

    private void updateInformationFragment(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);
        Bundle args = new Bundle();
        args.putString("Date",sharedPref.getString(getString(R.string.saved_date),""));
        args.putString("Time",sharedPref.getString(getString(R.string.saved_time),""));
        args.putString("Speed",sharedPref.getString(getString(R.string.saved_speed),""));
        args.putString("Latitude",sharedPref.getString(getString(R.string.saved_latitude),""));
        args.putString("Longitude",sharedPref.getString(getString(R.string.saved_longitude),""));

        // Update fragment with new args
        CarInformationFragment newFragment = new CarInformationFragment();
        newFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.car_status_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

}
