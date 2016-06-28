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
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class SmsReceiver extends BroadcastReceiver{

    boolean coordinateState = true;
    @Override
    public void onReceive(Context context, Intent intent){
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String msgsBody = "";
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
                } else {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                msgsBody = msgs[i].getMessageBody();
                if (msgsBody != null) {
                    Log.e("SmsReceiver", "onReceive Incoming adress: " + msgs[i].getOriginatingAddress());
                    Log.e("SmsReceiver", "onReceive Tracker adress: " + sharedPref.getString("pref_key_tracker_number", "false"));
                    if (utils.compare(msgs[i].getOriginatingAddress(), sharedPref.getString("pref_key_tracker_number", "false"))) {
                        Log.e("SmsReceiver", "onReceive SmsType: " + getSmsType(msgsBody));
                        switch (getSmsType(msgsBody)) {
                            case "ggps":
                                handleSmsggps(msgsBody,editor,context);
                                break;
                        }

                    }
                }
            }
        }
    }



    private String getSmsType(String str){
        if (str.isEmpty()){
            return "";
        }
        CharSequence[] searchterms = {"Data Link:","Clock Sync:",")","Last Configuration was performed on:",
                "GPS:","Url:","Code Ver:","INI:","DI","I/O ID:","Digital Outputs are set to:","Text:",
                "New Text:","OPS","OPS PART","FLUSH","Static Nav is","00000.00s.0.000","BLog:","LVCAN ProgNum:","Prog:"};
        String[] returnterms = {"getstatus","getweektime","getops","getcfgtime","getgps","ggps",
                "getver","getinfo","getio","readio","setdigout","getparam","setparam","getparam 1271",
                "readops","flush","sn","banlist","crashlog","lvcangetprog/lvcansetprog","lvcangetinfo"};

        for (int i = 0; i < searchterms.length; i++) {
            if (str.contains(searchterms[i])){
                return returnterms[i];
            }
        }

        return "";
    }

    private void handleSmsggps(String msgsBody,SharedPreferences.Editor editor, Context context){
        // Update values from text
        updateValuefromString(msgsBody + " ", "D:", editor, context);
        updateValuefromString(msgsBody + " ", "T:", editor, context);
        updateValuefromString(msgsBody + " ", "S:", editor, context);
        coordinateState = updateValuefromString(msgsBody + " ", "C:", editor, context);
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        updateIntent.putExtra("button_view_location_state", coordinateState);
        context.sendBroadcast(updateIntent);
    }


    private boolean updateValuefromString(String str, String searchTerm, SharedPreferences.Editor editor,Context context){
        int startindex = str.indexOf(searchTerm);
        int endindex;
        String value;
        int time;

        if (startindex != -1) {
            switch (searchTerm) {
                case "D:":
                    endindex = str.indexOf(' ', startindex);
                    value = str.substring(startindex + searchTerm.length(), endindex);
                    editor.putString(context.getString(R.string.saved_date), value);
                    return true;
                case "T:":
                    TimeZone timezone = TimeZone.getDefault();
                    Date currentDate = new Date();
                    time = timezone.getOffset(currentDate.getTime());
                    startindex += searchTerm.length();
                    endindex = str.indexOf(':',startindex);
                    time += 3600000 * Integer.parseInt(str.substring(startindex,endindex));
                    startindex = endindex + 1;
                    endindex = str.indexOf(':',startindex);
                    time += 60000 * Integer.parseInt(str.substring(startindex,endindex));
                    startindex = endindex + 1;
                    endindex = str.indexOf(' ', startindex);
                    time += 1000 * Integer.parseInt(str.substring(startindex,endindex));

                    value = String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(time),
                            TimeUnit.MILLISECONDS.toMinutes(time) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                            TimeUnit.MILLISECONDS.toSeconds(time) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
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
