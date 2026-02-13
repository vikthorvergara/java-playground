package com.github.vikthorvergara.hashmap.lru;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUHashMapTest {

    private LRUHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new LRUHashMap<>(3);
    }

    @Test
    void putAndGet() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @Test
    void overwriteValue() {
        map.put("key", 100);
        assertEquals(100, map.get("key"));

        map.put("key", 200);
        assertEquals(200, map.get("key"));
        assertEquals(1, map.size());
    }

    @Test
    void removeEntry() {
        map.put("x", 10);
        map.put("y", 20);

        assertEquals(10, map.remove("x"));
        assertNull(map.get("x"));
        assertEquals(1, map.size());

        assertNull(map.remove("nonexistent"));
    }

    @Test
    void getNullKeyReturnsNull() {
        assertNull(map.get(null));
    }

    @Test
    void putNullKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> map.put(null, 1));
    }

    @Test
    void containsKey() {
        map.put("present", 1);

        assertTrue(map.containsKey("present"));
        assertFalse(map.containsKey("absent"));
    }

    @Test
    void sizeTracking() {
        assertEquals(0, map.size());

        map.put("a", 1);
        assertEquals(1, map.size());

        map.put("b", 2);
        assertEquals(2, map.size());

        map.remove("a");
        assertEquals(1, map.size());
    }

    @Test
    void evictsLeastRecentlyUsed() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        map.put("d", 4);

        assertNull(map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
        assertEquals(4, map.get("d"));
        assertEquals(3, map.size());
    }

    @Test
    void getUpdatesAccessOrder() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        map.get("a");

        map.put("d", 4);

        assertNull(map.get("b"));
        assertEquals(1, map.get("a"));
        assertEquals(3, map.get("c"));
        assertEquals(4, map.get("d"));
    }

    @Test
    void putUpdatesAccessOrder() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        map.put("a", 10);

        map.put("d", 4);

        assertNull(map.get("b"));
        assertEquals(10, map.get("a"));
        assertEquals(3, map.get("c"));
        assertEquals(4, map.get("d"));
    }

    @Test
    void evictionChain() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        map.put("e", 5);
        map.put("f", 6);

        assertNull(map.get("a"));
        assertNull(map.get("b"));
        assertNull(map.get("c"));
        assertEquals(4, map.get("d"));
        assertEquals(5, map.get("e"));
        assertEquals(6, map.get("f"));
        assertEquals(3, map.size());
    }

    @Test
    void lruAndMruTracking() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        assertEquals("a", map.lruKey());
        assertEquals("c", map.mruKey());

        map.get("a");

        assertEquals("b", map.lruKey());
        assertEquals("a", map.mruKey());
    }

    @Test
    void removeNullKeyReturnsNull() {
        assertNull(map.remove(null));
    }

    @Test
    void singleCapacity() {
        var singleMap = new LRUHashMap<String, Integer>(1);
        singleMap.put("a", 1);
        assertEquals(1, singleMap.get("a"));

        singleMap.put("b", 2);
        assertNull(singleMap.get("a"));
        assertEquals(2, singleMap.get("b"));
        assertEquals(1, singleMap.size());
    }

    @Test
    void zeroCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LRUHashMap<>(0));
    }

    @Test
    void negativeCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LRUHashMap<>(-1));
    }
}
