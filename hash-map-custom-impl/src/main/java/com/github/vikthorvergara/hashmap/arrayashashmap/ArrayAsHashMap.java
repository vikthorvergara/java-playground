package com.github.vikthorvergara.hashmap.arrayashashmap;

public class ArrayAsHashMap {

    Object[][] hashArray = new Object[0][0];

    public void put(String key, Object value) {
        int index = key.hashCode();
        Object[] array = {key, value};
        hashArray[index] = array;
    }

    public Object get(String key) {
        int index = key.hashCode();
        var array = hashArray[index];
        return array[1];
    }

    static void main() {
        var hashMap = new ArrayAsHashMap();
        System.out.println();
        hashMap.put("hello", "world");
        hashMap.put("hi", "vikthor");

        hashMap.get("hello");

        hashMap.get("hi");
        hashMap.get("123");
    }
}
