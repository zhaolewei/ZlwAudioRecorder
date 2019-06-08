package fftlib;

import com.zlw.main.recorderlib.utils.Logger;

/**
 * FFT 数据处理工厂
 */
public class FftFactory {
    private static final String TAG = FftFactory.class.getSimpleName();
    private Level level = Level.Original;

    public FftFactory(Level level) {
//        this.level = level;
    }

    public byte[] makeFftData(byte[] pcmData) {
//        Logger.d(TAG, "pcmData length: %s", pcmData.length);
        if (pcmData.length < 1024) {
            Logger.d(TAG, "makeFftData");
            return null;
        }

        double[] doubles = ByteUtils.toHardDouble(ByteUtils.toShorts(pcmData));
        double[] fft = FFT.fft(doubles, 0);

        switch (level) {
            case Original:
                return ByteUtils.toSoftBytes(fft);
            case Maximal:
//                return doFftMaximal(fft);
            default:
                return ByteUtils.toHardBytes(fft);
        }
    }


    private byte[] doFftMaximal(double[] fft) {
        byte[] bytes = ByteUtils.toSoftBytes(fft);
        byte[] result = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++) {

            if (isSimpleData(bytes, i)) {
                result[i] = bytes[i];
            } else {
                result[Math.max(i - 1, 0)] = (byte) (bytes[i] / 2);
                result[Math.min(i + 1, result.length - 1)] = (byte) (bytes[i] / 2);
            }
        }

        return result;
    }

    private boolean isSimpleData(byte[] data, int i) {

        int start = Math.max(0, i - 5);
        int end = Math.min(data.length, i + 5);

        byte max = 0, min = 127;
        for (int j = start; j < end; j++) {
            if (data[j] > max) {
                max = data[j];
            }
            if (data[j] < min) {
                min = data[j];
            }
        }

        return data[i] == min || data[i] == max;
    }


    /**
     * FFT 处理等级
     */
    public enum Level {

        /**
         * 原始数据，不做任何优化
         */
        Original,

        /**
         * 对音乐进行优化
         */
        Music,

        /**
         * 对人声进行优化
         */
        People,

        /**
         * 极限优化
         */
        Maximal
    }

}
