package com.rulex.bsb.utils;

import org.apache.commons.lang.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * 嵌入式数据库连接
 */
public class LevelDBUtil {

    private static DB dataDB = null;
    private static DB mataDB = null;

    private static final String DATA_PATH = "data";
    private static final String FLAG_PATH = "mata";

    private LevelDBUtil() {

    }

    /**
     * Connect to the LevelDB database
     */
    public static DB getDataDB() {

        if (null == dataDB) {
            Options options = new Options();
            options.createIfMissing(true);
            String path = System.getProperty("user.dir");
            try {
                String dataPath = path.substring(0, StringUtils.lastIndexOf(path, File.separator)) + File.separator + DATA_PATH;
                dataDB = factory.open(new File(dataPath), options);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return dataDB;
    }

    /**
     * Connect to the LevelDB database
     */
    public static DB getMataDB() {
        if (null == mataDB) {
            Options options = new Options();
            options.createIfMissing(true);
            String path = System.getProperty("user.dir");
            try {
                mataDB = factory.open(new File(path.substring(0, StringUtils.lastIndexOf(path, File.separator)) + File.separator + FLAG_PATH), options);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mataDB;
    }

    /**
     * 关闭数据库连接
     */
    public static void closeDB() {
        try {
            if (null != dataDB) {
                dataDB.close();
                dataDB = null;
            }
            if (null != mataDB) {
                mataDB.close();
                mataDB = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
