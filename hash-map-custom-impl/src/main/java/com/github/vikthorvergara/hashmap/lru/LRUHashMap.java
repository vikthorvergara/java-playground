package com.github.vikthorvergara.hashmap.lru;

public class LRUHashMap<K, V> {

    private static final int DEFAULT_TABLE_CAPACITY = 32;

    private final int maxCapacity;
    private int size;

    private Node<K, V>[] table;
    private Node<K, V> head;
    private Node<K, V> tail;

    static class Node<K, V> {
        final K key;
        V value;
        final int hash;
        Node<K, V> next;
        Node<K, V> prev;
        Node<K, V> linkPrev;
        Node<K, V> linkNext;

        Node(K key, V value, int hash) {
            this.key = key;
            this.value = value;
            this.hash = hash;
        }
    }

    @SuppressWarnings("unchecked")
    public LRUHashMap(int maxCapacity) {
        if (maxCapacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.maxCapacity = maxCapacity;
        int tableSize = Math.max(DEFAULT_TABLE_CAPACITY, Integer.highestOneBit(maxCapacity * 2 - 1));
        this.table = new Node[tableSize];
        this.size = 0;
    }

    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        int hash = hash(key);
        int index = indexFor(hash);

        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && node.key.equals(key)) {
                node.value = value;
                moveToHead(node);
                return;
            }
        }

        if (size >= maxCapacity) {
            evictLRU();
        }

        Node<K, V> newNode = new Node<>(key, value, hash);
        newNode.next = table[index];
        table[index] = newNode;

        addToHead(newNode);
        size++;
    }

    public V get(K key) {
        if (key == null) return null;

        int hash = hash(key);
        int index = indexFor(hash);

        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && node.key.equals(key)) {
                moveToHead(node);
                return node.value;
            }
        }

        return null;
    }

    public V remove(K key) {
        if (key == null) return null;

        int hash = hash(key);
        int index = indexFor(hash);

        Node<K, V> prev = null;
        for (Node<K, V> node = table[index]; node != null; prev = node, node = node.next) {
            if (node.hash == hash && node.key.equals(key)) {
                if (prev == null) {
                    table[index] = node.next;
                } else {
                    prev.next = node.next;
                }
                removeFromList(node);
                size--;
                return node.value;
            }
        }

        return null;
    }

    public boolean containsKey(K key) {
        if (key == null) return false;

        int hash = hash(key);
        int index = indexFor(hash);

        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && node.key.equals(key)) {
                return true;
            }
        }

        return false;
    }

    public int size() {
        return size;
    }

    public int maxCapacity() {
        return maxCapacity;
    }

    K lruKey() {
        return tail != null ? tail.key : null;
    }

    K mruKey() {
        return head != null ? head.key : null;
    }

    private void addToHead(Node<K, V> node) {
        node.linkPrev = null;
        node.linkNext = head;
        if (head != null) {
            head.linkPrev = node;
        }
        head = node;
        if (tail == null) {
            tail = node;
        }
    }

    private void removeFromList(Node<K, V> node) {
        if (node.linkPrev != null) {
            node.linkPrev.linkNext = node.linkNext;
        } else {
            head = node.linkNext;
        }
        if (node.linkNext != null) {
            node.linkNext.linkPrev = node.linkPrev;
        } else {
            tail = node.linkPrev;
        }
        node.linkPrev = null;
        node.linkNext = null;
    }

    private void moveToHead(Node<K, V> node) {
        if (node == head) return;
        removeFromList(node);
        addToHead(node);
    }

    private void evictLRU() {
        if (tail == null) return;
        Node<K, V> victim = tail;

        int index = indexFor(victim.hash);
        Node<K, V> prev = null;
        for (Node<K, V> node = table[index]; node != null; prev = node, node = node.next) {
            if (node == victim) {
                if (prev == null) {
                    table[index] = node.next;
                } else {
                    prev.next = node.next;
                }
                break;
            }
        }

        removeFromList(victim);
        size--;
    }

    private int hash(K key) {
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private int indexFor(int hash) {
        return (hash & 0x7FFFFFFF) % table.length;
    }

    public static void main(String[] args) {
        var map = new LRUHashMap<String, Integer>(3);
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        System.out.println("LRU HashMap (capacity=3)");
        System.out.println("get(one) = " + map.get("one"));
        System.out.println("get(two) = " + map.get("two"));
        System.out.println("size = " + map.size());
        map.put("four", 4);
        System.out.println("After adding 'four' (should evict LRU):");
        System.out.println("get(three) = " + map.get("three"));
        System.out.println("get(four) = " + map.get("four"));
        System.out.println("size = " + map.size());
    }
}
