package com.rulex.bsb;

import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class Destory {

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        factory.destroy(new File("data"), options);
        factory.destroy(new File("mata"), options);

//        ExecutorService executorService = Executors.newCachedThreadPool();



    }
}
