package com.kiwi.moon.coinz;

import android.content.Context;
import android.content.SharedPreferences;
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
    private EditText coinsInput;

    //private EditText passwordInput;

    private final String preferencesFile = "MyPrefsFile";   //For storing preferences



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        shilInput = (EditText) findViewById(R.id.depositSHIL);
        dolrInput = (EditText) findViewById(R.id.depositDOLR);
        quidInput = (EditText) findViewById(R.id.depositQUID);
        penyInput = (EditText) findViewById(R.id.depositPENY);
        coinsInput = (EditText) findViewById(R.id.depositCoins);

        //If deposit card is pressed
        final CardView cardDeposit = findViewById(R.id.cardDeposit);
        cardDeposit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Log.d(TAG, "CLICKED");
                //Call function to deposit money
                depositVal();
            }
        });

    }

    public void depositVal() {

        /*int totalCoins = Integer.parseInt(shilInput.toString()) + Integer.parseInt(dolrInput.toString()) + Integer.parseInt(quidInput.toString()) + Integer.parseInt(penyInput.toString());

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
        }*/

        Log.d(TAG, coinsInput.toString());

        if (coinsInput.getText() == null){
            Toast.makeText(getApplicationContext(), "Please Enter A Value" , Toast.LENGTH_SHORT).show();
        }
        else if (coinsInput.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter A Value", Toast.LENGTH_SHORT).show();
        }
        else if (Integer.parseInt(coinsInput.getText().toString()) < 1){
            Toast.makeText(getApplicationContext(), "Please Enter A Number Greater Than 0",
                    Toast.LENGTH_LONG).show();
        }
        else if (Integer.parseInt(coinsInput.getText().toString()) > 25){
            Toast.makeText(getApplicationContext(), "Please Enter A Number Less Than 26",
                    Toast.LENGTH_LONG).show();
        }
        else {
            if (user == null){
                Toast.makeText(getApplicationContext(), "User is Null",
                        Toast.LENGTH_LONG).show();

            }
            else {
                Log.d(TAG, "DEPOSITING");
                deposit();
            }
        }
    }

    public void deposit() {

        double gold = 0;

        if (coinsCollectedData.features.size() == 0){
            Log.d(TAG, "NO COINS COLLECTED TO DEPOSIT");
            Toast.makeText(getApplicationContext(), "You Don't Have Any Coins To Deposit! Go Collect Some", Toast.LENGTH_SHORT).show();
        }
        else {
            for (int i = 0; i < Integer.parseInt(coinsInput.getText().toString()); i++){

                Log.d(TAG, "Value: " + coinsCollectedData.features.get(i).properties.value);
                Log.d(TAG, "Rate: " + coinsCollectedData.rates.DOLR);

                switch (coinsCollectedData.features.get(i).properties.currency) {

                    case "DOLR":
                        gold = gold + exchangeConversion(Double.parseDouble(coinsCollectedData.features.get(i).properties.value), Double.parseDouble(coinsCollectedData.rates.DOLR));
                        break;

                    case "SHIL":
                        gold = gold + exchangeConversion(Double.parseDouble(coinsCollectedData.features.get(i).properties.value), Double.parseDouble(coinsCollectedData.rates.SHIL));
                        break;

                    case "PENY":
                        gold = gold + exchangeConversion(Double.parseDouble(coinsCollectedData.features.get(i).properties.value), Double.parseDouble(coinsCollectedData.rates.PENY));
                        break;

                    case "QUID":
                        gold = gold + exchangeConversion(Double.parseDouble(coinsCollectedData.features.get(i).properties.value), Double.parseDouble(coinsCollectedData.rates.QUID));
                        break;
                }

                coinsCollectedData.features.remove(i);

                Log.d(TAG, "Coins Collected Data is now: " + coinsCollectedData.toJson());


            }
        }

        Log.d(TAG, "Gold Value of coins: " + gold);
        user.bankGold = user.bankGold + gold;
        updateFireBaseUser();


    }

    public double exchangeConversion(double amount, double rate){

        return amount/rate;
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
        userStore.put("DOLR Coins", user.dolrCoins);
        userStore.put("SHIL Coins", user.shilCoins);
        userStore.put("PENY Coins", user.penyCoins);
        userStore.put("QUID Coins", user.quidCoins);

        Log.d(TAG, "BANK GOLD STORING AS: " + user.bankGold);

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
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        usr.dayCoins = document.getLong("Day Coins").intValue();
                        usr.dayWalked = (Double) document.getData().get("Day Walked");
                        usr.bankGold = (Double) document.getData().get("Bank GOLD");
                        usr.totalCoins = document.getLong("Total Coins").intValue();
                        usr.totalWalked = (Double) document.getData().get("Total Walked");
                        usr.dolr = (Double) document.getData().get("DOLR Collected");
                        usr.shil = (Double) document.getData().get("SHIL Collected");
                        usr.quid = (Double) document.getData().get("QUID Collected");
                        usr.peny = (Double) document.getData().get("PENY Collected");
                        user.dolrCoins = document.getLong("DOLR Coins").intValue();
                        user.shilCoins = document.getLong("SHIL Coins").intValue();
                        user.quidCoins = document.getLong("QUID Coins").intValue();
                        user.penyCoins = document.getLong("PENY Coins").intValue();

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

            rates = new Rates(shil, dolr, quid, peny);


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

        user = getUser();

    }

    @Override
    protected void onPause() {
        super.onPause();

        //We do this in onPause as we want this effect to happen before reaching map activity to read it

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        //We need an Editor object to make preference changes
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("coinsCollected", coinsCollectedData.toJson());

        Log.d(TAG, "STORING COINS AS: " + coinsCollectedData.toJson());

        //Apply the edits
        editor.apply();

        //Save data in FireStore
        updateFireBaseUser();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}

