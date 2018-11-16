package com.rulex.bsb;

import com.rulex.bsb.utils.TypeUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Pow {
    /**
     * 工作量证明
     *
     * @param args
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        long l = System.currentTimeMillis();
        for(int i = 0; i < 100000000; i++) {
            String s = "hello word!" + i;
            MessageDigest instance = MessageDigest.getInstance("SHA-1");
            instance.update(s.getBytes("UTF-8"));
            String s1 = TypeUtils.bytesToHexString(instance.digest());
            if (s1.substring(0, 5).equals("00000")) {
                System.out.println(s1 + "----" + i);
                long l1 = System.currentTimeMillis();
                System.out.println("挖矿用时=" + (l1 - l) + "ms");
                break;
            }
        }
    }



//    public static void main(String[] args) {
//        SecureRandom random = new SecureRandom();
//        byte[] bytes = new byte[16];
//        random.nextBytes(bytes);
//        System.out.println(bytesToHexString(bytes));
//        System.out.println(bytesToHexString(bytes).length());
//    }


}
