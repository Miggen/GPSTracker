package se.simulator.findmycar_gpstracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class CarInformationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myInflatedView = inflater.inflate(R.layout.fragment_car_information, container, false);

        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.pref_file_key),getContext().MODE_PRIVATE);

        String text = "";

        text += getResources().getString(R.string.carinformationfragment_text_date) + sharedPref.getString(getString(R.string.saved_date),"") + "\n";
        text += getResources().getString(R.string.carinformationfragment_text_time) + sharedPref.getString(getString(R.string.saved_time),"") + "\n";
        text += getResources().getString(R.string.carinformationfragment_text_speed) + sharedPref.getString(getString(R.string.saved_speed),"") + "\n";
        text += getResources().getString(R.string.carinformationfragment_text_latitude) + sharedPref.getString(getString(R.string.saved_latitude),"") + "\n";
        text += getResources().getString(R.string.carinformationfragment_text_longitude) + sharedPref.getString(getString(R.string.saved_longitude),"") + "\n";
        TextView textview = (TextView) myInflatedView.findViewById(R.id.text_car_information);
        textview.setText(text);

        return myInflatedView;
    }
}
