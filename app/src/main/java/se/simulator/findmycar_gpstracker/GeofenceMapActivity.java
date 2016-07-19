package se.simulator.findmycar_gpstracker;

import android.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;

public class GeofenceMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 2;
    private static final int numberOfGeofences = 5;
    private static final double geofenceDefaultWidth = 0.1;
    private static final double geofenceDefaultHeight = 0.1;
    private static final double geofenceDefaultCoordinateLatitude = 59.329323;  //Stockholm, Gustav Adolfs torg
    private static final double geofenceDefaultCoordinateLongitude = 18.068581;
    private static final int zoomPadding = 100;
    private static final int typeCircle = 0;
    private static final int typeRectangle = 1;

    private GoogleMap mMap;
    private Circle circle;
    private Polygon rectangle;
    private Marker markerCenter;
    private Marker markerTopLeft;
    private Marker markerTopRight;
    private Marker markerBottomLeft;
    private Marker markerBottomRight;
    private int zoneNumber = 0;
    private int shapeType = typeCircle;
    private SharedPreferences sharedPref;
    private final String noCoordinates = "no coordinates";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_geofence_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_geofence_map);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPref = getSharedPreferences(getString(R.string.pref_file_key),MODE_PRIVATE);

        addSpinners();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.geofence_map);
        mapFragment.getMapAsync(this);
    }

    private void addSpinners(){
        final Spinner zoneSpinner = (Spinner) findViewById(R.id.geofence_zone_selection);
        final Spinner shapeSpinner = (Spinner) findViewById(R.id.geofence_shape_selection);

        zoneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                zoneNumber = zoneSpinner.getSelectedItemPosition();
                shapeType = Character.getNumericValue(sharedPref.getString(getString(R.string.pref_geofence_shape),"00000").charAt(zoneNumber));
                shapeSpinner.setSelection(shapeType);

                drawMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        shapeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                shapeType = shapeSpinner.getSelectedItemPosition();
                SharedPreferences.Editor editor = sharedPref.edit();

                String shapes = sharedPref.getString(getString(R.string.pref_geofence_shape),"00000");
                shapes = changeCharInPosition(zoneNumber,Integer.toString(shapeType).charAt(0),shapes);

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
        char[] charArray = str.toCharArray();
        charArray[position] = ch;
        return new String(charArray);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMap();

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            private double diffLatitude;
            private double diffLongitude;
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (marker.equals(markerCenter) && shapeType == typeCircle){
                    diffLatitude = markerCenter.getPosition().latitude- markerTopLeft.getPosition().latitude;
                    diffLongitude = markerCenter.getPosition().longitude - markerTopLeft.getPosition().longitude;
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (shapeType == typeCircle){
                    if(marker.equals(markerCenter)){
                        circle.setCenter(marker.getPosition());
                        LatLng markerTopLeftPosition = new LatLng(markerCenter.getPosition().latitude - diffLatitude,markerCenter.getPosition().longitude - diffLongitude);
                        markerTopLeft.setPosition(markerTopLeftPosition);
                    }
                    else if(marker.equals(markerTopLeft)){
                        circle.setRadius(distanceBetweenLatLng(markerCenter.getPosition(),markerTopLeft.getPosition()));
                    }
                }
                else{
                    if (marker.equals(markerTopLeft)){
                        if(markerTopLeft.getPosition().latitude - markerBottomLeft.getPosition().latitude < 0){
                            markerTopLeft.setPosition(new LatLng(markerBottomLeft.getPosition().latitude,markerTopLeft.getPosition().longitude));
                        }
                        if(markerTopRight.getPosition().longitude - markerTopLeft.getPosition().longitude < 0){
                            markerTopLeft.setPosition(new LatLng(markerTopLeft.getPosition().latitude,markerTopRight.getPosition().longitude));
                        }
                        markerTopRight.setPosition(new LatLng(markerTopLeft.getPosition().latitude,markerTopRight.getPosition().longitude));
                        markerBottomLeft.setPosition(new LatLng(markerBottomLeft.getPosition().latitude,markerTopLeft.getPosition().longitude));
                    }
                    else if (marker.equals(markerTopRight)){
                        if(markerTopRight.getPosition().latitude - markerBottomRight.getPosition().latitude < 0){
                            markerTopRight.setPosition(new LatLng(markerBottomRight.getPosition().latitude,markerTopRight.getPosition().longitude));
                        }
                        if(markerTopRight.getPosition().longitude - markerTopLeft.getPosition().longitude < 0){
                            markerTopRight.setPosition(new LatLng(markerTopRight.getPosition().latitude,markerTopLeft.getPosition().longitude));
                        }
                        markerTopLeft.setPosition(new LatLng(markerTopRight.getPosition().latitude,markerTopLeft.getPosition().longitude));
                        markerBottomRight.setPosition(new LatLng(markerBottomRight.getPosition().latitude,markerTopRight.getPosition().longitude));
                    }
                    else if (marker.equals(markerBottomLeft)){
                        if(markerTopLeft.getPosition().latitude - markerBottomLeft.getPosition().latitude < 0){
                            markerBottomLeft.setPosition(new LatLng(markerTopLeft.getPosition().latitude,markerBottomLeft.getPosition().longitude));
                        }
                        if(markerBottomRight.getPosition().longitude - markerBottomLeft.getPosition().longitude < 0){
                            markerBottomLeft.setPosition(new LatLng(markerBottomLeft.getPosition().latitude,markerBottomRight.getPosition().longitude));
                        }
                        markerTopLeft.setPosition(new LatLng(markerTopLeft.getPosition().latitude,markerBottomLeft.getPosition().longitude));
                        markerBottomRight.setPosition(new LatLng(markerBottomLeft.getPosition().latitude,markerBottomRight.getPosition().longitude));
                    }
                    else if (marker.equals(markerBottomRight)){
                        if(markerTopRight.getPosition().latitude - markerBottomRight.getPosition().latitude < 0){
                            markerBottomRight.setPosition(new LatLng(markerTopRight.getPosition().latitude,markerBottomRight.getPosition().longitude));
                        }
                        if(markerBottomRight.getPosition().longitude - markerBottomLeft.getPosition().longitude < 0){
                            markerBottomRight.setPosition(new LatLng(markerBottomRight.getPosition().latitude,markerBottomLeft.getPosition().longitude));
                        }
                        markerTopRight.setPosition(new LatLng(markerTopRight.getPosition().latitude,markerBottomRight.getPosition().longitude));
                        markerBottomLeft.setPosition(new LatLng(markerBottomRight.getPosition().latitude,markerBottomLeft.getPosition().longitude));
                    }

                    rectangle.setPoints(Arrays.asList(markerTopLeft.getPosition(),markerTopRight.getPosition(),markerBottomRight.getPosition(),markerBottomLeft.getPosition()));
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                saveMarkerCoordinates();
            }
        });
    }

    private void saveMarkerCoordinates(){
        String coordinateString = sharedPref.getString(getString(R.string.pref_geofence_coordinates),noCoordinates);
        String before;
        String after;
        int endIndex = 0;

        if (!coordinateString.equals(noCoordinates)){
            for (int i = 0; i < zoneNumber; i++) {
                endIndex = coordinateString.indexOf(':',endIndex + 1);
            }
            if (endIndex == 0){
                before = "";
            }
            else{
                before = coordinateString.substring(0,endIndex+1);
            }
            endIndex = coordinateString.indexOf(':',endIndex + 1);
            after = coordinateString.substring(endIndex+1);


            if (shapeType == typeCircle){
                double TopLeftLatitude = markerTopLeft.getPosition().latitude;
                double TopLeftLongitude = markerTopLeft.getPosition().longitude;
                if (TopLeftLatitude-markerCenter.getPosition().latitude < 0){
                    TopLeftLatitude = 2*markerCenter.getPosition().latitude - TopLeftLatitude;
                }
                if (markerCenter.getPosition().longitude - TopLeftLongitude < 0){
                    TopLeftLongitude = 2*markerCenter.getPosition().longitude - TopLeftLongitude;
                }

                coordinateString = String.format("%s %s %s %s :"
                        ,markerCenter.getPosition().latitude
                        ,markerCenter.getPosition().longitude
                        ,TopLeftLatitude
                        ,TopLeftLongitude);
            }else if(shapeType == typeRectangle){
                coordinateString = String.format("%s %s %s %s :"
                        ,(markerTopLeft.getPosition().latitude + markerBottomLeft.getPosition().latitude)/2
                        ,(markerTopLeft.getPosition().longitude + markerTopRight.getPosition().longitude)/2
                        ,markerTopLeft.getPosition().latitude
                        ,markerTopLeft.getPosition().longitude);
            }
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.pref_geofence_coordinates),before + coordinateString + after);
            editor.commit();
        }
    }

    private float distanceBetweenLatLng(LatLng obj1, LatLng obj2){
        float[] radius = new float[1];
        Location.distanceBetween(obj1.latitude,obj1.longitude,obj2.latitude,obj2.longitude,radius);
        return radius[0];
    }

    private void drawMap(){
        mMap.clear();

        String coordinateString = sharedPref.getString(getString(R.string.pref_geofence_coordinates),noCoordinates);
        if (coordinateString.equals(noCoordinates)){
            initCoordinates();
            return;
        }

        LatLng centerCoordinates = getLatLngFromString(coordinateString,0);
        LatLng topLeftCoordinates = getLatLngFromString(coordinateString,1);

        if (shapeType == typeCircle){
            CircleOptions circleOptions = new CircleOptions()
                    .center(centerCoordinates)
                    .radius(distanceBetweenLatLng(centerCoordinates,topLeftCoordinates))
                    .fillColor(ContextCompat.getColor(this,R.color.color_geofence_background))
                    .strokeColor(ContextCompat.getColor(this,R.color.color_geofence_edge));
            circle = mMap.addCircle(circleOptions);

            markerCenter = mMap.addMarker(new MarkerOptions().position(centerCoordinates).draggable(true));
            markerTopLeft = mMap.addMarker(new MarkerOptions().position(topLeftCoordinates).draggable(true));


            // Zoom to fit
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(markerCenter.getPosition());
            builder.include(markerTopLeft.getPosition());
            builder.include(new LatLng(
                    2*markerCenter.getPosition().latitude - markerTopLeft.getPosition().latitude,
                    2*markerCenter.getPosition().longitude - markerTopLeft.getPosition().longitude
            ));
            LatLngBounds bounds = builder.build();

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,zoomPadding));
        }
        else{
            markerTopLeft = mMap.addMarker(new MarkerOptions().position(topLeftCoordinates).draggable(true));
            markerBottomLeft = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(2*centerCoordinates.latitude-topLeftCoordinates.latitude,topLeftCoordinates.longitude))
                    .draggable(true));
            markerTopRight = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(topLeftCoordinates.latitude,2*centerCoordinates.longitude - topLeftCoordinates.longitude))
                    .draggable(true));
            markerBottomRight = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(2*centerCoordinates.latitude-topLeftCoordinates.latitude,2*centerCoordinates.longitude - topLeftCoordinates.longitude))
                    .draggable(true));

            PolygonOptions polygonOptions = new PolygonOptions()
                    .add(markerTopLeft.getPosition(),markerBottomLeft.getPosition(),markerBottomRight.getPosition(),markerTopRight.getPosition())
                    .strokeColor(ContextCompat.getColor(this,R.color.color_geofence_edge))
                    .fillColor(ContextCompat.getColor(this,R.color.color_geofence_background));

            rectangle = mMap.addPolygon(polygonOptions);
        }


    }

    private LatLng getLatLngFromString(String str, int opt){
        double latitude;
        double longitude;
        int endIndex;

        for (int i = 0; i < zoneNumber; i++) {
            str = str.substring(str.indexOf(':')+1);
        }

        try {
            if (opt == 0) {
                endIndex = str.indexOf(' ');
                latitude = Double.parseDouble(str.substring(0, endIndex));
                str = str.substring(endIndex + 1);
                endIndex = str.indexOf(' ');
                longitude = Double.parseDouble(str.substring(0, endIndex));
            } else {
                endIndex = str.indexOf(' ');
                endIndex = str.indexOf(' ', endIndex + 1);
                str = str.substring(endIndex + 1);
                endIndex = str.indexOf(' ');
                latitude = Double.parseDouble(str.substring(0, endIndex));
                str = str.substring(endIndex + 1);
                endIndex = str.indexOf(' ');
                longitude = Double.parseDouble(str.substring(0, endIndex));
            }
        }catch (Exception e){
            latitude = 0;
            longitude = 0;
            Log.e("Parse error", "getLatLngFromString: ", e);
        }

        return new LatLng(latitude,longitude);
    }

    private void initCoordinates(){
        if (checkPermissionLocation()){
            try {
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                String centerLatitude = Double.toString(lastKnownLocation.getLatitude());
                String centerLongitude = Double.toString(lastKnownLocation.getLongitude());
                String topLeftLatitude = Double.toString(lastKnownLocation.getLatitude() + geofenceDefaultHeight/2);
                String topLeftLongitude = Double.toString(lastKnownLocation.getLongitude() - geofenceDefaultWidth/2);
                String coordinateString = "";
                for (int i = 0; i < numberOfGeofences; i++) {
                    coordinateString += String.format("%s %s %s %s :"
                            ,centerLatitude
                            ,centerLongitude
                            ,topLeftLatitude
                            ,topLeftLongitude);
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.pref_geofence_coordinates),coordinateString);
                editor.commit();
                drawMap();
            }catch (SecurityException e) {

            }

        }
    }

    private boolean checkPermissionLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initCoordinates();
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    String centerLatitude = Double.toString(geofenceDefaultCoordinateLatitude);
                    String centerLongitude = Double.toString(geofenceDefaultCoordinateLongitude);
                    String topLeftLatitude = Double.toString(geofenceDefaultCoordinateLatitude + geofenceDefaultHeight/2);
                    String topLeftLongitude = Double.toString(geofenceDefaultCoordinateLongitude - geofenceDefaultWidth/2);
                    String coordinateString = "";
                    for (int i = 0; i < numberOfGeofences; i++) {
                        coordinateString += String.format("%s %s %s %s :"
                                ,centerLatitude
                                ,centerLongitude
                                ,topLeftLatitude
                                ,topLeftLongitude);
                    }
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.pref_geofence_coordinates),coordinateString);
                    editor.commit();
                    drawMap();
                }
                break;
            }
        }
    }
}
