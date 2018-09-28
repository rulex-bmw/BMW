package com.rulex.bmw;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class Async03 extends RecursiveTask<Integer> {
    private static final int THRESHOLD = 5;
    private int beginning;
    private int ending;


    public Async03(int beginning, int ending) {
        super();
        this.beginning = beginning;
        this.ending = ending;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        boolean canCompute = (ending - beginning) <= THRESHOLD;
        if (canCompute) {
            System.out.println("no need fork, ThreadID=" + Thread.currentThread().getId());
            System.out.println("no need fork, beginning=" + this.beginning);
            System.out.println("no need fork, ending=" + this.ending);
            for(int i = beginning; i <= ending; ++i) {
                sum += i;
            }
            return sum;
        } else {
            int interim = (this.ending + this.beginning) / 2;
            System.out.println("need fork, ThreadID=" + Thread.currentThread().getId());
            System.out.println("need fork, interim=" + interim);
            Async03 leftTask = new Async03(this.beginning, interim);
            Async03 rightTask = new Async03(interim + 1, this.ending);
            leftTask.fork();
            rightTask.fork();
            int leftResult = leftTask.join();
            int rightResult = rightTask.join();
            sum = leftResult + rightResult;
            return sum;
        }
    }

    public static void main(String[] args) {
        ForkJoinPool FJPool = new ForkJoinPool();
        Async03 task = new Async03(1, 20);
        Future<Integer> result = FJPool.submit(task);
        try {
            System.out.println("main result=" + result.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
