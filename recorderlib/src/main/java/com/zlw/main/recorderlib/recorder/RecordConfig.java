package com.zlw.main.recorderlib.recorder;

import android.media.AudioFormat;

import java.io.Serializable;

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
    private int channel = AudioFormat.CHANNEL_IN_DEFAULT;

    /**
     * Audio data format
     */
    private int encoding = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 采样率
     */
    private int frequency = 44100;

    public RecordConfig() {
    }

    public RecordConfig(RecordFormat format) {
        this.format = format;
    }

    public RecordConfig(RecordFormat format, int channel, int encoding, int frequency) {
        this.format = format;
        this.channel = channel;
        this.encoding = encoding;
        this.frequency = frequency;
    }

    public RecordFormat getFormat() {
        return format;
    }

    public void setFormat(RecordFormat format) {
        this.format = format;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getEncoding() {
        return encoding;
    }

    public void setEncoding(int encoding) {
        this.encoding = encoding;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
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
