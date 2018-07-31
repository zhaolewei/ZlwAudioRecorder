package com.zlw.main.recorderlib.recorder;

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
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，
     * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的
     *
     * @param pcmAudioByteCount 不包括header的音频数据总长度
     * @param sampleRate        采样率,也就是录制时使用的频率
     * @param channels          audioRecord的频道数量
     */
    public static byte[] generateWavFileHeader(long pcmAudioByteCount, long sampleRate, int channels) {
        pcmAudioByteCount = pcmAudioByteCount - 44;
        long totalDataLen = pcmAudioByteCount + 44; // 不包含前8个字节的WAV文件总长度
        //TODO: ENCODING_PCM_8BIT 位宽支持
        long byteRate = sampleRate * 2 * channels;
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (2 * channels);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (pcmAudioByteCount & 0xff);
        header[41] = (byte) ((pcmAudioByteCount >> 8) & 0xff);
        header[42] = (byte) ((pcmAudioByteCount >> 16) & 0xff);
        header[43] = (byte) ((pcmAudioByteCount >> 24) & 0xff);
        return header;
    }

    /**
     * 将header写入到pcm中 不修改文件名
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

    public static void pcmToWav(File pcmFile, byte[] header, boolean isCover) throws IOException {
        if (!FileUtils.isFile(pcmFile)) {
            return;
        }
        if (isCover) {
            writeHeader(pcmFile, header);
        } else {
            String pcmPath = pcmFile.getAbsolutePath();
            String wavPath = pcmPath.substring(0, pcmPath.length() - 4) + ".wav";
            writeHeader(new File(wavPath), header);
        }
    }


    private static byte[] getHeader(String wavFile) {
        if (!new File(wavFile).isFile()) {
            return null;
        }
        byte[] buffer = null;
        File file = new File(wavFile);
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
        int byteRate = bytes2ToInt(header, 28);//28-31
        int waveSize = bytes2ToInt(header, 40);//40-43
        return waveSize * 1000 / byteRate;
    }

    private static int bytes2ToInt(byte[] src, int offset) {

        return ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
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

        stringBuilder.append(bytes2ToInt(header, 4));
        stringBuilder.append(",");

        for (int i = 8; i < 16; i++) {
            stringBuilder.append((char) header[i]);
        }
        stringBuilder.append(",");

        for (int i = 16; i < 24; i++) {
            stringBuilder.append(header[i]);
        }
        stringBuilder.append(",");

        stringBuilder.append(bytes2ToInt(header, 24));
        stringBuilder.append(",");

        stringBuilder.append(bytes2ToInt(header, 28));
        stringBuilder.append(",");

        for (int i = 32; i < 36; i++) {
            stringBuilder.append(header[i]);
        }
        stringBuilder.append(",");

        for (int i = 36; i < 40; i++) {
            stringBuilder.append((char) header[i]);
        }
        stringBuilder.append(",");

        stringBuilder.append(bytes2ToInt(header, 40));

        return stringBuilder.toString();
    }


}
