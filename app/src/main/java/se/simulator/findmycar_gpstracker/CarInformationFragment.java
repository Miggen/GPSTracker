package se.simulator.findmycar_gpstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.TreeSet;


public class CarInformationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myInflatedView = inflater.inflate(R.layout.fragment_car_information, container, false);

        Bundle args = getArguments();
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.pref_file_key),getContext().MODE_PRIVATE);
        int startIndex;
        int endIndex;
        int counter;
        String textId1 = "";
        String textValue1 = "";
        TextView textViewId1 = (TextView) myInflatedView.findViewById(R.id.car_information_identifier_1);
        TextView textViewValue1 = (TextView) myInflatedView.findViewById(R.id.car_information_value_1);

        switch (args.getString("Selected View")) {
            case "ggps":
                textId1 += getResources().getString(R.string.ggps_text_date)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.ggps_saved_date),"") + "\n";
                textId1 += getResources().getString(R.string.ggps_text_time) + "\n";
                textValue1 += sharedPref.getString(getString(R.string.ggps_saved_time),"") + "\n";
                textId1 += getResources().getString(R.string.ggps_text_speed) + "\n";
                textValue1 += sharedPref.getString(getString(R.string.ggps_saved_speed),"") + "\n";
                textId1 += getResources().getString(R.string.ggps_text_latitude) + "\n";
                textValue1 += sharedPref.getString(getString(R.string.ggps_saved_latitude),"") + "\n";
                textId1 += getResources().getString(R.string.ggps_text_longitude) + "\n";
                textValue1 += sharedPref.getString(getString(R.string.ggps_saved_longitude),"") + "\n";
                break;
            case "getgps":
                textId1 += getResources().getString(R.string.getgps_text_date)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getgps_saved_date),"") + "\n";
                textId1 += getResources().getString(R.string.getgps_text_time)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getgps_saved_time),"") + "\n";
                textId1 += getResources().getString(R.string.getgps_text_gps_state)+ "\n";
                textValue1 += sharedPref.getBoolean(getString(R.string.getgps_saved_gps_state),false) + "\n";
                textId1 += getResources().getString(R.string.getgps_text_latitude)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getgps_saved_latitude),"") + "\n";
                textId1 += getResources().getString(R.string.getgps_text_longitude)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getgps_saved_longitude),"") + "\n";
                textId1 += getResources().getString(R.string.getgps_text_altitude)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getgps_saved_altitude),"") + "\n";
                textId1 += getResources().getString(R.string.getgps_text_direction)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getgps_saved_direction),"") + "\n";
                textId1 += getResources().getString(R.string.getgps_text_speed)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getgps_saved_speed),"") + "\n";
                textId1 += getResources().getString(R.string.getgps_text_satellites)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getgps_saved_satellites),"") + "\n";
                break;
            case "getio":
                textId1 += getResources().getString(R.string.getio_text_date)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getio_saved_date_updated),"") + "\n";
                textId1 += getResources().getString(R.string.getio_text_time)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getio_saved_time_updated),"") + "\n";

                String values = sharedPref.getString(getString(R.string.getio_saved_digital_inputs),"");
                for (int i = 0; i < values.length(); i++) {
                    textId1 += getResources().getString(R.string.getio_text_digital_inputs) + " " + (i+1) + ":" +"\n";
                    textValue1 += values.charAt(i) + "\n";
                }

                values = sharedPref.getString(getString(R.string.getio_saved_analog_inputs),"");
                startIndex = 0;
                endIndex = values.indexOf(' ',startIndex);
                counter = 0;
                while(endIndex != -1) {
                    textId1 += getResources().getString(R.string.getio_text_analog_inputs) + " " + ++counter + ":" +"\n";
                    textValue1 += values.substring(startIndex,endIndex) + "\n";

                    startIndex = endIndex + 1;
                    endIndex = values.indexOf(' ',startIndex);
                }

                values = sharedPref.getString(getString(R.string.getio_saved_digital_outputs),"");
                for (int i = 0; i < values.length(); i++) {
                    textId1 += getResources().getString(R.string.getio_text_digital_outputs) + " " + (i+1) + ":" +"\n";
                    textValue1 += values.charAt(i) + "\n";
                }
                break;
            case "getstatus":
                textId1 += getResources().getString(R.string.getstatus_text_date_updated)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getstatus_saved_date_updated),"") + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_time_updated)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getstatus_saved_time_updated),"") + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_data_link_status)+ "\n";
                textValue1 += sharedPref.getBoolean(getString(R.string.getstatus_saved_data_link_status),false) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_gprs_status)+ "\n";
                textValue1 += sharedPref.getBoolean(getString(R.string.getstatus_saved_gprs_status),false) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_phone_status)+ "\n";
                textValue1 += sharedPref.getInt(getString(R.string.getstatus_saved_phone_status),2) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_sim_status)+ "\n";
                textValue1 += sharedPref.getInt(getString(R.string.getstatus_saved_sim_status),0) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_operator_id)+ "\n";
                textValue1 += sharedPref.getInt(getString(R.string.getstatus_saved_operator_id),0) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_signal_quality)+ "\n";
                textValue1 += sharedPref.getInt(getString(R.string.getstatus_saved_signal_quality),0) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_new_sms)+ "\n";
                textValue1 += sharedPref.getBoolean(getString(R.string.getstatus_saved_new_sms),false) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_roaming)+ "\n";
                textValue1 += sharedPref.getBoolean(getString(R.string.getstatus_saved_roaming),false) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_sms_full)+ "\n";
                textValue1 += sharedPref.getBoolean(getString(R.string.getstatus_saved_sms_full),false) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_GSM_tower_location_area_code)+ "\n";
                textValue1 += sharedPref.getInt(getString(R.string.getstatus_saved_GSM_tower_location_area_code),0) + "\n";
                textId1 += getResources().getString(R.string.getstatus_text_GSM_tower_cell_id)+ "\n";
                textValue1 += sharedPref.getInt(getString(R.string.getstatus_saved_GSM_tower_cell_id),0) + "\n";
                break;
            case "getweektime":
                textId1 += getResources().getString(R.string.getweektime_text_clock_sync)+ "\n";
                textValue1 += sharedPref.getBoolean(getString(R.string.getweektime_saved_clock_sync),false) + "\n";
                textId1 += getResources().getString(R.string.getweektime_text_day_of_week)+ "\n";
                textValue1 += sharedPref.getInt(getString(R.string.getweektime_saved_day_of_week),0) + "\n";
                textId1 += getResources().getString(R.string.getweektime_text_time)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getweektime_saved_time),"") + "\n";
                textId1 += getResources().getString(R.string.getweektime_text_week_time)+ "\n";
                textValue1 += sharedPref.getInt(getString(R.string.getweektime_saved_week_time),0) + "\n";
                break;
            case "getops":
                textId1 += getResources().getString(R.string.getops_text_operators)+ "\n";
                values = sharedPref.getString(getString(R.string.getops_saved_operators),"");
                startIndex = 0;
                endIndex = values.indexOf(':');
                while (endIndex != -1){
                   textValue1 += values.substring(startIndex,endIndex) + "\n";
                    startIndex = endIndex + 1;
                    endIndex = values.indexOf(':', startIndex);
                }
                break;
            case "getcfgtime":
                textId1 += getResources().getString(R.string.getcfgtime_text_description)+ "\n";
                textValue1 += "\n";
                textId1 += getResources().getString(R.string.getcfgtime_text_date)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getcfgtime_saved_date),"") + "\n";
                textId1 += getResources().getString(R.string.getcfgtime_text_time)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getcfgtime_saved_time),"") + "\n";
                break;
            case "getver":
                textId1 += getResources().getString(R.string.getver_text_firmware_version)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getver_saved_firmware_version),"") + "\n";
                textId1 += getResources().getString(R.string.getver_text_firmware_revision)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getver_saved_firmware_revision),"") + "\n";
                textId1 += getResources().getString(R.string.getver_text_IMEI)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getver_saved_IMEI),"") + "\n";
                textId1 += getResources().getString(R.string.getver_text_device_id)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getver_saved_device_id),"") + "\n";
                textId1 += getResources().getString(R.string.getver_text_bootloader_version)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getver_saved_bootloader_version),"") + "\n";
                textId1 += getResources().getString(R.string.getver_text_modem_app_version)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getver_saved_modem_app_version),"") + "\n";
                break;
            case "getinfo":
                textId1 += getResources().getString(R.string.getinfo_text_device_initialization_time)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_device_initialization_time),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_RTC_time)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_RTC_time),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_restart_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_restart_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_error_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_error_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_sent_records_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_sent_records_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_broken_records_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_broken_records_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_CRC_failed_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_CRC_failed_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_GPRS_failed_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_GPRS_failed_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_link_failed_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_link_failed_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_UPD_timeout_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_UPD_timeout_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_sent_sms_counter)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_sent_sms_counter),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_noGPS_timer)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_noGPS_timer),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_GPS_state)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_GPS_state),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_average_satellites)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_average_satellites),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_reset_source_identification)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_reset_source_identification),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_data_mode)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_data_mode),"") + "\n";
                textId1 += getResources().getString(R.string.getinfo_text_records_found)+ "\n";
                textValue1 += sharedPref.getString(getString(R.string.getinfo_saved_records_found),"") + "\n";
                break;
            case "getparam":
                textId1 += "Parameter " + sharedPref.getString(getString(R.string.getparam_saved_param_id),"") + ":" + "\n";
                textValue1 += sharedPref.getString(getString(R.string.getparam_saved_param_value),"") + "\n";
                break;
            case "setparam":
                textId1 += "Parameter " + sharedPref.getString(getString(R.string.setparam_saved_param_id),"") + ":" + "\n";
                textValue1 += sharedPref.getString(getString(R.string.setparam_saved_param_value),"") + "\n";
                break;
        }


        textViewId1.setText(textId1);
        textViewValue1.setText(textValue1);

        return myInflatedView;
    }
}
