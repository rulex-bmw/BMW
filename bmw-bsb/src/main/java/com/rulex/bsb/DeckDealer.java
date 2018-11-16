package com.rulex.bsb;

import com.rulex.bsb.utils.*;
import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class DeckDealer {


    {
        try {
            KeyPair pair = CryptoUtils.generatorKeyPair("EC", "secp256k1");
            dsk = pair.getPrivate().getEncoded();
            dpk = pair.getPublic().getEncoded();
            SecureRandom secureRandom = new SecureRandom();
            seed = new byte[32];
            secureRandom.nextBytes(seed);
            StringBuffer stringBuffer = new StringBuffer();
            for(int i = 0; i < 64; i++) {
                String s = Integer.toHexString(new SecureRandom().nextInt(16));
                stringBuffer.append(s);
            }
            max256b = new BigInteger("10000000000000000000000000000000000000000000000000000000000000000", 16);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }


    private static byte[] seed = {};

    private static byte[] dsk;

    private static byte[] dpk;

    private static final Integer CARD_INDEX = 7;

    private static BigInteger max256b;

    //存储未抽牌
    private static List<Short> cards = new ArrayList<>();
    //为每一位玩家存储最新盐
    private static Map<String, byte[]> salts = new HashMap<>();
    //所有抽牌证明
    private static Map<String, List<String>> proofs = new HashMap<>();
    //剩余牌数
    private static Integer count = 0;


    /**
     * 开始游戏
     *
     * @param cardNum 卡牌数量
     * @param deckNum 几副
     * @param pks
     */
    public static Object[] openGame(Integer cardNum, Integer deckNum, List<byte[]> pks) throws Exception {
        new DeckDealer();
        cards.clear();
        for(Short i = 0; i < deckNum; i++) {
            for(Short j = 0; j < cardNum; j++) {
                cards.add(j);
            }
        }
        int destPos = seed.length;
        int l;
        for(byte[] pk : pks) {
            l = pk.length;
            seed = Arrays.copyOf(seed, destPos + l);
            System.arraycopy(pk, 0, seed, destPos, l);
            destPos += l;
        }
        seed = CryptoUtils.sign(dsk, SHA256.getSHA256Bytes(seed));
        byte[] s = SHA256.getSHA256Bytes(seed);

        for(byte[] pk : pks) {
            String pkk = Base64.toBase64String(pk);
            salts.put(pkk, s);
            proofs.put(pkk, new ArrayList<>());
        }

        count = cardNum * deckNum;
        Object[] o = {s, dpk};
        return o;
    }

    public static byte[] drawCard(byte[] pk, byte[] sig) throws Exception {
        if (count <= 0) {
            throw new Exception("No more card can be drawn");
        }

        String pkk = Base64.toBase64String(pk);

        byte[] s = salts.get(pkk);

        if (s.length <= 0) {
            throw new Exception(String.format("Unknown player: %s", pkk));
        }

        if (!CryptoUtils.verify(pk, sig, s)) {
            throw new Exception("pk owner have wrong signature!");
        }
        //重新生成盐和种子
        int l = seed.length;
        int sl = sig.length;
        seed = Arrays.copyOf(seed, l + sl);
        System.arraycopy(sig, 0, seed, l, sl);
        seed = CryptoUtils.sign(dsk, SHA256.getSHA256Bytes(seed));
        s = SHA256.getSHA256Bytes(seed);
        //抽取卡牌
        BigInteger i = new BigInteger(TypeUtils.bytesToHexString(s), 16).multiply(new BigInteger(count.toString())).divide(max256b);
        int index = i.intValue();
        Short card = cards.get(index);
        cards.set(index, cards.get(--count));
        cards.remove(count.shortValue());
        //将牌放入盐中并进行crc验证
        Arrays.fill(s, CARD_INDEX, CARD_INDEX + 1, TypeUtils.uint8ToByte(card));
        byte crc = CRC8Util.calcCrc8(s);
        Arrays.fill(s, s.length - 1, s.length, crc);
        //更新玩家的最新盐
        salts.put(pkk, s);
        //放入抽牌证明
        List<String> p = proofs.get(pkk);
        p.add(Base64.toBase64String(SHA256.getSHA256Bytes(s)));

        return CryptoUtils.ECDHEncrypt(pk, s);
    }


    /**
     * 抓取剩余牌
     *
     * @param pks   玩家公钥集合
     * @param signs 签名集合
     * @return 抓取的牌
     */
    public static List<Short> drawLeftCards(List<byte[]> pks, List<byte[]> signs) throws Exception {

        //牌数小于等于零，抛出异常
        if (count <= 0) {
            throw new DataException("No more card can be drawn");
        }

        //salt的数量与公钥数量和sign的数量不同，抛出异常
        int pc = salts.size();
        if (pks.size() != pc || signs.size() != pc) {
            throw new DataException("Not a consented draw cards request");
        }

        //将所有的公钥合并
        byte[] initial = new byte[0];
        int length;
        for(byte[] pk : pks) {
            length = initial.length;
            initial = Arrays.copyOf(initial, pk.length + length);
            System.arraycopy(pk, 0, initial, length, pk.length);
        }
        //哈希公钥数组，得到摘要
        byte[] h2s = SHA256.getSHA256Bytes(initial);

        //验证签名
        for(int i = 0; i < pks.size(); i++) {
            byte[] salt = salts.get(Base64.toBase64String(pks.get(i)));
            if (salt == null || salt.length == 0) {
                throw new DataException("Unknown player: " + Base64.toBase64String(pks.get(i)));
            }
            boolean flag = ECDSA.verify(h2s, pks.get(i), "SHA1withECDSA", signs.get(i));
            //验证签名失败，抛出异常
            if (flag == false) {
                throw new DataException("Signature verification failed");
            }
        }
        //验证成功，取出所有牌
        List<Short> card = new ArrayList<>();
        card.addAll(cards);
        cards.clear();
        count = 0;
        return card;
    }


    /**
     * 返还牌
     *
     * @param pk  玩家公钥
     * @param sig 签名
     * @param cs  牌信息
     */

    public static void returnCards(byte[] pk, byte[] sig, List<byte[]> cs) throws Exception {

        //验证玩家身份
        String pkStr = Base64.toBase64String(pk);
        byte[] salt = salts.get(pkStr);
        if (salt == null || salt.length == 0) {
            throw new DataException("Unknown player: " + pkStr);
        }
        boolean flag = ECDSA.verify(salt, pk, "SHA1withECDSA", sig);
        //验证签名失败，抛出异常
        if (flag == true) {
            throw new DataException("Signature verification failed");
        }

        //验证欲返回牌库的牌是否合理
        for(byte[] c : cs) {
            if (proofs.get(pkStr).indexOf(Base64.toBase64String(SHA256.getSHA256Bytes(c))) < 0) {
                throw new DataException("Unproven card" + TypeUtils.bytesToHexString(c) + "to return");
            }
        }

        //还牌
        for(byte[] c : cs) {
            cards.add(Short.valueOf(c[CARD_INDEX]));
            count++;
        }

        //生成新的seed
        seed = Arrays.copyOf(seed, seed.length + sig.length);
        System.arraycopy(sig, 0, seed, seed.length, sig.length);
        seed = SHA256.getSHA256Bytes(seed);
        seed = ECDSA.sign(seed, dsk, "SHA1withECDSA");
        salts.put(pkStr, SHA256.getSHA256Bytes(seed));
    }


    public static void main(String[] args) throws Exception {
        String[] cardNames = {
                "2♢", "3♢", "4♢", "5♢", "6♢", "7♢", "8♢", "9♢", "10♢", "J♢", "Q♢", "K♢", "A♢",
                "2♧", "3♧", "4♧", "5♧", "6♧", "7♧", "8♧", "9♧", "10♧", "J♧", "Q♧", "K♧", "A♧",
                "2♡", "3♡", "4♡", "5♡", "6♡", "7♡", "8♡", "9♡", "10♡", "J♡", "Q♡", "K♡", "A♡",
                "2♤", "3♤", "4♤", "5♤", "6♤", "7♤", "8♤", "9♤", "10♤", "J♤", "Q♤", "K♤", "A♤",
                "b🃏", "c🃏"
        };
        //测试游戏
        List<byte[]> pks = new ArrayList<>();
        List<byte[]> sks = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            KeyPair pair = CryptoUtils.generatorKeyPair("EC", "secp256k1");
            pks.add(pair.getPublic().getEncoded());
            sks.add(pair.getPrivate().getEncoded());
        }
        Object[] objects = DeckDealer.openGame(54, 1, pks);
        List<byte[]> es = new ArrayList<>();
        Map<Integer, List<Short>> cbps = new HashMap<>();
        for(int i = 0; i < 4; i++) {
            cbps.put(i, new ArrayList<>());
        }
        for(int i = 0; i < 4; i++) {
            es.add((byte[]) objects[0]);
        }
        long round = Math.round(Math.floor((54 * 1) / 4));
        for(long i = 0; i < round - 1; i++) {
            for(int j = 0; j < 4; j++) {
                byte[] r = DeckDealer.drawCard(pks.get(j), CryptoUtils.sign(sks.get(j), es.get(j)));
                es.set(j, CryptoUtils.ECDHDecrypt(sks.get(j), r));
                byte[] s = es.get(j);
                List<Short> c = cbps.get(j);
                c.add(TypeUtils.byteToUnit8(s[CARD_INDEX]));
            }
        }

        System.out.println("12轮抽牌结果:");
        for(int i = 0; i < 4; i++) {
            List<Short> shorts = cbps.get(i);
            System.out.print(i + 1 + "号玩家牌：");
            for(Short s : shorts) {
                System.out.print(cardNames[s] + " ");
            }
            System.out.println("\n");
        }


//        System.out.println(bytes.length);
//        System.out.println(Arrays.toString(bytes));
//        String s = Integer.toHexString(i);
//        System.out.println(s);


        //CRC校验
//        DeckDealer.openGame(54, 1, pks);
//        seed = CryptoUtils.sign(dsk.getEncoded(), SHA256.getSHA256(seed));
//        String s = SHA256.getSHA256(seed);
//        short card = 255;
//        byte cm = TypeUtils.uint8ToByte(card);
//        byte[] bytes = TypeUtils.hexStringToByte(s);
//        System.out.println(Arrays.toString(bytes));
//        Arrays.fill(bytes, CARD_INDEX - 1, CARD_INDEX, cm);
//        byte crc = CRC8Util.calcCrc8(bytes);
//        System.out.println(crc);

        //用一个字节储存牌的信息
//        DeckDealer.openGame(54, 1, pks);
//        seed = CryptoUtils.sign(dsk.getEncoded(), SHA256.getSHA256(seed));
//        String s = SHA256.getSHA256(seed);
//        short card = 255;
//        byte cm = TypeUtils.uint8ToByte(card);
//        byte[] bytes = TypeUtils.hexStringToByte(s);
//        System.out.println(Arrays.toString(bytes));
//        Arrays.fill(bytes, CARD_INDEX - 1, CARD_INDEX, cm);
//        System.out.println(Arrays.toString(bytes));

//        short i = 32767;
//        short i = 254;
//        byte[] bytes = TypeUtils.Int2ByteArray(i);
//        short uint16 = TypeUtils.getUint8(i);
//        System.out.println(uint16);
//        byte[] bytes = TypeUtils.shortToByte(uint16);
//        System.out.println(Arrays.toString(bytes));
//        String s = TypeUtils.bytesToHexString(bytes);
//        System.out.println(s);
//        short uint8 = TypeUtils.getUint8(i);
//        System.out.println(uint8);
//        byte bytes = TypeUtils.uint8ToByte(i);
//        System.out.println(bytes);
//        short i1 = TypeUtils.byteToUnit8(bytes);
//        System.out.println(i1);
//        byte[] b = {1, 2, 3, 4, 5, 6, 7, 8, 9};
//        byte a = 10;
//        Arrays.fill(b, b.length - 1, b.length, a);
//        System.out.println(Arrays.toString(b));


//        byte[] bytes = TypeUtils.shortToByte(i2);
//        System.out.println(Arrays.toString(bytes));
//        System.out.println(TypeUtils.bytesToHexString(bytes));
//        short i1 = TypeUtils.byteToShort(bytes);
//        System.out.println(i1);
//        String s = Integer.toHexString((int) i2);
//        System.out.println(s);

        //biginteger测试
//        BigInteger b1 = new BigInteger("999");
//        BigInteger b2 = new BigInteger("1000");
//        System.out.println("add: " + b1.add(b2));
//        System.out.println("sub: " + b1.subtract(b2));
//        System.out.println("mul: " + b1.multiply(b2));
//        System.out.println("div: " + b1.divide(b2));
//        BigInteger[] result = b1.divideAndRemainder(b2);
//        System.out.println(Arrays.toString(result));
//        System.out.println("商: " + result[0]);
//        System.out.println("余: " + result[1]);
//        openGame(54, 1, pks);
//        System.out.println(Arrays.toString(seed));
//        for(int i = 0; i < 100; i++) {
//            seed = CryptoUtils.sign(dsk.getEncoded(), SHA256.getSHA256(seed));
//            String s = SHA256.getSHA256(seed);
//            BigInteger index = new BigInteger(s, 16).multiply(new BigInteger(count.toString())).divide(max256b);
//            System.out.println(index);
//        }


//
//        Object[] objects = DeckDealer.openGame(54, 1, pks);
//        System.out.println(((String) objects[0]).length());
//        System.out.println(seed.length);
//        String sha256 = SHA256.getSHA256(seed);
//        System.out.println(sha256);
//        byte[] x = TypeUtils.hexStringToByte(sha256);
//        System.out.println(x.length);
//        System.out.println(Arrays.toString(x));
//        System.out.println(Arrays.toString(dpk.getEncoded()));
//        System.out.println(Arrays.toString(dsk.getEncoded()));
//        System.out.println(max256b);


        //测试数组合并
//        int destPos = seed.length;
//        int l;
//        byte[] encoded;
//        for(PublicKey pk : pks) {
//            encoded = pk.getEncoded();
//            l = encoded.length;
//            seed = Arrays.copyOf(seed, destPos + l);
//            System.arraycopy(encoded, 0, seed, destPos, l);
//            System.out.println(seed.length);
//            destPos += l;
//        }
//        System.out.println(Arrays.toString(seed));

        //测试种子签名
//        for(int i = 0; i < 2; i++) {
//            int destPos = seed.length;
//            int l;
//            byte[] encoded;
//            for(PublicKey pk : pks) {
//                encoded = pk.getEncoded();
//                l = encoded.length;
//                seed = Arrays.copyOf(seed, destPos + l);
//                System.arraycopy(encoded, 0, seed, destPos, l);
//                destPos += l;
//            }
//            System.out.println(seed.length);
//            String sha256 = SHA256.getSHA256(seed);
//            seed = CryptoUtils.sign(dsk, sha256);
//            System.out.println(seed.length);
//        }


        //测试测试生成256位随机数
//        StringBuffer stringBuffer = new StringBuffer();
//        for(int i = 0; i < 64; i++) {
//            String s = Integer.toHexString(new SecureRandom().nextInt(16));
//            stringBuffer.append(s);
//        }
//        System.out.println(stringBuffer.toString().toUpperCase());


    }

}
