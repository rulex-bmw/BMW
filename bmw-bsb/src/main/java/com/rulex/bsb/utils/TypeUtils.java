package com.rulex.bsb.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

public class TypeUtils {

    public static String doubleToString(double i) {
        DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
        return decimalFormat.format(i);
    }

    /**
     * byte[]转inputStream
     *
     * @param buf
     * @return
     */
    public static final InputStream byte2Input(byte[] buf) {
        return new ByteArrayInputStream(buf);
    }


    /**
     * inputstream转byte[]
     *
     * @param inStream
     * @return
     * @throws IOException
     */
    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }


    /**
     * 将boolean转成byte[]
     *
     * @param val
     * @return byte[]
     */
    public static byte[] Boolean2ByteArray(boolean val) {
        int tmp = (val == false) ? 0 : 1;
        return ByteBuffer.allocate(4).putInt(tmp).array();
    }


    /**
     * 将byte[]转成boolean
     *
     * @param data
     * @return boolean
     */
    public static boolean ByteArray2Boolean(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        int tmp = ByteBuffer.wrap(data, 0, 4).getInt();
        return (tmp == 0) ? false : true;
    }


    /**
     * 将int转成byte[]
     *
     * @param val
     * @return byte[]
     */
    public static byte[] Int2ByteArray(int val) {
        return ByteBuffer.allocate(4).putInt(val).array();
    }


    /**
     * 将byte[]转成int
     *
     * @param data
     * @return int
     */
    public static int ByteArray2Int(byte[] data) {
        if (data == null || data.length < 4) {
            return 0xDEADBEEF;
        }
        return ByteBuffer.wrap(data, 0, 4).getInt();
    }

    /**
     * 将float转成byte[]
     *
     * @param val
     * @return byte[]
     */
    public static byte[] Float2ByteArray(float val) {
        return ByteBuffer.allocate(4).putFloat(val).array();
    }


    /**
     * 将byte[]转成float
     *
     * @param data
     * @return float
     */
    public static float ByteArray2Float(byte[] data) {
        if (data == null || data.length < 4) {
            return -1234.0f;
        }
        return ByteBuffer.wrap(data).getFloat();
    }


    public static byte[] double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for(int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }


    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for(int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

}
