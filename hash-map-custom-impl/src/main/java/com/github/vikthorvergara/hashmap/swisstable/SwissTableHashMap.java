package com.github.vikthorvergara.hashmap.swisstable;

public class SwissTableHashMap<K, V> {

    private static final int GROUP_SIZE = 16;
    private static final int DEFAULT_GROUPS = 4;
    private static final float LOAD_FACTOR = 0.875f;

    private static final byte EMPTY = (byte) 0b1111_1111;
    private static final byte DELETED = (byte) 0b1000_0000;

    private byte[] control;
    private K[] keys;
    private V[] values;
    private int capacity;
    private int numGroups;
    private int size;

    @SuppressWarnings("unchecked")
    public SwissTableHashMap() {
        this.numGroups = DEFAULT_GROUPS;
        this.capacity = numGroups * GROUP_SIZE;
        this.control = new byte[capacity];
        this.keys = (K[]) new Object[capacity];
        this.values = (V[]) new Object[capacity];
        this.size = 0;
        java.util.Arrays.fill(control, EMPTY);
    }

    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (size >= capacity * LOAD_FACTOR) resize();

        long hash = hash(key);
        int h1 = h1(hash);
        byte h2 = h2(hash);

        int groupIndex = h1 % numGroups;
        for (int probe = 0; probe < numGroups; probe++) {
            int groupStart = ((groupIndex + probe) % numGroups) * GROUP_SIZE;

            for (int i = 0; i < GROUP_SIZE; i++) {
                int slot = groupStart + i;
                if (control[slot] == h2 && keys[slot].equals(key)) {
                    values[slot] = value;
                    return;
                }
            }

            for (int i = 0; i < GROUP_SIZE; i++) {
                int slot = groupStart + i;
                if (control[slot] == EMPTY || control[slot] == DELETED) {
                    control[slot] = h2;
                    keys[slot] = key;
                    values[slot] = value;
                    size++;
                    return;
                }
            }
        }
    }

    public V get(K key) {
        if (key == null) return null;

        long hash = hash(key);
        int h1 = h1(hash);
        byte h2 = h2(hash);

        int groupIndex = h1 % numGroups;
        for (int probe = 0; probe < numGroups; probe++) {
            int groupStart = ((groupIndex + probe) % numGroups) * GROUP_SIZE;
            boolean hasEmpty = false;

            for (int i = 0; i < GROUP_SIZE; i++) {
                int slot = groupStart + i;
                if (control[slot] == h2 && keys[slot].equals(key)) {
                    return values[slot];
                }
                if (control[slot] == EMPTY) hasEmpty = true;
            }

            if (hasEmpty) return null;
        }

        return null;
    }

    public V remove(K key) {
        if (key == null) return null;

        long hash = hash(key);
        int h1 = h1(hash);
        byte h2 = h2(hash);

        int groupIndex = h1 % numGroups;
        for (int probe = 0; probe < numGroups; probe++) {
            int groupStart = ((groupIndex + probe) % numGroups) * GROUP_SIZE;
            boolean hasEmpty = false;

            for (int i = 0; i < GROUP_SIZE; i++) {
                int slot = groupStart + i;
                if (control[slot] == h2 && keys[slot].equals(key)) {
                    V oldValue = values[slot];
                    control[slot] = DELETED;
                    keys[slot] = null;
                    values[slot] = null;
                    size--;
                    return oldValue;
                }
                if (control[slot] == EMPTY) hasEmpty = true;
            }

            if (hasEmpty) return null;
        }

        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        byte[] oldControl = control;
        K[] oldKeys = keys;
        V[] oldValues = values;
        int oldCapacity = capacity;

        numGroups *= 2;
        capacity = numGroups * GROUP_SIZE;
        control = new byte[capacity];
        keys = (K[]) new Object[capacity];
        values = (V[]) new Object[capacity];
        java.util.Arrays.fill(control, EMPTY);
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldControl[i] != EMPTY && oldControl[i] != DELETED) {
                put(oldKeys[i], oldValues[i]);
            }
        }
    }

    private long hash(K key) {
        long h = key.hashCode();
        h = h * 0x9E3779B97F4A7C15L;
        h ^= (h >>> 30);
        return h;
    }

    private int h1(long hash) {
        return (int) ((hash >>> 7) & 0x7FFFFFFF);
    }

    private byte h2(long hash) {
        byte b = (byte) (hash & 0x7F);
        if (b == (byte) 0x7F || b < 0) b = 0;
        return b;
    }

    public static void main(String[] args) {
        var map = new SwissTableHashMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        System.out.println("Swiss Table HashMap");
        System.out.println("get(one) = " + map.get("one"));
        System.out.println("get(two) = " + map.get("two"));
        System.out.println("get(three) = " + map.get("three"));
        System.out.println("size = " + map.size());
        map.remove("two");
        System.out.println("After removing 'two': get(two) = " + map.get("two"));
        System.out.println("size = " + map.size());
    }
}
