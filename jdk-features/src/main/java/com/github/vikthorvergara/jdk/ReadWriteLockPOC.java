package com.github.vikthorvergara.jdk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class ReadWriteLockPOC {

    interface ConcurrentCache<K, V> {
        V get(K key);
        void put(K key, V value);
        String name();
    }

    static class ReadWriteLockCache<K, V> implements ConcurrentCache<K, V> {
        private final Map<K, V> map = new HashMap<>();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        @Override
        public V get(K key) {
            lock.readLock().lock();
            try {
                return map.get(key);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public void put(K key, V value) {
            lock.writeLock().lock();
            try {
                map.put(key, value);
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public String name() {
            return "ReadWriteLockCache";
        }
    }

    static class StampedLockCache<K, V> implements ConcurrentCache<K, V> {
        private final Map<K, V> map = new HashMap<>();
        private final StampedLock lock = new StampedLock();

        @Override
        public V get(K key) {
            var stamp = lock.tryOptimisticRead();
            var value = map.get(key);
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    value = map.get(key);
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return value;
        }

        @Override
        public void put(K key, V value) {
            var stamp = lock.writeLock();
            try {
                map.put(key, value);
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        @Override
        public String name() {
            return "StampedLockCache (optimistic)";
        }
    }

    static class SynchronizedCache<K, V> implements ConcurrentCache<K, V> {
        private final Map<K, V> map = new HashMap<>();

        @Override
        public synchronized V get(K key) {
            return map.get(key);
        }

        @Override
        public synchronized void put(K key, V value) {
            map.put(key, value);
        }

        @Override
        public String name() {
            return "SynchronizedCache";
        }
    }

    record BenchmarkConfig(int readerThreads, int writerThreads, int opsPerThread, String label) {}

    record BenchmarkResult(String cacheName, String scenario, long elapsedMs, long readOps, long writeOps, double opsPerMs) {}

    static BenchmarkResult benchmarkCache(ConcurrentCache<Integer, String> cache, BenchmarkConfig config) throws InterruptedException {
        var keySpace = 1000;
        for (var i = 0; i < keySpace; i++) {
            cache.put(i, "initial-" + i);
        }

        var readOps = new AtomicLong();
        var writeOps = new AtomicLong();
        var latch = new CountDownLatch(1);

        var readers = new Thread[config.readerThreads()];
        for (var i = 0; i < readers.length; i++) {
            readers[i] = Thread.ofPlatform().start(() -> {
                try { latch.await(); } catch (InterruptedException e) { return; }
                var rng = new java.util.Random();
                for (var j = 0; j < config.opsPerThread(); j++) {
                    cache.get(rng.nextInt(keySpace));
                    readOps.incrementAndGet();
                }
            });
        }

        var writers = new Thread[config.writerThreads()];
        for (var i = 0; i < writers.length; i++) {
            writers[i] = Thread.ofPlatform().start(() -> {
                try { latch.await(); } catch (InterruptedException e) { return; }
                var rng = new java.util.Random();
                for (var j = 0; j < config.opsPerThread(); j++) {
                    var key = rng.nextInt(keySpace);
                    cache.put(key, "updated-" + key + "-" + j);
                    writeOps.incrementAndGet();
                }
            });
        }

        var start = System.nanoTime();
        latch.countDown();
        for (var t : readers) t.join();
        for (var t : writers) t.join();
        var elapsed = (System.nanoTime() - start) / 1_000_000;

        var totalOps = readOps.get() + writeOps.get();
        var opsPerMs = elapsed > 0 ? (double) totalOps / elapsed : totalOps;

        return new BenchmarkResult(cache.name(), config.label(), elapsed, readOps.get(), writeOps.get(), opsPerMs);
    }

    static void printResult(BenchmarkResult r) {
        System.out.printf("  %-32s | %s | reads: %,10d | writes: %,10d | time: %5d ms | throughput: %,.0f ops/ms%n",
                r.cacheName(), r.scenario(), r.readOps(), r.writeOps(), r.elapsedMs(), r.opsPerMs());
    }

    static void runBenchmarkSuite(BenchmarkConfig config) throws InterruptedException {
        System.out.printf("%n--- %s ---%n", config.label());

        ConcurrentCache<Integer, String>[] caches = new ConcurrentCache[]{
                new SynchronizedCache<>(),
                new ReadWriteLockCache<>(),
                new StampedLockCache<>()
        };

        for (var cache : caches) {
            var result = benchmarkCache(cache, config);
            printResult(result);
        }
    }

    static void showOptimisticReadPattern() {
        System.out.println("\n=== StampedLock Optimistic Read Pattern (step by step) ===");

        var lock = new StampedLock();
        var x = new double[]{1.0};
        var y = new double[]{2.0};

        var stamp = lock.tryOptimisticRead();
        System.out.println("  1. tryOptimisticRead() -> stamp: " + stamp);

        var currentX = x[0];
        var currentY = y[0];
        System.out.printf("  2. Read values without locking: x=%.1f, y=%.1f%n", currentX, currentY);

        var valid = lock.validate(stamp);
        System.out.println("  3. validate(stamp) -> " + valid + " (no concurrent write happened)");

        if (!valid) {
            stamp = lock.readLock();
            try {
                currentX = x[0];
                currentY = y[0];
                System.out.println("  4. Fallback: acquired read lock, re-read values");
            } finally {
                lock.unlockRead(stamp);
            }
        } else {
            System.out.println("  4. Optimistic read succeeded, no lock needed");
        }

        var writeStamp = lock.writeLock();
        x[0] = 10.0;
        y[0] = 20.0;
        lock.unlockWrite(writeStamp);
        System.out.printf("  5. Write happened: x=%.1f, y=%.1f%n", x[0], y[0]);

        stamp = lock.tryOptimisticRead();
        currentX = x[0];
        currentY = y[0];

        var writeStamp2 = lock.tryWriteLock();
        if (writeStamp2 != 0) {
            x[0] = 100.0;
            lock.unlockWrite(writeStamp2);
        }

        valid = lock.validate(stamp);
        System.out.println("  6. After concurrent write: validate(stamp) -> " + valid + " (write invalidated our read)");

        if (!valid) {
            stamp = lock.readLock();
            try {
                currentX = x[0];
                currentY = y[0];
                System.out.printf("  7. Fallback read lock: x=%.1f, y=%.1f%n", currentX, currentY);
            } finally {
                lock.unlockRead(stamp);
            }
        }
    }

    static void showLockDowngrade() {
        System.out.println("\n=== StampedLock Lock Downgrade Pattern ===");

        var lock = new StampedLock();
        var data = new String[]{"original"};

        System.out.println("  Starting with write lock...");
        var writeStamp = lock.writeLock();
        System.out.println("  Write lock acquired (stamp: " + writeStamp + ")");

        data[0] = "modified-by-writer";
        System.out.println("  Data updated to: " + data[0]);

        System.out.println("  Converting write lock -> read lock (downgrade)...");
        var readStamp = lock.tryConvertToReadLock(writeStamp);

        if (readStamp != 0) {
            try {
                System.out.println("  Downgrade successful (new stamp: " + readStamp + ")");
                System.out.println("  Reading data under read lock: " + data[0]);
                System.out.println("  Other readers can now access concurrently");
            } finally {
                lock.unlockRead(readStamp);
                System.out.println("  Read lock released");
            }
        } else {
            lock.unlockWrite(writeStamp);
            System.out.println("  Downgrade failed, released write lock");
        }

        System.out.println("\n  Lock upgrade pattern (read -> write)...");
        var rStamp = lock.readLock();
        System.out.println("  Read lock acquired (stamp: " + rStamp + ")");
        var currentData = data[0];
        System.out.println("  Read data: " + currentData);

        System.out.println("  Attempting upgrade to write lock...");
        var wStamp = lock.tryConvertToWriteLock(rStamp);
        if (wStamp != 0) {
            try {
                data[0] = "modified-after-upgrade";
                System.out.println("  Upgrade successful! Updated data to: " + data[0]);
            } finally {
                lock.unlockWrite(wStamp);
            }
        } else {
            lock.unlockRead(rStamp);
            System.out.println("  Upgrade failed (contention), released read lock");
            wStamp = lock.writeLock();
            try {
                data[0] = "modified-after-manual-escalation";
                System.out.println("  Manually acquired write lock, updated data to: " + data[0]);
            } finally {
                lock.unlockWrite(wStamp);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Cache: ReadWriteLock vs StampedLock ===");

        var opsPerThread = 200_000;

        for (var round = 0; round < 2; round++) {
            runBenchmarkSuite(new BenchmarkConfig(8, 1, opsPerThread, "Read-heavy (8 readers, 1 writer)"));
            runBenchmarkSuite(new BenchmarkConfig(4, 4, opsPerThread, "Balanced (4 readers, 4 writers)"));
            runBenchmarkSuite(new BenchmarkConfig(1, 8, opsPerThread, "Write-heavy (1 reader, 8 writers)"));
            runBenchmarkSuite(new BenchmarkConfig(16, 2, opsPerThread, "Very read-heavy (16 readers, 2 writers)"));
        }

        showOptimisticReadPattern();
        showLockDowngrade();
    }
}
