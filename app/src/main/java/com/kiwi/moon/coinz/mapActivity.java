package com.kiwi.moon.coinz;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
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
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class mapActivity extends AppCompatActivity implements
        OnMapReadyCallback, LocationEngineListener, PermissionsListener, DownloadCompleteRunner, UserListener{

    //*******************************************
    //Declare variables
    //*******************************************
    private String TAG = "mapActivity";
    private MapView mapView;
    private MapboxMap map;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;

    private DrawerLayout drawerLayout;

    String data;
    TextView totalCoins;
    TextView ghostTimeTrialTime;
    int timeCount = 0;

    Timer ghostTimer;
    CountDownTimer timeTrialTimer;


    private String downloadDate = "";   //Format:YYYY/MM/DD
    private String coinsDownloadDate = "";
    private String fireStoreDate = "";
    private String currentUser = "";
    private final String preferencesFile = "MyPrefsFile";   //For storing preferences
    Date now = new Date();
    String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(now);

    //Access a cloud firestore instance from the bank activity
    FirebaseAuth mAuth =  FirebaseAuth.getInstance();

    User user;

    private JsonData jsonData;   //Holds data about the coins on the map
    private JsonData coinsCollectedData;   //Holds data about the coins collected


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.d(TAG, "BEGIN");

        user = User.getinstance();
        user.delegate = this;

        //*******************************************
        //Toolbar for navigation drawer
        //*******************************************
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        else {
            Log.d(TAG, "Action bar is null");
        }


        //*******************************************
        //Navigation drawer and its functionality
        //*******************************************
        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        //*******************************************
                        //Go to whichever activity the user selects
                        //*******************************************

                        //Protection from opening the activities without the data downloaded
                        if (jsonData.getRates().DOLR.equals("")){
                            Toast.makeText(getApplicationContext(), "Please wait until the map data has finished downloading", Toast.LENGTH_LONG).show();
                        }
                        else {

                            switch (id) {

                                //Goto the bank activity passing the coins collected data
                                case R.id.drawer_bank:
                                    Intent intent = new Intent(mapActivity.this, BankActivity.class);
                                    intent.putExtra("coinsCollected", coinsCollectedData.toJson());
                                    Log.d(TAG, "MAP TO BANK STORED COINS ARE: " + coinsCollectedData.toJson());
                                    startActivity(intent);
                                    break;

                                //Goto the optional features activity
                                case R.id.drawer_optional:
                                    Intent intent1 = new Intent(mapActivity.this, OptionalFeaturesActivity.class);
                                    startActivity(intent1);
                                    break;

                                //Goto the personal info activity
                                case R.id.drawer_personal:
                                    Intent intent2 = new Intent(mapActivity.this, PersonalActivity.class);
                                    startActivity(intent2);
                                    break;

                                //Goto the statistics activity
                                case R.id.drawer_statistics:
                                    Intent intent3 = new Intent(mapActivity.this, StatisticsActivity.class);
                                    startActivity(intent3);
                                    break;
                            }

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

        //*******************************************
        //Set up Mapbox
        //*******************************************
        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = findViewById(R.id.mapboxMapView);


        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //*******************************************
        //Set up any text boxes on the screen
        //*******************************************
        totalCoins = findViewById(R.id.totalCoins);
        ghostTimeTrialTime = findViewById(R.id.ghostTimeTrialTime);
        totalCoins.setText("Coins Collected: " + 0);
        ghostTimeTrialTime.setText("Time");
    }

    //*******************************************
    //This is necessary for highlighting the
    //navigation drawer options when clicked
    //*******************************************
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    //*******************************************
    //Override the DownloadCompleteRunner function
    //*******************************************
    @Override
    public void downloadComplete(String result) {
        data = result;
        displayMarkers();
    }

    //*******************************************
    //Parse the json data and display the markers
    //*******************************************
    public void displayMarkers() {

        String dg = "";
        String tg = "";
        String app = "";

        Rates rates = null;

        /*String shil = "";
        String dolr = "";
        String quid = "";
        String peny = "";*/

        try {

            JSONObject collection = new JSONObject(data);
            dg = collection.getString("date-generated");
            tg = collection.getString("time-generated");
            app = collection.getString("approximate-time-remaining");

            JSONObject ratesl = collection.getJSONObject("rates");
            String shil = ratesl.getString("SHIL");
            String dolr = ratesl.getString("DOLR");
            String quid = ratesl.getString("QUID");
            String peny = ratesl.getString("PENY");

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
        List<Coin> coins2 = new ArrayList<>();

        if (fs != null){
            for (int i = 0; i < fs.size(); i++) {
                Geometry g = fs.get(i).geometry();

                String gt = "";
                if (g !=null){
                    gt = g.toJson();
                }
                else {
                    Log.d(TAG, "g is null");
                }
                Point p = Point.fromJson(gt);

                LatLng latLng = new LatLng(p.latitude(), p.longitude());

                JsonObject obj = fs.get(i).properties();

                String currency="";
                String id="";
                String value="";
                String marker_symbol="";
                String marker_color="";

                if (obj!=null){
                    JsonElement currencyt = obj.get("currency");
                    JsonElement idt = obj.get("id");
                    JsonElement valuet = obj.get("value");
                    JsonElement marker_symbolt = obj.get("marker-symbol");
                    JsonElement marker_colort = obj.get("marker-color");

                    currency = currencyt.getAsString();
                    id = idt.getAsString();
                    value = valuet.getAsString();
                    marker_symbol = marker_symbolt.getAsString();
                    marker_color = marker_colort.getAsString();
                }

                Icon icon;

                switch (marker_color) {

                    case "#ffdf00":
                        icon = iconYellow;
                        break;

                    case "#0000ff":
                        icon = iconBlue;
                        break;

                    case "#ff0000":
                        icon = iconRed;
                        break;

                    default:
                        icon = iconGreen;
                }

                Properties props = new Properties(id, value, currency, marker_symbol, marker_color);
                Coin coin = new Coin("Feature", g, props);
                coins.add(coin);
                coins2.add(coin);

                map.addMarker(new MarkerOptions().title(value).snippet(currency).icon(icon).position(latLng));

            }
        }
        else {
            Log.d(TAG, "fs is null");
        }

        jsonData = new JsonData(fc.type(), dg, tg, app, rates, coins);


        //*******************************************
        //If the last time we saved the coins collected
        //was not today, then reset the coins collected
        //and update the date saying when we last had
        //stored collected coins
        //*******************************************
        if (coinsCollectedData == null || !currentDate.equals(coinsDownloadDate)){

            coinsCollectedData = new JsonData(fc.type(), dg, tg, app, rates, coins2);

            while (!coinsCollectedData.getFeatures().isEmpty()){

                coinsCollectedData.getFeatures().remove(0);
            }

            coinsDownloadDate = currentDate;

        }
    }

    //*******************************************
    //When the map is ready, set up location and
    //downlaod the markers if needed
    //*******************************************
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(TAG, "[onMapReady] mapBox is null");
        }
        else {
            map = mapboxMap;
            //Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            //Make location information available
            enableLocation();

            //Get markers
            if (!currentDate.equals(downloadDate)) {
                DownLoadFileTask downLoadFileTask = new DownLoadFileTask();
                downLoadFileTask.delegate = this;   //Set the delegate so the class knows to use this activity's downloadComplete function
                Log.d(TAG, "Downloading file from " + "http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson");
                downLoadFileTask.execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson");
                downloadDate = currentDate;
            }
            //If the user is different from the previous user download the markers again
            else if (!currentUser.equals(mAuth.getUid())){
                currentUser = mAuth.getUid();
                DownLoadFileTask downLoadFileTask = new DownLoadFileTask();
                downLoadFileTask.delegate = this;
                Log.d(TAG, "Downloading file from " + "http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson");
                downLoadFileTask.execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson");
                downloadDate = currentDate;
                Log.d(TAG, "[OnMapReady] Downloading map since it is a new day");
            }
            else {   //If the map stored is up to date/has been downloaded before

                //Restore preferences
                SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

                //Use "" as default value (this might be the first time the app is run)
                String json = settings.getString("JSON", "");
                Log.d(TAG, "[onMapReady] Recalled JSON is '" + json + "'");

                data = json;

                //Display the markers
                //We call this here since we already have the markers as they were stored in shared preferences
                displayMarkers();
            }
        }
    }


    //*******************************************
    //Ask for permission by the user to find their
    //lcoation and use it
    //*******************************************
    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {   //If we have permission to access the location
            Log.d(TAG, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {   //If we don't have permission to access the lcoation
            Log.d(TAG, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }

    }

    //*******************************************
    //Initialize the location engine
    //*******************************************
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

    //*******************************************
    //Initialize the location layer
    //*******************************************
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

    //*******************************************
    //Set the camera position
    //*******************************************
    private void setCameraPosition(Location location) {
        com.mapbox.mapboxsdk.geometry.LatLng latLng = new com.mapbox.mapboxsdk.geometry.LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng((latLng)));
    }


    //*******************************************
    //Check the markers for if we can collect them
    //*******************************************
    public void checkMarkers() {
        LatLng olatLng= new LatLng(originLocation.getLatitude(), originLocation.getLongitude());

        List<Marker> markers = map.getMarkers();

        for (int i = 0; i < markers.size(); i++) {

            Double dist = olatLng.distanceTo(markers.get(i).getPosition());

            if (dist <= 25) {   //If we are within 25 metres of the coin

                Log.d(TAG, "WITHIN 25m");
                removeMarker(markers.get(i), i);

            }
        }
    }

    //*******************************************
    //Remove the marker from the map and update
    //the relevant data
    //*******************************************
    public void removeMarker(Marker marker, int i) {

        Log.d(TAG, "START OF FUNC");


        user.dayCoins++;
        user.totalCoins++;
        switch (marker.getSnippet()) {   //Update the user's statistics on the value collected

            case "SHIL":
                user.shil = user.shil + Double.parseDouble(marker.getTitle());
                user.shilCoins = user.shilCoins + 1;
                break;

            case "DOLR":
                user.dolr = user.dolr + Double.parseDouble(marker.getTitle());
                user.dolrCoins = user.dolrCoins + 1;
                break;

            case "PENY":
                user.peny = user.peny + Double.parseDouble(marker.getTitle());
                user.penyCoins = user.penyCoins + 1;
                break;

            case "QUID":
                user.quid = user.quid + Double.parseDouble(marker.getTitle());
                user.quidCoins = user.quidCoins + 1;
                break;
        }

        totalCoins.setText("Coins Collected: " + user.dayCoins);

        coinsCollectedData.getFeatures().add(jsonData.getFeatures().get(i));   //Add the coin to the user's collection
        jsonData.getFeatures().removeAll(coinsCollectedData.getFeatures());   //Remove it from the map's collection
        map.removeMarker(marker);   //Remove the marker from the mapbox map
        marker.remove();

        user.updateUser();   //Update the user

        Toast.makeText(getApplicationContext(), "Coin collected!",
                Toast.LENGTH_SHORT).show();


        //*******************************************
        //Deal with what happens when the user collects
        //a coin and in a bonus mode
        //*******************************************
        if (user.timeTrialMode){
            timeTrialTimer.cancel();
            createTimeTrialTimer();
        }

        if (user.dayCoins == 50) {
            if (user.ghostMode){
                ghostWin();
            }
            else if (user.timeTrialMode){
                timeTrialWin();
            }
        }


    }

    //*******************************************
    //When the location of the player changes
    //*******************************************
    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(TAG, "[onLocationChanged] location is null");
        } else {
            Log.d(TAG, "[onLocationChanged] location is not null");


            //*******************************************
            //If the player has actually moved
            //the player must have moved for the program
            //to calculate distance moved
            //*******************************************
            if (originLocation!=null) {

                LatLng latLng = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
                LatLng latLng1 = new LatLng(location.getLatitude(), location.getLongitude());

                Double dist = latLng.distanceTo(latLng1);   //Get distance moved

                user.dayWalked = user.dayWalked + dist;
                user.totalWalked = user.totalWalked + dist;
                user.bankGold = user.bankGold + (dist / 100000);   //Bonus feature where moving gives GOLD

                user.updateUser();
            }

            //Update location and camera
            originLocation = location;
            setCameraPosition(location);

            //Check if we are near enough to a marker now that we have moved
            checkMarkers();
        }
    }

    //*******************************************
    //Once the program is connected to the location
    //*******************************************
    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(TAG, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    //*******************************************
    //When the user wants a reason for the permissions
    //*******************************************
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain){
        Log.d(TAG, "Permissions: " + permissionsToExplain.toString());
        // Present toast or dialog.
        Toast.makeText(getApplicationContext(), "We Need Your Location For You To Play The Game", Toast.LENGTH_SHORT).show();
    }

    //*******************************************
    //When we receive the result from asking for
    //permissions
    //*******************************************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //*******************************************
    //When we receive the result from asking for
    //permissions we can enable location tracking
    //*******************************************
    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(TAG, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            // Open a dialogue with the user
            Toast.makeText(getApplicationContext(), "We Need Your Location For You To Play The Game", Toast.LENGTH_SHORT).show();
        }
    }

    //*******************************************
    //Stop the location listener
    //Prevents memory leaks
    //*******************************************
    private void stopLocationListener() {
        Log.d(TAG, "Stopping Location Engine Listener");
        if (locationEngine !=null) locationEngine.removeLocationUpdates();
        if (locationEngine !=null) locationEngine.deactivate();
        if (locationEngine !=null) locationEngine.removeLocationEngineListener(this);
        if (locationEngine !=null) locationEngine = null;
    }

    //*******************************************
    //When the activity starts
    //*******************************************
    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "[OnStart] is called");

        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();   //Start the location layer plugin
        }

        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();   //Start the location engine
        }

        //Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        //Use "" as default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        fireStoreDate = settings.getString("lastFireStoreDate", "");
        currentUser = settings.getString("currentUser", "");
        String coinsCollected = settings.getString("coinsCollected", "");
        coinsDownloadDate = settings.getString("coinsLastDownloadDate", "");

        Log.d(TAG, "THE STORED COINS ARE: " + coinsCollected);


        //If there are problems with the json
        //or we don't have any json
        if (coinsCollected == null){
            Log.d(TAG, "coins collected is null");
        }
        else if (coinsCollected.equals("[]")){
            Log.d(TAG, "coins collected is []");
        }
        else if (coinsCollected.equals("")){
            Log.d(TAG, "coins collected is '' ");
        }
        else {

            coinsCollectedData = new JsonData(coinsCollected);   //Create the coinsCollectedData

        }

        //Start the map if the user data has already been downloaded
        //Otherwise carry on and wait until it is downloaded
        Log.d(TAG, "[start] starting check");
        if (user.loaded){   //If we haven't retrieved the user data before
            Log.d(TAG, "MAPVIEW START CALLED");

            if (!currentDate.equals(fireStoreDate)) {   //If it's a new day then update Firebase info
                user.dayCoins = 0;
                user.dayWalked = 0;
                user.coinsDepositedDay = 0;

                user.updateUser();
            }
            fireStoreDate = currentDate;
            totalCoins.setText("Coins Collected: " + user.dayCoins);

            mapView.onStart();   //Start the map (the game)
        }
        else {
            user.getUser();
        }


        //*******************************************
        //Deal with the bonus features that require timers
        //*******************************************
        ghostTimeTrialTime.setAlpha(0.0f);   //Ghost mode and time trial mode share the same text box
                                            //Make the text box invisible

        //If the user is using ghost mode
        //We don't need to make a method of creating a timer for ghost mode as this is only done
        //once
        if (user.ghostMode){
            ghostTimeTrialTime.setAlpha(1.0f);   //Make the text box invisible
            if (ghostTimer == null){
                ghostTimer = new Timer();   //Create a new timer that counts up every second
                ghostTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ghostTimeTrialTime.setText("Time: " + timeCount);
                                timeCount++;
                            }
                        });
                    }
                }, 1000, 1000);   //Update every second
            }
        }

        //If the user is using time trial mode
        //We methodise creating a time trial timer since we have to create a new timer every time a
        //coin has been collected i.e. the timer is reset whenever we collect a coin
        if (user.timeTrialMode) {
            createTimeTrialTimer();   //Create a timer
        }


        //*******************************************
        //Cancel the timers if the user is not in a
        //bonus mode anymore (when the user de-activates
        //a mode)
        //*******************************************
        if (!user.timeTrialMode){
            if (timeTrialTimer!=null){
                timeTrialTimer.cancel();
            }
        }
        if (!user.ghostMode){
            if (ghostTimer!=null){
                ghostTimer.cancel();
            }
        }

    }

    //*******************************************
    //Override the userListener function so that
    //when the user data is downloaded we can start
    //the map/set some data
    //*******************************************
    @Override
    public void onDataLoaded() {

        Log.d(TAG, "[onDataLoaded map] data is laoded");
        Log.d(TAG, "MAPVIEW START CALLED");

        if (!currentDate.equals(fireStoreDate)){
            user.dayCoins = 0;
            user.dayWalked = 0;
            user.coinsDepositedDay = 0;

            user.updateUser();
        }
        fireStoreDate = currentDate;
        totalCoins.setText("Coins Collected: " + user.dayCoins);

        mapView.onStart();   //Start the map (the game)

    }

    //*******************************************
    //Create a time trial timer that counts down
    //*******************************************
    public void createTimeTrialTimer(){

        ghostTimeTrialTime.setAlpha(1.0f);
        if (timeTrialTimer == null){
            timeTrialTimer = new CountDownTimer(120000, 1000) {   //120 seconds to collect a coin, update every second
                @Override
                public void onTick(long millisUntilFinished) {
                    ghostTimeTrialTime.setText("Time Left: " + millisUntilFinished/1000);
                }

                @Override
                public void onFinish() {
                    timeTrialFail();
                }
            }.start();
        }

    }

    //*******************************************
    //When the user fails time trial
    //Remove a coin and display a message or
    //give a message if there are no coins to lose
    //*******************************************
    public void timeTrialFail() {

        ghostTimeTrialTime.setAlpha(0.0f);
        if (coinsCollectedData.getFeatures().size()==0){
            Toast.makeText(getApplicationContext(), "You Have No Coins To Lose!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "You Lost A Coin!", Toast.LENGTH_SHORT).show();
            coinsCollectedData.getFeatures().remove(coinsCollectedData.getFeatures().size()-1);
        }

    }

    //*******************************************
    //If the user has collected all the coins
    //and is in time trial mode
    //display a message congratulating them
    //For now, there is a reward, however, the
    //amount needs to be optimised for fun play
    //
    //It should be noted that while I wanted the
    //player to be able to do time trial mode with
    //any amount of coins left to collect, the reward should
    //really be proportionate to the coins collected
    //while in time trial mode
    //*******************************************
    public void timeTrialWin(){

        ghostTimeTrialTime.setAlpha(0.0f);
        Toast.makeText(getApplicationContext(), "You Won Time Trial Mode!", Toast.LENGTH_SHORT).show();
        user.bankGold = user.bankGold + 25;
        user.updateUser();
    }

    //*******************************************
    //If the user has collected all the coins and
    //is in ghost mode then check whether they beat
    //their previous time and reward appropriately
    //
    //As with time trial mode the reward amount should
    //be optimised for fun play
    //*******************************************
    public void ghostWin(){

        ghostTimeTrialTime.setAlpha(0.0f);
        if (timeCount<user.ghostTime){
            Toast.makeText(getApplicationContext(), "You Beat Your Previous Time!", Toast.LENGTH_SHORT).show();
            user.ghostTime = timeCount;
            user.bankGold = user.bankGold + 50;
            user.updateUser();
        }
        else{
            user.bankGold = user.bankGold + 20;
            user.updateUser();
            Toast.makeText(getApplicationContext(), "You Collected All The Coins But Did Not Beat Your Previous Time", Toast.LENGTH_SHORT).show();
        }
    }


    //*******************************************
    //Overriding some activity functions but, for now,
    //not using them aside from calling map functions
    //*******************************************
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

    //*******************************************
    //When the activity is finished with
    //*******************************************
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();   //Stop the map

        //*******************************************
        //Prevent location memory leaks
        //*******************************************
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }

        if (locationEngine != null) {
            stopLocationListener();
        }

        //*******************************************
        //Store the data in shared preferences or to Firebase
        //*******************************************
        //All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        //We need an Editor object to make preference changes
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        editor.putString("lastFireStoreDate", fireStoreDate);
        editor.putString("JSON", jsonData.toJson());
        editor.putString("currentUser", mAuth.getUid());
        editor.putString("coinsCollected", coinsCollectedData.toJson());
        editor.putString("coinsLastDownloadDate", coinsDownloadDate);

        //Apply the edits
        editor.apply();


        //Save data in FireStore
        user.updateUser();

    }

    //*******************************************
    //When the user presses back, confirm their choice,
    //cancel any timers if they do want to leave
    //*******************************************
    @Override
    public void onBackPressed() {

        //Open a dialog with the user to confirm them wanting to leave the map
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (i){
                    case DialogInterface.BUTTON_POSITIVE:   //If the user does want to back out of the map
                        user.ghostMode=false;
                        user.timeTrialMode=false;
                        user.loaded=false;

                        //Cancel timers if necessary
                        if (ghostTimer!=null){
                            Log.d(TAG, "Cancelling the ghost timer");
                            ghostTimer.cancel();
                        }
                        if (timeTrialTimer!=null){
                            Log.d(TAG, "Cancelling the time trial timer");
                            timeTrialTimer.cancel();
                        }

                        //We cannot do super.onBackPressed
                        //So we simply start the activity
                        Intent intent = new Intent(mapActivity.this, loginActivity.class);
                        startActivity(intent);
                        finish();   //This prevents the user from backing back into the map activity
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        //Dialog builder / dialog display
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are You Sure You Want To Leave The Game? You Will Lose Progress In The Bonus Game Modes. The Coins You Have Collected Are Safe Though.")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();


    }

    //*******************************************
    //Overriding some activity functions but, for now,
    //not using them aside from calling map functions
    //*******************************************
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
