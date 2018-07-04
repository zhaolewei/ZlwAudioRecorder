package com.main.zlw.zlwaudiorecorder.base;

import android.app.Application;

/**
 * @author zlw on 2018/7/4.
 */
public class MyApp extends Application {

    private MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        this.instance = this;
    }
}
