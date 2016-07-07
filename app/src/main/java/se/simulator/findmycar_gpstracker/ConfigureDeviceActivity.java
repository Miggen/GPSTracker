package se.simulator.findmycar_gpstracker;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;

public class ConfigureDeviceActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_configure_device);
        setSupportActionBar(toolbar);
    }

    public void discardChanges(View view){
        finish();
    }

    public void acceptChanges(View view){
        if (checkPermissionSMS()){
            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigureDeviceActivity.this);
            builder.setMessage(R.string.configure_device_warning);
            builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    finish();
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
