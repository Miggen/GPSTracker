package se.simulator.findmycar_gpstracker;

import android.*;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ConfigureDeviceActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    CheckBox resetDeviceCheckBox;
    Spinner sleepModeSpinner;
    Spinner numberSlotSpinner;
    CheckBox gprsEnabledCheckBox;
    EditText oldLoginUser;
    EditText oldLoginPassword;
    ArrayList<String> messageQueue;
    SmsManager smsManager;
    PendingIntent sentPI;
    String to_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_configure_device);
        setSupportActionBar(toolbar);

        resetDeviceCheckBox = (CheckBox) findViewById(R.id.configure_checkbox_reset_device);
        resetDeviceCheckBox.setChecked(true);

        sleepModeSpinner = (Spinner) findViewById(R.id.configure_sleep_mode_spinner);
        sleepModeSpinner.setSelection(1);

        numberSlotSpinner = (Spinner) findViewById(R.id.configure_number_slot_spinner);
        numberSlotSpinner.setSelection(1);

        oldLoginUser = (EditText) findViewById(R.id.configure_old_user);
        oldLoginPassword = (EditText) findViewById(R.id.configure_old_password);

        gprsEnabledCheckBox = (CheckBox) findViewById(R.id.configure_checkbox_gprs_enabled);
        gprsEnabledCheckBox.setChecked(false);

        messageQueue = new ArrayList<String>();
    }

    public void toggleAdvancedView(View view){
        CheckBox checkBox = (CheckBox) view;
        if(checkBox.isChecked()){
            findViewById(R.id.configure_advanced_layout).setVisibility(ViewGroup.VISIBLE);
            checkBox.setText(getString(R.string.configure_advanced_show_less));
        }
        else{
            findViewById(R.id.configure_advanced_layout).setVisibility(ViewGroup.GONE);
            checkBox.setText(getString(R.string.configure_advanced_show_more));
        }

    }

    public void toggleLoginInfoView(View view){
        CheckBox checkBox = (CheckBox) view;
        if(checkBox.isChecked()){
            findViewById(R.id.configure_login_info_layout).setVisibility(ViewGroup.VISIBLE);
        }
        else{
            findViewById(R.id.configure_login_info_layout).setVisibility(ViewGroup.GONE);
        }
    }

    public void discardChanges(View view){
        finish();
    }

    BroadcastReceiver receiverSmsSent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(getResultCode())
            {
                case Activity.RESULT_OK:
                    messageQueue.remove(0);
                    if(messageQueue.size() == 0){
                        unregisterReceiver(receiverSmsSent);
                        finish();
                    }
                    else{
                        smsManager.sendTextMessage(to_number,null,messageQueue.get(0),sentPI,null);
                    }
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void acceptChanges(View view){
        if (checkPermissionSMS()){
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigureDeviceActivity.this);
            builder.setMessage(R.string.configure_device_warning);
            builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    String sent = "SMS_SENT";
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);
                    smsManager = SmsManager.getDefault();
                    String msg;
                    to_number = sharedPref.getString(getString(R.string.pref_key_tracker_number),"");
                    String loginUser = sharedPref.getString(getString(R.string.pref_key_login_user),"");
                    String loginPassword = sharedPref.getString(getString(R.string.pref_key_login_password),"");

                    sentPI = PendingIntent.getBroadcast(ConfigureDeviceActivity.this,0,new Intent(sent),0);
                    registerReceiver(receiverSmsSent,new IntentFilter(sent));

                    if (resetDeviceCheckBox.isChecked()){
                        msg = oldLoginUser.getText() + " " +
                                oldLoginPassword.getText() +
                                " resetprof";
                        sendSms(msg);
                    }

                    if (!oldLoginUser.equals(loginUser)){
                        msg = oldLoginUser.getText() + " " +
                                oldLoginPassword.getText() +
                                " setparam 1252 " + loginUser;
                        sendSms(msg);
                    }

                    if (!oldLoginPassword.equals(loginPassword)){
                        msg = loginUser + " " +
                                oldLoginPassword.getText() +
                                " setparam 1253 " + loginPassword;
                        sendSms(msg);
                    }

                    if (!(resetDeviceCheckBox.isChecked() && sleepModeSpinner.getSelectedItemPosition()==0)){
                        msg = loginUser + " " +
                                loginPassword +
                                " setparam 1000 " + sleepModeSpinner.getSelectedItemPosition();
                        sendSms(msg);
                    }

                    if (!gprsEnabledCheckBox.isChecked()){
                        msg = loginUser + " " +
                                loginPassword +
                                " setparam 1240 0";
                        sendSms(msg);
                    }

                    if (numberSlotSpinner.getSelectedItemPosition() != 0){
                        msg = loginUser + " " +
                                loginPassword +
                                " setparam 1260 " + sharedPref.getString(getString(R.string.pref_key_user_number),"");
                        sendSms(msg);
                        msg = loginUser + " " +
                                loginPassword +
                                " setparam 150 " + sharedPref.getString(getString(R.string.pref_key_user_number),"");
                        sendSms(msg);
                    }
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    private void sendSms(String msg){
        if(messageQueue.size() == 0){
            messageQueue.add(msg);
            smsManager.sendTextMessage(to_number,null,msg,sentPI,null);
        }
        else{
            messageQueue.add(msg);
        }
    }

    private boolean checkPermissionSMS() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED ) {
            return true;
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.SEND_SMS},PERMISSION_REQUEST_SEND_SMS);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode) {
            case PERMISSION_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    finish();
                }
                return;
            }
        }
    }

    private void setParameters(String[] parameters, String[] values){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);
        SmsManager sm = SmsManager.getDefault();
        String msg;
        String to_number = sharedPref.getString(getString(R.string.pref_key_tracker_number),"");

        for (int i = 0; i < parameters.length; i++) {
            msg = sharedPref.getString(getString(R.string.pref_key_login_user),"") + " " +
                    sharedPref.getString(getString(R.string.pref_key_login_password),"") +
                    " setparam " + parameters[i] + " " + values[i];
            sm.sendTextMessage(to_number,null,msg,null,null);
        }
    }
}
