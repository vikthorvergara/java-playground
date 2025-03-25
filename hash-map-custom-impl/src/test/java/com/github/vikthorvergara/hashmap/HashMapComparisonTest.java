package com.github.vikthorvergara.hashmap;

import com.github.vikthorvergara.hashmap.lru.LRUHashMap;
import com.github.vikthorvergara.hashmap.offheap.OffHeapHashMap;
import com.github.vikthorvergara.hashmap.robinhood.RobinHoodHashMap;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HashMapComparisonTest {

    private static final int ENTRIES = 1_000_000;
    private static final String[] KEYS = new String[ENTRIES];
    private static final String[] VALS = new String[ENTRIES];

    static {
        for (int i = 0; i < ENTRIES; i++) {
            KEYS[i] = "key" + i;
            VALS[i] = "val" + i;
        }
    }

    @Test
    @Order(1)
    void compareGcPressure() {
        var gc = ManagementFactory.getGarbageCollectorMXBeans();
        Runtime rt = Runtime.getRuntime();

        System.out.println("=== GC Pressure Comparison ===");
        System.out.println(String.format("%,d", ENTRIES) + " entries. Heap: -Xmx256m -Xms128m. Keys/values pre-allocated.");
        System.out.println();

        long[] jdkStats = measureRetained(gc, rt, () -> {
            var map = new HashMap<String, String>();
            for (int i = 0; i < ENTRIES; i++) map.put(KEYS[i], VALS[i]);
            return map;
        });

        long[] robinStats = measureRetained(gc, rt, () -> {
            var map = new RobinHoodHashMap<String, String>();
            for (int i = 0; i < ENTRIES; i++) map.put(KEYS[i], VALS[i]);
            return map;
        });

        long[] lruStats = measureRetained(gc, rt, () -> {
            var map = new LRUHashMap<String, String>(ENTRIES);
            for (int i = 0; i < ENTRIES; i++) map.put(KEYS[i], VALS[i]);
            return map;
        });

        long[] ohStats;
        forceGc(rt);
        long ohBaseline = usedHeapMB(rt);
        long ohGcBefore = gcCount(gc);
        long ohTimeBefore = gcTime(gc);
        var offHeap = new OffHeapHashMap();
        for (int i = 0; i < ENTRIES; i++) offHeap.put(KEYS[i], VALS[i]);
        forceGc(rt);
        long ohRetained = usedHeapMB(rt) - ohBaseline;
        long ohGcRuns = gcCount(gc) - ohGcBefore;
        long ohGcTime = gcTime(gc) - ohTimeBefore;
        ohStats = new long[]{ohRetained, ohGcRuns, ohGcTime};
        offHeap.close();

        System.out.printf("%-18s | %-12s | %-12s | %-12s | %s%n",
                "", "HashMap", "Robin Hood", "LRU", "Off-Heap");
        System.out.println("-".repeat(75));
        System.out.printf("%-18s | %-12s | %-12s | %-12s | %s%n",
                "Retained heap", jdkStats[0] + " MB", robinStats[0] + " MB", lruStats[0] + " MB", ohStats[0] + " MB");
        System.out.printf("%-18s | %-12s | %-12s | %-12s | %s%n",
                "GC objects", "1M Nodes", "3 arrays", "1M Nodes", "0 (native)");
        System.out.printf("%-18s | %-12s | %-12s | %-12s | %s%n",
                "GC runs", jdkStats[1], robinStats[1], lruStats[1], ohStats[1]);
        System.out.printf("%-18s | %-12s | %-12s | %-12s | %s%n",
                "GC time", jdkStats[2] + " ms", robinStats[2] + " ms", lruStats[2] + " ms", ohStats[2] + " ms");
        System.out.printf("%-18s | %-12s | %-12s | %-12s | %s%n",
                "Data location", "Heap", "Heap", "Heap", "Native mem");
    }

    @Test
    @Order(2)
    void compareEvictionBehavior() {
        int targetCapacity = 5;
        int totalInserts = 10;

        var jdk = new HashMap<String, String>();
        var robin = new RobinHoodHashMap<String, String>();
        var lru = new LRUHashMap<String, String>(targetCapacity);
        var offHeap = new OffHeapHashMap();

        System.out.println("=== Eviction Behavior Comparison ===");
        System.out.println("Target capacity: " + targetCapacity + ". Inserting " + totalInserts + " entries (a through j).");
        System.out.println();
        System.out.printf("%-12s | %-12s | %-12s | %-12s | %s%n",
                "After put", "HashMap", "Robin Hood", "LRU", "Off-Heap");
        System.out.println("-".repeat(70));

        String[] keys = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        for (int i = 0; i < totalInserts; i++) {
            jdk.put(keys[i], keys[i]);
            robin.put(keys[i], keys[i]);
            lru.put(keys[i], keys[i]);
            offHeap.put(keys[i], keys[i]);

            System.out.printf("put(%-2s)      | size=%-7d | size=%-7d | size=%-7d | size=%d%n",
                    keys[i], jdk.size(), robin.size(), lru.size(), offHeap.size());
        }

        offHeap.close();
    }

    @SuppressWarnings("unused")
    private long[] measureRetained(java.util.List<GarbageCollectorMXBean> gc, Runtime rt,
                                   java.util.function.Supplier<Object> factory) {
        forceGc(rt);
        long baseline = usedHeapMB(rt);
        long gcBefore = gcCount(gc);
        long gcTimeBefore = gcTime(gc);

        Object ref = factory.get();

        forceGc(rt);
        long retained = usedHeapMB(rt) - baseline;
        long gcRuns = gcCount(gc) - gcBefore;
        long gcTimeTotal = gcTime(gc) - gcTimeBefore;

        if (ref.hashCode() == Integer.MIN_VALUE) System.out.print("");

        ref = null;
        forceGc(rt);

        return new long[]{retained, gcRuns, gcTimeTotal};
    }

    private void forceGc(Runtime rt) {
        for (int i = 0; i < 3; i++) rt.gc();
    }

    private long usedHeapMB(Runtime rt) {
        return (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
    }

    private long gcCount(java.util.List<GarbageCollectorMXBean> gc) {
        return gc.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
    }

    private long gcTime(java.util.List<GarbageCollectorMXBean> gc) {
        return gc.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();
    }

    private String survivingKeys(LRUHashMap<String, String> map, String[] keys) {
        var sb = new StringBuilder();
        for (String key : keys) {
            if (map.containsKey(key)) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(key);
            }
        }
        return sb.toString();
    }

    private String evictedKeys(LRUHashMap<String, String> map, String[] keys) {
        var sb = new StringBuilder();
        for (String key : keys) {
            if (!map.containsKey(key)) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(key);
            }
        }
        return sb.toString();
    }
}
