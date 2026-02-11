package com.github.vikthorvergara.hashmap.robinhood;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RobinHoodHashMapTest {

    private RobinHoodHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new RobinHoodHashMap<>();
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
    void collisionHandling() {
        var m = new RobinHoodHashMap<CollisionKey, String>();
        m.put(new CollisionKey("a", 1), "val_a");
        m.put(new CollisionKey("b", 1), "val_b");
        m.put(new CollisionKey("c", 1), "val_c");

        assertEquals("val_a", m.get(new CollisionKey("a", 1)));
        assertEquals("val_b", m.get(new CollisionKey("b", 1)));
        assertEquals("val_c", m.get(new CollisionKey("c", 1)));
    }

    @Test
    void removeWithBackwardShift() {
        var m = new RobinHoodHashMap<CollisionKey, String>();
        m.put(new CollisionKey("a", 5), "val_a");
        m.put(new CollisionKey("b", 5), "val_b");
        m.put(new CollisionKey("c", 5), "val_c");

        m.remove(new CollisionKey("a", 5));

        assertNull(m.get(new CollisionKey("a", 5)));
        assertEquals("val_b", m.get(new CollisionKey("b", 5)));
        assertEquals("val_c", m.get(new CollisionKey("c", 5)));
        assertEquals(2, m.size());
    }

    @Test
    void removeNullKeyReturnsNull() {
        assertNull(map.remove(null));
    }

    record CollisionKey(String value, int forcedHash) {
        @Override
        public int hashCode() {
            return forcedHash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CollisionKey that)) return false;
            return value.equals(that.value) && forcedHash == that.forcedHash;
        }
    }
}
