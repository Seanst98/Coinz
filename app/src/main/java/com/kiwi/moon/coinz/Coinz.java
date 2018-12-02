package com.kiwi.moon.coinz;

import android.app.Application;

//import com.squareup.leakcanary.LeakCanary;

public class Coinz extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //if (LeakCanary.isInAnalyzerProcess(this)) {
            //return;
        //}
        //LeakCanary.install(this);
    }
}


//This is where I initially had leaky canary implemented
//However, I had removed any memory leaks I could find
//So, leaky canary is no longer needed

