package com.zlw.main.recorderlib.recorder;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.zlw.main.recorderlib.utils.FileUtils;
import com.zlw.main.recorderlib.utils.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author zhaolewei on 2018/7/10.
 */
public class RecordHelper {
    private static final String TAG = RecordHelper.class.getSimpleName();
    private volatile static RecordHelper instance;
    private volatile RecordState state = RecordState.IDLE;
    private static final int RECORD_AUDIO_BUFFER_TIMES = 1;

    private RecordListener recordListener;
    private RecordConfig currentConfig;
    private AudioRecordThread audioRecordThread;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private File recordFile = null;
    private File tmpFile = null;
    private List<File> files = new ArrayList<>();

    private RecordHelper() {
    }

    public static RecordHelper getInstance() {

        if (instance == null) {
            synchronized (RecordHelper.class) {
                if (instance == null) {
                    instance = new RecordHelper();
                }
            }
        }
        return instance;
    }

    public RecordState getState() {
        return state;
    }

    public void setRecordListener(RecordListener recordListener) {
        this.recordListener = recordListener;
    }

    public void start(String filePath, RecordConfig config) {
        this.currentConfig = config;
        if (state != RecordState.IDLE) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }
        recordFile = new File(filePath);
        String tempFilePath = getTempFilePath();
        Logger.i(TAG, "tmpPCM File: %s", tempFilePath);
        tmpFile = new File(tempFilePath);
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    public void stop() {
        if (state == RecordState.IDLE) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }

        if (state == RecordState.PAUSE) {
            makeFile();
            state = RecordState.IDLE;
        } else {
            state = RecordState.STOP;
        }
        notifyState();
    }

    public void pause() {
        if (state != RecordState.RECORDING) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }
        state = RecordState.PAUSE;
        notifyState();
    }

    public void resume() {
        if (state != RecordState.PAUSE) {
            Logger.e(TAG, "状态异常当前状态： %s", state.name());
            return;
        }
        String tempFilePath = getTempFilePath();
        Logger.i(TAG, "tmpPCM File: %s", tempFilePath);
        tmpFile = new File(tempFilePath);
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    private void notifyState() {
        if (recordListener == null) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                recordListener.onStateChange(state);
            }
        });
    }

    private void notifyError(final String error) {
        if (recordListener == null) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                recordListener.onError(error);
            }
        });
    }

    private class AudioRecordThread extends Thread {
        private AudioRecord audioRecord;
        private int bufferSize;

        AudioRecordThread() {
            bufferSize = AudioRecord.getMinBufferSize(currentConfig.getFrequency(),
                    currentConfig.getChannel(), currentConfig.getEncoding()) * RECORD_AUDIO_BUFFER_TIMES;
            Logger.d(TAG, "record buffer size = %s", bufferSize);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, currentConfig.getFrequency(),
                    currentConfig.getChannel(), currentConfig.getEncoding(), bufferSize);
        }

        @Override
        public void run() {
            super.run();
            state = RecordState.RECORDING;
            notifyState();
            Logger.d(TAG, "开始录制");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(tmpFile);
                audioRecord.startRecording();
                byte[] byteBuffer = new byte[bufferSize];

                while (state == RecordState.RECORDING) {
                    int end = audioRecord.read(byteBuffer, 0, byteBuffer.length);
                    fos.write(byteBuffer, 0, end);
                    fos.flush();
                }
                audioRecord.stop();
                files.add(tmpFile);
                if (state == RecordState.STOP) {
                    makeFile();
                } else {
                    Logger.i(TAG, "暂停！");
                }
            } catch (Exception e) {
                Logger.e(e, TAG, e.getMessage());
                notifyError("录音失败");
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (state != RecordState.PAUSE) {
                state = RecordState.IDLE;
                notifyState();
                Logger.d(TAG, "录音结束");
            }
        }
    }


    private void makeFile() {
        //合并文件
        boolean mergeSuccess = mergePcmFiles(recordFile, files);
        if (!mergeSuccess) {
            notifyError("合并失败");
            return;
        }
        //转换
        if (recordFile.getAbsolutePath().endsWith(RecordConfig.RecordFormat.WAV.getExtension())) {
            byte[] header = WavUtils.generateWavFileHeader(recordFile.length(), currentConfig.getFrequency(), currentConfig.getChannel());
            WavUtils.writeHeader(recordFile, header);
        }
        Logger.i(TAG, "录音完成！ path: %s ； 大小：%s", recordFile.getAbsoluteFile(), recordFile.length());
    }


    /**
     * 合并Pcm文件
     *
     * @param recordFile 输出文件
     * @param files      多个文件源
     * @return 是否成功
     */
    private boolean mergePcmFiles(File recordFile, List<File> files) {
        if (recordFile == null || files == null || files.size() <= 0) {
            return false;
        }

        FileOutputStream fos = null;
        BufferedOutputStream outputStream = null;
        byte[] buffer = new byte[1024];
        try {
            fos = new FileOutputStream(recordFile);
            outputStream = new BufferedOutputStream(fos);

            for (int i = 0; i < files.size(); i++) {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(files.get(i)));
                int readCount;
                while ((readCount = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readCount);
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Logger.e(e, TAG, e.getMessage());
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < files.size(); i++) {
            files.get(i).delete();
        }
        files.clear();
        return true;
    }

    /**
     * 根据当前的时间生成相应的文件名
     * 实例 record_20160101_13_15_12
     */
    private String getTempFilePath() {
        String fileDir = String.format(Locale.getDefault(), "%s/Record/", Environment.getExternalStorageDirectory().getAbsolutePath());
        if (!FileUtils.createOrExistsDir(fileDir)) {
            Logger.e(TAG, "文件夹创建失败：%s", fileDir);
        }
        String fileName = String.format(Locale.getDefault(), "record_tmp_%s", FileUtils.getNowString(new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.SIMPLIFIED_CHINESE)));
        return String.format(Locale.getDefault(), "%s%s.pcm", fileDir, fileName);
    }

    /**
     * 表示当前状态
     */
    public enum RecordState {
        /**
         * 空闲状态
         */
        IDLE,
        /**
         * 录音中
         */
        RECORDING,
        /**
         * 暂停中
         */
        PAUSE,
        /**
         * 正在停止
         */
        STOP
    }
}
