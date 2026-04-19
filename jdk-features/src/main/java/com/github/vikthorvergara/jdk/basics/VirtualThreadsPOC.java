package com.github.vikthorvergara.jdk.basics;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class VirtualThreadsPOC {

    static void platformVsVirtual() throws InterruptedException {
        var platform = Thread.ofPlatform().name("plat-").unstarted(() ->
                System.out.println("platform: " + Thread.currentThread() + " virtual=" + Thread.currentThread().isVirtual()));
        platform.start();
        platform.join();

        var virtual = Thread.ofVirtual().name("virt-").unstarted(() ->
                System.out.println("virtual:  " + Thread.currentThread() + " virtual=" + Thread.currentThread().isVirtual()));
        virtual.start();
        virtual.join();
    }

    static void virtualThreadFactory() throws InterruptedException {
        ThreadFactory factory = Thread.ofVirtual().name("worker-", 0).factory();

        var threads = IntStream.range(0, 5)
                .mapToObj(i -> factory.newThread(() ->
                        System.out.println("ran on " + Thread.currentThread().getName())))
                .toList();
        threads.forEach(Thread::start);
        for (var t : threads) t.join();
    }

    static void virtualPerTaskExecutor() throws InterruptedException {
        var counter = new AtomicInteger();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 10_000).forEach(i -> executor.submit(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                counter.incrementAndGet();
            }));
        }
        System.out.println("completed tasks = " + counter.get());
    }

    static long blockingTask(int durationMs) {
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return durationMs;
    }

    static void scaleComparison() throws InterruptedException {
        int taskCount = 2_000;
        int sleepMs = 100;

        long platformStart = System.nanoTime();
        try (var pool = Executors.newFixedThreadPool(200)) {
            IntStream.range(0, taskCount).forEach(i -> pool.submit(() -> blockingTask(sleepMs)));
        }
        long platformMs = (System.nanoTime() - platformStart) / 1_000_000;

        long virtualStart = System.nanoTime();
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, taskCount).forEach(i -> pool.submit(() -> blockingTask(sleepMs)));
        }
        long virtualMs = (System.nanoTime() - virtualStart) / 1_000_000;

        System.out.println(taskCount + " tasks x " + sleepMs + "ms blocking:");
        System.out.println("  platform pool(200) -> " + platformMs + "ms");
        System.out.println("  virtual per-task   -> " + virtualMs + "ms");
    }

    static void pinningWithSynchronized() throws InterruptedException {
        var lock = new Object();
        var trace = new java.util.concurrent.CopyOnWriteArrayList<String>();

        var threads = IntStream.range(0, 3).mapToObj(i ->
                Thread.ofVirtual().unstarted(() -> {
                    synchronized (lock) {
                        trace.add(Thread.currentThread() + " entered");
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        trace.add(Thread.currentThread() + " exiting");
                    }
                })
        ).toList();
        threads.forEach(Thread::start);
        for (var t : threads) t.join();
        trace.forEach(System.out::println);
    }

    static void reentrantLockNoPinning() throws InterruptedException {
        var lock = new ReentrantLock();
        var trace = new java.util.concurrent.CopyOnWriteArrayList<String>();

        var threads = IntStream.range(0, 3).mapToObj(i ->
                Thread.ofVirtual().unstarted(() -> {
                    lock.lock();
                    try {
                        trace.add(Thread.currentThread() + " entered");
                        Thread.sleep(20);
                        trace.add(Thread.currentThread() + " exiting");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();
                    }
                })
        ).toList();
        threads.forEach(Thread::start);
        for (var t : threads) t.join();
        trace.forEach(System.out::println);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- platform vs virtual ---");
        platformVsVirtual();

        System.out.println("\n--- virtual thread factory ---");
        virtualThreadFactory();

        System.out.println("\n--- virtual per-task executor (10k tasks) ---");
        virtualPerTaskExecutor();

        System.out.println("\n--- scale comparison: platform vs virtual ---");
        scaleComparison();

        System.out.println("\n--- synchronized pins carrier thread ---");
        pinningWithSynchronized();

        System.out.println("\n--- ReentrantLock does not pin ---");
        reentrantLockNoPinning();
    }
}
