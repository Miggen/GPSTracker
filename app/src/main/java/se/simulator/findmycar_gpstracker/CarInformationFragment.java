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
        }


        textViewId1.setText(textId1);
        textViewValue1.setText(textValue1);

        return myInflatedView;
    }
}
