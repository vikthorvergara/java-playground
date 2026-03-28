package com.github.vikthorvergara.jdk;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class LockFreeStackPOC {

    interface ConcurrentStack<T> {
        void push(T value);
        T pop();
        String name();
    }

    static class TreiberStack<T> implements ConcurrentStack<T> {
        private record Node<T>(T value, Node<T> next) {}

        private final AtomicReference<Node<T>> top = new AtomicReference<>(null);

        @Override
        public void push(T value) {
            var newNode = new Node<>(value, null);
            while (true) {
                var currentTop = top.get();
                newNode = new Node<>(value, currentTop);
                if (top.compareAndSet(currentTop, newNode)) {
                    return;
                }
            }
        }

        @Override
        public T pop() {
            while (true) {
                var currentTop = top.get();
                if (currentTop == null) {
                    return null;
                }
                if (top.compareAndSet(currentTop, currentTop.next())) {
                    return currentTop.value();
                }
            }
        }

        @Override
        public String name() {
            return "TreiberStack (lock-free CAS)";
        }
    }

    static class LockedStack<T> implements ConcurrentStack<T> {
        private record Node<T>(T value, Node<T> next) {}

        private final ReentrantLock lock = new ReentrantLock();
        private Node<T> top = null;

        @Override
        public void push(T value) {
            lock.lock();
            try {
                top = new Node<>(value, top);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public T pop() {
            lock.lock();
            try {
                if (top == null) return null;
                var value = top.value();
                top = top.next();
                return value;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String name() {
            return "LockedStack (ReentrantLock)";
        }
    }

    record BenchmarkResult(String name, long pushOps, long popOps, long elapsedMs, double opsPerMs) {}

    static BenchmarkResult benchmarkStack(ConcurrentStack<Integer> stack, int producerCount, int consumerCount, int opsPerThread) throws InterruptedException {
        var latch = new CountDownLatch(1);
        var pushCount = new AtomicInteger();
        var popCount = new AtomicInteger();

        var producers = new Thread[producerCount];
        for (var i = 0; i < producerCount; i++) {
            producers[i] = Thread.ofPlatform().start(() -> {
                try { latch.await(); } catch (InterruptedException e) { return; }
                for (var j = 0; j < opsPerThread; j++) {
                    stack.push(j);
                    pushCount.incrementAndGet();
                }
            });
        }

        var consumers = new Thread[consumerCount];
        for (var i = 0; i < consumerCount; i++) {
            consumers[i] = Thread.ofPlatform().start(() -> {
                try { latch.await(); } catch (InterruptedException e) { return; }
                for (var j = 0; j < opsPerThread; j++) {
                    stack.pop();
                    popCount.incrementAndGet();
                }
            });
        }

        var start = System.nanoTime();
        latch.countDown();
        for (var t : producers) t.join();
        for (var t : consumers) t.join();
        var elapsed = (System.nanoTime() - start) / 1_000_000;

        var totalOps = pushCount.get() + popCount.get();
        var opsPerMs = elapsed > 0 ? (double) totalOps / elapsed : totalOps;

        return new BenchmarkResult(stack.name(), pushCount.get(), popCount.get(), elapsed, opsPerMs);
    }

    static void printResult(BenchmarkResult r) {
        System.out.printf("  %-35s | pushes: %,10d | pops: %,10d | time: %5d ms | throughput: %,.0f ops/ms%n",
                r.name(), r.pushOps(), r.popOps(), r.elapsedMs(), r.opsPerMs());
    }

    static void runBenchmarkSuite(int producers, int consumers, int opsPerThread, String label) throws InterruptedException {
        System.out.printf("%n--- %s ---%n", label);

        ConcurrentStack<Integer>[] stacks = new ConcurrentStack[]{
                new TreiberStack<>(),
                new LockedStack<>()
        };

        for (var stack : stacks) {
            var result = benchmarkStack(stack, producers, consumers, opsPerThread);
            printResult(result);
        }
    }

    static void showCasRetryPattern() {
        System.out.println("\n=== CAS Retry Loop Pattern ===");

        var value = new AtomicInteger(0);

        System.out.println("  Initial value: " + value.get());
        System.out.println("  Attempting CAS: expected=0, new=42");
        var success = value.compareAndSet(0, 42);
        System.out.println("  CAS result: " + success + ", value: " + value.get());

        System.out.println("  Attempting CAS: expected=0, new=99 (should fail, current is 42)");
        success = value.compareAndSet(0, 99);
        System.out.println("  CAS result: " + success + ", value: " + value.get());

        System.out.println("\n  getAndUpdate (multiply by 3):");
        var previous = value.getAndUpdate(v -> v * 3);
        System.out.println("  Previous: " + previous + ", Current: " + value.get());

        System.out.println("\n  updateAndGet (add 8):");
        var updated = value.updateAndGet(v -> v + 8);
        System.out.println("  Updated value: " + updated);

        System.out.println("\n  accumulateAndGet (max with 200):");
        updated = value.accumulateAndGet(200, Math::max);
        System.out.println("  After max(current, 200): " + updated);

        System.out.println("\n  accumulateAndGet (max with 100, should stay 200):");
        updated = value.accumulateAndGet(100, Math::max);
        System.out.println("  After max(current, 100): " + updated);
    }

    static void showConcurrentCasRetry() throws InterruptedException {
        System.out.println("\n=== Concurrent CAS with Contention ===");

        var counter = new AtomicInteger(0);
        var casFailures = new AtomicInteger(0);
        var threadCount = 8;
        var incrementsPerThread = 100_000;

        var threads = new Thread[threadCount];
        for (var i = 0; i < threadCount; i++) {
            threads[i] = Thread.ofPlatform().start(() -> {
                for (var j = 0; j < incrementsPerThread; j++) {
                    while (true) {
                        var current = counter.get();
                        if (counter.compareAndSet(current, current + 1)) {
                            break;
                        }
                        casFailures.incrementAndGet();
                    }
                }
            });
        }

        for (var t : threads) t.join();

        var expectedValue = threadCount * incrementsPerThread;
        System.out.printf("  Expected value: %,d%n", expectedValue);
        System.out.printf("  Actual value:   %,d%n", counter.get());
        System.out.printf("  CAS failures:   %,d (retries due to contention)%n", casFailures.get());
        System.out.printf("  Failure ratio:  %.2f%% of total attempts%n",
                100.0 * casFailures.get() / (expectedValue + casFailures.get()));
    }

    static void showLockFreeStackCorrectness() throws InterruptedException {
        System.out.println("\n=== Lock-Free Stack Correctness Check ===");

        var stack = new TreiberStack<Integer>();
        var threadCount = 8;
        var itemsPerThread = 50_000;
        var totalItems = threadCount * itemsPerThread;
        var pushSum = new AtomicInteger(0);
        var popSum = new AtomicInteger(0);

        var pushThreads = new Thread[threadCount];
        for (var i = 0; i < threadCount; i++) {
            var offset = i * itemsPerThread;
            pushThreads[i] = Thread.ofPlatform().start(() -> {
                for (var j = 0; j < itemsPerThread; j++) {
                    var val = offset + j;
                    stack.push(val);
                    pushSum.addAndGet(val);
                }
            });
        }
        for (var t : pushThreads) t.join();

        System.out.printf("  Pushed %,d items (sum: %,d)%n", totalItems, pushSum.get());

        var popThreads = new Thread[threadCount];
        for (var i = 0; i < threadCount; i++) {
            popThreads[i] = Thread.ofPlatform().start(() -> {
                while (true) {
                    var val = stack.pop();
                    if (val == null) break;
                    popSum.addAndGet(val);
                }
            });
        }
        for (var t : popThreads) t.join();

        System.out.printf("  Popped items (sum: %,d)%n", popSum.get());
        System.out.printf("  Sums match: %s%n", pushSum.get() == popSum.get());
        System.out.printf("  Stack empty: %s%n", stack.pop() == null);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Lock-Free Stack (Treiber) vs Locked Stack ===");

        var opsPerThread = 200_000;

        for (var round = 0; round < 2; round++) {
            runBenchmarkSuite(4, 4, opsPerThread, "Balanced (4 producers, 4 consumers)");
            runBenchmarkSuite(8, 8, opsPerThread, "High contention (8 producers, 8 consumers)");
            runBenchmarkSuite(1, 1, opsPerThread, "Low contention (1 producer, 1 consumer)");
            runBenchmarkSuite(8, 2, opsPerThread, "Producer-heavy (8 producers, 2 consumers)");
        }

        showCasRetryPattern();
        showConcurrentCasRetry();
        showLockFreeStackCorrectness();
    }
}
