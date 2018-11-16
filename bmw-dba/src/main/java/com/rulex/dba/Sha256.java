package com.rulex.dba;


import java.security.MessageDigest;

public class Sha256 {

    //得到32字节的byte[]
    public static byte[] getSHA256(byte[] bytes) throws Exception {
        MessageDigest messageDigest;

        messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes);

        return messageDigest.digest();
    }

    //将byte转为16进制
    public static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String tem = null;
        for (int i = 0; i < bytes.length; i++) {

            tem = Integer.toHexString(bytes[i] & 0xFF);

            if (tem.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(tem);
        }
        return stringBuffer.toString();
    }
}