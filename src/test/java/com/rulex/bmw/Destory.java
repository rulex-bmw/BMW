package com.rulex.bmw;

import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class Destory {

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        factory.destroy(new File("data"), options);
        factory.destroy(new File("mata"), options);

//        ExecutorService executorService = Executors.newCachedThreadPool();



    }
}
