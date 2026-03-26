package com.github.vikthorvergara.jdk;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class ConcurrencyLocksAtomicsPOC {

    interface Counter {
        void increment();
        long get();
        String name();
    }

    static class SynchronizedCounter implements Counter {
        private long value = 0;

        @Override
        public synchronized void increment() {
            value++;
        }

        @Override
        public synchronized long get() {
            return value;
        }

        @Override
        public String name() {
            return "SynchronizedCounter";
        }
    }

    static class ReentrantLockCounter implements Counter {
        private final ReentrantLock lock;
        private long value = 0;

        ReentrantLockCounter(boolean fair) {
            this.lock = new ReentrantLock(fair);
        }

        @Override
        public void increment() {
            lock.lock();
            try {
                value++;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public long get() {
            lock.lock();
            try {
                return value;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String name() {
            return "ReentrantLockCounter(fair=" + lock.isFair() + ")";
        }
    }

    static class AtomicCounter implements Counter {
        private final AtomicLong value = new AtomicLong(0);

        @Override
        public void increment() {
            value.incrementAndGet();
        }

        @Override
        public long get() {
            return value.get();
        }

        @Override
        public String name() {
            return "AtomicCounter";
        }
    }

    record BenchmarkResult(String name, long totalOps, long elapsedMs, double opsPerMs) {}

    static BenchmarkResult benchmark(Counter counter, int threadCount, int incrementsPerThread) throws InterruptedException {
        var threads = new Thread[threadCount];
        for (var i = 0; i < threadCount; i++) {
            threads[i] = Thread.ofPlatform().unstarted(() -> {
                for (var j = 0; j < incrementsPerThread; j++) {
                    counter.increment();
                }
            });
        }

        var start = System.nanoTime();
        for (var t : threads) t.start();
        for (var t : threads) t.join();
        var elapsed = (System.nanoTime() - start) / 1_000_000;

        var totalOps = (long) threadCount * incrementsPerThread;
        var opsPerMs = elapsed > 0 ? (double) totalOps / elapsed : totalOps;

        return new BenchmarkResult(counter.name(), totalOps, elapsed, opsPerMs);
    }

    static void printResult(BenchmarkResult result, long actualValue) {
        System.out.printf("  %-40s | ops: %,10d | time: %5d ms | throughput: %,.0f ops/ms | value: %,d%n",
                result.name(), result.totalOps(), result.elapsedMs(), result.opsPerMs(), actualValue);
    }

    static void runContendedBenchmark(int threads, int incrementsPerThread) throws InterruptedException {
        System.out.printf("%n--- Contended: %d threads x %,d increments ---%n", threads, incrementsPerThread);

        Counter[] counters = {
                new SynchronizedCounter(),
                new ReentrantLockCounter(false),
                new ReentrantLockCounter(true),
                new AtomicCounter()
        };

        for (var counter : counters) {
            var result = benchmark(counter, threads, incrementsPerThread);
            printResult(result, counter.get());
        }
    }

    static void runUncontendedBenchmark(int incrementsPerThread) throws InterruptedException {
        System.out.printf("%n--- Uncontended: 1 thread x %,d increments ---%n", incrementsPerThread);

        Counter[] counters = {
                new SynchronizedCounter(),
                new ReentrantLockCounter(false),
                new AtomicCounter()
        };

        for (var counter : counters) {
            var result = benchmark(counter, 1, incrementsPerThread);
            printResult(result, counter.get());
        }
    }

    static void showTryLockWithTimeout() throws InterruptedException {
        System.out.println("\n=== tryLock() with Timeout ===");

        var lock = new ReentrantLock();
        var value = new long[]{0};

        var holder = Thread.ofPlatform().start(() -> {
            lock.lock();
            try {
                System.out.println("  Holder thread acquired lock, holding for 200ms...");
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } finally {
                lock.unlock();
                System.out.println("  Holder thread released lock");
            }
        });

        Thread.sleep(20);

        var contender = Thread.ofPlatform().start(() -> {
            try {
                System.out.println("  Contender trying to acquire lock with 50ms timeout...");
                var acquired = lock.tryLock(50, TimeUnit.MILLISECONDS);
                if (acquired) {
                    try {
                        value[0] = 42;
                        System.out.println("  Contender acquired lock (unexpected in this scenario)");
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("  Contender FAILED to acquire lock within 50ms (expected)");
                }

                System.out.println("  Contender retrying with 500ms timeout...");
                acquired = lock.tryLock(500, TimeUnit.MILLISECONDS);
                if (acquired) {
                    try {
                        value[0] = 99;
                        System.out.println("  Contender acquired lock on retry, set value to " + value[0]);
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        holder.join();
        contender.join();
        System.out.println("  Final value: " + value[0]);
    }

    static void showFairness() throws InterruptedException {
        System.out.println("\n=== Lock Fairness Comparison ===");

        for (var fair : new boolean[]{false, true}) {
            var lock = new ReentrantLock(fair);
            var order = new java.util.concurrent.ConcurrentLinkedQueue<String>();

            lock.lock();

            var threads = new Thread[5];
            for (var i = 0; i < threads.length; i++) {
                var threadName = "T" + i;
                threads[i] = Thread.ofPlatform().name(threadName).start(() -> {
                    lock.lock();
                    try {
                        order.add(threadName);
                    } finally {
                        lock.unlock();
                    }
                });
                Thread.sleep(10);
            }

            Thread.sleep(50);
            lock.unlock();

            for (var t : threads) t.join();

            System.out.printf("  fair=%-5s | acquisition order: %s%n", fair, order);
        }
        System.out.println("  (fair=true guarantees FIFO ordering of waiting threads)");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Counter Synchronization Benchmark ===");

        for (var i = 0; i < 2; i++) {
            runUncontendedBenchmark(1_000_000);
        }

        for (var i = 0; i < 2; i++) {
            runContendedBenchmark(4, 100_000);
            runContendedBenchmark(8, 100_000);
            runContendedBenchmark(16, 100_000);
        }

        showTryLockWithTimeout();
        showFairness();
    }
}
