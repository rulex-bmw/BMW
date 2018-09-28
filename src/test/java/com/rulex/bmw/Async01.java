package com.rulex.bmw;

public class Async01 {

    public static void main(String[] args) {

        System.out.println("get request from TC thread id=" + Thread.currentThread().getId());
        Runnable createRM = new CreateRichMedia("Thread RM");
        new Thread(createRM).start();
        Runnable tCUPD = new SendToCUPD("Thread CUPD");
        new Thread(tCUPD).start();
        System.out.println("End main method");


    }

    static class SendToCUPD implements Runnable {
        public String TName;

        public SendToCUPD(String TName) {
            this.TName = TName;
        }

        @Override
        public void run() {
            System.out.println("b4 send to cupd");
            try {
                System.out.println(" sending to cupd, thread id=" + Thread.currentThread().getId());
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("end send to cupd");
        }
    }

    static class CreateRichMedia implements Runnable {
        public String TName;

        public CreateRichMedia(String TName) {
            this.TName = TName;
        }

        @Override
        public void run() {
            System.out.println("start response Rich media");
            try {
                System.out.println("creating response Rich media, thread id=" + Thread.currentThread().getId());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("End response Rich media");
        }
    }

}
