package com.kiwi.moon.coinz;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class Coinz extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
