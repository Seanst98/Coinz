package com.kiwi.moon.coinz;

import com.google.gson.annotations.SerializedName;

//*******************************************
//Properties class that is a utility class
//to hold data about a coin
//*******************************************
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