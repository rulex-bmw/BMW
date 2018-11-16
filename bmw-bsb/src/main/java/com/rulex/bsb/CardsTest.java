package com.rulex.bsb;

import java.util.*;

public class CardsTest {
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