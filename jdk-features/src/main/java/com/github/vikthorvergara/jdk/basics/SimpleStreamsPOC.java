package com.github.vikthorvergara.jdk.basics;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleStreamsPOC {

    static void streamCreation() {
        var fromList = List.of("apple", "banana", "cherry").stream().toList();
        System.out.println("List.stream() -> " + fromList);

        var fromArray = Arrays.stream(new int[]{10, 20, 30, 40}).boxed().toList();
        System.out.println("Arrays.stream(int[]) -> " + fromArray);

        var fromOf = Stream.of("x", "y", "z").toList();
        System.out.println("Stream.of() -> " + fromOf);

        var generated = Stream.generate(() -> "echo").limit(4).toList();
        System.out.println("Stream.generate() -> " + generated);

        var iterated = Stream.iterate(1, n -> n * 2).limit(6).toList();
        System.out.println("Stream.iterate(1, n->n*2) -> " + iterated);

        var iteratedWithPredicate = Stream.iterate(1, n -> n <= 100, n -> n * 3).toList();
        System.out.println("Stream.iterate(1, n<=100, n->n*3) -> " + iteratedWithPredicate);
    }

    static void mapAndFilter() {
        var names = List.of("alice", "bob", "charlie", "diana", "eve");

        var uppercased = names.stream()
                .map(String::toUpperCase)
                .toList();
        System.out.println("map(toUpperCase) -> " + uppercased);

        var longNames = names.stream()
                .filter(n -> n.length() > 3)
                .toList();
        System.out.println("filter(length > 3) -> " + longNames);

        var lengths = names.stream()
                .map(String::length)
                .toList();
        System.out.println("map(length) -> " + lengths);

        var filtered = names.stream()
                .filter(n -> n.startsWith("a") || n.startsWith("e"))
                .map(String::toUpperCase)
                .toList();
        System.out.println("filter(starts a|e) + map(upper) -> " + filtered);
    }

    static void forEachAndPeek() {
        System.out.print("forEach: ");
        List.of(1, 2, 3, 4, 5).stream()
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        var result = List.of(10, 20, 30).stream()
                .peek(n -> System.out.print("peek(" + n + ") "))
                .map(n -> n * 2)
                .toList();
        System.out.println("-> map(*2) -> " + result);
    }

    static void collectToSetAndMap() {
        var numbers = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3);

        var asSet = numbers.stream().collect(Collectors.toSet());
        System.out.println("collect(toSet()) -> " + asSet);

        var names = List.of("alice", "bob", "charlie");
        var nameToLength = names.stream()
                .collect(Collectors.toMap(n -> n, String::length));
        System.out.println("collect(toMap(name, length)) -> " + nameToLength);
    }

    static void countMinMax() {
        var numbers = List.of(5, 12, 3, 8, 1, 19, 7);

        long count = numbers.stream().filter(n -> n > 5).count();
        System.out.println("count(n > 5) -> " + count);

        var min = numbers.stream().min(Integer::compareTo);
        System.out.println("min() -> " + min.orElseThrow());

        var max = numbers.stream().max(Integer::compareTo);
        System.out.println("max() -> " + max.orElseThrow());
    }

    static void findOperations() {
        var names = List.of("alice", "bob", "charlie", "diana");

        var first = names.stream().filter(n -> n.length() > 3).findFirst();
        System.out.println("findFirst(length > 3) -> " + first.orElse("none"));

        var any = names.stream().filter(n -> n.contains("a")).findAny();
        System.out.println("findAny(contains 'a') -> " + any.orElse("none"));

        var firstEmpty = names.stream().filter(n -> n.length() > 100).findFirst();
        System.out.println("findFirst(length > 100) -> " + firstEmpty.orElse("none"));
    }

    static void matchOperations() {
        var numbers = List.of(2, 4, 6, 8, 10);

        boolean allEven = numbers.stream().allMatch(n -> n % 2 == 0);
        System.out.println("allMatch(even) on [2,4,6,8,10] -> " + allEven);

        boolean anyGreaterThan9 = numbers.stream().anyMatch(n -> n > 9);
        System.out.println("anyMatch(>9) on [2,4,6,8,10] -> " + anyGreaterThan9);

        boolean noneNegative = numbers.stream().noneMatch(n -> n < 0);
        System.out.println("noneMatch(<0) on [2,4,6,8,10] -> " + noneNegative);

        var mixed = List.of(1, 2, 3, 4, 5);
        boolean allEvenMixed = mixed.stream().allMatch(n -> n % 2 == 0);
        System.out.println("allMatch(even) on [1,2,3,4,5] -> " + allEvenMixed);
    }

    public static void main(String[] args) {
        System.out.println("--- stream creation ---");
        streamCreation();

        System.out.println("\n--- map and filter ---");
        mapAndFilter();

        System.out.println("\n--- forEach and peek ---");
        forEachAndPeek();

        System.out.println("\n--- collect to Set and Map ---");
        collectToSetAndMap();

        System.out.println("\n--- count, min, max ---");
        countMinMax();

        System.out.println("\n--- find operations ---");
        findOperations();

        System.out.println("\n--- match operations ---");
        matchOperations();
    }
}
