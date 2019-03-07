package com.rulex.bsb.service;

public class CunsumerThread extends Thread {


    public void run() {

        while (!this.isInterrupted()) {// The thread does not interrupt the execution loop
            try {

                Thread.sleep(10); // The thread does not interrupt the execution loop every 10ms
                BSBService.Consumer();

            } catch (InterruptedException e) {

                e.printStackTrace();

            }
        }
    }
}
