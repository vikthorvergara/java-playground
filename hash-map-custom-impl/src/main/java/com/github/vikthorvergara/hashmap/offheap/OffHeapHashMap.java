package com.github.vikthorvergara.hashmap.offheap;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

public class OffHeapHashMap implements AutoCloseable {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;
    private static final int ENTRY_SIZE = 512;
    private static final int HASH_OFFSET = 0;
    private static final int KEY_LEN_OFFSET = 4;
    private static final int VAL_LEN_OFFSET = 8;
    private static final int OCCUPIED_OFFSET = 12;
    private static final int DATA_OFFSET = 16;
    private static final int MAX_DATA_SIZE = ENTRY_SIZE - DATA_OFFSET;

    private Arena arena;
    private MemorySegment segment;
    private int capacity;
    private int size;

    public OffHeapHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.size = 0;
        this.arena = Arena.ofShared();
        this.segment = arena.allocate((long) capacity * ENTRY_SIZE);
        segment.fill((byte) 0);
    }

    public void put(String key, String value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (size >= capacity * LOAD_FACTOR) resize();

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valBytes = value != null ? value.getBytes(StandardCharsets.UTF_8) : new byte[0];
        if (keyBytes.length + valBytes.length > MAX_DATA_SIZE) {
            throw new IllegalArgumentException("Key + value too large for entry slot");
        }

        int hash = hash(key);
        int index = (hash & 0x7FFFFFFF) % capacity;

        for (int i = 0; i < capacity; i++) {
            int idx = (index + i) % capacity;
            long base = (long) idx * ENTRY_SIZE;

            int occupied = segment.get(ValueLayout.JAVA_INT, base + OCCUPIED_OFFSET);
            if (occupied == 0) {
                writeEntry(base, hash, keyBytes, valBytes);
                size++;
                return;
            }

            int storedHash = segment.get(ValueLayout.JAVA_INT, base + HASH_OFFSET);
            if (storedHash == hash) {
                String storedKey = readKey(base);
                if (storedKey.equals(key)) {
                    writeEntry(base, hash, keyBytes, valBytes);
                    return;
                }
            }
        }
    }

    public String get(String key) {
        if (key == null) return null;
        int hash = hash(key);
        int index = (hash & 0x7FFFFFFF) % capacity;

        for (int i = 0; i < capacity; i++) {
            int idx = (index + i) % capacity;
            long base = (long) idx * ENTRY_SIZE;

            int occupied = segment.get(ValueLayout.JAVA_INT, base + OCCUPIED_OFFSET);
            if (occupied == 0) return null;

            int storedHash = segment.get(ValueLayout.JAVA_INT, base + HASH_OFFSET);
            if (storedHash == hash) {
                String storedKey = readKey(base);
                if (storedKey.equals(key)) {
                    return readValue(base);
                }
            }
        }

        return null;
    }

    public String remove(String key) {
        if (key == null) return null;
        int hash = hash(key);
        int index = (hash & 0x7FFFFFFF) % capacity;

        for (int i = 0; i < capacity; i++) {
            int idx = (index + i) % capacity;
            long base = (long) idx * ENTRY_SIZE;

            int occupied = segment.get(ValueLayout.JAVA_INT, base + OCCUPIED_OFFSET);
            if (occupied == 0) return null;

            int storedHash = segment.get(ValueLayout.JAVA_INT, base + HASH_OFFSET);
            if (storedHash == hash) {
                String storedKey = readKey(base);
                if (storedKey.equals(key)) {
                    String oldValue = readValue(base);
                    clearEntry(base);
                    size--;
                    rehashFrom(idx);
                    return oldValue;
                }
            }
        }

        return null;
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    @Override
    public void close() {
        if (arena != null) {
            arena.close();
            arena = null;
            segment = null;
        }
    }

    private void writeEntry(long base, int hash, byte[] keyBytes, byte[] valBytes) {
        segment.set(ValueLayout.JAVA_INT, base + HASH_OFFSET, hash);
        segment.set(ValueLayout.JAVA_INT, base + KEY_LEN_OFFSET, keyBytes.length);
        segment.set(ValueLayout.JAVA_INT, base + VAL_LEN_OFFSET, valBytes.length);
        segment.set(ValueLayout.JAVA_INT, base + OCCUPIED_OFFSET, 1);
        MemorySegment.copy(MemorySegment.ofArray(keyBytes), 0, segment, base + DATA_OFFSET, keyBytes.length);
        if (valBytes.length > 0) {
            MemorySegment.copy(MemorySegment.ofArray(valBytes), 0, segment, base + DATA_OFFSET + keyBytes.length, valBytes.length);
        }
    }

    private String readKey(long base) {
        int keyLen = segment.get(ValueLayout.JAVA_INT, base + KEY_LEN_OFFSET);
        byte[] buf = new byte[keyLen];
        MemorySegment.copy(segment, base + DATA_OFFSET, MemorySegment.ofArray(buf), 0, keyLen);
        return new String(buf, StandardCharsets.UTF_8);
    }

    private String readValue(long base) {
        int keyLen = segment.get(ValueLayout.JAVA_INT, base + KEY_LEN_OFFSET);
        int valLen = segment.get(ValueLayout.JAVA_INT, base + VAL_LEN_OFFSET);
        if (valLen == 0) return "";
        byte[] buf = new byte[valLen];
        MemorySegment.copy(segment, base + DATA_OFFSET + keyLen, MemorySegment.ofArray(buf), 0, valLen);
        return new String(buf, StandardCharsets.UTF_8);
    }

    private void clearEntry(long base) {
        for (int i = 0; i < ENTRY_SIZE; i++) {
            segment.set(ValueLayout.JAVA_BYTE, base + i, (byte) 0);
        }
    }

    private void rehashFrom(int removedIndex) {
        int idx = (removedIndex + 1) % capacity;
        while (true) {
            long base = (long) idx * ENTRY_SIZE;
            int occupied = segment.get(ValueLayout.JAVA_INT, base + OCCUPIED_OFFSET);
            if (occupied == 0) break;

            int storedHash = segment.get(ValueLayout.JAVA_INT, base + HASH_OFFSET);
            int naturalIndex = (storedHash & 0x7FFFFFFF) % capacity;

            if (needsRelocation(naturalIndex, idx, removedIndex)) {
                long removedBase = (long) removedIndex * ENTRY_SIZE;
                MemorySegment.copy(segment, base, segment, removedBase, ENTRY_SIZE);
                clearEntry(base);
                removedIndex = idx;
            }

            idx = (idx + 1) % capacity;
        }
    }

    private boolean needsRelocation(int naturalIndex, int currentIndex, int emptyIndex) {
        if (currentIndex >= emptyIndex) {
            return naturalIndex <= emptyIndex || naturalIndex > currentIndex;
        }
        return naturalIndex <= emptyIndex && naturalIndex > currentIndex;
    }

    private void resize() {
        int oldCapacity = capacity;
        Arena oldArena = arena;
        MemorySegment oldSegment = segment;

        capacity *= 2;
        arena = Arena.ofShared();
        segment = arena.allocate((long) capacity * ENTRY_SIZE);
        segment.fill((byte) 0);
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            long base = (long) i * ENTRY_SIZE;
            int occupied = oldSegment.get(ValueLayout.JAVA_INT, base + OCCUPIED_OFFSET);
            if (occupied != 0) {
                String key = readKeyFrom(oldSegment, base);
                String value = readValueFrom(oldSegment, base);
                put(key, value);
            }
        }

        oldArena.close();
    }

    private String readKeyFrom(MemorySegment seg, long base) {
        int keyLen = seg.get(ValueLayout.JAVA_INT, base + KEY_LEN_OFFSET);
        byte[] buf = new byte[keyLen];
        MemorySegment.copy(seg, base + DATA_OFFSET, MemorySegment.ofArray(buf), 0, keyLen);
        return new String(buf, StandardCharsets.UTF_8);
    }

    private String readValueFrom(MemorySegment seg, long base) {
        int keyLen = seg.get(ValueLayout.JAVA_INT, base + KEY_LEN_OFFSET);
        int valLen = seg.get(ValueLayout.JAVA_INT, base + VAL_LEN_OFFSET);
        if (valLen == 0) return "";
        byte[] buf = new byte[valLen];
        MemorySegment.copy(seg, base + DATA_OFFSET + keyLen, MemorySegment.ofArray(buf), 0, valLen);
        return new String(buf, StandardCharsets.UTF_8);
    }

    private int hash(String key) {
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    public static void main(String[] args) {
        try (var map = new OffHeapHashMap()) {
            map.put("one", "1");
            map.put("two", "2");
            map.put("three", "3");
            System.out.println("Off-Heap HashMap (Foreign Memory API)");
            System.out.println("get(one) = " + map.get("one"));
            System.out.println("get(two) = " + map.get("two"));
            System.out.println("get(three) = " + map.get("three"));
            System.out.println("size = " + map.size());
            map.remove("two");
            System.out.println("After removing 'two': get(two) = " + map.get("two"));
            System.out.println("size = " + map.size());
        }
    }
}
