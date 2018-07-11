//package com.main.zlw.zlwaudiorecorder.recorder;
//
//
//import android.os.Environment;
//
//import com.blankj.utilcode.util.FileUtils;
//import com.blankj.utilcode.util.TimeUtils;
//import com.main.zlw.zlwaudiorecorder.utils.Logger;
//
//import java.text.SimpleDateFormat;
//import java.util.Locale;
//
///**
// * @author zhaolewei on 2018/7/10.
// */
//public class RecordManager {
//    private static final String TAG = RecordManager.class.getSimpleName();
//    private volatile static RecordManager instance;
//
//    private RecordManager() {
//    }
//
//    public static RecordManager getInstance() {
//        if (instance == null) {
//            synchronized (RecordManager.class) {
//                if (instance == null) {
//                    instance = new RecordManager();
//                }
//            }
//        }
//        return instance;
//    }
//
//
//    public void start() {
//        RecordService.startRecording(getFilePath());
//    }
//
//    public void stop() {
//        RecordService.stopRecording();
//    }
//
//    public void resume() {
//        RecordService.resumeRecording();
//    }
//
//    public void pasue() {
//        RecordService.pauseRecording();
//    }
//
//    /**
//     * 根据当前的时间生成相应的文件名
//     * 实例 record_20160101_13_15_12
//     */
//    private String getFilePath() {
//        String fileDir = String.format(Locale.getDefault(), "%s/Record/", Environment.getExternalStorageDirectory().getAbsolutePath());
//        if (!FileUtils.createOrExistsDir(fileDir)) {
//            Logger.w(TAG, "文件夹创建失败：%s", fileDir);
//            return null;
//        }
//        String fileName = String.format(Locale.getDefault(), "record_%s", TimeUtils.getNowString(new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.SIMPLIFIED_CHINESE)));
//        return String.format(Locale.getDefault(), "%s%s.wav", fileDir, fileName);
//    }
//
//
//    public enum RecordFormat {
//        /**
//         * mp3格式
//         */
//        MP3(".mp3"),
//        /**
//         * wav格式
//         */
//        WAV(".wav"),
//        /**
//         * pcm格式
//         */
//        PCM(".pcm");
//
//        private String extension;
//
//        public String getExtension() {
//            return extension;
//        }
//
//        RecordFormat(String extension) {
//            this.extension = extension;
//        }
//    }
//
//
//}
