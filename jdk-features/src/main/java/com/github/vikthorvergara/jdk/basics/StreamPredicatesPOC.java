package com.github.vikthorvergara.jdk.basics;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class StreamPredicatesPOC {

    record Person(String name, int age, String city) {}

    static void predicateCombinators() {
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> isEven = n -> n % 2 == 0;

        var numbers = List.of(-4, -3, -2, -1, 0, 1, 2, 3, 4, 5);

        var positiveAndEven = numbers.stream().filter(isPositive.and(isEven)).toList();
        System.out.println("and(positive, even) -> " + positiveAndEven);

        var positiveOrEven = numbers.stream().filter(isPositive.or(isEven)).toList();
        System.out.println("or(positive, even) -> " + positiveOrEven);

        var notPositive = numbers.stream().filter(isPositive.negate()).toList();
        System.out.println("negate(positive) -> " + notPositive);

        var equalToZero = numbers.stream().filter(Predicate.isEqual(0)).toList();
        System.out.println("isEqual(0) -> " + equalToZero);
    }

    static void sortedOperations() {
        var numbers = List.of(5, 2, 8, 1, 9, 3);

        var natural = numbers.stream().sorted().toList();
        System.out.println("sorted() -> " + natural);

        var reversed = numbers.stream().sorted(Comparator.reverseOrder()).toList();
        System.out.println("sorted(reverseOrder) -> " + reversed);

        var people = List.of(
                new Person("alice", 30, "NYC"),
                new Person("bob", 25, "LA"),
                new Person("charlie", 30, "NYC"),
                new Person("diana", 25, "NYC"),
                new Person("eve", 40, "LA")
        );

        var byCityThenAge = people.stream()
                .sorted(Comparator.comparing(Person::city).thenComparing(Person::age))
                .toList();
        System.out.println("sorted(city, then age) -> " + byCityThenAge);

        var byCityThenAgeReversed = people.stream()
                .sorted(Comparator.comparing(Person::city).thenComparing(Person::age).reversed())
                .toList();
        System.out.println("sorted(city, then age).reversed() -> " + byCityThenAgeReversed);
    }

    static void distinctLimitSkip() {
        var numbers = List.of(1, 2, 2, 3, 3, 3, 4, 5, 5, 6, 7, 8, 9, 10);

        var uniq = numbers.stream().distinct().toList();
        System.out.println("distinct() -> " + uniq);

        var firstFour = numbers.stream().distinct().limit(4).toList();
        System.out.println("distinct().limit(4) -> " + firstFour);

        var skipped = numbers.stream().distinct().skip(3).toList();
        System.out.println("distinct().skip(3) -> " + skipped);

        var page = numbers.stream().distinct().skip(2).limit(3).toList();
        System.out.println("distinct().skip(2).limit(3) -> " + page);
    }

    static void takeWhileDropWhile() {
        var sorted = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        var taken = sorted.stream().takeWhile(n -> n < 5).toList();
        System.out.println("takeWhile(n < 5) -> " + taken);

        var dropped = sorted.stream().dropWhile(n -> n < 5).toList();
        System.out.println("dropWhile(n < 5) -> " + dropped);

        var unsorted = List.of(1, 2, 3, 10, 2, 3, 11);
        var takenUnsorted = unsorted.stream().takeWhile(n -> n < 5).toList();
        System.out.println("takeWhile on unsorted [1,2,3,10,2,3,11] -> " + takenUnsorted);

        var droppedUnsorted = unsorted.stream().dropWhile(n -> n < 5).toList();
        System.out.println("dropWhile on unsorted [1,2,3,10,2,3,11] -> " + droppedUnsorted);
    }

    public static void main(String[] args) {
        System.out.println("--- predicate combinators ---");
        predicateCombinators();

        System.out.println("\n--- sorted operations ---");
        sortedOperations();

        System.out.println("\n--- distinct, limit, skip ---");
        distinctLimitSkip();

        System.out.println("\n--- takeWhile and dropWhile ---");
        takeWhileDropWhile();
    }
}
