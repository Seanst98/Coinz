package com.kiwi.moon.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.common.collect.BiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.BoundingBox;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import java.util.Calendar;

public class mapActivity extends AppCompatActivity implements
        OnMapReadyCallback, LocationEngineListener, PermissionsListener, DownloadCompleteRunner{

    private String TAG = "mapActivity";
    private MapView mapView;
    private MapboxMap map;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;

    private DrawerLayout drawerLayout;

    String data;
    int totalCoinsCol;
    TextView totalCoins;
    private String downloadDate = "";   //Format:YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile";   //For storing preferences
    Date now = new Date();
    String currentDate = new SimpleDateFormat("yyyy/MM/dd").format(now);


    public class Properties {

        public Properties(String i, String v, String c, String ms, String mc) {
            id = i;
            value = v;
            currency = c;
            marker_symbol = ms;
            marker_color = mc;
        }

        public String id;
        public String value;
        public String currency;

        @SerializedName("marker-symbol")
        public String marker_symbol;

        @SerializedName("marker-color")
        public String marker_color;
    }

    public class Coin {

        public String type;
        public Properties properties;
        public Geometry geometry;

        public Coin(String t, Geometry g, Properties p) {
            type = t;
            properties = p;
            geometry = g;
        }

    }

    public class JsonData {

        public String type;

        @SerializedName("date-generated")
        public String date_generated;

        @SerializedName("time-generated")
        public String time_generated;

        @SerializedName("approximate-time-remaining")
        public String approximate_time_remaining;

        public Rates rates;

        public List<Coin> features;

        public JsonData(String t, String d, String tg, String app, Rates r, List<Coin> f) {
            type = t;
            date_generated = d;
            time_generated = tg;
            approximate_time_remaining = app;
            rates = r;
            features = f;
        }


        public String toJson() {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            return gson.toJson(this);
        }
    }

    public class Rates {

        public String SHIL;
        public String DOLR;
        public String QUID;
        public String PENY;

        public Rates(String s, String d, String q, String p) {
            SHIL = s;
            DOLR = d;
            QUID = q;
            PENY = p;
        }
    }

    private JsonData jsonData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.d(TAG, "BEGIN");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        switch (id) {

                            case R.id.drawer_bank:
                                Toast.makeText(getApplicationContext(), "drawer selected", Toast.LENGTH_SHORT).show();

                            case R.id.drawer_optional:
                                Toast.makeText(getApplicationContext(), "optional selected", Toast.LENGTH_SHORT).show();

                            case R.id.drawer_personal:
                                Toast.makeText(getApplicationContext(), "personal selected", Toast.LENGTH_SHORT).show();

                            case R.id.drawer_statistics:
                                Toast.makeText(getApplicationContext(), "statistics selected", Toast.LENGTH_SHORT).show();
                        }
                        //Set item as selected to persist highlight
                        menuItem.setChecked(true);

                        //Close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        //Take user to screen depending on menuItem

                        return true;
                    }
                }
        );

        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = (MapView) findViewById(R.id.mapboxMapView);


        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        totalCoinsCol = 0;
        totalCoins = (TextView) findViewById(R.id.totalCoins);

        totalCoins.setText("Total Coins Collected: " + totalCoinsCol);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void downloadComplete(String result) {
        data = result;

        Log.d(TAG, "Download was " + data);
        displayMarkers();

    }

    public void displayMarkers() {

        String dg = "";
        String tg = "";
        String app = "";

        Rates rates = null;

        String shil = "";
        String dolr = "";
        String quid = "";
        String peny = "";

        try {

            JSONObject collection = new JSONObject(data);
            dg = collection.getString("date-generated");
            tg = collection.getString("time-generated");
            app = collection.getString("approximate-time-remaining");

            JSONObject ratesl = collection.getJSONObject("rates");
            shil = ratesl.getString("SHIL");
            dolr = ratesl.getString("DOLR");
            quid = ratesl.getString("QUID");
            peny = ratesl.getString("PENY");

            rates = new Rates(shil, dolr, quid, peny);


        } catch (JSONException e) {
            Log.d(TAG, "JSONException " + e.toString());
        }

        IconFactory iconFactory = IconFactory.getInstance(mapActivity.this);

        Icon iconRed = iconFactory.fromResource(R.drawable.marker_red);
        Icon iconBlue = iconFactory.fromResource(R.drawable.marker_blue);
        Icon iconGreen = iconFactory.fromResource(R.drawable.marker_green);
        Icon iconYellow = iconFactory.fromResource(R.drawable.marker_yellow);

        FeatureCollection fc = FeatureCollection.fromJson(data);
        List<Feature> fs = fc.features();

        List<Coin> coins = new ArrayList<>();

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

            Properties props = new Properties(id, value, currency, marker_symbol, marker_color);
            Coin coin = new Coin("Feature", g, props);
            coins.add(coin);

            Marker marker = map.addMarker(new MarkerOptions().title(value).snippet(currency).icon(icon).position(latLng));

            jsonData = new JsonData(fc.type(), dg, tg, app, rates, coins);


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
            if (!currentDate.equals(downloadDate)) {
                DownLoadFileTask downLoadFileTask = new DownLoadFileTask();
                downLoadFileTask.delegate = this;
                Log.d(TAG, "Downloading file from " + "http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson");
                downLoadFileTask.execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson");
                downloadDate = currentDate;
                Log.d(TAG, "[OnMapReady] Downloading map since it is a new day");
            }
            else {

                //Restore preferences
                SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

                //Use "" as default value (this might be the first time the app is run)
                String json = settings.getString("JSON", "");
                Log.d(TAG, "[onMapReady] Recalled JSON is '" + json + "'");

                data = json;

                displayMarkers();
            }
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

    @SuppressWarnings({"MissingPermission", "ResourceType"})
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

    public void checkMarkers() {
        LatLng olatLng= new LatLng(originLocation.getLatitude(), originLocation.getLongitude());

        List<Marker> markers = map.getMarkers();

        for (int i = 0; i < markers.size(); i++) {

            Double dist = olatLng.distanceTo(markers.get(i).getPosition());

            if (dist <= 25) {

                removeMarker(markers.get(i), i);

            }
        }
    }

    public void removeMarker(Marker marker, int i) {

        Toast.makeText(getApplicationContext(), "Coin collected!",
                Toast.LENGTH_SHORT).show();


        jsonData.features.remove(i);
        map.removeMarker(marker);

        marker.remove();

        totalCoinsCol++;
        totalCoins.setText("Total Coins Collected: " + totalCoinsCol);

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(TAG, "[onLocationChanged] location is null");
        } else {
            Log.d(TAG, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);

            checkMarkers();
        }
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

    private void stopLocationListener() {
        Log.d(TAG, "Stopping Location Engine Listener");
        if (locationEngine !=null) locationEngine.removeLocationUpdates();
        if (locationEngine !=null) locationEngine.deactivate();
        if (locationEngine !=null) locationEngine.removeLocationEngineListener(this);
        if (locationEngine !=null) locationEngine = null;
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();

        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }

        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }

        //Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        //Use "" as default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        Log.d(TAG, "[onStart] Recalled lastDownloadDate is '" + downloadDate + "'");
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

        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }

        if (locationEngine != null) {
            stopLocationListener();
        }

        Log.d(TAG, "[onStop] Storing lastDownloadDate of " + downloadDate);
        Log.d(TAG, "[onStop] Storing JSON " + data);
        //All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        //We need an Editor object to make preference changes
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        //editor.putString("JSON", data);
        editor.putString("JSON", jsonData.toJson());


        Log.d(TAG, "[onStop] JSON CONVERSION " + jsonData.toJson());

        //Apply the edits
        editor.apply();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
        stopLocationListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        stopLocationListener();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}
