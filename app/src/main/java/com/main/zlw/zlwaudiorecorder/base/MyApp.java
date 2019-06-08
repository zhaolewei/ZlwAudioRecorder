package com.main.zlw.zlwaudiorecorder.base;

import android.app.Application;

import com.zlw.loggerlib.Logger;
import com.zlw.main.recorderlib.recorder.wav.WavUtils;
import com.zlw.main.recorderlib.utils.ByteUtils;

/**
 * @author zlw on 2018/7/4.
 */
public class MyApp extends Application {

    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Logger.w("zlwTest", "TEST-----------------");
        byte[] header1 = WavUtils.generateWavFileHeader(1024, 16000, 1, 16);
        byte[] header2 = WavUtils.generateWavFileHeader(1024, 16000, 1, 16);

        Logger.d("zlwTest", "Wav1: %s", WavUtils.headerToString(header1));
        Logger.d("zlwTest", "Wav2: %s", WavUtils.headerToString(header2));

        Logger.w("zlwTest", "TEST-2----------------");

        Logger.d("zlwTest", "Wav1: %s", ByteUtils.toString(header1));
        Logger.d("zlwTest", "Wav2: %s", ByteUtils.toString(header2));
    }

    public static MyApp getInstance() {
        return instance;
    }
}
