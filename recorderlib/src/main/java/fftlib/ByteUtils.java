package fftlib;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author zhaoleweion 2018/8/3.
 */
public class ByteUtils {
    private static final String TAG = ByteUtils.class.getSimpleName();

    /**
     * short[] 转 byte[]
     */
    public static byte[] toBytes(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i]);
            dest[i * 2 + 1] = (byte) (src[i] >> 8);
        }

        return dest;
    }

    /**
     * 浮点转换为字节
     */
    public static byte[] toBytes(float f) {
        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        int len = b.length;
        byte[] dest = new byte[len];
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;

    }

    public static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    /**
     * short[] 转 byte[]
     */
    public static byte[] toBytes(short src) {
        byte[] dest = new byte[2];
        dest[0] = (byte) (src);
        dest[1] = (byte) (src >> 8);

        return dest;
    }

    /**
     * int 转 byte[]
     */
    public static byte[] toBytes(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) (i & 0xff);
        b[1] = (byte) ((i >> 8) & 0xff);
        b[2] = (byte) ((i >> 16) & 0xff);
        b[3] = (byte) ((i >> 24) & 0xff);
        return b;
    }


    /**
     * String 转 byte[]
     */
    public static byte[] toBytes(String str) {
        return str.getBytes();
    }

    /**
     * long类型转成byte数组
     */
    public static byte[] toBytes(long number) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, number);
        return buffer.array();
    }

    public static int toInt(byte[] src, int offset) {
        return ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
    }

    public static int toInt(byte[] src) {
        return toInt(src, 0);
    }

    /**
     * 字节数组到long的转换.
     */
    public static long toLong(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(b, 0, b.length);
        return buffer.getLong();
    }

    /**
     * byte[] 转 short[]
     * short： 2字节
     */
    public static short[] toShorts(byte[] src) {
        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) ((src[i * 2] & 0xff) | ((src[2 * i + 1] & 0xff) << 8));
        }
        return dest;
    }

    public static byte[] merger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    public static String toString(byte[] b) {
        return Arrays.toString(b);
    }

    /**
     * 将byte[] 追加到文件末尾
     */
    public static void byte2File(byte[] buf, File file) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            long fileLength = file.length();
            randomAccessFile.seek(fileLength);
            randomAccessFile.write(buf);
        } catch (Exception e) {
        }
    }

    /**
     * 将byte[] 追加到文件末尾
     */
    public static byte[] byte2FileForResult(byte[] buf, File file) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            long fileLength = file.length();
            randomAccessFile.seek(fileLength);
            randomAccessFile.write(buf);
        } catch (Exception e) {
        }
        return file2Bytes(file);

    }


    public static byte[] file2Bytes(File file) {

        byte[] buffer = null;
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (Exception e) {
        }
        return buffer;
    }

    public static double[] toHardDouble(short[] shorts) {
        int length = 512;
        double[] ds = new double[length];
        for (int i = 0; i < length; i++) {
            ds[i] = shorts[i];
        }
        return ds;
    }

    public static byte[] toHardBytes(double[] doubles) {
        byte[] bytes = new byte[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            double item = doubles[i];
            bytes[i] = (byte) (item > 127 ? 127 : item);
        }
        return bytes;
    }

    public static short[] toHardShort(double[] doubles) {
        short[] bytes = new short[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            double item = doubles[i];
            bytes[i] = (short) (item > 32767 ? 32767 : item);
        }
        return bytes;
    }

    public static byte[] toSoftBytes(double[] doubles) {
        double max = getMax(doubles);

        double sc = 1f;
        if (max > 127) {
            sc = (max / 128f);
        }

        byte[] bytes = new byte[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            double item = doubles[i] / sc;
            bytes[i] = (byte) (item > 127 ? 127 : item);
        }
        return bytes;
    }

    public static short[] toSoftShorts(double[] doubles) {
        double max = getMax(doubles);

        double sc = 1f;
        if (max > 127) {
            sc = (max / 128f);
        }

        short[] bytes = new short[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            double item = doubles[i] / sc;
            bytes[i] = (short) (item > 32767 ? 32767 : item);
        }
        return bytes;
    }

    public static double getMax(double[] data) {
        double max = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] > max) {
                max = data[i];
            }
        }
        return max;
    }
}
