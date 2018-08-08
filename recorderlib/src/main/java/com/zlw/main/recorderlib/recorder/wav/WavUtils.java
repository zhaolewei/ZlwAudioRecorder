package com.zlw.main.recorderlib.recorder.wav;

import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.utils.ByteUtils;
import com.zlw.main.recorderlib.utils.FileUtils;
import com.zlw.main.recorderlib.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author zhaolewei on 2018/7/3.
 *         pcm 转 wav 工具类
 *         http://soundfile.sapp.org/doc/WaveFormat/
 */
public class WavUtils {
    private static final String TAG = WavUtils.class.getSimpleName();

    /**
     * 生成wav格式的Header
     * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk（可选）,Data chunk
     *
     * @param totalAudioLen 不包括header的音频数据总长度
     * @param sampleRate    采样率,也就是录制时使用的频率
     * @param channels      audioRecord的频道数量
     * @param sampleBits    位宽
     */
    public static byte[] generateWavFileHeader(int totalAudioLen, int sampleRate, int channels, int sampleBits) {
        WavHeader wavHeader = new WavHeader(totalAudioLen, sampleRate, (short) channels, (short) sampleBits);
        return wavHeader.getHeader();
    }

    /**
     * 将header写入到pcm文件中 不修改文件名
     *
     * @param file   写入的pcm文件
     * @param header wav头数据
     */
    public static void writeHeader(File file, byte[] header) {
        if (!FileUtils.isFile(file)) {
            return;
        }

        RandomAccessFile wavRaf = null;
        try {
            wavRaf = new RandomAccessFile(file, "rw");
            wavRaf.seek(0);
            wavRaf.write(header);
            wavRaf.close();
        } catch (Exception e) {
            Logger.e(e, TAG, e.getMessage());
        } finally {
            try {
                if (wavRaf != null) {
                    wavRaf.close();
                }
            } catch (IOException e) {
                Logger.e(e, TAG, e.getMessage());

            }
        }
    }


    /**
     * Pcm 转 WAV 文件
     *
     * @param pcmFile File
     * @param header  wavHeader
     * @throws IOException Exception
     */
    public static void pcmToWav(File pcmFile, byte[] header) throws IOException {
        if (!FileUtils.isFile(pcmFile)) {
            return;
        }
        String pcmPath = pcmFile.getAbsolutePath();
        String wavPath = pcmPath.substring(0, pcmPath.length() - 4) + ".wav";
        writeHeader(new File(wavPath), header);
    }

    /**
     * 获取WAV文件的头信息
     *
     * @param wavFilePath 文件地址
     * @return header
     */
    private static byte[] getHeader(String wavFilePath) {
        if (!new File(wavFilePath).isFile()) {
            return null;
        }
        byte[] buffer = null;
        File file = new File(wavFilePath);
        final int size = 44;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream(size);
            byte[] b = new byte[size];
            int len;
            if ((len = fis.read(b)) != size) {
                Logger.e(TAG, "读取失败 len: %s", len);
                return null;
            }
            bos.write(b, 0, len);
            buffer = bos.toByteArray();
        } catch (Exception e) {
            Logger.e(e, TAG, e.getMessage());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
                if (bos != null) {
                    bos.close();
                    bos = null;
                }
            } catch (IOException e) {
                Logger.e(e, TAG, e.getMessage());
            }
        }
        return buffer;
    }

    /**
     * 获取wav音频时长 ms
     *
     * @param filePath wav文件路径
     * @return 时长   -1: 获取失败
     */
    public static long getWavDuration(String filePath) {
        if (!filePath.endsWith(RecordConfig.RecordFormat.WAV.getExtension())) {
            return -1;
        }
        byte[] header = getHeader(filePath);
        return getWavDuration(header);
    }

    /**
     * 获取wav音频时长 ms
     *
     * @param header wav音频文件字节数组
     * @return 时长   -1: 获取失败
     */
    public static long getWavDuration(byte[] header) {
        if (header == null || header.length < 44) {
            Logger.e(TAG, "header size有误");
            return -1;
        }
        int byteRate = ByteUtils.toInt(header, 28);//28-31
        int waveSize = ByteUtils.toInt(header, 40);//40-43
        return waveSize * 1000L / byteRate;
    }

    public static String headerToString(byte[] header) {
        if (header == null || header.length < 44) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            stringBuilder.append((char) header[i]);
        }
        stringBuilder.append(",");

        stringBuilder.append(ByteUtils.toInt(header, 4));
        stringBuilder.append(",");

        for (int i = 8; i < 16; i++) {
            stringBuilder.append((char) header[i]);
        }
        stringBuilder.append(",");

        for (int i = 16; i < 24; i++) {
            stringBuilder.append(header[i]);
        }
        stringBuilder.append(",");

        stringBuilder.append(ByteUtils.toInt(header, 24));
        stringBuilder.append(",");

        stringBuilder.append(ByteUtils.toInt(header, 28));
        stringBuilder.append(",");

        for (int i = 32; i < 36; i++) {
            stringBuilder.append(header[i]);
        }
        stringBuilder.append(",");

        for (int i = 36; i < 40; i++) {
            stringBuilder.append((char) header[i]);
        }
        stringBuilder.append(",");

        stringBuilder.append(ByteUtils.toInt(header, 40));

        return stringBuilder.toString();
    }

    public static class WavHeader {
        /**
         * RIFF数据块
         */
        final String riffChunkId = "RIFF";
        int riffChunkSize;
        final String riffType = "WAVE";

        /**
         * FORMAT 数据块
         */
        final String formatChunkId = "fmt ";
        final int formatChunkSize = 16;
        final short audioFormat = 1;
        short channels;
        int sampleRate;
        int byteRate;
        short blockAlign;
        short sampleBits;

        /**
         * FORMAT 数据块
         */
        final String dataChunkId = "data";
        int dataChunkSize;

        WavHeader(int totalAudioLen, int sampleRate, short channels, short sampleBits) {
            this.riffChunkSize = totalAudioLen;
            this.channels = channels;
            this.sampleRate = sampleRate;
            this.byteRate = sampleRate * sampleBits / 8 * channels;
            this.blockAlign = (short) (channels * sampleBits / 8);
            this.sampleBits = sampleBits;
            this.dataChunkSize = totalAudioLen - 44;
        }

        public byte[] getHeader() {
            byte[] result;
            result = ByteUtils.merger(ByteUtils.toBytes(riffChunkId), ByteUtils.toBytes(riffChunkSize));
            result = ByteUtils.merger(result, ByteUtils.toBytes(riffType));
            result = ByteUtils.merger(result, ByteUtils.toBytes(formatChunkId));
            result = ByteUtils.merger(result, ByteUtils.toBytes(formatChunkSize));
            result = ByteUtils.merger(result, ByteUtils.toBytes(audioFormat));
            result = ByteUtils.merger(result, ByteUtils.toBytes(channels));
            result = ByteUtils.merger(result, ByteUtils.toBytes(sampleRate));
            result = ByteUtils.merger(result, ByteUtils.toBytes(byteRate));
            result = ByteUtils.merger(result, ByteUtils.toBytes(blockAlign));
            result = ByteUtils.merger(result, ByteUtils.toBytes(sampleBits));
            result = ByteUtils.merger(result, ByteUtils.toBytes(dataChunkId));
            result = ByteUtils.merger(result, ByteUtils.toBytes(dataChunkSize));
            return result;
        }
    }

}
