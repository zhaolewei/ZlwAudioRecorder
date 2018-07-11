package com.zlw.main.recorderlib.recorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import com.zlw.main.recorderlib.utils.FileUtils;
import com.zlw.main.recorderlib.utils.Logger;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 录音服务
 *
 * @author zhaolewei
 */
public class RecordService extends Service {
    private static final String TAG = RecordService.class.getSimpleName();

    /**
     * 录音配置
     */
    private static RecordConfig currentConfig = new RecordConfig();

    private final static String ACTION_NAME = "action_type";

    private final static int ACTION_INVALID = 0;

    private final static int ACTION_START_RECORD = 1;

    private final static int ACTION_STOP_RECORD = 2;

    private final static int ACTION_RESUME_START = 3;

    private final static int ACTION_PAUSE_START = 4;

    private final static String PARAM_PATH = "path";


    public RecordService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey(ACTION_NAME)) {
            switch (bundle.getInt(ACTION_NAME, ACTION_INVALID)) {
                case ACTION_START_RECORD:
                    doStartRecording(bundle.getString(PARAM_PATH));
                    break;
                case ACTION_STOP_RECORD:
                    doStopRecording();
                    break;
                case ACTION_RESUME_START:
                    doResumeRecording();
                    break;
                case ACTION_PAUSE_START:
                    doPauseRecording();
                    break;
                default:
                    break;
            }
            return START_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }


    public static void startRecording(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.putExtra(ACTION_NAME, ACTION_START_RECORD);
        intent.putExtra(PARAM_PATH, getFilePath());
        context.startService(intent);
    }

    public static void stopRecording(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.putExtra(ACTION_NAME, ACTION_STOP_RECORD);
        context.startService(intent);
    }

    public static void resumeRecording(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.putExtra(ACTION_NAME, ACTION_RESUME_START);
        context.startService(intent);
    }

    public static void pauseRecording(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        intent.putExtra(ACTION_NAME, ACTION_PAUSE_START);
        context.startService(intent);
    }

    /**
     * 改变录音格式
     */
    public static boolean changeFormat(RecordConfig.RecordFormat recordFormat) {
        if (getState() == RecordHelper.RecordState.IDLE) {
            currentConfig.setFormat(recordFormat);
            return true;
        }
        return false;
    }

    /**
     * 获取当前的录音状态
     */
    public static RecordHelper.RecordState getState() {
        return RecordHelper.getInstance().getState();
    }

    public static void setRecordListener(RecordListener recordListener) {
        RecordHelper.getInstance().setRecordListener(recordListener);
    }


    private void doStartRecording(String path) {
        Logger.v(TAG, "doStartRecording path: %s", path);
        RecordHelper.getInstance().start(path, currentConfig);
    }

    private void doResumeRecording() {
        Logger.v(TAG, "doResumeRecording");
        RecordHelper.getInstance().resume();
    }

    private void doPauseRecording() {
        Logger.v(TAG, "doResumeRecording");
        RecordHelper.getInstance().pause();
    }

    private void doStopRecording() {
        Logger.v(TAG, "doStopRecording");
        RecordHelper.getInstance().stop();
        stopSelf();
    }

    /**
     * 根据当前的时间生成相应的文件名
     * 实例 record_20160101_13_15_12
     */
    private static String getFilePath() {
        String fileDir = String.format(Locale.getDefault(), "%s/Record/", Environment.getExternalStorageDirectory().getAbsolutePath());
        if (!FileUtils.createOrExistsDir(fileDir)) {
            Logger.w(TAG, "文件夹创建失败：%s", fileDir);
            return null;
        }
        String fileName = String.format(Locale.getDefault(), "record_%s", FileUtils.getNowString(new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.SIMPLIFIED_CHINESE)));
        return String.format(Locale.getDefault(), "%s%s.%s", fileDir, fileName, currentConfig.getFormat().getExtension());
    }


}
