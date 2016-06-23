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
                    updateValuefromString(msgs[i].getMessageBody() + " ", "C:", editor, context);
                    editor.commit();

                    // Update GUI
                    Intent updateIntent = new Intent();
                    updateIntent.setAction(context.getString(R.string.filter_message_received));
                    context.sendBroadcast(updateIntent);
                }
            }

        }
    }


    private void updateValuefromString(String str, String searchTerm, SharedPreferences.Editor editor,Context context){
        int startindex = str.indexOf(searchTerm);
        int endindex;
        String value;
        if (startindex != -1) {
            switch (searchTerm) {
                case "D:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex);
                    editor.putString(context.getString(R.string.saved_date), value);
                    break;
                case "T:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex);
                    editor.putString(context.getString(R.string.saved_time), value);
                    break;
                case "S:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex);
                    editor.putString(context.getString(R.string.saved_speed), value);
                    break;
                case "C:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex-1);
                    editor.putString(context.getString(R.string.saved_latitude), value);
                    startindex = endindex + 1;
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex, endindex);
                    editor.putString(context.getString(R.string.saved_longitude), value);
                    break;
            }
        }
        else{
            switch (searchTerm) {
                case "D:":
                    editor.putString(context.getString(R.string.saved_date), "");
                    break;
                case "T:":
                    editor.putString(context.getString(R.string.saved_time), "");
                    break;
                case "S:":
                    editor.putString(context.getString(R.string.saved_speed), "");
                    break;
                case "C:":
                    editor.putString(context.getString(R.string.saved_latitude), "");
                    editor.putString(context.getString(R.string.saved_longitude), "");
                    break;
            }
        }

    }
}
