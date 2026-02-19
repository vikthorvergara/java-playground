package com.github.vikthorvergara.functionalinterfaces.defaultmethods;

import java.util.List;

public class DefaultMethodsPOC {

    interface Greetable {
        default String greet(String name) {
            return formatGreeting(name, "Hello");
        }

        private String formatGreeting(String name, String prefix) {
            return prefix + ", " + name + "!";
        }
    }

    interface Farewell {
        default String greet(String name) {
            return "Goodbye, " + name + "!";
        }
    }

    static class PoliteBot implements Greetable, Farewell {
        @Override
        public String greet(String name) {
            return Greetable.super.greet(name) + " / " + Farewell.super.greet(name);
        }
    }

    interface Loggable {
        default String tag() {
            return getClass().getSimpleName();
        }

        default String log(String message) {
            return "[%s] %s".formatted(tag(), message);
        }
    }

    static class Service implements Loggable {
        @Override
        public String tag() {
            return "SVC";
        }
    }

    interface Versioned<T> {
        T value();

        default int version() {
            return 1;
        }

        static <T> Versioned<T> of(T value) {
            return () -> value;
        }

        static <T> Versioned<T> v2(T value) {
            return new Versioned<>() {
                @Override
                public T value() {
                    return value;
                }

                @Override
                public int version() {
                    return 2;
                }
            };
        }
    }

    interface Identifiable {
        String id();

        private String prefix() {
            return "ID";
        }

        default String fullId() {
            return prefix() + "-" + id();
        }
    }

    record Entity(String id, String name) implements Identifiable {}

    interface Filterable<T> {
        List<T> items();

        default List<T> filter(java.util.function.Predicate<T> predicate) {
            return items().stream().filter(predicate).toList();
        }

        default long count(java.util.function.Predicate<T> predicate) {
            return filter(predicate).size();
        }
    }

    record Catalog(List<String> items) implements Filterable<String> {}

    public static void main(String[] args) {
        System.out.println("=== diamond problem resolution ===");
        var bot = new PoliteBot();
        System.out.println("  " + bot.greet("Barney"));

        System.out.println("\n=== overriding defaults ===");
        var service = new Service();
        System.out.println("  Default tag would be: " + new Loggable(){}.tag());
        System.out.println("  Overridden tag: " + service.tag());
        System.out.println("  " + service.log("system started"));

        System.out.println("\n=== static factory methods ===");
        var v1 = Versioned.of("config-a");
        var v2 = Versioned.v2("config-b");
        System.out.println("  v1: value=%s version=%d".formatted(v1.value(), v1.version()));
        System.out.println("  v2: value=%s version=%d".formatted(v2.value(), v2.version()));

        System.out.println("\n=== private methods in interfaces ===");
        var entity = new Entity("42", "Widget");
        System.out.println("  " + entity.fullId());
        System.out.println("  name: " + entity.name());

        System.out.println("\n=== evolving APIs with defaults ===");
        var catalog = new Catalog(List.of("apple", "avocado", "banana", "blueberry", "cherry"));
        System.out.println("  all items: " + catalog.items());
        System.out.println("  starts with 'a': " + catalog.filter(s -> s.startsWith("a")));
        System.out.println("  starts with 'b': " + catalog.count(s -> s.startsWith("b")));
    }
}
