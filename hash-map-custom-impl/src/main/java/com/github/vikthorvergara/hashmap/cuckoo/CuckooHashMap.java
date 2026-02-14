package com.github.vikthorvergara.hashmap.cuckoo;

public class CuckooHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.5f;
    private static final int MAX_EVICTIONS = 500;

    private K[] keys1;
    private V[] values1;
    private K[] keys2;
    private V[] values2;
    private int capacity;
    private int size;
    private int hashSeed;

    @SuppressWarnings("unchecked")
    public CuckooHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.keys1 = (K[]) new Object[capacity];
        this.values1 = (V[]) new Object[capacity];
        this.keys2 = (K[]) new Object[capacity];
        this.values2 = (V[]) new Object[capacity];
        this.size = 0;
        this.hashSeed = 31;
    }

    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        int idx1 = index1(key);
        if (keys1[idx1] != null && keys1[idx1].equals(key)) {
            values1[idx1] = value;
            return;
        }

        int idx2 = index2(key);
        if (keys2[idx2] != null && keys2[idx2].equals(key)) {
            values2[idx2] = value;
            return;
        }

        if (size >= capacity * LOAD_FACTOR) {
            resize();
        }

        insertWithEviction(key, value);
    }

    private void insertWithEviction(K key, V value) {
        K currentKey = key;
        V currentValue = value;

        for (int i = 0; i < MAX_EVICTIONS; i++) {
            int idx1 = index1(currentKey);
            if (keys1[idx1] == null) {
                keys1[idx1] = currentKey;
                values1[idx1] = currentValue;
                size++;
                return;
            }

            K evictedKey = keys1[idx1];
            V evictedValue = values1[idx1];
            keys1[idx1] = currentKey;
            values1[idx1] = currentValue;
            currentKey = evictedKey;
            currentValue = evictedValue;

            int idx2 = index2(currentKey);
            if (keys2[idx2] == null) {
                keys2[idx2] = currentKey;
                values2[idx2] = currentValue;
                size++;
                return;
            }

            evictedKey = keys2[idx2];
            evictedValue = values2[idx2];
            keys2[idx2] = currentKey;
            values2[idx2] = currentValue;
            currentKey = evictedKey;
            currentValue = evictedValue;
        }

        rehash();
        insertWithEviction(currentKey, currentValue);
    }

    public V get(K key) {
        if (key == null) return null;

        int idx1 = index1(key);
        if (keys1[idx1] != null && keys1[idx1].equals(key)) {
            return values1[idx1];
        }

        int idx2 = index2(key);
        if (keys2[idx2] != null && keys2[idx2].equals(key)) {
            return values2[idx2];
        }

        return null;
    }

    public V remove(K key) {
        if (key == null) return null;

        int idx1 = index1(key);
        if (keys1[idx1] != null && keys1[idx1].equals(key)) {
            V value = values1[idx1];
            keys1[idx1] = null;
            values1[idx1] = null;
            size--;
            return value;
        }

        int idx2 = index2(key);
        if (keys2[idx2] != null && keys2[idx2].equals(key)) {
            V value = values2[idx2];
            keys2[idx2] = null;
            values2[idx2] = null;
            size--;
            return value;
        }

        return null;
    }

    public boolean containsKey(K key) {
        if (key == null) return false;

        int idx1 = index1(key);
        if (keys1[idx1] != null && keys1[idx1].equals(key)) return true;

        int idx2 = index2(key);
        return keys2[idx2] != null && keys2[idx2].equals(key);
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        K[] oldKeys1 = keys1;
        V[] oldValues1 = values1;
        K[] oldKeys2 = keys2;
        V[] oldValues2 = values2;
        int oldCapacity = capacity;

        capacity *= 2;
        hashSeed = hashSeed * 37 + 7;
        keys1 = (K[]) new Object[capacity];
        values1 = (V[]) new Object[capacity];
        keys2 = (K[]) new Object[capacity];
        values2 = (V[]) new Object[capacity];
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldKeys1[i] != null) insertWithEviction(oldKeys1[i], oldValues1[i]);
            if (oldKeys2[i] != null) insertWithEviction(oldKeys2[i], oldValues2[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        K[] oldKeys1 = keys1;
        V[] oldValues1 = values1;
        K[] oldKeys2 = keys2;
        V[] oldValues2 = values2;
        int oldCapacity = capacity;

        capacity *= 2;
        keys1 = (K[]) new Object[capacity];
        values1 = (V[]) new Object[capacity];
        keys2 = (K[]) new Object[capacity];
        values2 = (V[]) new Object[capacity];
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldKeys1[i] != null) insertWithEviction(oldKeys1[i], oldValues1[i]);
            if (oldKeys2[i] != null) insertWithEviction(oldKeys2[i], oldValues2[i]);
        }
    }

    private int index1(K key) {
        int h = key.hashCode();
        h ^= (h >>> 16);
        return (h & 0x7FFFFFFF) % capacity;
    }

    private int index2(K key) {
        int h = key.hashCode() * hashSeed;
        h ^= (h >>> 16);
        return (h & 0x7FFFFFFF) % capacity;
    }

    static void main(String[] args) {
        var map = new CuckooHashMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        System.out.println("Cuckoo HashMap");
        System.out.println("get(one) = " + map.get("one"));
        System.out.println("get(two) = " + map.get("two"));
        System.out.println("get(three) = " + map.get("three"));
        System.out.println("size = " + map.size());
        map.remove("two");
        System.out.println("After removing 'two': get(two) = " + map.get("two"));
        System.out.println("size = " + map.size());
    }
}
