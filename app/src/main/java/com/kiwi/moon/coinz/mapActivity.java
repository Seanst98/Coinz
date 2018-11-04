package com.kiwi.moon.coinz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.BiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class mapActivity extends AppCompatActivity implements
        OnMapReadyCallback, LocationEngineListener, PermissionsListener, DownloadCompleteRunner{

    private String TAG = "mapActivity";
    private MapView mapView;
    private MapboxMap map;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;

    String data;

    int totalCoinsCol;
    TextView totalCoins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.d(TAG, "BEGIN");


        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = (MapView) findViewById(R.id.mapboxMapView);


        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        totalCoinsCol = 0;
        totalCoins = (TextView) findViewById(R.id.totalCoins);

        totalCoins.setText("Total Coins Collected: " + totalCoinsCol);
    }

    @Override
    public void downloadComplete(String result) {
        data = result;

        /*try {
            GeoJsonOptions geoJsonOptions = new GeoJsonOptions().withCluster(false);
            GeoJsonSource geoJsonSource = new GeoJsonSource("geojson-source", data, geoJsonOptions);
            map.addSource(geoJsonSource);
        } catch (Throwable t) {
            Log.d(TAG, "FAIL");
        }*/

        /*SymbolLayer myLayer = new SymbolLayer("my.layer.id", "geojson-source");
        myLayer.setProperties(PropertyFactory.iconImage("{marker-symbol}"), PropertyFactory.iconAllowOverlap(true));
        map.addLayer(myLayer);*/


        IconFactory iconFactory = IconFactory.getInstance(mapActivity.this);

        Icon iconRed = iconFactory.fromResource(R.drawable.marker_red);
        Icon iconBlue = iconFactory.fromResource(R.drawable.marker_blue);
        Icon iconGreen = iconFactory.fromResource(R.drawable.marker_green);
        Icon iconYellow = iconFactory.fromResource(R.drawable.marker_yellow);

        FeatureCollection fc = FeatureCollection.fromJson(data);
        List<Feature> fs = fc.features();

        for (int i = 0; i < fs.size(); i++) {
            Geometry g = fs.get(i).geometry();
            String gt = g.toJson();
            Point p = Point.fromJson(gt);

            LatLng latLng = new LatLng(p.latitude(), p.longitude());

            JsonObject obj = fs.get(i).properties();
            JsonElement currencyt = obj.get("currency");
            JsonElement idt = obj.get("id");
            JsonElement valuet = obj.get("value");
            JsonElement marker_symbolt = obj.get("marker-symbol");
            JsonElement marker_colort = obj.get("marker-color");

            String currency = currencyt.getAsString();
            String id = idt.getAsString();
            String value = valuet.getAsString();
            String marker_symbol = marker_symbolt.getAsString();
            String marker_color = marker_colort.getAsString();

            Icon icon;




            if (marker_color.equals("#ffdf00")) {
                icon = iconYellow;

            }
            else if (marker_color.equals("#0000ff")) {
                icon = iconBlue;
            }
            else if (marker_color.equals("#ff0000")){
                icon = iconRed;

            }
            else {
                icon = iconGreen;
            }


            map.addMarker(new MarkerOptions().title(value).snippet(currency).icon(icon).position(latLng));

        }

        Log.d(TAG, "success ");

    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(TAG, "[onMapReady] mapBox is null");

        }
        else {
            Log.d(TAG, "mapbox is not null");
            map = mapboxMap;
            //Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            //Make location information available
            enableLocation();

            //Get markers
            DownLoadFileTask downLoadFileTask = new DownLoadFileTask();
            downLoadFileTask.delegate = this;
            downLoadFileTask.execute("http://homepages.inf.ed.ac.uk/stg/coinz/2018/10/03/coinzmap.geojson");

        }
    }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(TAG, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(TAG, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }

    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this)
                .obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000);   //Preferably every 5 seconds
        locationEngine.setFastestInterval(1000);   //At most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation!=null){
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        }
        else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer(){
        if (mapView == null) {
            Log.d(TAG, "mapView is null");
        } else {
            if (map == null) {
                Log.d(TAG, "map is null");
            } else {
                locationLayerPlugin = new LocationLayerPlugin(mapView,
                        map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }

    }

    private void setCameraPosition(Location location) {
        com.mapbox.mapboxsdk.geometry.LatLng latLng = new com.mapbox.mapboxsdk.geometry.LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng((latLng)));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(TAG, "[onLocationChanged] location is null");
        } else {
            Log.d(TAG, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);


            LatLng olatLng= new LatLng(originLocation.getLatitude(), originLocation.getLongitude());

            List<Marker> markers = map.getMarkers();

            for (int i = 0; i < markers.size(); i++) {

                Double dist = olatLng.distanceTo(markers.get(i).getPosition());

                if (dist <= 25) {

                    removeMarker(markers.get(i));
                    Toast.makeText(mapActivity.this, "Coin collected!",
                            Toast.LENGTH_SHORT).show();
                    map.removeMarker(markers.get(i));

                    totalCoinsCol++;
                    totalCoins.setText("Total Coins Collected: " + totalCoinsCol);
                }
            }
        }
    }

    public void removeMarker(Marker marker) {

        String currency = marker.getSnippet();
        String value = marker.getTitle();

    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(TAG, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain){
        Log.d(TAG, "Permissions: " + permissionsToExplain.toString());
        // Present toast or dialog.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(TAG, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            // Open a dialogue with the user
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}
