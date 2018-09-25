package com.eroc.bmw.service;

import com.eroc.bmw.dao.LevelDBDao;
import com.eroc.bmw.dao.LevelDBDaoImpl;
import org.iq80.leveldb.DB;

import java.io.IOException;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;

public class Verify {
    /**
     * Verify the partial structure header static code block
     */
    static {
        LevelDBDao levelDBDao = new LevelDBDaoImpl();
        try {
            DB db = levelDBDao.getDb("data");
            String value = asString(db.get(bytes("000000")));
            db.close();
            if (value != null) {
                levelDBDao.verifyHeaderData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        System.out.println("start");
    }
}