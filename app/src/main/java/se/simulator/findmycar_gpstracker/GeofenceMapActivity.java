package se.simulator.findmycar_gpstracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeofenceMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng coordinates;
    private int zoomLevel;
    Spinner zoneSpinner;
    Spinner shapeSpinner;
    int zoneNumber = 0;
    int shapeNumber = 0;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_geofence_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_geofence_map);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle args = getIntent().getExtras();
        coordinates = new LatLng(args.getDouble("latitude"),
                args.getDouble("longitude"));
        zoomLevel = args.getInt("zoomLevel");

        sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);

        addSpinners();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.geofence_map);
        mapFragment.getMapAsync(this);
    }

    private void addSpinners(){
        zoneSpinner = (Spinner) findViewById(R.id.geofence_zone_selection);
        shapeSpinner = (Spinner) findViewById(R.id.geofence_shape_selection);

        zoneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                zoneNumber = zoneSpinner.getSelectedItemPosition();
                shapeNumber = Character.getNumericValue(sharedPref.getString(getString(R.string.pref_geofence_shape),"00000").charAt(zoneNumber));
                Log.e("Test", "onItemSelected: " + shapeNumber);
                shapeSpinner.setSelection(shapeNumber);

                drawMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        shapeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                shapeNumber = shapeSpinner.getSelectedItemPosition();
                SharedPreferences.Editor editor = sharedPref.edit();

                String shapes = sharedPref.getString(getString(R.string.pref_geofence_shape),"00000");
                shapes = changeCharInPosition(zoneNumber,(char) shapeNumber,shapes);

                Toast.makeText(GeofenceMapActivity.this, shapes, Toast.LENGTH_SHORT).show();
                editor.putString(getString(R.string.pref_geofence_shape),shapes);
                editor.commit();

                drawMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    public String changeCharInPosition(int position, char ch, String str){
        Log.e("Before", "changeCharInPosition: " + str);
        char[] charArray = str.toCharArray();
        charArray[position] = ch;

        Log.e("After", "changeCharInPosition: " + new String(charArray));
        return new String(charArray);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMap();

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // Update GUI
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // Save state
            }
        });
    }

    private void drawMap(){
        /**
        mMap.addMarker(new MarkerOptions().position(coordinates).title(getString(R.string.map_title_marker)).draggable(true));
        CircleOptions circleOptions = new CircleOptions().center(coordinates).radius(10);
        final Circle circle = mMap.addCircle(circleOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates,zoomLevel));
        */
    }
}
