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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
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
                    Log.e("SmsReceiver", "onReceive Tracker adress: " + sharedPref.getString(context.getString(R.string.pref_key_tracker_number), "false"));
                    if (utils.compare(msgs[i].getOriginatingAddress(), sharedPref.getString(context.getString(R.string.pref_key_tracker_number), "false"))) {
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
                            case "getstatus":
                                handleSmsgetstatus(msgsBody + " ",editor,context);
                                break;
                            case "getweektime":
                                handleSmsgetweektime(msgsBody + " ",editor,context);
                                break;
                            case "getops":
                                handleSmsgetops(msgsBody + " ",editor,context);
                                break;
                            case "getcfgtime":
                                handleSmsgetcfgtime(msgsBody + " ",editor,context);
                                break;
                            case "getver":
                                handleSmsgetver(msgsBody + " ",editor,context);
                                break;
                            case "getinfo":
                                handleSmsgetinfo(msgsBody + " ",editor,context);
                                break;
                            case "getparam":
                                handleSmsgetparam(msgsBody + " ",editor,context);
                                break;
                            case "setparam":
                                handleSmssetparam(msgsBody + " ",editor,context);
                                break;
                            case "warning":
                                handleSmswarning(msgsBody + " ",editor,context);
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
        CharSequence[] searchterms = {"WARNING:", "Data Link:", "Clock Sync:", ")", "Last Configuration was performed on:",
                "Lat:", "Url:", "Code Ver:", "INI:", "DI", "I/O ID:", "Digital Outputs are set to:", "New Text:", "Text:", "New Val:", "Val:",
                "New Text:", "OPS", "OPS PART", "FLUSH", "Static Nav is", "00000.00s.0.000", "BLog:", "LVCAN ProgNum:", "Prog:"};
        String[] returnterms = {"warning", "getstatus", "getweektime", "getops", "getcfgtime", "getgps", "ggps",
                "getver", "getinfo", "getio", "readio", "setdigout", "setparam", "getparam", "setparam" , "getparam", "setparam", "getparam 1271",
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

        //Set Date & Time updated to current date & time
        Calendar calendar = new GregorianCalendar();
        editor.putString(context.getString(R.string.getio_saved_date_updated),String.format("%02d/%02d/%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DATE)));

        if (calendar.get(Calendar.AM_PM) == Calendar.AM){
            editor.putString(context.getString(R.string.getio_saved_time_updated),String.format("%02d:%02d:%02d",
                    calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)));
        }
        else
        {
            editor.putString(context.getString(R.string.getio_saved_time_updated),String.format("%02d:%02d:%02d",
                    calendar.get(Calendar.HOUR)+12,
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)));
        }

        // Get Digital Inputs
        String values = "";
        startIndex = msgsBody.indexOf("DI");
        while (startIndex != -1){
            endIndex = msgsBody.indexOf(' ', startIndex);
            values += msgsBody.charAt(endIndex-1);
            startIndex = msgsBody.indexOf("DI", endIndex);
        }
        editor.putString(context.getString(R.string.getio_saved_digital_inputs),values);

        // Get Analog Inputs
        values = "";
        startIndex = msgsBody.indexOf("AI", endIndex);
        while (startIndex != -1){
            startIndex = msgsBody.indexOf(':',startIndex) + 1;
            endIndex = msgsBody.indexOf(' ', startIndex);
            values += msgsBody.substring(startIndex,endIndex) + " ";
            startIndex = msgsBody.indexOf("AI", endIndex);
        }
        editor.putString(context.getString(R.string.getio_saved_analog_inputs),values);

        // Get Analog Inputs
        values = "";
        startIndex = msgsBody.indexOf("DO", endIndex);
        while (startIndex != -1){
            endIndex = msgsBody.indexOf(' ', startIndex);
            values += msgsBody.charAt(endIndex-1);
            startIndex = msgsBody.indexOf("DO", endIndex);
        }
        editor.putString(context.getString(R.string.getio_saved_digital_outputs),values);

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmsgetstatus(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex;
        int endIndex;

        //Set Date & Time updated to current date & time
        Calendar calendar = new GregorianCalendar();
        editor.putString(context.getString(R.string.getstatus_saved_date_updated),String.format("%02d/%02d/%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DATE)));

        if (calendar.get(Calendar.AM_PM) == Calendar.AM){
            editor.putString(context.getString(R.string.getstatus_saved_time_updated),String.format("%02d:%02d:%02d",
                    calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)));
        }
        else
        {
            editor.putString(context.getString(R.string.getstatus_saved_time_updated),String.format("%02d:%02d:%02d",
                    calendar.get(Calendar.HOUR)+12,
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)));
        }

        //Get Data Link status
        startIndex = msgsBody.indexOf("Data Link: ") + 11;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putBoolean(context.getString(R.string.getstatus_saved_data_link_status), msgsBody.substring(startIndex, endIndex).equals("1"));

        //Get GPRS status
        startIndex = msgsBody.indexOf("GPRS: ",endIndex) + 6;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putBoolean(context.getString(R.string.getstatus_saved_gprs_status), msgsBody.substring(startIndex, endIndex).equals("1"));

        //Get Phone status
        startIndex = msgsBody.indexOf("Phone: ",endIndex) + 7;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putInt(context.getString(R.string.getstatus_saved_phone_status), Integer.parseInt(msgsBody.substring(startIndex, endIndex)));

        //Get SIM status
        startIndex = msgsBody.indexOf("SIM: ",endIndex) + 5;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putInt(context.getString(R.string.getstatus_saved_sim_status), Integer.parseInt(msgsBody.substring(startIndex, endIndex)));

        //Get Operator
        startIndex = msgsBody.indexOf("OP: ",endIndex) + 4;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putInt(context.getString(R.string.getstatus_saved_operator_id), Integer.parseInt(msgsBody.substring(startIndex, endIndex)));

        //Get Signal Quality
        startIndex = msgsBody.indexOf("Signal: ",endIndex) + 8;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putInt(context.getString(R.string.getstatus_saved_signal_quality), Integer.parseInt(msgsBody.substring(startIndex, endIndex)));

        //Check for new sms
        startIndex = msgsBody.indexOf("NewSMS: ",endIndex) + 8;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putBoolean(context.getString(R.string.getstatus_saved_new_sms), msgsBody.substring(startIndex, endIndex).equals("1"));

        //Get Roaming status
        startIndex = msgsBody.indexOf("Roaming: ",endIndex) + 9;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putBoolean(context.getString(R.string.getstatus_saved_roaming), msgsBody.substring(startIndex, endIndex).equals("1"));

        //Check SMS storage full?
        startIndex = msgsBody.indexOf("SMSFull: ",endIndex) + 9;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putBoolean(context.getString(R.string.getstatus_saved_sms_full), msgsBody.substring(startIndex, endIndex).equals("1"));

        //Get GSM Tower Location Area Code
        startIndex = msgsBody.indexOf("LAC: ",endIndex) + 5;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putInt(context.getString(R.string.getstatus_saved_GSM_tower_location_area_code), Integer.parseInt(msgsBody.substring(startIndex, endIndex)));

        //Get GSM Tower ID
        startIndex = msgsBody.indexOf("Cell ID: ",endIndex) + 9;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putInt(context.getString(R.string.getstatus_saved_GSM_tower_cell_id), Integer.parseInt(msgsBody.substring(startIndex, endIndex)));

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmsgetweektime(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex;
        int endIndex;

        //Get Clock Sync
        startIndex = msgsBody.indexOf("Clock Sync: ") + 12;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putBoolean(context.getString(R.string.getweektime_saved_clock_sync), msgsBody.substring(startIndex, endIndex).equals("1"));

        //Get Day of Week
        startIndex = msgsBody.indexOf("DOW: ",endIndex) + 5;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putInt(context.getString(R.string.getweektime_saved_day_of_week), Integer.parseInt(msgsBody.substring(startIndex, endIndex)));

        // Get Time
        TimeZone timezone = TimeZone.getDefault();
        Date currentDate = new Date();
        int time = timezone.getOffset(currentDate.getTime());  // Get time zone offset
        startIndex = msgsBody.indexOf("Time ", endIndex) + 5;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 3600000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex));  // Get hours
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(' ', startIndex);
        time += 60000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get minutes

        editor.putString(context.getString(R.string.getweektime_saved_time), String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time))));

        //Get Week Time
        startIndex = msgsBody.indexOf("Weektime: ",endIndex) + 10;
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putInt(context.getString(R.string.getweektime_saved_week_time), Integer.parseInt(msgsBody.substring(startIndex, endIndex)));

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmsgetops(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex = 0;
        int endIndex;

        String operators = "";

        endIndex = msgsBody.indexOf(")");
        while (endIndex != -1){
            operators += msgsBody.substring(startIndex,endIndex) + ':';
            startIndex = endIndex + 3;
            endIndex = msgsBody.indexOf(")",startIndex-2);
        }

        editor.putString(context.getString(R.string.getops_saved_operators),operators);

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmsgetcfgtime(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex;
        int endIndex;

        //Get Last configuration Date
        startIndex = msgsBody.indexOf("Last Configuration was performed on: ") + "Last Configuration was performed on: ".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getcfgtime_saved_date), msgsBody.substring(startIndex, endIndex));

        // Get Last configuration Time
        TimeZone timezone = TimeZone.getDefault();
        Date currentDate = new Date();
        int time = timezone.getOffset(currentDate.getTime());  // Get time zone offset
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 3600000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex));  // Get hours
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 60000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get minutes
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(' ', startIndex);
        time += 1000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get seconds

        editor.putString(context.getString(R.string.getcfgtime_saved_time), String.format("%02d:%02d:%02d",
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

    private void handleSmsgetver(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex;
        int endIndex;

        //Get Firmware version
        startIndex = msgsBody.indexOf("Code Ver:") + "Code Ver:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getver_saved_firmware_version), msgsBody.substring(startIndex, endIndex));

        //Get Firmware revision
        startIndex = msgsBody.indexOf("Rev:",endIndex) + "Rev:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getver_saved_firmware_revision), msgsBody.substring(startIndex, endIndex));

        //Get Device IMEI
        startIndex = msgsBody.indexOf("Device IMEI:",endIndex) + "Device IMEI:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getver_saved_IMEI), msgsBody.substring(startIndex, endIndex));

        //Get Device ID
        startIndex = msgsBody.indexOf("Device ID:",endIndex) + "Device ID:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getver_saved_device_id), msgsBody.substring(startIndex, endIndex));

        //Get Bootloader version
        startIndex = msgsBody.indexOf("Bootloader Ver:",endIndex) + "Bootloader Ver:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getver_saved_bootloader_version), msgsBody.substring(startIndex, endIndex));

        //Get Modem Application version
        startIndex = msgsBody.indexOf("Modem APP Ver:",endIndex) + "Modem APP Ver:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getver_saved_modem_app_version), msgsBody.substring(startIndex, endIndex));

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmsgetinfo(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex;
        int endIndex;

        //Get Device initialization Date
        startIndex = msgsBody.indexOf("INI:") + "INI:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        String date = msgsBody.substring(startIndex, endIndex);

        // Get Device initialization Time
        TimeZone timezone = TimeZone.getDefault();
        Date currentDate = new Date();
        int time = timezone.getOffset(currentDate.getTime());  // Get time zone offset
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 3600000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex));  // Get hours
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(' ', startIndex);
        time += 60000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get minutes

        editor.putString(context.getString(R.string.getinfo_saved_device_initialization_time),
                date + " " +
                        String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time))));

        //Get RTC Date
        startIndex = msgsBody.indexOf("RTC:",endIndex) + "RTC:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        date = msgsBody.substring(startIndex, endIndex);

        // Get RTC Time
        time = timezone.getOffset(currentDate.getTime());  // Get time zone offset
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(':', startIndex);
        time += 3600000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex));  // Get hours
        startIndex = endIndex + 1;
        endIndex = msgsBody.indexOf(' ', startIndex);
        time += 60000 * Integer.parseInt(msgsBody.substring(startIndex, endIndex)); // Get minutes

        editor.putString(context.getString(R.string.getinfo_saved_RTC_time),
                date + " " +
                        String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toHours(time),
                                TimeUnit.MILLISECONDS.toMinutes(time) -
                                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time))));

        //Get Restart counter
        startIndex = msgsBody.indexOf("RST:",endIndex) + "RST:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_restart_counter), msgsBody.substring(startIndex, endIndex));

        //Get Error counter
        startIndex = msgsBody.indexOf("ERR:",endIndex) + "ERR:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_error_counter), msgsBody.substring(startIndex, endIndex));

        //Get sent records counter
        startIndex = msgsBody.indexOf("SR:",endIndex) + "SR:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_sent_records_counter), msgsBody.substring(startIndex, endIndex));

        //Get broken records counter
        startIndex = msgsBody.indexOf("BR:",endIndex) + "BR:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_broken_records_counter), msgsBody.substring(startIndex, endIndex));

        //Get CRC fail counter
        startIndex = msgsBody.indexOf("CF:",endIndex) + "CF:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_CRC_failed_counter), msgsBody.substring(startIndex, endIndex));

        //Get GPRS failed counter
        startIndex = msgsBody.indexOf("FG:",endIndex) + "FG:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_GPRS_failed_counter), msgsBody.substring(startIndex, endIndex));

        //Get link failed counter
        startIndex = msgsBody.indexOf("FL:",endIndex) + "FL:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_link_failed_counter), msgsBody.substring(startIndex, endIndex));

        //Get UPD timeout counter
        startIndex = msgsBody.indexOf("UT:",endIndex) + "UT:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_UPD_timeout_counter), msgsBody.substring(startIndex, endIndex));

        //Get Sent SMS counter
        startIndex = msgsBody.indexOf("SMS:",endIndex) + "SMS:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_sent_sms_counter), msgsBody.substring(startIndex, endIndex));

        //Get time without GPS
        startIndex = msgsBody.indexOf("NOGPS:",endIndex) + "NOGPS:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_noGPS_timer), msgsBody.substring(startIndex, endIndex));

        //Get GPS receiver state
        startIndex = msgsBody.indexOf("GPS:",endIndex) + "GPS:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        switch (msgsBody.substring(startIndex, endIndex)) {
            case "0":
                editor.putString(context.getString(R.string.getinfo_saved_GPS_state), "OFF");
                break;
            case "1":
                editor.putString(context.getString(R.string.getinfo_saved_GPS_state), "restarting");
                break;
            case "2":
                editor.putString(context.getString(R.string.getinfo_saved_GPS_state), "ON but no fix");
                break;
            case "3":
                editor.putString(context.getString(R.string.getinfo_saved_GPS_state), "ON and operational");
                break;
            case "4":
                editor.putString(context.getString(R.string.getinfo_saved_GPS_state), "sleep mode");
                break;
        }

        //Get average number of satellites
        startIndex = msgsBody.indexOf("SAT:",endIndex) + "SAT:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_average_satellites), msgsBody.substring(startIndex, endIndex));

        //Get Reset source identification
        startIndex = msgsBody.indexOf("RS:",endIndex) + "RS:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        switch (msgsBody.substring(startIndex, endIndex)){
            case "1":
                editor.putString(context.getString(R.string.getinfo_saved_reset_source_identification), "Low Power");
                break;
            case "2":
                editor.putString(context.getString(R.string.getinfo_saved_reset_source_identification), "W Watchdog");
                break;
            case "3":
                editor.putString(context.getString(R.string.getinfo_saved_reset_source_identification), "I Watchdog");
                break;
            case "4":
                editor.putString(context.getString(R.string.getinfo_saved_reset_source_identification), "Software reset");
                break;
            case "5":
                editor.putString(context.getString(R.string.getinfo_saved_reset_source_identification), "Power On");
                break;
            case "6":
                editor.putString(context.getString(R.string.getinfo_saved_reset_source_identification), "Pin Reset");
                break;
        }

        //Get data mode state
        startIndex = msgsBody.indexOf("MD:",endIndex) + "MD:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        switch (msgsBody.substring(startIndex, endIndex)){
            case "0":
                editor.putString(context.getString(R.string.getinfo_saved_data_mode), "Home and Stop");
                break;
            case "1":
                editor.putString(context.getString(R.string.getinfo_saved_data_mode), "Home and Moving");
                break;
            case "2":
                editor.putString(context.getString(R.string.getinfo_saved_data_mode), "Roaming and Stop");
                break;
            case "3":
                editor.putString(context.getString(R.string.getinfo_saved_data_mode), "Roaming and Moving");
                break;
            case "4":
                editor.putString(context.getString(R.string.getinfo_saved_data_mode), "Unknown and Stop");
                break;
            case "5":
                editor.putString(context.getString(R.string.getinfo_saved_data_mode), "Unknown and Moving");
                break;
        }

        //Get number of records found
        startIndex = msgsBody.indexOf("RF:",endIndex) + "RF:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getinfo_saved_records_found), msgsBody.substring(startIndex, endIndex));

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmsgetparam(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex;
        int endIndex;

        // Get parameter ID
        startIndex = msgsBody.indexOf("Param ID:") + "Param ID:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.getparam_saved_param_id), msgsBody.substring(startIndex, endIndex));

        // Get parameter value
        startIndex = msgsBody.indexOf("Val:",endIndex);
        if (startIndex != -1) {
            editor.putString(context.getString(R.string.getparam_saved_param_value), msgsBody.substring(startIndex + "Val:".length()));
        }
        else{
            startIndex = msgsBody.indexOf("Text:",endIndex) + "Text:".length();
            editor.putString(context.getString(R.string.getparam_saved_param_value), msgsBody.substring(startIndex));
        }


        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmssetparam(String msgsBody, SharedPreferences.Editor editor, Context context){
        int startIndex;
        int endIndex;

        // Get parameter ID
        startIndex = msgsBody.indexOf("Param ID:") + "Param ID:".length();
        endIndex = msgsBody.indexOf(' ',startIndex);
        editor.putString(context.getString(R.string.setparam_saved_param_id), msgsBody.substring(startIndex, endIndex));

        // Get parameter value
        startIndex = msgsBody.indexOf("New Val:",endIndex);
        if (startIndex != -1) {
            editor.putString(context.getString(R.string.setparam_saved_param_value), msgsBody.substring(startIndex + "New Val:".length()));
        }
        else{
            startIndex = msgsBody.indexOf("New Text:",endIndex) + "New Text:".length();
            editor.putString(context.getString(R.string.setparam_saved_param_value), msgsBody.substring(startIndex));
        }

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }

    private void handleSmswarning(String msgsBody, SharedPreferences.Editor editor, Context context){

        if(msgsBody.contains("Value detected")){
            editor.putString(context.getString(R.string.setparam_saved_param_id), "");
            editor.putString(context.getString(R.string.setparam_saved_param_value), msgsBody);
        }
        else{
            editor.putString(context.getString(R.string.getparam_saved_param_id), "");
            editor.putString(context.getString(R.string.getparam_saved_param_value), msgsBody);
        }

        // Commit changes
        editor.commit();

        // Update GUI
        Intent updateIntent = new Intent();
        updateIntent.setAction(context.getString(R.string.filter_message_received));
        context.sendBroadcast(updateIntent);
    }
}
