package se.simulator.findmycar_gpstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;


public class SmsReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.pref_file_key),context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            PhoneNumberUtils utils = new PhoneNumberUtils();
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            boolean coordinateState = true;

            for (int i = 0; i < msgs.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = bundle.getString("format");
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }
                else {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                Log.e("SmsReciver", "onReceive Incoming adress: "+ msgs[i].getOriginatingAddress());
                Log.e("SmsReciver", "onReceive Tracker adress: "+ sharedPref.getString("pref_key_tracker_number","false"));
                if (utils.compare(msgs[i].getOriginatingAddress(),sharedPref.getString("pref_key_tracker_number","false"))) {
                    // Update values from text
                    updateValuefromString(msgs[i].getMessageBody() + " ", "D:", editor, context);
                    updateValuefromString(msgs[i].getMessageBody() + " ", "T:", editor, context);
                    updateValuefromString(msgs[i].getMessageBody() + " ", "S:", editor, context);
                    coordinateState = updateValuefromString(msgs[i].getMessageBody() + " ", "C:", editor, context);
                    editor.commit();

                    // Update GUI
                    Intent updateIntent = new Intent();
                    updateIntent.setAction(context.getString(R.string.filter_message_received));
                    updateIntent.putExtra("button_view_location_state",coordinateState);
                    context.sendBroadcast(updateIntent);
                }
            }

        }
    }


    private boolean updateValuefromString(String str, String searchTerm, SharedPreferences.Editor editor,Context context){
        int startindex = str.indexOf(searchTerm);
        int endindex;
        String value;
        if (startindex != -1) {
            switch (searchTerm) {
                case "D:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex);
                    editor.putString(context.getString(R.string.saved_date), value);
                    return true;
                case "T:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex);
                    editor.putString(context.getString(R.string.saved_time), value);
                    return true;
                case "S:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex);
                    editor.putString(context.getString(R.string.saved_speed), value);
                    return true;
                case "C:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex-1);
                    double latitude = Double.parseDouble(value);
                    startindex = endindex + 1;

                    endindex = str.indexOf(' ', startindex);
                    String longitudeString = str.substring(startindex, endindex);
                    double longitude = Double.parseDouble(value);

                    if (latitude >= -90 && latitude <= 90 && longitude >=-180 && longitude <= 180) {
                        editor.putString(context.getString(R.string.saved_latitude), value);
                        editor.putString(context.getString(R.string.saved_longitude), longitudeString);
                        return true;
                    }
                    else{
                        editor.putString(context.getString(R.string.saved_latitude), "");
                        editor.putString(context.getString(R.string.saved_longitude), "");
                        return false;
                    }
            }
        }
        else{
            switch (searchTerm) {
                case "D:":
                    editor.putString(context.getString(R.string.saved_date), "");
                    return true;
                case "T:":
                    editor.putString(context.getString(R.string.saved_time), "");
                    return true;
                case "S:":
                    editor.putString(context.getString(R.string.saved_speed), "");
                    return true;
                case "C:":
                    editor.putString(context.getString(R.string.saved_latitude), "");
                    editor.putString(context.getString(R.string.saved_longitude), "");
                    return false;
            }
        }
        return true;
    }
}
