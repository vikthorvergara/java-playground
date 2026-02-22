package com.github.vikthorvergara.functionalinterfaces.memoization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MemoizationPOC {

    static <T, R> Function<T, R> memoize(Function<T, R> fn) {
        Map<T, R> cache = new HashMap<>();
        return input -> cache.computeIfAbsent(input, fn);
    }

    record Product(String name, double price, String category) {}

    @FunctionalInterface
    interface PricingStrategy {
        double calculate(double basePrice);

        static PricingStrategy flat(double discount) {
            return price -> price - discount;
        }

        static PricingStrategy percentage(double percent) {
            return price -> price * (1 - percent / 100);
        }

        static PricingStrategy tiered(double threshold, double belowPercent, double abovePercent) {
            return price -> price <= threshold
                ? price * (1 - belowPercent / 100)
                : price * (1 - abovePercent / 100);
        }
    }

    static long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static void main(String[] args) {
        System.out.println("=== memoize (expensive computation) ===");
        Function<Integer, Long> fib = memoize(MemoizationPOC::fibonacci);
        var fibInputs = List.of(10, 20, 30, 35);
        for (var n : fibInputs) {
            long start = System.nanoTime();
            long result = fib.apply(n);
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            System.out.println("  fib(%d) = %d (%dms)".formatted(n, result, elapsed));
        }

        System.out.println("\n=== memoize (second call hits cache) ===");
        for (var n : fibInputs) {
            long start = System.nanoTime();
            long result = fib.apply(n);
            long elapsed = System.nanoTime() - start;
            System.out.println("  fib(%d) = %d (%dns - cached)".formatted(n, result, elapsed));
        }

        System.out.println("\n=== memoize (string transform) ===");
        Function<String, String> slugify = memoize(s -> {
            System.out.println("    computing slug for: " + s);
            return s.trim().toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");
        });
        System.out.println("  " + slugify.apply("Hello World!"));
        System.out.println("  " + slugify.apply("Functional Java"));
        System.out.println("  " + slugify.apply("Hello World!"));

        System.out.println("\n=== pricing strategy (static factories) ===");
        var products = List.of(
            new Product("RTX 5090", 2499.99, "GPU"),
            new Product("Keychron Q1", 169.00, "Keyboard"),
            new Product("JBL Tune 520BT", 49.95, "Audio"));

        var flat20 = PricingStrategy.flat(20);
        var tenPercent = PricingStrategy.percentage(10);
        var tiered = PricingStrategy.tiered(100, 5, 15);

        for (var p : products) {
            System.out.println("  %s ($%.2f):".formatted(p.name(), p.price()));
            System.out.println("    flat $20 off  -> $%.2f".formatted(flat20.calculate(p.price())));
            System.out.println("    10%% off       -> $%.2f".formatted(tenPercent.calculate(p.price())));
            System.out.println("    tiered        -> $%.2f".formatted(tiered.calculate(p.price())));
        }

        System.out.println("\n=== dynamic strategy selection ===");
        Map<String, PricingStrategy> strategies = Map.of(
            "GPU", PricingStrategy.percentage(5),
            "Keyboard", PricingStrategy.flat(15),
            "Audio", PricingStrategy.percentage(20));

        for (var p : products) {
            var strategy = strategies.getOrDefault(p.category(), PricingStrategy.flat(0));
            System.out.println("  %s: $%.2f -> $%.2f (%s discount)".formatted(
                p.name(), p.price(), strategy.calculate(p.price()), p.category()));
        }
    }
}
