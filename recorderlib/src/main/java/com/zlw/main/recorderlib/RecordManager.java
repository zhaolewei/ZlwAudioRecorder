package com.zlw.main.recorderlib;


import android.annotation.SuppressLint;
import android.app.Application;

import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.RecordListener;
import com.zlw.main.recorderlib.recorder.RecordService;
import com.zlw.main.recorderlib.utils.Logger;

/**
 * @author zhaolewei on 2018/7/10.
 */
public class RecordManager {
    private static final String TAG = RecordManager.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private volatile static RecordManager instance;
    private Application context;

    private RecordManager() {
    }

    public static RecordManager getInstance() {
        if (instance == null) {
            synchronized (RecordManager.class) {
                if (instance == null) {
                    instance = new RecordManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param application Application
     * @param showLog     是否开启日志
     */
    public void init(Application application, boolean showLog) {
        this.context = application;
        Logger.IsDebug = showLog;
    }


    public void start() {
        if (context == null) {
            Logger.e(TAG, "未进行初始化");
            return;
        }
        Logger.i(TAG, "start...");
        RecordService.startRecording(context);
    }

    public void stop() {
        if (context == null) {
            return;
        }
        RecordService.stopRecording(context);
    }

    public void resume() {
        if (context == null) {
            return;
        }
        RecordService.resumeRecording(context);
    }

    public void pasue() {
        if (context == null) {
            return;
        }
        RecordService.pauseRecording(context);
    }

    public void setRecordListener(RecordListener listener) {
        RecordService.setRecordListener(listener);
    }

    public boolean changeFormat(RecordConfig.RecordFormat recordFormat) {
        return RecordService.changeFormat(recordFormat);
    }

    /**
     * 获取当前的录音状态
     *
     * @return 状态
     */
    public RecordHelper.RecordState getState() {
        return RecordService.getState();
    }

}
