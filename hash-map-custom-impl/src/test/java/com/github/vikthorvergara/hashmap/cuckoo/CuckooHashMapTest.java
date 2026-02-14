package com.github.vikthorvergara.hashmap.cuckoo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CuckooHashMapTest {

    private CuckooHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new CuckooHashMap<>();
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
    void resizeOnHighLoad() {
        for (int i = 0; i < 100; i++) {
            map.put("key" + i, i);
        }

        assertEquals(100, map.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, map.get("key" + i));
        }
    }

    @Test
    void cycleTriggersRehash() {
        for (int i = 0; i < 50; i++) {
            map.put("item" + i, i);
        }

        assertEquals(50, map.size());
        for (int i = 0; i < 50; i++) {
            assertEquals(i, map.get("item" + i));
        }
    }

    @Test
    void removeNullKeyReturnsNull() {
        assertNull(map.remove(null));
    }

    @Test
    void containsKeyNullReturnsFalse() {
        assertFalse(map.containsKey(null));
    }

    @Test
    void overwriteInTable2() {
        for (int i = 0; i < 30; i++) {
            map.put("k" + i, i);
        }

        for (int i = 0; i < 30; i++) {
            map.put("k" + i, i * 10);
        }

        assertEquals(30, map.size());
        for (int i = 0; i < 30; i++) {
            assertEquals(i * 10, map.get("k" + i));
        }
    }
}
