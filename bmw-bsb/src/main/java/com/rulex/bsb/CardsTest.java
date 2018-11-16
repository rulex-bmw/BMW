package com.rulex.bsb;

import com.rulex.bsb.utils.CryptoUtils;
import com.rulex.bsb.utils.ECDSA;
import com.rulex.bsb.utils.SHA256;
import com.rulex.bsb.utils.TypeUtils;

import java.security.KeyPair;
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
        for (int i = 0; i < 4; i++) {
            KeyPair pair = CryptoUtils.generatorKeyPair("EC", "secp256k1");
            pks.add(pair.getPublic().getEncoded());
            sks.add(pair.getPrivate().getEncoded());
        }
        Object[] objects = DeckDealer.openGame(54, 1, pks);
        List<byte[]> es = new ArrayList<>();
        Map<Integer, List<Short>> cbps = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            cbps.put(i, new ArrayList<>());
        }
        for (int i = 0; i < 4; i++) {
            es.add((byte[]) objects[0]);
        }
        long round = Math.round(Math.floor((54 * 1) / 4));
        for (long i = 0; i < round - 1; i++) {
            for (int j = 0; j < 4; j++) {
                byte[] r = DeckDealer.drawCard(pks.get(j), CryptoUtils.sign(sks.get(j), es.get(j)));
                es.set(j, CryptoUtils.ECDHDecrypt(sks.get(j), r));
                byte[] s = es.get(j);
                List<Short> c = cbps.get(j);
                c.add(TypeUtils.byteToUnit8(s[CARD_INDEX]));
            }
        }

        System.out.println("12轮抽牌结果:");
        for (int i = 0; i < 4; i++) {
            List<Short> shorts = cbps.get(i);
            System.out.print(i + 1 + "号玩家牌：");
            for (Short s : shorts) {
                System.out.print(cardNames[s] + " ");
            }
            System.out.println("\n");
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
        byte[] h2s = SHA256.getSHA256Bytes(initial);

        List<byte[]> signs=new ArrayList<>();


        for (int i = 0; i < 4; i++) {
            signs.add(ECDSA.sign(h2s, sks.get(i), "SHA1withECDSA"));
        }


         List<Short> lc= DeckDealer.drawLeftCards(pks, signs);


        System.out.println("抓取的剩余牌:");

            for (Short s : lc) {
                System.out.print(cardNames[s] + " ");
            }
            System.out.println("\n");
        }
}