package com.kiwi.moon.coinz;

import com.mapbox.geojson.Geometry;

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