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
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String msgsBody = "";
        if (bundle != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.pref_file_key), context.MODE_PRIVATE);
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
                                handleSmsggps(msgsBody, editor, context);
                                break;
                            case "getgps":
                                handleSmsgetgps(msgsBody + " ",editor,context);
                                break;
                            case "getio":
                                handleSmsgetio(msgsBody + " ",editor,context);
                                break;
                        }

                    }
                }
            }
        }
    }


    private String getSmsType(String str) {
        if (str.isEmpty()) {
            return "";
        }
        CharSequence[] searchterms = {"Data Link:", "Clock Sync:", ")", "Last Configuration was performed on:",
                "GPS:", "Url:", "Code Ver:", "INI:", "DI", "I/O ID:", "Digital Outputs are set to:", "Text:",
                "New Text:", "OPS", "OPS PART", "FLUSH", "Static Nav is", "00000.00s.0.000", "BLog:", "LVCAN ProgNum:", "Prog:"};
        String[] returnterms = {"getstatus", "getweektime", "getops", "getcfgtime", "getgps", "ggps",
                "getver", "getinfo", "getio", "readio", "setdigout", "getparam", "setparam", "getparam 1271",
                "readops", "flush", "sn", "banlist", "crashlog", "lvcangetprog/lvcansetprog", "lvcangetinfo"};

        for (int i = 0; i < searchterms.length; i++) {
            if (str.contains(searchterms[i])) {
                return returnterms[i];
            }
        }

        return "";
    }

    private void handleSmsggps(String msgsBody, SharedPreferences.Editor editor, Context context) {
        // Get Date
        int startIndex = msgsBody.indexOf("D:") + 2;   // + 2 for length of search term
        int endIndex = msgsBody.indexOf(' ', startIndex);
        editor.putString(context.getString(R.string.ggps_saved_date), msgsBody.substring(startIndex, endIndex));

        // Get Time
        TimeZone timezone = TimeZone.getDefault();
        Date currentDate = new Date();
        int time = timezone.getOffset(currentDate.getTime());  // Get time zone offset
        startIndex = msgsBody.indexOf("T:", endIndex) + 2;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 3600000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex));  // Get hours
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 60000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get minutes
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(' ', startIndex);
        time += 1000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get seconds

        editor.putString(context.getString(R.string.ggps_saved_time), String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))));

        // Get Speed
        startIndex = msgsBody.indexOf("S:", endIndex) + 2;
        endIndex = msgsBody.indexOf(' ', startIndex);
        editor.putString(context.getString(R.string.ggps_saved_speed), msgsBody.substring(startIndex, endIndex));

        // Get latitude & longitude
        startIndex = msgsBody.indexOf("C:", endIndex) + 2;
        endIndex = msgsBody.indexOf(' ', startIndex);
        String latitudeString = msgsBody.substring(startIndex, endIndex - 1);
        double latitudeDouble = Double.parseDouble(latitudeString);
        startIndex = endIndex + 1;

        endIndex = msgsBody.indexOf(' ', startIndex);
        String longitudeString = msgsBody.substring(startIndex, endIndex);
        double longitudeDouble = Double.parseDouble(longitudeString);

        if (latitudeDouble >= -90 && latitudeDouble <= 90 && longitudeDouble >= -180 && longitudeDouble <= 180) {
            editor.putString(context.getString(R.string.ggps_saved_latitude), latitudeString);
            editor.putString(context.getString(R.string.ggps_saved_longitude), longitudeString);
            editor.putBoolean(context.getString(R.string.ggps_saved_coordinate_state), true);
        } else {
            editor.putString(context.getString(R.string.ggps_saved_latitude), "");
            editor.putString(context.getString(R.string.ggps_saved_longitude), "");
            editor.putBoolean(context.getString(R.string.ggps_saved_coordinate_state), false);
        }

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }


    private void handleSmsgetgps(String msgsBody, SharedPreferences.Editor editor, Context context){
        // Get GPS state
        int startIndex = msgsBody.indexOf("GPS:") + 4;   // + 4 for length of search term
        editor.putBoolean(context.getString(R.string.getgps_saved_gps_state),msgsBody.charAt(startIndex) == '1');

        // Get # of available satellites
        startIndex = msgsBody.indexOf("Sat:",startIndex) + 4;
        int endIndex = msgsBody.indexOf(' ', startIndex);
        editor.putString(context.getString(R.string.getgps_saved_satellites), msgsBody.substring(startIndex, endIndex));

        // Get latitude & longitude
        startIndex = msgsBody.indexOf("Lat:",startIndex) + 4;
        endIndex = msgsBody.indexOf(' ', startIndex);
        String latitudeString = msgsBody.substring(startIndex, endIndex);
        double latitudeDouble = Double.parseDouble(latitudeString);

        startIndex = msgsBody.indexOf("Long:",startIndex) + 5;
        endIndex = msgsBody.indexOf(' ', startIndex);
        String longitudeString = msgsBody.substring(startIndex, endIndex);
        double longitudeDouble = Double.parseDouble(longitudeString);

        if (latitudeDouble >= -90 && latitudeDouble <= 90 && longitudeDouble >= -180 && longitudeDouble <= 180) {
            editor.putString(context.getString(R.string.getgps_saved_latitude), latitudeString);
            editor.putString(context.getString(R.string.getgps_saved_longitude), longitudeString);
            editor.putBoolean(context.getString(R.string.getgps_saved_coordinate_state), true);
        } else {
            editor.putString(context.getString(R.string.getgps_saved_latitude), "");
            editor.putString(context.getString(R.string.getgps_saved_longitude), "");
            editor.putBoolean(context.getString(R.string.getgps_saved_coordinate_state), false);
        }

        // Get Altitude
        startIndex = msgsBody.indexOf("Alt:",endIndex) + 4;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getgps_saved_altitude), msgsBody.substring(startIndex, endIndex));

        // Get Speed
        startIndex = msgsBody.indexOf("Speed:",endIndex) + 6;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getgps_saved_speed), msgsBody.substring(startIndex, endIndex));

        // Get Direction
        startIndex = msgsBody.indexOf("Dir:",endIndex) + 4;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getgps_saved_direction), msgsBody.substring(startIndex, endIndex));

        // Get Date
        startIndex = msgsBody.indexOf("Date: ",endIndex) + 6;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getgps_saved_date), msgsBody.substring(startIndex, endIndex));

        // Get Time
        TimeZone timezone = TimeZone.getDefault();
        Date currentDate = new Date();
        int time = timezone.getOffset(currentDate.getTime());  // Get time zone offset
        startIndex = msgsBody.indexOf("Time: ", endIndex) + 6;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 3600000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex));  // Get hours
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 60000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get minutes
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(' ', startIndex);
        time += 1000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get seconds

        editor.putString(context.getString(R.string.getgps_saved_time), String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))));

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmsgetio(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex;
        int endIndex = 0;
        // Get Digital Inputs
        HashSet<String> digitalInputs = new HashSet<String>();
        startIndex = msgsBody.indexOf("DI");
        while (startIndex != -1){
            endIndex = msgsBody.indexOf(' ', startIndex);
            digitalInputs.add(msgsBody.substring(startIndex, endIndex));
            startIndex = msgsBody.indexOf("DI", endIndex);
        }
        editor.putStringSet(context.getString(R.string.getio_saved_digital_inputs),digitalInputs);

        // Get Analog Inputs
        HashSet<String> analogInputs = new HashSet<String>();
        startIndex = msgsBody.indexOf("AI", endIndex);
        while (startIndex != -1){
            endIndex = msgsBody.indexOf(' ', startIndex);
            analogInputs.add(msgsBody.substring(startIndex, endIndex));
            startIndex = msgsBody.indexOf("AI", endIndex);
        }
        editor.putStringSet(context.getString(R.string.getio_saved_analog_inputs),analogInputs);

        // Get Analog Inputs
        HashSet<String> digitalOutputs = new HashSet<String>();
        startIndex = msgsBody.indexOf("DO", endIndex);
        while (startIndex != -1){
            endIndex = msgsBody.indexOf(' ', startIndex);
            digitalOutputs.add(msgsBody.substring(startIndex, endIndex));
            startIndex = msgsBody.indexOf("DO", endIndex);
        }
        editor.putStringSet(context.getString(R.string.getio_saved_digital_outputs),digitalOutputs);

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private int occurrences(String msgsBody, String searchTerm){

        if (msgsBody == null){
            return 0;
        }

        int number = 0;
        int index = msgsBody.indexOf(searchTerm);

        while(index != -1){
            number++;
            index = msgsBody.indexOf(searchTerm,index + 1);
        }

        return number;
    }

}
