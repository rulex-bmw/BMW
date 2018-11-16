package com.rulex.dba;

import com.rulex.bsb.utils.DataException;

import java.util.*;

public class Cards {
    //剩余牌数
    public static int count = 0;
    //每个玩家最新的salt
    public static Map<String, byte[]> salts = new HashMap();
    //剩余的牌
    public static List<Integer> cards = new ArrayList<>();
    //抓到的牌
    public static Map<String, List<String>> proofs = new HashMap();

    private static byte[] seed = new byte[32];
    private static byte[] dsk = new byte[32];

    //牌下标
    public static int CARD_INDEX = 7;


    /**
     * 抓取剩余牌
     *
     * @param pks   玩家公钥集合
     * @param signs 签名集合
     * @return 抓取的牌
     */
    public static List<Integer> drawLeftCards(List<byte[]> pks, List<byte[]> signs) throws Exception {

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
        for (byte[] pk : pks) {
            length = initial.length;
            initial = Arrays.copyOf(initial, pk.length + length);
            System.arraycopy(pk, 0, initial, length, pk.length);
        }
        //哈希公钥数组，得到摘要
        byte[] h2s = Sha256.getSHA256(initial);

        //验证签名
        for (int i = 0; i < pks.size(); i++) {
            byte[] salt = salts.get(Base64.getEncoder().encodeToString(pks.get(i)));
            if (salt == null || salt.length == 0) {
                throw new DataException("Unknown player: " + Base64.getEncoder().encodeToString(pks.get(i)));
            }
            boolean flag = ECDSA.verify(h2s, pks.get(i), "SHA1withECDSA", signs.get(i));
            //验证签名失败，抛出异常
            if (flag == false) {
                throw new DataException("Signature verification failed");
            }
        }
        //验证成功，取出所有牌
        List<Integer> card = new ArrayList<>();
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
        String pkStr = Base64.getEncoder().encodeToString(pk);
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
        for (byte[] c : cs) {
            if (proofs.get(pkStr).indexOf(Base64.getEncoder().encodeToString(Sha256.getSHA256(c))) < 0) {
                throw new DataException("Unproven card" + Sha256.byte2Hex(c) + "to return");
            }
        }

        //还牌
        for (byte[] c : cs) {
            cards.add(Integer.valueOf(c[CARD_INDEX]));
            count++;
        }

        //生成新的seed
        seed = Arrays.copyOf(seed, seed.length + sig.length);
        System.arraycopy(sig, 0, seed, seed.length, sig.length);
        seed = Sha256.getSHA256(seed);
        seed = ECDSA.sign(seed, dsk, "SHA1withECDSA");
        salts.put(pkStr, Sha256.getSHA256(seed));
    }

    public static void main(String[] args) {

        byte[] seed = new byte[32];
        Random random = new Random();
        random.nextBytes(seed);

        List<byte[]> cs = new ArrayList<>();
        cs.add(seed);
        cs.add(seed);

        for (byte[] c : cs) {
            cards.add(Integer.valueOf(c[CARD_INDEX]));
            count++;
        }

        System.out.println(cards.size());
    }
}