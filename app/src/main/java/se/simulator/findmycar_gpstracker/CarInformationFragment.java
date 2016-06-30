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


public class CarInformationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myInflatedView = inflater.inflate(R.layout.fragment_car_information, container, false);

        Bundle args = getArguments();
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.pref_file_key),getContext().MODE_PRIVATE);
        String text = "";
        TextView textview = (TextView) myInflatedView.findViewById(R.id.text_car_information);

        switch (args.getString("Selected View")) {
            case "ggps":
                text += getResources().getString(R.string.carinformationfragment_text_date) + sharedPref.getString(getString(R.string.ggps_saved_date),"") + "\n";
                text += getResources().getString(R.string.carinformationfragment_text_time) + sharedPref.getString(getString(R.string.ggps_saved_time),"") + "\n";
                text += getResources().getString(R.string.carinformationfragment_text_speed) + sharedPref.getString(getString(R.string.ggps_saved_speed),"") + "\n";
                text += getResources().getString(R.string.carinformationfragment_text_latitude) + sharedPref.getString(getString(R.string.ggps_saved_latitude),"") + "\n";
                text += getResources().getString(R.string.carinformationfragment_text_longitude) + sharedPref.getString(getString(R.string.ggps_saved_longitude),"") + "\n";
                break;
            case "getgps":
                text += sharedPref.getBoolean(getString(R.string.getgps_saved_gps_state),false) + "\n";
                text += sharedPref.getString(getString(R.string.getgps_saved_satellites),"") + "\n";
                text += sharedPref.getString(getString(R.string.getgps_saved_latitude),"") + "\n";
                text += sharedPref.getString(getString(R.string.getgps_saved_longitude),"") + "\n";
                text += sharedPref.getString(getString(R.string.getgps_saved_altitude),"") + "\n";
                text += sharedPref.getString(getString(R.string.getgps_saved_speed),"") + "\n";
                text += sharedPref.getString(getString(R.string.getgps_saved_direction),"") + "\n";
                text += sharedPref.getString(getString(R.string.getgps_saved_date),"") + "\n";
                text += sharedPref.getString(getString(R.string.getgps_saved_time),"") + "\n";
                break;
            case "getio":
                for (String str:sharedPref.getStringSet(getString(R.string.getio_saved_digital_inputs),new HashSet<String>())) {
                    text += str + "\n";
                }
                for (String str:sharedPref.getStringSet(getString(R.string.getio_saved_analog_inputs),new HashSet<String>())) {
                    text += str + "\n";
                }
                for (String str:sharedPref.getStringSet(getString(R.string.getio_saved_digital_outputs),new HashSet<String>())) {
                    text += str + "\n";
                }
                break;
        }


        textview.setText(text);

        return myInflatedView;
    }
}
