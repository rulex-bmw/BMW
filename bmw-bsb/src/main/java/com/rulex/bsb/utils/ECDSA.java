package com.rulex.bsb.utils;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ECDSA {

    //算法名称
    private static String ALGORITHM_NAME = "EC";


    /**
     * 初始化密钥
     *
     * @param curveName 所用曲线名字
     * @return KeyPair 秘钥信息
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair getKeyPair(String curveName) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_NAME);

        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(curveName);

        keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        return keyPair;
    }


    /**
     * 执行签名
     *
     * @param data                   签名数据
     * @param privateKey             私钥
     * @param signatureAlgorithmName 签名算法名字
     * @return byte[] 签名
     * @throws Exception
     */
    public static byte[] sign(byte[] data, byte[] privateKey, String signatureAlgorithmName) throws Exception {

        //获取私钥
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_NAME);
        PrivateKey sk = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

        //签名
        Signature signature = Signature.getInstance(signatureAlgorithmName);
        signature.initSign(sk);
        signature.update(data);
        byte[] sign = signature.sign();

        return sign;
    }


    /**
     * 验证签名
     *
     * @param data                   签名数据
     * @param publicKey              公钥
     * @param signatureAlgorithmName 签名算法名字
     * @param sign                   签名
     * @return boolean 验证签名结果
     * @throws Exception
     */
    public static boolean verify(byte[] data, byte[] publicKey, String signatureAlgorithmName, byte[] sign) throws Exception {

        //获取公钥
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_NAME);
        PublicKey pk = keyFactory.generatePublic(x509EncodedKeySpec);

        //验证
        Signature signature = Signature.getInstance(signatureAlgorithmName);
        signature.initVerify(pk);
        signature.update(data);
        boolean bool = signature.verify(sign);

        return bool;
    }

}
