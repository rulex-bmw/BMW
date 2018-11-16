package com.rulex.bsb.utils;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.Encoding;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import static org.fusesource.leveldbjni.JniDBFactory.bytes;

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
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
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
//        KeyPair pair = CryptoUtils.generatorKeyPair("EC", "secp256k1");
//        PublicKey aPublic = pair.getPublic();
//        PrivateKey aPrivate = pair.getPrivate();
//        System.out.println("java.security genneratory：");
//        System.out.println("privateKey: " + Arrays.toString(aPrivate.getEncoded()));
//        System.out.println("publicKey: " + Arrays.toString(aPublic.getEncoded()));


//        String msg = "hello world ECIES!";
//        byte[] obj = CryptoUtils.ECDHEncrypt(aPublic.getEncoded(), msg);
//        System.out.println(obj.length);
//        String s = CryptoUtils.ECDHDecrypt(aPrivate.getEncoded(), obj);
//        System.out.println(s);
//        byte[] sign = CryptoUtils.sign(aPrivate.getEncoded(), msg);
//        System.out.println(sign.length);
//        boolean verify = CryptoUtils.verify(aPublic.getEncoded(), sign, msg);
//        System.out.println(verify);


//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");//EC,DiffieHellman,DSA,RSA
//        // curveName这里取值：secp256k1
//        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
//        keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
//        KeyPair keyPair = keyPairGenerator.generateKeyPair();
//        // 获取公钥
//        PublicKey aPublic = keyPair.getPublic();
//        // 获取私钥
//        PrivateKey aPrivate = keyPair.getPrivate();
//        CryptoUtils.encrypt(aPublic, "1234");


//        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//        ECParameterSpec ecSpec = NamedCurve.getECParameterSpec("secp192k1");
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDH", "BC");
//        SecureRandom sr = new SecureRandom();
//        // 取消掉这个注释后 结果倒是固定 不过不能得到想要的数据
//         sr.setSeed(hexStringToByte("02 D7 2D 3D".replace(" ", "")));
//        keyGen.initialize(ecSpec, sr); // 公私钥 工厂
//        KeyPair pair = keyGen.generateKeyPair(); // 生成公私钥

//        BCECPublicKey cpk = (BCECPublicKey) pair.getPublic();
//        ECPoint.Fp point = (ECPoint.Fp) cpk.getQ();
//        System.out.println(pair.getPublic());
//
//        // ecdhkey 当我先知道这个 ecdhkey 能不能把已知的值set进去 得到一个固定的结果???
//        byte[] ecdhkey = point.getEncoded(true);
//        System.out.println("ecdhkey: " + printBytesToHexString(ecdhkey));
//
//        java.security.spec.ECPoint sp = ECPointUtil.decodePoint(ecSpec.getCurve(), serverPBK);
//        KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
//        ECPublicKeySpec pubSpec = new ECPublicKeySpec(sp, ecGenParameterSpec);
//        ECPublicKey myECPublicKey = (ECPublicKey) kf.generatePublic(pubSpec);
//
//        KeyAgreement agreement = KeyAgreement.getInstance("ECDH", "BC");
//        agreement.init(pair.getPrivate());
//        agreement.doPhase(myECPublicKey, true);
//        System.out.println("secret: " + printBytesToHexString(agreement.generateSecret()));
//
//        byte[] xx = agreement.generateSecret();
//        byte[] result = new byte[16];
//        // 只需要前面16个字节
//        System.arraycopy(xx, 0, result, 0, 16);
//
//        // 打印最后的密文
//        System.out.println("result: " + printBytesToHexString(result));

//        KeyPair akeyPair = generatorKeyPair("EC", "secp256k1");
//        KeyPair bkeyPair = generatorKeyPair("EC", "secp256k1");
//        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");//akeyagreement
//        keyAgreement.init(akeyPair.getPrivate());//ask
//        keyAgreement.doPhase(bkeyPair.getPublic(), true);//bpk
//        byte[] bytes = keyAgreement.generateSecret();
//        System.out.println(TypeUtils.bytesToHexString(bytes));
//        KeyAgreement bkeyAgreement = KeyAgreement.getInstance("ECDH");//bkeyagreement
//        bkeyAgreement.init(bkeyPair.getPrivate());//bpk
//        bkeyAgreement.doPhase(akeyPair.getPublic(), true);//ask
//        byte[] bbytes = keyAgreement.generateSecret();
//        System.out.println(TypeUtils.bytesToHexString(bbytes));
//        System.out.println(TypeUtils.bytesToHexString(bbytes).equals(TypeUtils.bytesToHexString(bytes)));
//        PrivateKey aPrivate = bkeyPair.getPrivate();
//        PrivateKey aPrivate1 = akeyPair.getPrivate();
//        System.out.println(TypeUtils.bytesToHexString(aPrivate.getEncoded()) + "\n" + TypeUtils.bytesToHexString(aPrivate1.getEncoded()));

