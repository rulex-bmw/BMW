package com.rulex.bsb;

import com.rulex.bsb.utils.LevelDBUtil;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.util.Arrays;

public class Destory {

    public static void main(String[] args) throws IOException {
        byte[] key = {1};
        byte[] value1 = {12};
        byte[] value2 = {1};
        DB dataDB = LevelDBUtil.getDataDB();
        dataDB.put(key, value1);
        dataDB.put(key, value2);
        byte[] bytes = dataDB.get(key);
        System.out.println(Arrays.toString(bytes));

    }
}
