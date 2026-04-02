package com.github.vikthorvergara.jdk.basics;

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
    }
}
