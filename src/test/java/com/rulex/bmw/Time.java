package com.rulex.bmw;

import java.util.concurrent.TimeUnit;

public class Time {

    public static void main(String[] args) throws InterruptedException {
        long l = System.nanoTime();
        System.out.println(l);
        Thread.sleep(3000);
        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - l) + "ms");


    }

}
