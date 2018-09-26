package com.eroc.bmw.util;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class LevelDBUtil {

    /**
     * Connect to the LevelDB database
     */
    public static DB getDb(String fileName) throws IOException {

        Options options = new Options();
        options.createIfMissing(true);
        DB db = factory.open(new File(fileName), options);

        return db;
    }
}
