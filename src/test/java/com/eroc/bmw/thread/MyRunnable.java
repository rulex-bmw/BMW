package com.eroc.bmw.thread;

public class MyRunnable implements Runnable {
    private String name;
    private boolean stop;
    private int i = 0;

    public MyRunnable(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        for(i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + "--" + name + "--" + i);
        }
    }


    public void stopThread() {
        this.stop = true;
    }
}
