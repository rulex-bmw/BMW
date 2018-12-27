package com.rulex.bsb.service;

public class CunsumerThread extends Thread {


    public void run() {

        while (!this.isInterrupted()) {// 线程未中断执行循环
            try {

                Thread.sleep(10); //每隔10ms执行一次
                BSBService.Consumer();

            } catch (InterruptedException e) {

                e.printStackTrace();

            }
        }
    }
}
