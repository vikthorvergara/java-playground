package com.github.vikthorvergara.functionalinterfaces.lazyresult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class LazyResultPOC {

    static class Lazy<T> {
        private final Supplier<T> supplier;
        private T value;
        private boolean computed;

        Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        T get() {
            if (!computed) {
                value = supplier.get();
                computed = true;
            }
            return value;
        }

        <R> Lazy<R> map(Function<T, R> fn) {
            return new Lazy<>(() -> fn.apply(get()));
        }
    }

    static <T> List<T> take(int n, T seed, UnaryOperator<T> next) {
        List<T> result = new ArrayList<>(n);
        T current = seed;
        for (int i = 0; i < n; i++) {
            result.add(current);
            current = next.apply(current);
        }
        return result;
    }

    sealed interface Result<T> {
        record Ok<T>(T value) implements Result<T> {}
        record Err<T>(String error) implements Result<T> {}

        static <T> Result<T> of(Supplier<T> supplier) {
            try {
                return new Ok<>(supplier.get());
            } catch (Exception e) {
                return new Err<>(e.getMessage());
            }
        }

        default <R> Result<R> map(Function<T, R> fn) {
            return switch (this) {
                case Ok<T>(var v) -> Result.of(() -> fn.apply(v));
                case Err<T>(var e) -> new Err<>(e);
            };
        }

        default <R> Result<R> flatMap(Function<T, Result<R>> fn) {
            return switch (this) {
                case Ok<T>(var v) -> fn.apply(v);
                case Err<T>(var e) -> new Err<>(e);
            };
        }

        default T orElse(T fallback) {
            return switch (this) {
                case Ok<T>(var v) -> v;
                case Err<T> _ -> fallback;
            };
        }
    }

    static Result<Integer> parseInt(String s) {
        return Result.of(() -> Integer.parseInt(s));
    }

    static Result<Double> safeDivide(int a, int b) {
        if (b == 0) return new Result.Err<>("division by zero");
        return new Result.Ok<>((double) a / b);
    }

    public static void main(String[] args) {
        System.out.println("=== lazy evaluation ===");
        var lazyMsg = new Lazy<>(() -> {
            System.out.println("  (computing message...)");
            return "hello from lazy";
        });
        System.out.println("  created lazy, not yet computed");
        System.out.println("  first get: " + lazyMsg.get());
        System.out.println("  second get: " + lazyMsg.get());

        System.out.println("\n=== lazy map (deferred) ===");
        var lazyNumber = new Lazy<>(() -> {
            System.out.println("  (computing base value...)");
            return 21;
        });
        var doubled = lazyNumber.map(n -> {
            System.out.println("  (doubling...)");
            return n * 2;
        });
        var asString = doubled.map(n -> {
            System.out.println("  (converting to string...)");
            return "result=" + n;
        });
        System.out.println("  pipeline built, nothing computed yet");
        System.out.println("  " + asString.get());
        System.out.println("  " + asString.get());

        System.out.println("\n=== sequence generator ===");
        System.out.println("  powers of 2: " + take(10, 1, n -> n * 2));
        System.out.println("  fibonacci: " + take(12, new int[]{0, 1}, pair -> new int[]{pair[1], pair[0] + pair[1]})
            .stream().map(pair -> pair[0]).toList());
        System.out.println("  collatz(13): " + take(10, 13, n -> n % 2 == 0 ? n / 2 : 3 * n + 1));

        System.out.println("\n=== Result.of ===");
        var ok = Result.of(() -> 42);
        var err = Result.of(() -> { throw new RuntimeException("boom"); });
        System.out.println("  ok: " + ok);
        System.out.println("  err: " + err);

        System.out.println("\n=== Result map/flatMap ===");
        System.out.println("  parseInt('123').map(*2): " + parseInt("123").map(n -> n * 2));
        System.out.println("  parseInt('abc').map(*2): " + parseInt("abc").map(n -> n * 2));

        var divResult = parseInt("100")
            .flatMap(n -> safeDivide(n, 3))
            .map("%.2f"::formatted);
        System.out.println("  100/3 formatted: " + divResult);

        var divByZero = parseInt("100")
            .flatMap(n -> safeDivide(n, 0))
            .map("%.2f"::formatted);
        System.out.println("  100/0 formatted: " + divByZero);

        var badInput = parseInt("nope")
            .flatMap(n -> safeDivide(n, 3))
            .map("%.2f"::formatted);
        System.out.println("  'nope'/3 formatted: " + badInput);

        System.out.println("\n=== Result orElse ===");
        System.out.println("  parseInt('42').orElse(0): " + parseInt("42").orElse(0));
        System.out.println("  parseInt('xx').orElse(0): " + parseInt("xx").orElse(0));

        System.out.println("\n=== pattern matching on Result ===");
        var inputs = List.of("10", "abc", "20", "", "30");
        for (var input : inputs) {
            var msg = switch (parseInt(input)) {
                case Result.Ok<Integer>(var v) -> "  '%s' -> parsed %d".formatted(input, v);
                case Result.Err<Integer>(var e) -> "  '%s' -> error: %s".formatted(input, e);
            };
            System.out.println(msg);
        }
    }
}
