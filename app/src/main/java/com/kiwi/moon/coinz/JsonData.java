package com.kiwi.moon.coinz;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//*******************************************
//JsonData that stores important information
//about the json
//*******************************************
public class JsonData {

    private String type;

    public String getType() {
        return type;
    }

    @SerializedName("date-generated")
    private String date_generated;

    public String getDate_generated() {
        return date_generated;
    }

    @SerializedName("time-generated")
    private String time_generated;

    public String getTime_generated() {
        return time_generated;
    }

    @SerializedName("approximate-time-remaining")
    private String approximate_time_remaining;

    public String getApproximate_time_remaining() {
        return approximate_time_remaining;
    }

    private Rates rates;

    public Rates getRates() {
        return rates;
    }

    private List<Coin> features;

    public List<Coin> getFeatures() {
        return features;
    }


    //*******************************************
    //Constructor that takes in the JsonData variables
    //*******************************************
    public JsonData(String t, String d, String tg, String app, Rates r, List<Coin> f) {
        type = t;
        date_generated = d;
        time_generated = tg;
        approximate_time_remaining = app;
        rates = r;
        features = f;
    }

    //*******************************************
    //Constructor that takes in the json String
    //*******************************************
    public JsonData(String json) {
        String dg = "";
        String tg = "";
        String app = "";

        Rates r = null;

        String shil;
        String dolr;
        String quid;
        String peny;

        String TAG = "mapActivity";

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

            r = new Rates(shil, dolr, quid, peny);


        } catch (JSONException e) {
            Log.d(TAG, "JSONException " + e.toString());
        }

        FeatureCollection fc = FeatureCollection.fromJson(json);
        List<Feature> fs = fc.features();

        List<Coin> coins = new ArrayList<>();

        if (fs != null){
            for (int i = 0; i < fs.size(); i++) {
                Geometry g = fs.get(i).geometry();

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

                Properties props = new Properties(id, value, currency, marker_symbol, marker_color);
                Coin coin = new Coin("Feature", g, props);
                coins.add(coin);

            }
        }
        else {
            Log.d(TAG, "fs is null");
        }

        type = fc.type();
        date_generated = dg;
        time_generated = tg;
        approximate_time_remaining = app;
        rates = r;
        features = coins;
    }


    //*******************************************
    //Function to build this class into a json string
    //*******************************************
    public String toJson() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(this);
    }
}