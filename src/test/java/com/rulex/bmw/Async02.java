package com.rulex.bmw;

import java.util.concurrent.*;

public class Async02 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Get request from TC, thread id=" + Thread.currentThread().getId());
        ExecutorService ES = Executors.newCachedThreadPool();
        Callable<Integer> rmTask = new RMTask();
        Callable<Integer> cupdTask = new CUPDTask();
        System.out.println("_______________________");
        Thread.sleep(3000);
        //ES submit task,
        // param is callable
        // return result
        Future<Integer> rmResult = ES.submit(rmTask);
        Future<Integer> cupdResult = ES.submit(cupdTask);
        Thread.sleep(2000);
        System.out.println("----------------------");
        ES.shutdown();
        System.out.println("ES shutdown, thread id=" + Thread.currentThread().getId());
        //ES shutdown
        try {
            Integer result = rmResult.get();
            System.out.println(cupdResult.isDone());
            System.out.println("process rmResult, thread id=" + Thread.currentThread().getId());
            System.out.println("process rmResult=" + result);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static class RMTask implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            // TODO Auto-generated method stub
            System.out.println("creating response Rich media, thread id=" + Thread.currentThread().getId());
            Thread.sleep(3000);
            System.out.println("end response Rich media, thread id=" + Thread.currentThread().getId());
            return new Integer(3);
        }
    }

    static class CUPDTask implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("creating CUPD msg, thread id=" + Thread.currentThread().getId());
            Thread.sleep(8000);
            System.out.println("end response CUPD msg, thread id=" + Thread.currentThread().getId());
            return new Integer(10);

        }
    }
}