package com.main.zlw.zlwaudiorecorder.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.main.zlw.zlwaudiorecorder.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author zhaolewei on 2018/7/10.
 */
public class RecordHelper {
    private static final String TAG = RecordHelper.class.getSimpleName();
    private volatile static RecordHelper instance;
    private volatile RecordState state = RecordState.IDLE;


    /**
     * 录音机参数
     */
    private static final int CONFIG_CHANNEL = AudioFormat.CHANNEL_IN_DEFAULT;
    private static final int CONFIG_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CONFIG_FREQUENCY = 44100;
    private static final int RECORD_AUDIO_BUFFER_TIMES = 1;

    private AudioRecordThread audioRecordThread;
    private File recordFile = null;

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

    public void start(String filePath) {
        if (state != RecordState.IDLE) {
            return;
        }
        recordFile = new File(filePath);
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    public void stop() {
        if (state == RecordState.IDLE) {
            return;
        }

        state = RecordState.STOP;
    }

    private class AudioRecordThread extends Thread {
        private AudioRecord audioRecord;
        private int bufferSize;

        AudioRecordThread() {
            bufferSize = AudioRecord.getMinBufferSize(CONFIG_FREQUENCY,
                    CONFIG_CHANNEL, CONFIG_ENCODING) * RECORD_AUDIO_BUFFER_TIMES;
            Logger.d(TAG, "record buffer size = %s", bufferSize);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, CONFIG_FREQUENCY,
                    CONFIG_CHANNEL, CONFIG_ENCODING, bufferSize);
        }

        @Override
        public void run() {
            super.run();
            state = RecordState.RECORDING;
            Logger.d(TAG, "开始录制");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(recordFile);
                audioRecord.startRecording();
                byte[] byteBuffer = new byte[bufferSize];

                while (state == RecordState.RECORDING) {
                    int end = audioRecord.read(byteBuffer, 0, byteBuffer.length);
                    fos.write(byteBuffer, 0, end);
                    fos.flush();
                }
                audioRecord.stop();
                Logger.i(TAG, "录音完成！ path: %s ； 大小：%s", recordFile.getAbsoluteFile(), recordFile.length());
            } catch (Exception e) {
                Logger.e(e, TAG, e.getMessage());
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            state = RecordState.IDLE;
            Logger.d(TAG, "录音结束");
        }
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
