package com.rulex.bsb.utils;

import com.google.protobuf.ByteString;
import com.google.protobuf.Type;
import com.rulex.bsb.pojo.Encoding;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class CryptoUtils {

    private CryptoUtils() {
    }


    /**
     * 生成keypair
     *
     * @param algorithm EC
     * @param stdName   secp256k1
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generatorKeyPair(String algorithm, String stdName) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);//EC,DiffieHellman,DSA,RSA
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(stdName);
        keyPairGenerator.initialize(256);
        keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * ECDH加密
     *
     * @param
     * @param msg
     * @return
     */
    public static byte[] ECDHEncrypt(byte[] publicKey, byte[] msg) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey pk = keyFactory.generatePublic(x509EncodedKeySpec);
        KeyPair keyPair = generatorKeyPair("EC", "secp256k1");
        PublicKey epk = keyPair.getPublic();
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(keyPair.getPrivate());
        keyAgreement.doPhase(pk, true);
        byte[] secret = keyAgreement.generateSecret();
        byte[] encKey = Arrays.copyOfRange(secret, 0, 16);
        String macKey = TypeUtils.bytesToHexString(Arrays.copyOfRange(secret, 16, 32));
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];//iv作为初始化向量
        secureRandom.nextBytes(iv);
        byte[] ct = AES256.AES_cbc_encrypt(msg, encKey, iv);//加密数据
        ByteString pct = ByteString.copyFrom(ct);
        ByteString pepk = ByteString.copyFrom(epk.getEncoded());
        ByteString piv = ByteString.copyFrom(iv);
        Encoding.ecies dataToMac = Encoding.ecies.newBuilder()
                .setCiphertext(pct)
                .setEphemPublicKey(pepk)
                .setIv(piv)
                .build();
        String mac = SHA256.sha256_HMAC(dataToMac.toString(), macKey);//hash（ct,epk,iv）
        ByteString pmac = ByteString.copyFrom(mac.getBytes());
        Encoding.ecies obj = Encoding.ecies.newBuilder().setIv(piv).setEphemPublicKey(pepk).setCiphertext(pct).setMac(pmac).build();
        return obj.toByteArray();
    }


    /**
     * ECDH解密
     *
     * @param
     * @param body
     * @return
     */
    public static byte[] ECDHDecrypt(byte[] privateKey, byte[] body) throws Exception {
        Encoding.ecies ecies = Encoding.ecies.parseFrom(body);
        byte[] ct = ecies.getCiphertext().toByteArray();
        byte[] iv = ecies.getIv().toByteArray();
        String bmac = ecies.getMac().toStringUtf8();
        byte[] pepk = ecies.getEphemPublicKey().toByteArray();
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pepk);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey epk = kf.generatePublic(x509EncodedKeySpec);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey sk = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(sk);
        keyAgreement.doPhase(epk, true);
        byte[] secret = keyAgreement.generateSecret();
        byte[] encKey = Arrays.copyOfRange(secret, 0, 16);
        String macKey = TypeUtils.bytesToHexString(Arrays.copyOfRange(secret, 16, 32));
        Encoding.ecies dataToMac = Encoding.ecies.newBuilder()
                .setCiphertext(ByteString.copyFrom(ct))
                .setEphemPublicKey(ByteString.copyFrom(pepk))
                .setIv(ByteString.copyFrom(iv))
                .build();
        String mac = SHA256.sha256_HMAC(dataToMac.toString(), macKey);
        if (!mac.equals(bmac)) {
            throw new Exception("Corrupted body - unmatched authentication code");
        }
        byte[] msg = AES256.AES_cbc_decrypt(ct, encKey, iv);
        return msg;
    }


    public static byte[] sign(byte[] privateKey, byte[] message) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey sk = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA1withECDSA");
        signature.initSign(sk);
        signature.update(message);
        return signature.sign();
    }

    public static boolean verify(byte[] publicKey, byte[] signed, byte[] message) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey pk = keyFactory.generatePublic(x509EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA1withECDSA");
        signature.initVerify(pk);
        signature.update(message);
        return signature.verify(signed);
    }

    public static void main(String[] args) throws Exception {
        //加密解密签名验证
//        KeyPair pair = CryptoUtils.generatorKeyPair("EC", "secp256k1");
//        PublicKey aPublic = pair.getPublic();
//        PrivateKey aPrivate = pair.getPrivate();
//        System.out.println("java.security genneratory：");
//        System.out.println("privateKey: " + Arrays.toString(aPrivate.getEncoded()));
//        System.out.println("publicKey: " + Arrays.toString(aPublic.getEncoded()));
//        String msg = "hello world ECIES!";
//        byte[] obj = CryptoUtils.ECDHEncrypt(aPublic.getEncoded(), msg);
//        String s = CryptoUtils.ECDHDecrypt(aPrivate.getEncoded(), obj);
//        System.out.println(s);
//        byte[] sign = CryptoUtils.sign(aPrivate.getEncoded(), msg);
//        boolean verify = CryptoUtils.verify(aPublic.getEncoded(), sign, msg);
//        System.out.println(verify);


        //ECDH密钥交换
//        KeyPair akeyPair = generatorKeyPair("EC", "secp256k1");
//        KeyPair bkeyPair = generatorKeyPair("EC", "secp256k1");
//        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");//akeyagreement
//        keyAgreement.init(akeyPair.getPrivate());//ask
//        keyAgreement.doPhase(bkeyPair.getPublic(), true);//bpk
//        byte[] bytes = keyAgreement.generateSecret();
//        KeyAgreement bkeyAgreement = KeyAgreement.getInstance("ECDH");//bkeyagreement
//        bkeyAgreement.init(bkeyPair.getPrivate());//bpk
//        bkeyAgreement.doPhase(akeyPair.getPublic(), true);//ask
//        byte[] bbytes = keyAgreement.generateSecret();
//        System.out.println(TypeUtils.bytesToHexString(bbytes).equals(TypeUtils.bytesToHexString(bytes)));


//
        // security生成公私钥
//        Security.addProvider(new BouncyCastleProvider());
//        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256k1");
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
//        keyPairGenerator.initialize(ecGenSpec, new SecureRandom());
//        java.security.KeyPair pair = keyPairGenerator.generateKeyPair();
//        ECPrivateKey privateKey = (ECPrivateKey) pair.getPrivate();
//        ECPublicKey publicKey = (ECPublicKey) pair.getPublic();
//        System.out.println("security gennerator:");
//        System.out.println(privateKey);
//        System.out.println(TypeUtils.bytesToHexString(publicKey.getW().getAffineX().toByteArray()));
//        System.out.println(TypeUtils.bytesToHexString(publicKey.getW().getAffineY().toByteArray()));


        //手动设置privateKey，生成publicKey
//        Security.addProvider(new BouncyCastleProvider());
//        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");
//        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
//        BigInteger k = new BigInteger("3", 16);
//        ECPoint Q = ecSpec.getG().multiply(k);
//        byte[] publicDerBytes = Q.getEncoded(false);
//        ECPoint point = ecSpec.getCurve().decodePoint(publicDerBytes);
//        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
//        ECPublicKey ecPublicKey = (ECPublicKey) keyFactory.generatePublic(pubSpec);
//        System.out.println(TypeUtils.bytesToHexString(ecPublicKey.getEncoded()));



    }


}
