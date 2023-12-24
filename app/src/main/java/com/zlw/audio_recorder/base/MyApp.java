package com.zlw.audio_recorder.base;

import android.app.Application;

/**
 * @author zlw on 2018/7/4.
 */
public class MyApp extends Application {

    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApp getInstance() {
        return instance;
    }
}
