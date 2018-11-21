package com.kiwi.moon.coinz;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

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


    public String toJson() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(this);
    }
}