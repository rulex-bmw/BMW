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
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }


    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }


    /**
     * 首字母转大写
     *
     * @param lower
     * @return String
     */
    public static String InitialsLow2Up(String lower) {
        return lower.substring(0, 1).toUpperCase() + lower.substring(1);
    }


    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray
     * @return
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for(int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hex
     * @return
     */
    public static byte[] hexStringToByte(String hex) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for(int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }


    /**
     * short到字节数组的转换.
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for(int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8;// 向右移8位
        }
        return b;
    }


    /**
     * 字节数组到short的转换.
     */
    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * uint8到字节数组的转换.
     */
    public static byte uint8ToByte(short number) {
        return  new Integer(number & 0xff).byteValue();
    }

    /**
     * 字节数组到uint8的转换.
     */
    public static short byteToUnit8(byte b) {
        return (short) (b & 0xff);
    }


    /**
     * 8位数
     *
     * @param s
     * @return
     */
    public static short getUint8(short s) {
        return (short) (s & 0x00ff);
    }

    /**
     * 16位数
     *
     * @param i
     * @return
     */
    public static int getUint16(int i) {
        return i & 0x0000ffff;
    }

    /**
     * 32位数
     *
     * @param l
     * @return
     */
    public static long getUint32(long l) {
        return l & 0x00000000ffffffff;
    }

}
