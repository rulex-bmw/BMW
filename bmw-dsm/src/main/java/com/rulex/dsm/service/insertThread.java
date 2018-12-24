package com.rulex.dsm.service;

public class insertThread extends Thread {

//main是主线程
//不能对同一线程对象两次调用start()方法
//run()方法的方法体代表了线程需要完成的任务，称之为线程执行体, 每个线程都会执行一遍run方法

//堆空间里有两个ThreadDemo对象，一个线程一个对象，都会进行50个输出,并且每个对象里的num不共享。

    int num = 50;

    @Override
    public void run() {
        for (int i = 0; i < 50; i++) {
            System.out.println(Thread.currentThread().getName() + " " + num--);

        }
    }


    public static void main(String[] args) {

        for (int j = 0; j < 50; j++) {

            System.out.println(Thread.currentThread().getName() + " " + j);

            if (j == 30) {
                // 创建一个新的线程 myThread1 此线程进入新建状态
//                Thread myThread1 = new ThreadDemo();
//                // 创建一个新的线程 myThread2 此线程进入新建状态
//                Thread myThread2 = new ThreadDemo();
                // 调用start()方法使得线程进入就绪状态
//                myThread1.start();
//                // 调用start()方法使得线程进入就绪状态
//                myThread2.start();
            }
        }
    }
}
