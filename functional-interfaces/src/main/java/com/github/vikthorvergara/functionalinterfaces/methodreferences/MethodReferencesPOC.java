package com.github.vikthorvergara.functionalinterfaces.methodreferences;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class MethodReferencesPOC {

    record Person(String name, int age) {
        String greeting() {
            return "Hi, I'm %s (%d)".formatted(name, age);
        }
    }

    static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    static int compareByAge(Person a, Person b) {
        return Integer.compare(a.age(), b.age());
    }

    static boolean isAdult(Person p) {
        return p.age() >= 18;
    }

    public static void main(String[] args) {
        var people = List.of(
            new Person("Barney", 9),
            new Person("Vikthor", 30),
            new Person("Bob", 35),
            new Person("Martin", 62));

        var words = List.of("hello", "world", "java", "streams");

        System.out.println("=== static method reference (Class::staticMethod) ===");
        System.out.println("  reversed: " + words.stream().map(MethodReferencesPOC::reverse).toList());
        var sortedPeople = people.stream().sorted(MethodReferencesPOC::compareByAge).toList();
        System.out.println("  sorted by age: " + sortedPeople.stream().map(Person::name).toList());
        System.out.println("  adults: " + people.stream().filter(MethodReferencesPOC::isAdult).map(Person::name).toList());

        System.out.println("\n=== bound instance method reference (instance::method) ===");
        var csv = String.join(",", words);
        System.out.println("  csv: " + csv);
        Function<String, Boolean> containsJava = csv::contains;
        System.out.println("  contains 'java': " + containsJava.apply("java"));
        System.out.println("  contains 'rust': " + containsJava.apply("rust"));

        var prefix = ">> ";
        UnaryOperator<String> addPrefix = prefix::concat;
        System.out.println("  prefixed: " + words.stream().map(addPrefix).toList());

        Supplier<Integer> barneyAge = people.getFirst()::age;
        System.out.println("  barney's age via supplier: " + barneyAge.get());

        System.out.println("\n=== unbound instance method reference (Class::instanceMethod) ===");
        System.out.println("  uppercased: " + words.stream().map(String::toUpperCase).toList());
        System.out.println("  lengths: " + words.stream().map(String::length).toList());

        var sortedByName = people.stream()
            .map(Person::name)
            .sorted(String::compareToIgnoreCase)
            .toList();
        System.out.println("  names sorted: " + sortedByName);

        System.out.println("\n=== constructor reference (Class::new) ===");
        BiFunction<String, Integer, Person> personFactory = Person::new;
        var ro = personFactory.apply("Ro", 70);
        System.out.println("  created: " + ro);

        Function<String, StringBuilder> sbFactory = StringBuilder::new;
        var builders = words.stream().map(sbFactory).toList();
        System.out.println("  builders reversed: " + builders.stream()
            .map(sb -> sb.reverse().toString())
            .toList());

        var names = List.of("Walter,52", "Cal,33");
        var parsed = names.stream()
            .map(s -> s.split(","))
            .map(parts -> personFactory.apply(parts[0], Integer.parseInt(parts[1])))
            .toList();
        System.out.println("  parsed people: " + parsed);

        System.out.println("\n=== combining reference kinds ===");
        var result = people.stream()
            .filter(MethodReferencesPOC::isAdult)
            .sorted(Comparator.comparing(Person::name))
            .map(Person::greeting)
            .collect(Collectors.joining("; "));
        System.out.println("  adult greetings: " + result);
    }
}
