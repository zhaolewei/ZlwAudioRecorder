package com.zlw.main.recorderlib.recorder;

import android.media.AudioFormat;
import android.os.Environment;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author zhaolewei on 2018/7/11.
 */
public class RecordConfig implements Serializable {
    /**
     * 录音格式 默认WAV格式
     */
    private RecordFormat format = RecordFormat.WAV;
    /**
     * 通道数:默认单通道
     */
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 位宽
     */
    private int encodingConfig = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 采样率
     */
    private int sampleRate = 16000;

    /*
        * 录音文件存放路径，默认sdcard/Record
     */
    private String recordDir = String.format(Locale.getDefault(),
            "%s/Record/",
            Environment.getExternalStorageDirectory().getAbsolutePath());

    public RecordConfig() {
    }

    public RecordConfig(RecordFormat format) {
        this.format = format;
    }

    /**
     * @param format         录音文件的格式
     * @param channelConfig  声道配置
     *                       单声道：See {@link AudioFormat#CHANNEL_IN_MONO}
     *                       双声道：See {@link AudioFormat#CHANNEL_IN_STEREO}
     * @param encodingConfig 位宽配置
     *                       8Bit： See {@link AudioFormat#ENCODING_PCM_8BIT}
     *                       16Bit: See {@link AudioFormat#ENCODING_PCM_16BIT},
     * @param sampleRate     采样率 hz: 8000/16000/44100
     */
    public RecordConfig(RecordFormat format, int channelConfig, int encodingConfig, int sampleRate) {
        this.format = format;
        this.channelConfig = channelConfig;
        this.encodingConfig = encodingConfig;
        this.sampleRate = sampleRate;
    }


    public String getRecordDir() {
        return recordDir;
    }

    public void setRecordDir(String recordDir) {
        this.recordDir = recordDir;
    }

    /**
     * 获取当前录音的采样位宽 单位bit
     *
     * @return 采样位宽 0: error
     */
    public int getEncoding() {
        if (encodingConfig == AudioFormat.ENCODING_PCM_8BIT) {
            return 8;
        } else if (encodingConfig == AudioFormat.ENCODING_PCM_16BIT) {
            return 16;
        } else {
            return 0;
        }
    }

    /**
     * 当前的声道数
     *
     * @return 声道数： 0：error
     */
    public int getChannelCount() {
        if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
            return 1;
        } else if (channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
            return 2;
        } else {
            return 0;
        }
    }

    //get&set

    public RecordFormat getFormat() {
        return format;
    }

    public void setFormat(RecordFormat format) {
        this.format = format;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public int getEncodingConfig() {
        return encodingConfig;
    }

    public void setEncodingConfig(int encodingConfig) {
        this.encodingConfig = encodingConfig;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }


    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "录制格式： %s,采样率：%sHz,位宽：%s bit,声道数：%s", format, sampleRate, getEncoding(), getChannelCount());
    }

    public enum RecordFormat {
        /**
         * mp3格式
         */
        MP3(".mp3"),
        /**
         * wav格式
         */
        WAV(".wav"),
        /**
         * pcm格式
         */
        PCM(".pcm");

        private String extension;

        public String getExtension() {
            return extension;
        }

        RecordFormat(String extension) {
            this.extension = extension;
        }
    }
}
