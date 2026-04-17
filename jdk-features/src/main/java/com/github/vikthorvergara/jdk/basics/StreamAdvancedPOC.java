package com.github.vikthorvergara.jdk.basics;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamAdvancedPOC {

    static void flatMapOperations() {
        var nested = List.of(List.of(1, 2, 3), List.of(4, 5), List.of(6, 7, 8, 9));
        var flat = nested.stream().flatMap(List::stream).toList();
        System.out.println("flatMap(List::stream) -> " + flat);

        var words = List.of("hello world", "java streams");
        var tokens = words.stream()
                .flatMap(s -> java.util.Arrays.stream(s.split(" ")))
                .toList();
        System.out.println("flatMap(split) -> " + tokens);

        var optionals = List.of(Optional.of("a"), Optional.<String>empty(), Optional.of("b"), Optional.<String>empty(), Optional.of("c"));
        var present = optionals.stream().flatMap(Optional::stream).toList();
        System.out.println("flatMap(Optional::stream) -> " + present);
    }

    static void primitiveStreams() {
        var numbers = List.of(1, 2, 3, 4, 5);

        int sum = numbers.stream().mapToInt(Integer::intValue).sum();
        System.out.println("mapToInt.sum -> " + sum);

        long longSum = numbers.stream().mapToLong(Integer::longValue).sum();
        System.out.println("mapToLong.sum -> " + longSum);

        double avg = numbers.stream().mapToDouble(Integer::doubleValue).average().orElse(0);
        System.out.println("mapToDouble.average -> " + avg);

        int rangeSum = IntStream.range(1, 5).sum();
        System.out.println("IntStream.range(1,5).sum -> " + rangeSum);

        int rangeClosedSum = IntStream.rangeClosed(1, 5).sum();
        System.out.println("IntStream.rangeClosed(1,5).sum -> " + rangeClosedSum);

        var squares = IntStream.rangeClosed(1, 5).map(n -> n * n).boxed().toList();
        System.out.println("rangeClosed(1,5).map(n*n) -> " + squares);
    }

    static void concatBuilderOfNullable() {
        var a = Stream.of(1, 2, 3);
        var b = Stream.of(4, 5, 6);
        var concatenated = Stream.concat(a, b).toList();
        System.out.println("Stream.concat -> " + concatenated);

        var built = Stream.<String>builder()
                .add("x")
                .add("y")
                .add("z")
                .build()
                .toList();
        System.out.println("Stream.builder -> " + built);

        String maybeNull = null;
        var fromNullable = Stream.ofNullable(maybeNull).toList();
        System.out.println("Stream.ofNullable(null) -> " + fromNullable);

        var fromValue = Stream.ofNullable("value").toList();
        System.out.println("Stream.ofNullable(value) -> " + fromValue);
    }

    static void parallelStreams() {
        var data = IntStream.rangeClosed(1, 200_000).boxed().toList();

        long seqStart = System.nanoTime();
        long seqSum = data.stream().mapToLong(StreamAdvancedPOC::heavyWork).sum();
        long seqMs = (System.nanoTime() - seqStart) / 1_000_000;

        long parStart = System.nanoTime();
        long parSum = data.parallelStream().mapToLong(StreamAdvancedPOC::heavyWork).sum();
        long parMs = (System.nanoTime() - parStart) / 1_000_000;

        System.out.println("sequential sum = " + seqSum + " in " + seqMs + "ms");
        System.out.println("parallel sum   = " + parSum + " in " + parMs + "ms");
    }

    static long heavyWork(int n) {
        long acc = 0;
        for (int i = 1; i <= 200; i++) {
            acc += (long) Math.sqrt((double) n * i) + i;
        }
        return acc;
    }

    static void teeingCollector() {
        var numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        record Stats(double avg, long count) {}

        var stats = numbers.stream().collect(Collectors.teeing(
                Collectors.averagingInt(Integer::intValue),
                Collectors.counting(),
                Stats::new
        ));
        System.out.println("teeing(avg, count) -> " + stats);

        record MinMax(int min, int max) {}
        var minMax = numbers.stream().collect(Collectors.teeing(
                Collectors.minBy(Integer::compareTo),
                Collectors.maxBy(Integer::compareTo),
                (mn, mx) -> new MinMax(mn.orElseThrow(), mx.orElseThrow())
        ));
        System.out.println("teeing(min, max) -> " + minMax);
    }

    static Collector<String, StringJoiner, String> joiningWithSeparator(String sep) {
        Supplier<StringJoiner> supplier = () -> new StringJoiner(sep);
        BiConsumer<StringJoiner, String> accumulator = StringJoiner::add;
        BinaryOperator<StringJoiner> combiner = StringJoiner::merge;
        Function<StringJoiner, String> finisher = StringJoiner::toString;
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    static void customCollector() {
        var words = List.of("alpha", "beta", "gamma", "delta");
        var joined = words.stream().collect(joiningWithSeparator(" | "));
        System.out.println("custom Collector joining -> " + joined);
    }

    public static void main(String[] args) {
        System.out.println("--- flatMap operations ---");
        flatMapOperations();

        System.out.println("\n--- primitive streams ---");
        primitiveStreams();

        System.out.println("\n--- concat, builder, ofNullable ---");
        concatBuilderOfNullable();

        System.out.println("\n--- parallel vs sequential ---");
        parallelStreams();

        System.out.println("\n--- teeing collector ---");
        teeingCollector();

        System.out.println("\n--- custom Collector ---");
        customCollector();
    }
}
