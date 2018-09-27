package com.rulex.bmw.service;

import com.rulex.bmw.dao.LevelDBDao;
import com.rulex.bmw.dao.LevelDBDaoImpl;
import com.rulex.bmw.util.LevelDBUtil;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;

public class Verify {

    public static Map<byte[], byte[]> hashKey = new HashMap<byte[], byte[]>();

    /**
     * Verify the partial structure header static code block
     */
    static {
        LevelDBDao levelDBDao = new LevelDBDaoImpl();
        try {
            DB db = LevelDBUtil.getDb("data");
            String value = asString(db.get(bytes("000000")));
            db.close();
            if (value != null) {
                hashKey = levelDBDao.verifyHeaderData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        System.out.println("start");
        Iterator<Map.Entry<byte[], byte[]>> entries = hashKey.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<byte[], byte[]> entry = entries.next();
            System.out.println("Key = " + asString(entry.getKey()) + ", Value = " + asString(entry.getValue()));
        }
    }

}