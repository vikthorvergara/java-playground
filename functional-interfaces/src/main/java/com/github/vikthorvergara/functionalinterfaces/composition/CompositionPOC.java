package com.github.vikthorvergara.functionalinterfaces.composition;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class CompositionPOC {

    @SafeVarargs
    static <T> UnaryOperator<T> pipeline(UnaryOperator<T>... steps) {
        UnaryOperator<T> result = UnaryOperator.identity();
        for (var step : steps) {
            var prev = result;
            result = t -> step.apply(prev.apply(t));
        }
        return result;
    }

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    static <A, B, R> Function<A, Function<B, R>> curry(BiFunction<A, B, R> fn) {
        return a -> b -> fn.apply(a, b);
    }

    static <A, B, C, R> Function<A, Function<B, Function<C, R>>> curry(TriFunction<A, B, C, R> fn) {
        return a -> b -> c -> fn.apply(a, b, c);
    }

    static <A, B, R> Function<B, R> partial(BiFunction<A, B, R> fn, A a) {
        return b -> fn.apply(a, b);
    }

    static <A, B, C, R> BiFunction<B, C, R> partial(TriFunction<A, B, C, R> fn, A a) {
        return (b, c) -> fn.apply(a, b, c);
    }

    public static void main(String[] args) {
        System.out.println("=== pipeline builder ===");
        var normalizeText = pipeline(
            String::trim,
            String::toLowerCase,
            (String s) -> s.replaceAll("\\s+", " "),
            (String s) -> s.replaceAll("[^a-z0-9 ]", ""));
        System.out.println("  '" + normalizeText.apply("  Hello,  WORLD!  ") + "'");

        var mathPipeline = pipeline(
            (Integer n) -> n * 2,
            (Integer n) -> n + 10,
            (Integer n) -> n * n);
        System.out.println("  pipeline(5): " + mathPipeline.apply(5));
        System.out.println("  pipeline(3): " + mathPipeline.apply(3));

        var slugify = pipeline(
            String::trim,
            String::toLowerCase,
            (String s) -> s.replaceAll("\\s+", "-"),
            (String s) -> s.replaceAll("[^a-z0-9-]", ""));
        var titles = List.of("Hello World!", "  Functional Java  ", "POC: Composition");
        System.out.println("  slugs: " + titles.stream().map(slugify).toList());

        System.out.println("\n=== currying (BiFunction) ===");
        BiFunction<String, String, String> concat = (a, b) -> a + b;
        var curriedConcat = curry(concat);
        var addHello = curriedConcat.apply("Hello, ");
        System.out.println("  " + addHello.apply("Vikthor"));
        System.out.println("  " + addHello.apply("Barney"));

        BiFunction<Double, Double, Double> power = (exp, base) -> Math.pow(base, exp);
        var curriedPow = curry(power);
        var square = curriedPow.apply(2.0);
        var cube = curriedPow.apply(3.0);
        System.out.println("  square(5): " + square.apply(5.0));
        System.out.println("  cube(3): " + cube.apply(3.0));

        System.out.println("\n=== currying (TriFunction) ===");
        TriFunction<String, String, String, String> buildUrl = (scheme, host, path) ->
            scheme + "://" + host + "/" + path;
        var curriedUrl = curry(buildUrl);
        var httpsUrl = curriedUrl.apply("https");
        var githubUrl = httpsUrl.apply("github.com");
        System.out.println("  " + githubUrl.apply("vikthorvergara"));
        System.out.println("  " + githubUrl.apply("openjdk/jdk"));
        System.out.println("  " + httpsUrl.apply("google.com").apply("search"));

        System.out.println("\n=== partial application ===");
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        var repeatStar = partial(repeat, "*");
        var repeatDash = partial(repeat, "-");
        System.out.println("  stars(5): " + repeatStar.apply(5));
        System.out.println("  dashes(10): " + repeatDash.apply(10));

        TriFunction<Double, Double, Double, Double> clamp = (min, max, value) ->
            Math.max(min, Math.min(max, value));
        var clampPercent = partial(clamp, 0.0);
        System.out.println("  clamp(0,100, -5): " + clampPercent.apply(100.0, -5.0));
        System.out.println("  clamp(0,100, 50): " + clampPercent.apply(100.0, 50.0));
        System.out.println("  clamp(0,100, 200): " + clampPercent.apply(100.0, 200.0));

        System.out.println("\n=== compose vs andThen ===");
        Function<Integer, Integer> doubleIt = n -> n * 2;
        Function<Integer, Integer> addTen = n -> n + 10;
        System.out.println("  compose (addTen then double): " + doubleIt.compose(addTen).apply(5));
        System.out.println("  andThen (double then addTen): " + doubleIt.andThen(addTen).apply(5));
    }
}
