package com.zlw.main.recorderlib.recorder.mp3;

import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordService;
import com.zlw.main.recorderlib.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhaolewei on 2018/8/2.
 */
public class Mp3EncodeThread extends Thread {
    private static final String TAG = Mp3EncodeThread.class.getSimpleName();

    /**
     * mp3文件的码率 32kbit/s = 4kb/s
     */
    private static final int OUT_BITRATE = 32;

    private List<ChangeBuffer> cacheBufferList = Collections.synchronizedList(new LinkedList<ChangeBuffer>());
    private File file;
    private FileOutputStream os;
    private byte[] mp3Buffer;
    private EncordFinishListener encordFinishListener;

    /**
     * 是否已停止录音
     */
    private volatile boolean isOver = false;

    /**
     * 是否继续轮询数据队列
     */
    private volatile boolean start = true;

    public Mp3EncodeThread(File file, int bufferSize) {
        this.file = file;
        mp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];
        RecordConfig currentConfig = RecordService.getCurrentConfig();
        int sampleRate = currentConfig.getSampleRate();
        Mp3Encoder.init(sampleRate, currentConfig.getChannelCount(), sampleRate, OUT_BITRATE);
    }

    @Override
    public void run() {
        try {
            this.os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Logger.e(e, TAG, e.getMessage());
            return;
        }

        while (start) {
            ChangeBuffer next = next();
            Logger.v(TAG, "处理数据：%s", next == null ? "null" : next.getReadSize());
            lameData(next);
        }
    }

    public void addChangeBuffer(ChangeBuffer changeBuffer) {
        if (changeBuffer != null) {
            cacheBufferList.add(changeBuffer);
            synchronized (this) {
                notify();
            }
        }
    }

    public void stopSafe(EncordFinishListener encordFinishListener) {
        this.encordFinishListener = encordFinishListener;
        isOver = true;
        synchronized (this) {
            notify();
        }
    }

    private ChangeBuffer next() {
        for (; ; ) {
            if (cacheBufferList == null || cacheBufferList.size() == 0) {
                try {
                    if (isOver) {
                        finish();
                    }
                    synchronized (this) {
                        wait();
                    }
                } catch (Exception e) {
                    Logger.e(e, TAG, e.getMessage());
                }
            } else {
                return cacheBufferList.remove(0);
            }
        }
    }

    private void lameData(ChangeBuffer changeBuffer) {
        if (changeBuffer == null) {
            return;
        }
        short[] buffer = changeBuffer.getData();
        int readSize = changeBuffer.getReadSize();
        if (readSize > 0) {
            int encodedSize = Mp3Encoder.encode(buffer, buffer, readSize, mp3Buffer);
            if (encodedSize < 0) {
                Logger.e(TAG, "Lame encoded size: " + encodedSize);
            }
            try {
                os.write(mp3Buffer, 0, encodedSize);
            } catch (IOException e) {
                Logger.e(e, TAG, "Unable to write to file");
            }
        }
    }

    private void finish() {
        start = false;
        final int flushResult = Mp3Encoder.flush(mp3Buffer);
        if (flushResult > 0) {
            try {
                os.write(mp3Buffer, 0, flushResult);
                os.close();
            } catch (final IOException e) {
                Logger.e(TAG, e.getMessage());
            }
        }
        Logger.d(TAG, "转换结束 :%s", file.length());
        if (encordFinishListener != null) {
            encordFinishListener.onFinish();
        }
    }

    public static class ChangeBuffer {
        private short[] rawData;
        private int readSize;

        public ChangeBuffer(short[] rawData, int readSize) {
            this.rawData = rawData.clone();
            this.readSize = readSize;
        }

        short[] getData() {
            return rawData;
        }

        int getReadSize() {
            return readSize;
        }
    }

    public interface EncordFinishListener {
        /**
         * 格式转换完毕
         */
        void onFinish();
    }
}
