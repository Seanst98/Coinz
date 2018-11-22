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
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    public JsonData(String json) {
        String dg = "";
        String tg = "";
        String app = "";

        Rates r = null;

        String shil = "";
        String dolr = "";
        String quid = "";
        String peny = "";

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

        type = fc.type();
        date_generated = dg;
        time_generated = tg;
        approximate_time_remaining = app;
        rates = r;
        features = coins;
    }


    public String toJson() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(this);
    }
}