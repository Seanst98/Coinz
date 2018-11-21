package com.kiwi.moon.coinz;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BankActivity extends AppCompatActivity {

    //Access a cloud firestore instance from the bank activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String TAG = "mapActivity";
    private Rates rates;

    String dolr;
    String peny;
    String shil;
    String quid;

    JsonData coinsCollectedData;

    User user;

    private EditText shilInput;
    private EditText dolrInput;
    private EditText quidInput;
    private EditText penyInput;

    //private EditText passwordInput;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        shilInput = (EditText) findViewById(R.id.depositSHIL);
        dolrInput = (EditText) findViewById(R.id.depositDOLR);
        quidInput = (EditText) findViewById(R.id.depositQUID);
        penyInput = (EditText) findViewById(R.id.depositPENY);

        //passwordInput = (EditText) findViewById(R.id.enterPassword);


        user = getUser();

        //If deposit card is pressed
        final CardView cardDeposit = findViewById(R.id.cardDeposit);
        cardDeposit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                //Call function to deposit money
                depositVal();
            }
        });

    }

    public void depositVal() {

        int totalCoins = Integer.parseInt(shilInput.toString()) + Integer.parseInt(dolrInput.toString()) + Integer.parseInt(quidInput.toString()) + Integer.parseInt(penyInput.toString());

        if (totalCoins < 1){
            Toast.makeText(getApplicationContext(), "Please Enter A Number Greater Than 0",
                    Toast.LENGTH_LONG).show();
        }
        else if (totalCoins > 25){
            Toast.makeText(getApplicationContext(), "Please Enter A Number Less Than 26",
                    Toast.LENGTH_LONG).show();
        }
        else {
            if (user == null){
                Toast.makeText(getApplicationContext(), "User is Null",
                        Toast.LENGTH_LONG).show();

            }
            else {
                deposit();
            }
        }
    }

    public void deposit() {

        double gold = user.shil  / Double.parseDouble(shil);


    }

    public void updateFireBaseUser(){
        //Save data in FireStore
        Map<String, Object> userStore = new HashMap<>();
        userStore.put("Day Coins" , user.dayCoins);
        userStore.put("Day Walked", user.dayWalked);
        userStore.put("Total Coins", user.totalCoins);
        userStore.put("Total Walked", user.totalWalked);
        userStore.put("Bank GOLD", user.bankGold);
        userStore.put("SHIL Collected", user.shil);
        userStore.put("QUID Collected", user.quid);
        userStore.put("PENY Collected", user.peny);
        userStore.put("DOLR Collected", user.dolr);

        db.collection("users").document(mAuth.getUid())
                .set(userStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document Snapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing document", e);
                    }
                });


    }

    public User getUser(){

        User usr = new User();
        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        usr.dayCoins = document.getLong("Day Coins").intValue();
                        Log.d(TAG, "Day Coins: " + document.getLong("Day Coins"));
                        usr.dayWalked = document.getLong("Day Walked");
                        usr.bankGold = document.getLong("Bank GOLD");
                        usr.totalCoins = document.getLong("Total Coins").intValue();
                        Log.d(TAG, "Total Coins: " + document.getLong("Total Coins"));
                        usr.totalWalked = document.getLong("Total Walked");
                        usr.dolr = document.getLong("DOLR Collected");
                        usr.shil = document.getLong("SHIL Collected");
                        usr.quid = document.getLong("QUID Collected");
                        usr.peny = document.getLong("PENY Collected");

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        return usr;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        String json = "";

        if(extras != null) {

            json = extras.getString("coinsCollected");
        }

        String dg = "";
        String tg = "";
        String app = "";

        Rates rates = null;

        String shil = "";
        String dolr = "";
        String quid = "";
        String peny = "";

        try {

            JSONObject collection = new JSONObject(json);
            dg = collection.getString("date-generated");
            tg = collection.getString("time-generated");
            app = collection.getString("approximate-time-remaining");

            JSONObject ratesl = collection.getJSONObject("rates");
            shil = ratesl.getString("SHIL");
            dolr = ratesl.getString("DOLR");
            quid = ratesl.getString("QUID");
            peny = ratesl.getString("PENY");

            //rates = Rates(shil, dolr, quid, peny);


        } catch (JSONException e) {
            Log.d(TAG, "JSONException " + e.toString());
        }

        FeatureCollection fc = FeatureCollection.fromJson(json);
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

            Properties props = new Properties(id, value, currency, marker_symbol, marker_color);
            Coin coin = new Coin("Feature", g, props);
            coins.add(coin);

        }

        coinsCollectedData = new JsonData(fc.type(), dg, tg, app, rates, coins);

    }
}
