package com.kiwi.moon.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.BoundingBox;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class mapActivity extends AppCompatActivity implements
        OnMapReadyCallback, LocationEngineListener, PermissionsListener, DownloadCompleteRunner{

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
    private String coinsCollected = "";
    private final String preferencesFile = "MyPrefsFile";   //For storing preferences
    Date now = new Date();
    String currentDate = new SimpleDateFormat("yyyy/MM/dd").format(now);

    //Access a cloud firestore instance from the bank activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth =  FirebaseAuth.getInstance();

    User user = User.getinstance();

    private JsonData jsonData;
    private JsonData coinsCollectedData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.d(TAG, "BEGIN");

        //*******************************************
        //Toolbar for navigation drawer
        //*******************************************
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);


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

                        switch (id) {

                            case R.id.drawer_bank:
                                Toast.makeText(getApplicationContext(), "bank selected", Toast.LENGTH_SHORT).show();


                                if (jsonData.rates.DOLR.equals("")){
                                    Toast.makeText(getApplicationContext(), "Please wait until the map data has finished downloading", Toast.LENGTH_LONG).show();

                                }
                                else {
                                    Intent intent = new Intent(mapActivity.this, BankActivity.class);
                                    intent.putExtra("coinsCollected", coinsCollectedData.toJson());
                                    Log.d(TAG, "MAP TO BANK STORED COINS ARE: " + coinsCollectedData.toJson());
                                    startActivity(intent);
                                }

                                break;

                            case R.id.drawer_optional:
                                Toast.makeText(getApplicationContext(), "optional selected", Toast.LENGTH_SHORT).show();
                                Intent intent1 = new Intent(mapActivity.this, OptionalFeaturesActivity.class);
                                startActivity(intent1);
                                break;

                            case R.id.drawer_personal:
                                Toast.makeText(getApplicationContext(), "personal selected", Toast.LENGTH_SHORT).show();
                                Intent intent2 = new Intent(mapActivity.this, PersonalActivity.class);
                                startActivity(intent2);
                                break;

                            case R.id.drawer_statistics:
                                Toast.makeText(getApplicationContext(), "statistics selected", Toast.LENGTH_SHORT).show();
                                Intent intent3 = new Intent(mapActivity.this, StatisticsActivity.class);
                                startActivity(intent3);
                                break;
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

        mapView = (MapView) findViewById(R.id.mapboxMapView);


        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //*******************************************
        //Set up any text boxes on the screen
        //*******************************************
        totalCoins = (TextView) findViewById(R.id.totalCoins);
        ghostTimeTrialTime = (TextView) findViewById(R.id.ghostTimeTrialTime);

        totalCoins.setText("Coins Collected: " + 0);
        ghostTimeTrialTime.setText("Time");
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
        List<Coin> coins2 = new ArrayList<>();

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
            coins2.add(coin);

            Marker marker = map.addMarker(new MarkerOptions().title(value).snippet(currency).icon(icon).position(latLng));


        }

        jsonData = new JsonData(fc.type(), dg, tg, app, rates, coins);


        if (coinsCollectedData == null || !currentDate.equals(coinsDownloadDate)){

            Log.d(TAG, "CLEARING COINS SINCE NULL OR NEW DAY");
            Log.d(TAG, "current: " + currentDate.toString());
            Log.d(TAG, "last coins: " + coinsDownloadDate.toString());

            coinsCollectedData = new JsonData(fc.type(), dg, tg, app, rates, coins2);

            while (!coinsCollectedData.features.isEmpty()){

                coinsCollectedData.features.remove(0);
            }

            coinsDownloadDate = currentDate;

        }
    }

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
                downLoadFileTask.delegate = this;
                Log.d(TAG, "Downloading file from " + "http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson");
                downLoadFileTask.execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + currentDate + "/coinzmap.geojson");
                downloadDate = currentDate;
                Log.d(TAG, "[OnMapReady] Downloading map since it is a new day");
            }
            else if (!currentUser.equals(mAuth.getUid())){
                currentUser = mAuth.getUid();
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

                Log.d(TAG, "WITHIN 25m");
                removeMarker(markers.get(i), i);

            }
        }
    }

    public void removeMarker(Marker marker, int i) {

        Log.d(TAG, "START OF FUNC");


        user.dayCoins++;
        user.totalCoins++;
        switch (marker.getSnippet()) {

            case "SHIL":
                user.shil = user.shil + Double.parseDouble(marker.getTitle());
                break;

            case "DOLR":
                user.dolr = user.dolr + Double.parseDouble(marker.getTitle());
                break;

            case "PENY":
                user.peny = user.peny + Double.parseDouble(marker.getTitle());
                break;

            case "QUID":
                user.quid = user.quid + Double.parseDouble(marker.getTitle());
                break;
        }

        totalCoins.setText("Coins Collected: " + user.dayCoins);

        Log.d(TAG, "Removing Marker At: " + jsonData.features.get(i).geometry);
        coinsCollectedData.features.add(jsonData.features.get(i));
        Log.d(TAG, "COINSCOLLECTED ADD: " + coinsCollectedData.toJson());
        jsonData.features.removeAll(coinsCollectedData.features);
        Log.d(TAG, "COINSCOLLECTED REMOVE " + coinsCollectedData.toJson());
        map.removeMarker(marker);
        marker.remove();

        user.updateUser();

        Toast.makeText(getApplicationContext(), "Coin collected!",
                Toast.LENGTH_SHORT).show();


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

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(TAG, "[onLocationChanged] location is null");
        } else {
            Log.d(TAG, "[onLocationChanged] location is not null");


            if (originLocation!=null) {
                LatLng latLng = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
                LatLng latLng1 = new LatLng(location.getLatitude(), location.getLongitude());

                Double dist = latLng.distanceTo(latLng1);

                user.dayWalked = user.dayWalked + dist;
                user.totalWalked = user.totalWalked + dist;

                user.updateUser();
            }

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

        Log.d(TAG, "[OnStart] is called");

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
        fireStoreDate = settings.getString("lastFireStoreDate", "");
        currentUser = settings.getString("currentUser", "");
        coinsCollected = settings.getString("coinsCollected", "");
        coinsDownloadDate = settings.getString("coinsLastDownloadDate", "");

        Log.d(TAG, "THE STORED COINS ARE: " + coinsCollected);


        if (coinsCollected.equals("[]")){
            Log.d(TAG, "coins collected is []");
        }
        else if (coinsCollected == null){

            Log.d(TAG, "coins collected is null");
        }
        else if (coinsCollected.equals("")){
            Log.d(TAG, "coins collected is '' ");
        }
        else {

            coinsCollectedData = new JsonData(coinsCollected);

        }


        //OK Here's an explanation of why I do this
        //So I needed the User class to be a singleton and for that I need a private constructor
        //I also need a listener for when the user data is loaded so that I can begin displaying
        //the map etc...
        //So I have this listener here to only slow down the map from displaying
        //This way the user's data is loaded before the map displays and prevents any errors

        if (!user.loaded){
            User usr = new User(1);
            usr.setCustomObjectListener(new User.myCustomObjectListener() {
                @Override
                public void onDataLoaded() {

                    Log.d(TAG, "MAPVIEW START CALLED");

                    if (!currentDate.equals(fireStoreDate)){
                        user.dayCoins = 0;
                        user.dayWalked = 0;
                        user.coinsDepositedDay = 0;

                        user.updateUser();
                    }
                    fireStoreDate = currentDate;
                    totalCoins.setText("Coins Collected: " + user.dayCoins);

                    mapView.onStart();

                }
            });
        }
        else {
            Log.d(TAG, "MAPVIEW START CALLED");

            if (!currentDate.equals(fireStoreDate)) {
                user.dayCoins = 0;
                user.dayWalked = 0;
                user.coinsDepositedDay = 0;

                user.updateUser();
            }
            fireStoreDate = currentDate;
            totalCoins.setText("Coins Collected: " + user.dayCoins);

            mapView.onStart();
        }

        ghostTimeTrialTime.setAlpha(0.0f);


        if (user.ghostMode){
            ghostTimeTrialTime.setAlpha(1.0f);
            if (ghostTimer == null){
                ghostTimer = new Timer();
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
                }, 1000, 1000);
            }
        }

        if (user.timeTrialMode) {
            createTimeTrialTimer();
        }

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

    public void createTimeTrialTimer(){

        ghostTimeTrialTime.setAlpha(1.0f);
        if (timeTrialTimer == null){
            timeTrialTimer = new CountDownTimer(120000, 1000) {
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

    public void timeTrialFail() {

        ghostTimeTrialTime.setAlpha(0.0f);
        if (coinsCollectedData.features.size()==0){
            Toast.makeText(getApplicationContext(), "You Have No Coins To Lose!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "You Lost A Coin!", Toast.LENGTH_SHORT).show();
            coinsCollectedData.features.remove(coinsCollectedData.features.size()-1);
        }

    }

    public void timeTrialWin(){

        ghostTimeTrialTime.setAlpha(0.0f);
        Toast.makeText(getApplicationContext(), "You Won Time Trial Mode!", Toast.LENGTH_SHORT).show();

    }

    public void ghostWin(){

        ghostTimeTrialTime.setAlpha(0.0f);
        if (timeCount<user.ghostTime){
            Toast.makeText(getApplicationContext(), "You Beat Your Previous Time!", Toast.LENGTH_SHORT).show();
            user.ghostTime = timeCount;
        }
        else{
            Toast.makeText(getApplicationContext(), "You Collected All The Coins But Did Not Beat Your Previous Time", Toast.LENGTH_SHORT).show();
        }
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

        user.updateUser();
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
