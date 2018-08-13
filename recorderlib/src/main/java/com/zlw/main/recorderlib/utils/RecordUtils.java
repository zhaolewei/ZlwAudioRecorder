package com.zlw.main.recorderlib.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author zhaolewei on 2018/7/31.
 */
public class RecordUtils {
    /**
     * 获取录音的声音分贝值
     * 计算公式：dB = 20 * log(a / a0);
     * @return 声音分贝值
     */
    public static long getMaxDecibels(byte[] input) {
        short[] amplitudes = ByteUtils.toShorts(input);
        if (amplitudes == null) {
            return 0;
        }
        float maxAmplitude = 2;
        for (float amplitude : amplitudes) {
            if (Math.abs(maxAmplitude) < Math.abs(amplitude)) {
                maxAmplitude = amplitude;
            }
        }
        return Math.round(20 * Math.log10(maxAmplitude));
    }


    public static float[] byteToFloat(byte[] input) {
        if (input == null) {
            return null;
        }
        int bytesPerSample = 2;
        ByteBuffer buffer = ByteBuffer.wrap(input);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer floatBuffer = FloatBuffer.allocate(input.length / bytesPerSample);
        for (int i = 0; i < floatBuffer.capacity(); i++) {
            floatBuffer.put(buffer.getShort(i * bytesPerSample));
        }
        return floatBuffer.array();
    }
}
