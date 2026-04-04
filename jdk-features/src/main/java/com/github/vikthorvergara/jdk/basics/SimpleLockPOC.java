package com.github.vikthorvergara.jdk.basics;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleLockPOC {

    static final ReentrantLock lock = new ReentrantLock();
    static int counter = 0;

    static void increment() {
        lock.lock();
        try {
            counter++;
        } finally {
            lock.unlock();
        }
    }

    static void tryLockWithTimeout() throws InterruptedException {
        var holder = Thread.ofPlatform().start(() -> {
            lock.lock();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });

        Thread.sleep(20);

        var acquired = lock.tryLock(50, TimeUnit.MILLISECONDS);
        System.out.println("tryLock(50ms) while held -> " + acquired);

        holder.join();

        acquired = lock.tryLock(50, TimeUnit.MILLISECONDS);
        System.out.println("tryLock(50ms) after release -> " + acquired);
        if (acquired) lock.unlock();
    }

    static void producerConsumerWithCondition() throws InterruptedException {
        var queueLock = new ReentrantLock();
        Condition notEmpty = queueLock.newCondition();
        Queue<Integer> buffer = new ArrayDeque<>();

        var consumer = Thread.ofPlatform().start(() -> {
            for (var i = 0; i < 5; i++) {
                queueLock.lock();
                try {
                    while (buffer.isEmpty()) notEmpty.await();
                    System.out.println("consumed " + buffer.poll());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    queueLock.unlock();
                }
            }
        });

        var producer = Thread.ofPlatform().start(() -> {
            for (var i = 1; i <= 5; i++) {
                queueLock.lock();
                try {
                    buffer.add(i);
                    System.out.println("produced " + i);
                    notEmpty.signal();
                } finally {
                    queueLock.unlock();
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        producer.join();
        consumer.join();
    }

    public static void main(String[] args) throws InterruptedException {
        var t1 = Thread.ofPlatform().start(() -> {
            for (var i = 0; i < 100_000; i++) increment();
        });
        var t2 = Thread.ofPlatform().start(() -> {
            for (var i = 0; i < 100_000; i++) increment();
        });

        t1.join();
        t2.join();

        System.out.println("counter = " + counter);
        System.out.println("expected = 200000");

        System.out.println("\n--- tryLock with timeout ---");
        tryLockWithTimeout();

        System.out.println("\n--- producer/consumer with Condition ---");
        producerConsumerWithCondition();
    }
}
