package com.github.vikthorvergara.reflection.deepops;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public class DeepOperationsPOC {

    static class Address {
        private String street;
        private String city;

        Address(String street, String city) {
            this.street = street;
            this.city = city;
        }

        @Override
        public String toString() {
            return "Address{street='%s', city='%s'}".formatted(street, city);
        }
    }

    static class Person {
        private String name;
        private int age;
        private Address address;
        private String[] hobbies;

        Person(String name, int age, Address address, String[] hobbies) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.hobbies = hobbies;
        }

        @Override
        public String toString() {
            return "Person{name='%s', age=%d, address=%s, hobbies=%s}"
                    .formatted(name, age, address, Arrays.toString(hobbies));
        }
    }

    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
            boolean.class, false,
            byte.class, (byte) 0,
            char.class, '\0',
            short.class, (short) 0,
            int.class, 0,
            long.class, 0L,
            float.class, 0.0f,
            double.class, 0.0d
    );

    private static Object newInstance(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        var paramTypes = constructor.getParameterTypes();
        var args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = PRIMITIVE_DEFAULTS.get(paramTypes[i]);
        }
        return constructor.newInstance(args);
    }

    public static Object deepCopy(Object obj) throws Exception {
        if (obj == null) return null;

        var clazz = obj.getClass();

        if (clazz.isPrimitive() || obj instanceof String || obj instanceof Number
                || obj instanceof Boolean || obj instanceof Character) {
            return obj;
        }

        if (clazz.isArray()) {
            int length = Array.getLength(obj);
            var componentType = clazz.getComponentType();
            var arrayCopy = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(arrayCopy, i, deepCopy(Array.get(obj, i)));
            }
            return arrayCopy;
        }

        var copy = newInstance(clazz);

        for (var current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                field.setAccessible(true);
                var value = field.get(obj);
                field.set(copy, deepCopy(value));
            }
        }

        return copy;
    }

    public static boolean deepEquals(Object a, Object b) throws Exception {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getClass() != b.getClass()) return false;

        var clazz = a.getClass();

        if (clazz.isPrimitive() || a instanceof String || a instanceof Number
                || a instanceof Boolean || a instanceof Character) {
            return a.equals(b);
        }

        if (clazz.isArray()) {
            int lengthA = Array.getLength(a);
            int lengthB = Array.getLength(b);
            if (lengthA != lengthB) return false;
            for (int i = 0; i < lengthA; i++) {
                if (!deepEquals(Array.get(a, i), Array.get(b, i))) return false;
            }
            return true;
        }

        for (var current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                field.setAccessible(true);
                var valA = field.get(a);
                var valB = field.get(b);
                if (!deepEquals(valA, valB)) return false;
            }
        }

        return true;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Deep Copy and Deep Equals via Reflection ===");

        var vikthor = new Person("Vikthor", 30,
                new Address("123 Main St", "Springfield"),
                new String[]{"coding", "gaming", "reading"});

        System.out.println("\nOriginal: " + vikthor);

        var copy = (Person) deepCopy(vikthor);
        System.out.println("Copy:     " + copy);

        System.out.println("\n=== Reference Equality vs Deep Equality ===");
        System.out.println("vikthor == copy:          " + (vikthor == copy));
        System.out.println("deepEquals(vikthor, copy): " + deepEquals(vikthor, copy));
        System.out.println("address == copy.address:  " + (vikthor.address == copy.address));
        System.out.println("hobbies == copy.hobbies:  " + (vikthor.hobbies == copy.hobbies));

        System.out.println("\n=== Modifying Copy ===");
        Field nameField = Person.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(copy, "Alice");

        Field ageField = Person.class.getDeclaredField("age");
        ageField.setAccessible(true);
        ageField.setInt(copy, 30);

        Field cityField = Address.class.getDeclaredField("city");
        cityField.setAccessible(true);
        cityField.set(copy.address, "Metropolis");

        copy.hobbies[0] = "painting";

        System.out.println("Original after modifying copy: " + vikthor);
        System.out.println("Modified copy:                 " + copy);
        System.out.println("deepEquals(vikthor, copy):      " + deepEquals(vikthor, copy));

        System.out.println("\n=== Comparing Different People ===");
        var alice = new Person("Alice", 30,
                new Address("456 Oak Ave", "Metropolis"),
                new String[]{"reading", "coding"});
        var bob = new Person("Bob", 35,
                new Address("789 Pine Rd", "Gotham"),
                new String[]{"gaming"});

        System.out.println("Alice: " + alice);
        System.out.println("Bob:   " + bob);
        System.out.println("deepEquals(alice, bob): " + deepEquals(alice, bob));

        var aliceCopy = (Person) deepCopy(alice);
        System.out.println("deepEquals(alice, aliceCopy): " + deepEquals(alice, aliceCopy));

        System.out.println("\n=== Null Handling ===");
        var personNoAddress = new Person("Vikthor", 30, null, new String[]{"coding"});
        var personNoAddressCopy = (Person) deepCopy(personNoAddress);
        System.out.println("Person with null address: " + personNoAddress);
        System.out.println("Copy:                     " + personNoAddressCopy);
        System.out.println("deepEquals: " + deepEquals(personNoAddress, personNoAddressCopy));
    }
}