//        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("B-571");
//
//        KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH", "BC");
//
//        g.initialize(ecSpec, new SecureRandom());
//
//        KeyPair aKeyPair = g.generateKeyPair();
//
//        KeyAgreement aKeyAgree = KeyAgreement.getInstance("ECDH", "BC");
//
//        aKeyAgree.init(aKeyPair.getPrivate());
//
//        KeyPair bKeyPair = g.generateKeyPair();
//
//        KeyAgreement bKeyAgree = KeyAgreement.getInstance("ECDH", "BC");
//
//        bKeyAgree.init(bKeyPair.getPrivate());
//
//        //
//        // agreement
//        //
//        aKeyAgree.doPhase(bKeyPair.getPublic(), true);
//        bKeyAgree.doPhase(aKeyPair.getPublic(), true);
//
//        byte[] aSecret = aKeyAgree.generateSecret();
//        byte[] bSecret = bKeyAgree.generateSecret();
//
//        System.out.println(aSecret);
//        System.out.println(bSecret);
//        if (aSecret.equals(bSecret)){
//            return true;
//        } else { return false; }


//        ECNamedCurveParameterSpec ecncp = ECNamedCurveTable.getParameterSpec("secp256k1");
//        KeyPairGenerator kp = KeyPairGenerator.getInstance("ECDH", "BC");
//        kp.initialize(ecncp, new SecureRandom());
//        KeyPair pair = kp.generateKeyPair();
//        PublicKey aPublic = pair.getPublic();
//        PrivateKey aPrivate = pair.getPrivate();
//


//
//
        // Generate Keys
//        Security.addProvider(new BouncyCastleProvider());
//        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256k1");
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
//        keyPairGenerator.initialize(ecGenSpec, new SecureRandom());
//        java.security.KeyPair pair = keyPairGenerator.generateKeyPair();
//        ECPrivateKey privateKey = (ECPrivateKey) pair.getPrivate();
//        ECPublicKey publicKeyExpected = (ECPublicKey) pair.getPublic();
//        System.out.println("bouncy castle gennerator:");
//        System.out.println(Arrays.toString(publicKeyExpected.getEncoded()));
//        PublicKey pk = publicKeyExpected;
//        System.out.println(Arrays.toString(pk.getEncoded()));


// Expected public key
//        System.out.print("Expected Public Key: " +
//                BaseEncoding.base64Url().encode(publicKeyExpected.getEncoded()));
//
// Generate public key from private key
//        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");
//        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
//
//        ECPoint Q = ecSpec.getG().multiply(privateKey.getD());
//        byte[] publicDerBytes = Q.getEncoded(false);
//
//        ECPoint point = ecSpec.getCurve().decodePoint(publicDerBytes);
//        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
//        ECPublicKey publicKeyGenerated = (ECPublicKey) keyFactory.generatePublic(pubSpec);
//
//// Generated public key from private key
//        System.out.print("Generated Public Key: " +
//                BaseEncoding.base64Url().encode(publicKeyGenerated.getEncoded()));


        // === here the magic happens ===
//        KeyFactory eckf = KeyFactory.getInstance("EC");
//        ECPoint point = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
//        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(name);
//        ECParameterSpec spec = new ECNamedCurveSpec(name, parameterSpec.getCurve(), parameterSpec.getG(), parameterSpec.getN(), parameterSpec.getH(), parameterSpec.getSeed());
//        ECPublicKey ecPublicKey = (ECPublicKey) eckf.generatePublic(new ECPublicKeySpec(point, spec));
//        System.out.println(ecPublicKey.getClass().getName());
//
//        // === test 123 ===
//        Cipher ecies = Cipher.getInstance("ECIESwithAES", "BC");
//        ecies.init(Cipher.ENCRYPT_MODE, ecPublicKey);
//        byte[] ct = ecies.doFinal("owlstead".getBytes(US_ASCII));
//        System.out.println(Hex.toHexString(ct));


    }


}
