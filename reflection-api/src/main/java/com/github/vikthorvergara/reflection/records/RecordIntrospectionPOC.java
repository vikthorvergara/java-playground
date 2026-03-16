package com.github.vikthorvergara.reflection.records;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecordIntrospectionPOC {

    record Person(String name, int age) {}

    record Product(String name, double price, String category) {}

    record Order(Person buyer, List<Product> items, LocalDate date) {}

    static class RegularClass {
        String field;
    }

    static void inspectRecordComponents(Class<?> recordClass) {
        System.out.println("Record: " + recordClass.getSimpleName());
        System.out.println("  isRecord: " + recordClass.isRecord());
        var components = recordClass.getRecordComponents();
        System.out.println("  Components (" + components.length + "):");
        for (var component : components) {
            System.out.printf("    - %s : %s (accessor: %s)%n",
                    component.getName(),
                    component.getType().getSimpleName(),
                    component.getAccessor().getName() + "()");
        }
        System.out.println();
    }

    static void checkIsRecord(Class<?>... classes) {
        System.out.println("=== isRecord() Check ===");
        for (var clazz : classes) {
            System.out.printf("  %s -> isRecord: %s%n", clazz.getSimpleName(), clazz.isRecord());
        }
        System.out.println();
    }

    @SuppressWarnings("unchecked")
    static <T extends Record> T instantiateRecord(Class<T> recordClass, Object... args) throws Exception {
        var components = recordClass.getRecordComponents();
        var paramTypes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            paramTypes[i] = components[i].getType();
        }
        Constructor<T> canonical = recordClass.getDeclaredConstructor(paramTypes);
        return canonical.newInstance(args);
    }

    static Map<String, Object> recordToMap(Record record) throws Exception {
        var map = new LinkedHashMap<String, Object>();
        var components = record.getClass().getRecordComponents();
        for (var component : components) {
            var accessor = component.getAccessor();
            var value = accessor.invoke(record);
            map.put(component.getName(), value);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    static <T extends Record> T mapToRecord(Map<String, Object> map, Class<T> recordClass) throws Exception {
        var components = recordClass.getRecordComponents();
        var paramTypes = new Class<?>[components.length];
        var args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            paramTypes[i] = components[i].getType();
            args[i] = map.get(components[i].getName());
        }
        Constructor<T> canonical = recordClass.getDeclaredConstructor(paramTypes);
        return canonical.newInstance(args);
    }

    static void readAllValues(Record record) throws Exception {
        System.out.println("Reading values from: " + record);
        var components = record.getClass().getRecordComponents();
        for (var component : components) {
            var accessor = component.getAccessor();
            var value = accessor.invoke(record);
            System.out.printf("  %s = %s%n", component.getName(), value);
        }
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Record Component Inspection ===");
        System.out.println();
        inspectRecordComponents(Person.class);
        inspectRecordComponents(Product.class);
        inspectRecordComponents(Order.class);

        checkIsRecord(Person.class, Product.class, Order.class, RegularClass.class, String.class);

        System.out.println("=== Dynamic Record Instantiation ===");
        var person = instantiateRecord(Person.class, "Vikthor", 30);
        System.out.println("Created Person: " + person);
        var product = instantiateRecord(Product.class, "Laptop", 1200.0, "Electronics");
        System.out.println("Created Product: " + product);
        System.out.println();

        System.out.println("=== Reading Record Values via Accessor Methods ===");
        var alice = new Person("Alice", 30);
        var laptop = new Product("Laptop", 1200.0, "Electronics");
        var mouse = new Product("Mouse", 25.99, "Peripherals");
        readAllValues(alice);
        readAllValues(laptop);
        readAllValues(mouse);

        var order = new Order(alice, List.of(laptop, mouse), LocalDate.of(2026, 3, 15));
        readAllValues(order);

        System.out.println("=== Record to Map Conversion ===");
        var personMap = recordToMap(alice);
        System.out.println("Person as Map: " + personMap);

        var productMap = recordToMap(laptop);
        System.out.println("Product as Map: " + productMap);

        var orderMap = recordToMap(order);
        System.out.println("Order as Map: " + orderMap);
        System.out.println();

        System.out.println("=== Map to Record Conversion ===");
        var rebuiltPerson = mapToRecord(personMap, Person.class);
        System.out.println("Rebuilt Person: " + rebuiltPerson);
        System.out.println("Equals original: " + rebuiltPerson.equals(alice));

        var rebuiltProduct = mapToRecord(productMap, Product.class);
        System.out.println("Rebuilt Product: " + rebuiltProduct);
        System.out.println("Equals original: " + rebuiltProduct.equals(laptop));
        System.out.println();

        System.out.println("=== Round-Trip Verification ===");
        var vikthor = new Person("Vikthor", 30);
        var map = recordToMap(vikthor);
        var roundTripped = mapToRecord(map, Person.class);
        System.out.println("Original:     " + vikthor);
        System.out.println("Map:          " + map);
        System.out.println("Round-tripped: " + roundTripped);
        System.out.println("Identical:     " + vikthor.equals(roundTripped));
    }
}
