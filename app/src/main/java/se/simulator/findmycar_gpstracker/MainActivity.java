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
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity{
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private boolean receiverSmsSentRegistered = false;
    private boolean receiverSmsDeliveredRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this,getString(R.string.pref_file_key),MODE_PRIVATE,R.xml.pref_general,false);

        //Check button states
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);
        if (sharedPref.getString("pref_key_tracker_number","").isEmpty())
        {
            TextView buttonGetLocation = (TextView) findViewById(R.id.button_get_location);
            buttonGetLocation.setEnabled(false);

            initialSetup();
        }

        String latitudeString = sharedPref.getString(getString(R.string.saved_latitude),"");
        String longitudeString = sharedPref.getString(getString(R.string.saved_longitude),"");
        if (!latitudeString.isEmpty() && !longitudeString.isEmpty()) {
            double latitude = Double.parseDouble(latitudeString);
            double longitude = Double.parseDouble(longitudeString);
            if (!(latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180)) {
                TextView buttonViewLocation = (TextView) findViewById(R.id.button_view_location);
                buttonViewLocation.setEnabled(false);
            }
        }
        else{
            TextView buttonViewLocation = (TextView) findViewById(R.id.button_view_location);
            buttonViewLocation.setEnabled(false);
        }

    }

    private void initialSetup(){
        TextView textview = (TextView) findViewById(R.id.text1);

        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
        textview.setText("Enter settings for configuration");
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
        if (receiverSmsSentRegistered){
            unregisterReceiver(receiverSmsSent);
            receiverSmsSentRegistered = false;
        }
        if (receiverSmsDeliveredRegistered){
            unregisterReceiver(receiverSmsDelivered);
            receiverSmsDeliveredRegistered = false;
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInformationFragment();
            TextView buttonViewLocation = (TextView) findViewById(R.id.button_view_location);
            buttonViewLocation.setEnabled(intent.getBooleanExtra("button_view_location_state",false));
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
        else
        {
            buttonGetLocation.setEnabled(true);
        }
    }

    BroadcastReceiver receiverSmsSent = new BroadcastReceiver() {
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
            unregisterReceiver(receiverSmsSent);
            receiverSmsSentRegistered = false;
        }
    };

    BroadcastReceiver receiverSmsDelivered = new BroadcastReceiver() {
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
            unregisterReceiver(receiverSmsDelivered);
            receiverSmsDeliveredRegistered = false;
        }
    };

    private void sendSMS(){
        String sent = "SMS_SENT";
        String delivered = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this,0,new Intent(sent),0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this,0,new Intent(delivered),0);

        // when the SMS has been sent
        registerReceiver(receiverSmsSent,new IntentFilter(sent));
        receiverSmsSentRegistered = true;

        // when the SMS has been delivered
        registerReceiver(receiverSmsDelivered,new IntentFilter(delivered));
        receiverSmsDeliveredRegistered = true;

        String[] preferenceKeys = {"pref_key_tracker_number",  "pref_key_login_user", "pref_key_login_password", "pref_key_sms_message"};
        String[] preferences = readPreferences(preferenceKeys);

        SmsManager sm = SmsManager.getDefault();
        String to_number = preferences[0];
        String msg = preferences[1] + " " + preferences[2] + " " + preferences[3];
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
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);
        double latitude = Double.parseDouble(sharedPref.getString(getString(R.string.saved_latitude),"1000"));
        double longitude = Double.parseDouble(sharedPref.getString(getString(R.string.saved_longitude),"1000"));
        int zoomLevel = sharedPref.getInt("pref_key_zoom_level",15);

        Intent intent = new Intent(this,MapActivity.class);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        intent.putExtra("zoomLevel",zoomLevel);
        startActivity(intent);
    }

    private void updateInformationFragment(){
        // Update fragment with new args
        CarInformationFragment newFragment = new CarInformationFragment();
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
