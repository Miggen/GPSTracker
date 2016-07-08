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
import android.content.res.Resources;
import android.database.DataSetObserver;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity{
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private boolean receiverSmsSentRegistered = false;
    private boolean receiverSmsDeliveredRegistered = false;
    private ListItem spinnerItem;

    Spinner spinner;
    SpinAdapter spinnerAdapter;

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this,getString(R.string.pref_file_key),MODE_PRIVATE,R.xml.pref_general,false);
        sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);

        if (sharedPref.getBoolean(getString(R.string.first_run),true))
        {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.first_run),false);
            editor.commit();
            initialSetup();
        }

        addSpinner();
    }

    private void addSpinner(){

        Set<String> smsMessageSet = sharedPref.getStringSet(getString(R.string.pref_key_sms_message),new HashSet<String>());
        String[] smsMessageValues = smsMessageSet.toArray(new String[smsMessageSet.size()]);

        final ListItem[] spinnerItems = new ListItem[smsMessageValues.length];
        int indexSpinnerItems = 0;
        int indexReferenceList = 0;
         for (String value : getResources().getStringArray(R.array.pref_list_values_sms_message)) {
             if (Arrays.asList(smsMessageValues).indexOf(value) != -1) {
                 spinnerItems[indexSpinnerItems] = new ListItem(value, getResources().getStringArray(R.array.pref_list_titles_sms_message)[indexReferenceList]);
                 indexSpinnerItems++;
             }
             indexReferenceList++;
         }

        spinnerAdapter = new SpinAdapter(this,android.R.layout.simple_spinner_dropdown_item,spinnerItems);
        if (spinnerItems.length > 0) {
            spinnerItem = spinnerItems[sharedPref.getInt(getString(R.string.spinner_position_main), 0)];
        }
        else
        {
            spinnerItem = new ListItem("","");
        }
        spinner = (Spinner) findViewById(R.id.spinner_main);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(sharedPref.getInt(getString(R.string.spinner_position_main),0));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerItem = spinnerAdapter.getItem(position);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.spinner_position_main),position);
                editor.commit();

                TextView parameterSelection = (TextView) findViewById(R.id.parameter_selection_main);
                TextView newValue = (TextView) findViewById(R.id.new_value_main);
                switch (spinnerItem.getId()){
                    case "getparam":
                        parameterSelection.setVisibility(TextView.VISIBLE);
                        newValue.setVisibility(TextView.GONE);
                        break;
                    case "setparam":
                        parameterSelection.setVisibility(TextView.VISIBLE);
                        newValue.setVisibility(TextView.VISIBLE);
                        break;
                    default:
                        parameterSelection.setVisibility(TextView.GONE);
                        newValue.setVisibility(TextView.GONE);
                        break;
                }

                updateInformationFragment();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initialSetup(){
        Intent intent = new Intent(this,ConfigureApplicationActivity.class);
        startActivity(intent);
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

        String msg = sharedPref.getString(getString(R.string.pref_key_login_user),"") + " " +
                sharedPref.getString(getString(R.string.pref_key_login_password),"") + " " + spinnerItem.getId();
        SmsManager sm = SmsManager.getDefault();
        String to_number = sharedPref.getString(getString(R.string.pref_key_tracker_number),"");

        switch (spinnerItem.getId()){
            case "getparam":
                msg += " " + ((TextView) findViewById(R.id.parameter_selection_main)).getText();
                break;
            case "setparam":
                msg += " " + ((TextView) findViewById(R.id.parameter_selection_main)).getText();
                msg += " " + ((TextView) findViewById(R.id.new_value_main)).getText();
                break;
        }

        sm.sendTextMessage(to_number,null,msg,sentPI,deliveredPI);
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
        String spinnerId = spinnerItem.getId();
        double latitude;
        double longitude;
        int zoomLevel;
        Intent intent;

        switch (spinnerId){
            case "ggps":
                latitude = Double.parseDouble(sharedPref.getString(getString(R.string.ggps_saved_latitude),"1000"));
                longitude = Double.parseDouble(sharedPref.getString(getString(R.string.ggps_saved_longitude),"1000"));
                zoomLevel = sharedPref.getInt(getString(R.string.pref_key_zoom_level),15);

                intent = new Intent(this,MapActivity.class);
                intent.putExtra("latitude",latitude);
                intent.putExtra("longitude",longitude);
                intent.putExtra("zoomLevel",zoomLevel);
                startActivity(intent);
                break;
            case "getgps":
                latitude = Double.parseDouble(sharedPref.getString(getString(R.string.getgps_saved_latitude),"1000"));
                longitude = Double.parseDouble(sharedPref.getString(getString(R.string.getgps_saved_longitude),"1000"));
                zoomLevel = sharedPref.getInt(getString(R.string.pref_key_zoom_level),15);

                intent = new Intent(this,MapActivity.class);
                intent.putExtra("latitude",latitude);
                intent.putExtra("longitude",longitude);
                intent.putExtra("zoomLevel",zoomLevel);
                startActivity(intent);
                break;
        }

    }

    private void updateInformationFragment(){

            TextView buttonViewLocation = (TextView) findViewById(R.id.button_view_location);

            // Update fragment with new args
            Bundle args = new Bundle();
            String spinnerId = spinnerItem.getId();
            args.putString("Selected View", spinnerId);

            switch (spinnerId){
                case "ggps":
                    buttonViewLocation.setEnabled(sharedPref.getBoolean(getString(R.string.ggps_saved_coordinate_state),false));
                    break;
                case "getgps":
                    buttonViewLocation.setEnabled(sharedPref.getBoolean(getString(R.string.getgps_saved_coordinate_state),false));
                    break;
                default:
                    buttonViewLocation.setEnabled(false);
                    break;
            }

            CarInformationFragment newFragment = new CarInformationFragment();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.car_status_container, newFragment);
            transaction.addToBackStack(null);
        try {
            transaction.commit();
        }
        catch(Exception e){
            Log.e("Fragment commit", "updateInformationFragment: Unable to commit fragment");
            // Do nothing.
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
        //No call for super(). Bug on API Level > 11.
    }
}
