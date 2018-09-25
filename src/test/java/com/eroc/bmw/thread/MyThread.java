package com.eroc.bmw.thread;

public class MyThread extends Thread {

    private String name;

    private int i = 0;

    public MyThread(String name) {
        this.name = name;
    }
//    public MyThread(Runnable runnable, String name) {
//        super(runnable);
//        this.name = name;
//    }

    @Override
    public void run() {
        for(i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + "--" + name + "--" + i);
        }
    }
}
