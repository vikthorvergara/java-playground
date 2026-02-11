package com.github.vikthorvergara.hashmap.robinhood;

public class RobinHoodHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private K[] keys;
    private V[] values;
    private int[] probeDistances;
    private int capacity;
    private int size;
    private int maxProbeDistance;

    @SuppressWarnings("unchecked")
    public RobinHoodHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.keys = (K[]) new Object[capacity];
        this.values = (V[]) new Object[capacity];
        this.probeDistances = new int[capacity];
        this.size = 0;
        this.maxProbeDistance = 0;
        java.util.Arrays.fill(probeDistances, -1);
    }

    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (size >= capacity * LOAD_FACTOR) resize();
        insertEntry(key, value);
    }

    private void insertEntry(K key, V value) {
        int index = indexFor(hash(key));
        int dist = 0;
        K currentKey = key;
        V currentValue = value;

        while (true) {
            if (probeDistances[index] == -1) {
                keys[index] = currentKey;
                values[index] = currentValue;
                probeDistances[index] = dist;
                size++;
                maxProbeDistance = Math.max(maxProbeDistance, dist);
                return;
            }

            if (probeDistances[index] != -1 && keys[index].equals(currentKey)) {
                values[index] = currentValue;
                return;
            }

            if (probeDistances[index] < dist) {
                K tempKey = keys[index];
                V tempValue = values[index];
                int tempDist = probeDistances[index];

                keys[index] = currentKey;
                values[index] = currentValue;
                probeDistances[index] = dist;
                maxProbeDistance = Math.max(maxProbeDistance, dist);

                currentKey = tempKey;
                currentValue = tempValue;
                dist = tempDist;
            }

            index = (index + 1) % capacity;
            dist++;
        }
    }

    public V get(K key) {
        if (key == null) return null;
        int index = indexFor(hash(key));

        for (int dist = 0; dist <= maxProbeDistance; dist++) {
            if (probeDistances[index] == -1) return null;
            if (keys[index].equals(key)) return values[index];
            index = (index + 1) % capacity;
        }

        return null;
    }

    public V remove(K key) {
        if (key == null) return null;
        int index = indexFor(hash(key));

        for (int dist = 0; dist <= maxProbeDistance; dist++) {
            if (probeDistances[index] == -1) return null;
            if (keys[index].equals(key)) {
                V oldValue = values[index];
                backwardShiftDelete(index);
                size--;
                return oldValue;
            }
            index = (index + 1) % capacity;
        }

        return null;
    }

    private void backwardShiftDelete(int index) {
        int next = (index + 1) % capacity;
        while (probeDistances[next] > 0) {
            keys[index] = keys[next];
            values[index] = values[next];
            probeDistances[index] = probeDistances[next] - 1;
            index = next;
            next = (next + 1) % capacity;
        }
        keys[index] = null;
        values[index] = null;
        probeDistances[index] = -1;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        K[] oldKeys = keys;
        V[] oldValues = values;
        int[] oldDistances = probeDistances;
        int oldCapacity = capacity;

        capacity *= 2;
        keys = (K[]) new Object[capacity];
        values = (V[]) new Object[capacity];
        probeDistances = new int[capacity];
        java.util.Arrays.fill(probeDistances, -1);
        size = 0;
        maxProbeDistance = 0;

        for (int i = 0; i < oldCapacity; i++) {
            if (oldDistances[i] != -1) {
                insertEntry(oldKeys[i], oldValues[i]);
            }
        }
    }

    private int hash(K key) {
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private int indexFor(int hash) {
        return (hash & 0x7FFFFFFF) % capacity;
    }

    public static void main(String[] args) {
        var map = new RobinHoodHashMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        System.out.println("Robin Hood HashMap");
        System.out.println("get(one) = " + map.get("one"));
        System.out.println("get(two) = " + map.get("two"));
        System.out.println("get(three) = " + map.get("three"));
        System.out.println("size = " + map.size());
        map.remove("two");
        System.out.println("After removing 'two': get(two) = " + map.get("two"));
        System.out.println("size = " + map.size());
    }
}
