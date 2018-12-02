package com.kiwi.moon.coinz;

//*******************************************
//Rates class that holds the rates of the day
//*******************************************
public class Rates {

    public String SHIL;
    public String DOLR;
    public String QUID;
    public String PENY;

    public Rates(String s, String d, String q, String p) {
        SHIL = s;
        DOLR = d;
        QUID = q;
        PENY = p;
    }
}