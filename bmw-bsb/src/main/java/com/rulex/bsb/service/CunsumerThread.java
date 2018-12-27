package com.rulex.bsb.service;

import java.io.IOException;

public class CunsumerThread extends Thread {


    public void run() {

        while (!this.isInterrupted()) {// 线程未中断执行循环
            try {

                BSBService.Consumer();
                Thread.sleep(10); //每隔10ms执行一次
            } catch (InterruptedException e) {

                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
