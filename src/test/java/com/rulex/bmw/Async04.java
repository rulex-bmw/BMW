package com.rulex.bmw;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Async04 {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        // get msg from tc
        System.out.println("Got reqeust from TC");
        // prepare RM
        System.out.println("c RM msg");
        // trigger cupd
        CompletableFuture<String> cupdResult = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                // TODO Auto-generated method stub
                try {
                    System.out.println("sleep b4");
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("sleep after");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return "msg from CUPD";
            }
        }, executor);
        // return cupd
        cupdResult.thenAccept(new Consumer<String>() {
            // callback method
            public void accept(String arg0) {
                System.out.println("return msg to TC=" + arg0);
            }
        });
        // return RM
        System.out.println("return RM msg to customer");
    }

}
