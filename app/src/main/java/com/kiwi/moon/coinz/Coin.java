package com.kiwi.moon.coinz;

import com.mapbox.geojson.Geometry;

//*******************************************
//Class Coin
//*******************************************
public class Coin {

    public String type;
    public Properties properties;   //Value, currency etc...
    public Geometry geometry;   //Location etc...

    public Coin(String t, Geometry g, Properties p) {   //Constructor
        type = t;
        properties = p;
        geometry = g;
    }

}