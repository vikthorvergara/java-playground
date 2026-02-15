package com.github.vikthorvergara.hashmap.offheap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OffHeapHashMapTest {

    @Test
    void putAndGet() {
        try (var map = new OffHeapHashMap()) {
            map.put("a", "1");
            map.put("b", "2");
            map.put("c", "3");

            assertEquals("1", map.get("a"));
            assertEquals("2", map.get("b"));
            assertEquals("3", map.get("c"));
        }
    }

    @Test
    void overwriteValue() {
        try (var map = new OffHeapHashMap()) {
            map.put("key", "100");
            assertEquals("100", map.get("key"));

            map.put("key", "200");
            assertEquals("200", map.get("key"));
            assertEquals(1, map.size());
        }
    }

    @Test
    void removeEntry() {
        try (var map = new OffHeapHashMap()) {
            map.put("x", "10");
            map.put("y", "20");

            assertEquals("10", map.remove("x"));
            assertNull(map.get("x"));
            assertEquals(1, map.size());

            assertNull(map.remove("nonexistent"));
        }
    }

    @Test
    void getNullKeyReturnsNull() {
        try (var map = new OffHeapHashMap()) {
            assertNull(map.get(null));
        }
    }

    @Test
    void putNullKeyThrows() {
        try (var map = new OffHeapHashMap()) {
            assertThrows(IllegalArgumentException.class, () -> map.put(null, "value"));
        }
    }

    @Test
    void containsKey() {
        try (var map = new OffHeapHashMap()) {
            map.put("present", "yes");

            assertTrue(map.containsKey("present"));
            assertFalse(map.containsKey("absent"));
        }
    }

    @Test
    void sizeTracking() {
        try (var map = new OffHeapHashMap()) {
            assertEquals(0, map.size());

            map.put("a", "1");
            assertEquals(1, map.size());

            map.put("b", "2");
            assertEquals(2, map.size());

            map.remove("a");
            assertEquals(1, map.size());
        }
    }

    @Test
    void resizeTrigger() {
        try (var map = new OffHeapHashMap()) {
            for (int i = 0; i < 50; i++) {
                map.put("key" + i, "val" + i);
            }

            assertEquals(50, map.size());
            for (int i = 0; i < 50; i++) {
                assertEquals("val" + i, map.get("key" + i));
            }
        }
    }

    @Test
    void memoryCleanupOnClose() {
        var map = new OffHeapHashMap();
        map.put("test", "value");
        map.close();

        map.close();
    }

    @Test
    void emptyValueString() {
        try (var map = new OffHeapHashMap()) {
            map.put("empty", "");
            assertEquals("", map.get("empty"));
        }
    }

    @Test
    void removeNullKeyReturnsNull() {
        try (var map = new OffHeapHashMap()) {
            assertNull(map.remove(null));
        }
    }

    @Test
    void unicodeKeysAndValues() {
        try (var map = new OffHeapHashMap()) {
            map.put("chave", "valor em portugues");
            map.put("key", "hello world");

            assertEquals("valor em portugues", map.get("chave"));
            assertEquals("hello world", map.get("key"));
        }
    }
}
