package se.simulator.findmycar_gpstracker;

import android.*;
import android.Manifest;
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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ConfigureApplicationActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_application);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_configure_application);
        setSupportActionBar(toolbar);
    }


    public void accept(View view){
        String tracker_number = ((TextView) findViewById(R.id.content_configure_application_tracker_number)).getText().toString();
        String userName = ((TextView) findViewById(R.id.content_configure_application_user)).getText().toString();
        String password = ((TextView) findViewById(R.id.content_configure_application_password)).getText().toString();
        String user_number = ((TextView) findViewById(R.id.content_configure_application_user_number)).getText().toString();
        if (tracker_number.isEmpty())
        {
            Toast.makeText(ConfigureApplicationActivity.this, getResources().getString(R.string.configure_no_number_toast), Toast.LENGTH_LONG).show();
        }
        else{
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.pref_key_tracker_number),tracker_number);
            editor.putString(getString(R.string.pref_key_login_user),userName);
            editor.putString(getString(R.string.pref_key_login_password),password);
            editor.putString(getString(R.string.pref_key_user_number),user_number);
            editor.commit();

            AlertDialog.Builder builder = new AlertDialog.Builder(ConfigureApplicationActivity.this);
            builder.setMessage(R.string.open_configure_device_description);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
               public void onClick(DialogInterface dialog, int id){
                   Intent intent = new Intent(ConfigureApplicationActivity.this,ConfigureDeviceActivity.class);
                   startActivity(intent);
                   finish();
               }
            });

            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


}
