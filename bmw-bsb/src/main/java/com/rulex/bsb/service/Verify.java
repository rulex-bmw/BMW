package com.rulex.bsb.service;

import com.rulex.bsb.dao.LevelDBDao;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.LevelDBUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.rulex.bsb.dao.LevelDBDao.HEADER_KEY;
import static org.fusesource.leveldbjni.JniDBFactory.asString;

public class Verify {

    public static Map<byte[], byte[]> levelDBKey = new HashMap<>();

    /**
     * Verify the partial structure header static code block
     */
    static {
        LevelDBDao levelDBDao = new LevelDBDao();
        try {

            // Determine if there is a first message
            String value = asString(LevelDBUtil.getDataDB().get(HEADER_KEY));

            if (value != null) {
                levelDBKey = levelDBDao.verifyHeaderData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//
//        System.out.println("start");
//
//    }
}