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
        Bundle args = getArguments();
        String text = "";
        if (args != null) {
            text += getResources().getString(R.string.carinformationfragment_text_date) + args.getString("Date") + "\n";
            text += getResources().getString(R.string.carinformationfragment_text_time) + args.getString("Time") + "\n";
            text += getResources().getString(R.string.carinformationfragment_text_speed) + args.getString("Speed") + "\n";
            text += getResources().getString(R.string.carinformationfragment_text_latitude) + args.getString("Latitude") + "\n";
            text += getResources().getString(R.string.carinformationfragment_text_longitude) + args.getString("Longitude") + "\n";
            TextView textview = (TextView) myInflatedView.findViewById(R.id.text_car_information);
            textview.setText(text);
        }

        return myInflatedView;
    }
}
