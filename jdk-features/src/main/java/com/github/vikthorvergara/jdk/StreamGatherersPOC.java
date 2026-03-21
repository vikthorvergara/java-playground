package com.github.vikthorvergara.jdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

public class StreamGatherersPOC {

    record Transaction(String id, String category, double amount) {}

    static <T> Gatherer<T, ?, List<T>> windowFixed(int size) {
        return Gatherer.<T, ArrayList<T>, List<T>>ofSequential(
                ArrayList::new,
                (window, element, downstream) -> {
                    window.add(element);
                    if (window.size() == size) {
                        List<T> batch = List.copyOf(window);
                        window.clear();
                        return downstream.push(batch);
                    }
                    return true;
                },
                (window, downstream) -> {
                    if (!window.isEmpty()) {
                        downstream.push(List.copyOf(window));
                    }
                }
        );
    }

    static <T> Gatherer<T, ?, List<T>> windowSliding(int size) {
        return Gatherer.<T, ArrayList<T>, List<T>>ofSequential(
                ArrayList::new,
                (window, element, downstream) -> {
                    window.add(element);
                    if (window.size() == size) {
                        List<T> batch = List.copyOf(window);
                        window.removeFirst();
                        return downstream.push(batch);
                    }
                    return true;
                }
        );
    }

    static <T> Gatherer<T, ?, T> distinctBy(Function<T, Object> keyExtractor) {
        return Gatherer.ofSequential(
                () -> new HashMap<Object, Boolean>(),
                (HashMap<Object, Boolean> seen, T element, Gatherer.Downstream<? super T> downstream) -> {
                    var key = keyExtractor.apply(element);
                    if (seen.putIfAbsent(key, Boolean.TRUE) == null) {
                        return downstream.push(element);
                    }
                    return true;
                }
        );
    }

    static Gatherer<Double, ?, Double> movingAverage(int windowSize) {
        return Gatherer.ofSequential(
                ArrayList::new,
                (List<Double> window, Double element, Gatherer.Downstream<? super Double> downstream) -> {
                    window.add(element);
                    if (window.size() > windowSize) {
                        window.removeFirst();
                    }
                    if (window.size() == windowSize) {
                        var avg = window.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                        return downstream.push(avg);
                    }
                    return true;
                }
        );
    }

    static <T> Gatherer<T, ?, T> takeWhileInclusive(Predicate<T> predicate) {
        return Gatherer.ofSequential(
                () -> new AtomicInteger(0),
                (AtomicInteger state, T element, Gatherer.Downstream<? super T> downstream) -> {
                    if (state.get() == 1) {
                        return false;
                    }
                    if (!predicate.test(element)) {
                        state.set(1);
                        downstream.push(element);
                        return false;
                    }
                    return downstream.push(element);
                }
        );
    }

    static <T> Gatherer<T, ?, Map.Entry<T, T>> zipWithNext() {
        return Gatherer.ofSequential(
                () -> new AtomicReference<T>(null),
                (AtomicReference<T> prev, T element, Gatherer.Downstream<? super Map.Entry<T, T>> downstream) -> {
                    T previous = prev.get();
                    prev.set(element);
                    if (previous != null) {
                        return downstream.push(Map.entry(previous, element));
                    }
                    return true;
                }
        );
    }

    static void builtInGatherers() {
        System.out.println("=== Built-in Gatherers: windowFixed ===");
        var fixedWindows = Stream.of(1, 2, 3, 4, 5, 6, 7)
                .gather(Gatherers.windowFixed(3))
                .toList();
        fixedWindows.forEach(w -> System.out.println("  " + w));

        System.out.println("\n=== Built-in Gatherers: windowSliding ===");
        var slidingWindows = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.windowSliding(3))
                .toList();
        slidingWindows.forEach(w -> System.out.println("  " + w));

        System.out.println("\n=== Built-in Gatherers: fold ===");
        var concatenated = Stream.of("Hello", " ", "Stream", " ", "Gatherers")
                .gather(Gatherers.fold(() -> "", String::concat))
                .toList();
        System.out.println("  Folded: " + concatenated);

        System.out.println("\n=== Built-in Gatherers: scan ===");
        var runningSum = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();
        System.out.println("  Running sum: " + runningSum);

        System.out.println("\n=== Built-in Gatherers: mapConcurrent ===");
        var mapped = Stream.of("gpu", "cpu", "ram")
                .gather(Gatherers.mapConcurrent(3, s -> s.toUpperCase() + "-PROCESSED"))
                .toList();
        System.out.println("  Concurrent map: " + mapped);
    }

    static void customGatherers() {
        System.out.println("\n=== Custom Gatherer: Fixed Window ===");
        var batches = Stream.of("a", "b", "c", "d", "e", "f", "g")
                .gather(windowFixed(3))
                .toList();
        batches.forEach(b -> System.out.println("  Batch: " + b));

        System.out.println("\n=== Custom Gatherer: Sliding Window ===");
        var sliding = Stream.of(10, 20, 30, 40, 50)
                .gather(windowSliding(3))
                .toList();
        sliding.forEach(w -> System.out.println("  Window: " + w));

        System.out.println("\n=== Custom Gatherer: Distinct By ===");
        var transactions = Stream.of(
                new Transaction("t1", "food", 25.0),
                new Transaction("t2", "tech", 999.0),
                new Transaction("t3", "food", 15.0),
                new Transaction("t4", "travel", 450.0),
                new Transaction("t5", "tech", 49.0)
        )
                .gather(distinctBy(Transaction::category))
                .toList();
        transactions.forEach(t -> System.out.println("  " + t));

        System.out.println("\n=== Custom Gatherer: Moving Average ===");
        var prices = Stream.of(100.0, 102.0, 98.0, 105.0, 110.0, 108.0, 112.0)
                .gather(movingAverage(3))
                .toList();
        prices.forEach(p -> System.out.printf("  %.2f%n", p));

        System.out.println("\n=== Custom Gatherer: Take While Inclusive ===");
        var taken = Stream.of(2, 4, 6, 7, 8, 10)
                .gather(takeWhileInclusive(n -> n % 2 == 0))
                .toList();
        System.out.println("  " + taken);

        System.out.println("\n=== Custom Gatherer: Zip With Next ===");
        var pairs = Stream.of("Mon", "Tue", "Wed", "Thu", "Fri")
                .gather(zipWithNext())
                .toList();
        pairs.forEach(p -> System.out.println("  " + p.getKey() + " -> " + p.getValue()));
    }

    static void composedGatherers() {
        System.out.println("\n=== Composed Gatherers: Window + Moving Avg Pipeline ===");
        var result = Stream.of(10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0)
                .gather(movingAverage(3)
                        .andThen(windowFixed(2)))
                .toList();
        result.forEach(w -> System.out.println("  " + w));

        System.out.println("\n=== Composed Gatherers: Scan + Sliding Window ===");
        var composed = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.scan(() -> 0, Integer::sum)
                        .andThen(windowSliding(3)))
                .toList();
        composed.forEach(w -> System.out.println("  " + w));
    }

    public static void main(String[] args) {
        builtInGatherers();
        customGatherers();
        composedGatherers();
    }
}
