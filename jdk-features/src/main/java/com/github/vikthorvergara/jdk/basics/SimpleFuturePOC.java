package com.github.vikthorvergara.jdk.basics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SimpleFuturePOC {

    static int slowSquare(int n) throws InterruptedException {
        Thread.sleep(100);
        return n * n;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        try (var pool = Executors.newFixedThreadPool(2)) {

            Callable<Integer> task = () -> slowSquare(7);
            Future<Integer> future = pool.submit(task);

            System.out.println("submitted, isDone=" + future.isDone());
            Integer result = future.get();
            System.out.println("result=" + result + " isDone=" + future.isDone());

            Future<Integer> slow = pool.submit(() -> {
                Thread.sleep(500);
                return 42;
            });

            try {
                slow.get(50, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("get(50ms) timed out, cancelling");
                slow.cancel(true);
            }
            System.out.println("slow.isCancelled=" + slow.isCancelled());
        }
    }
}
