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
                text += getResources().getString(R.string.carinformationfragment_text_date) + sharedPref.getString(getString(R.string.saved_date),"") + "\n";
                text += getResources().getString(R.string.carinformationfragment_text_time) + sharedPref.getString(getString(R.string.saved_time),"") + "\n";
                text += getResources().getString(R.string.carinformationfragment_text_speed) + sharedPref.getString(getString(R.string.saved_speed),"") + "\n";
                text += getResources().getString(R.string.carinformationfragment_text_latitude) + sharedPref.getString(getString(R.string.saved_latitude),"") + "\n";
                text += getResources().getString(R.string.carinformationfragment_text_longitude) + sharedPref.getString(getString(R.string.saved_longitude),"") + "\n";
                break;
            case "getgps":
                text += "Test : GPS data 2";
                break;
            case "getinfo":
                text += "Test : Tracker information";
                break;
        }


        textview.setText(text);

        return myInflatedView;
    }
}
