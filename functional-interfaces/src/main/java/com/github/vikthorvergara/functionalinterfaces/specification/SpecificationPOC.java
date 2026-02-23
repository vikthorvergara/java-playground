package com.github.vikthorvergara.functionalinterfaces.specification;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class SpecificationPOC {

    @FunctionalInterface
    interface Spec<T> {
        boolean isSatisfiedBy(T item);

        default Spec<T> and(Spec<T> other) {
            return item -> isSatisfiedBy(item) && other.isSatisfiedBy(item);
        }

        default Spec<T> or(Spec<T> other) {
            return item -> isSatisfiedBy(item) || other.isSatisfiedBy(item);
        }

        default Spec<T> not() {
            return item -> !isSatisfiedBy(item);
        }
    }

    record Product(String name, double price, String category, boolean inStock) {}

    record PriceRange(double min, double max) implements Spec<Product> {
        @Override
        public boolean isSatisfiedBy(Product p) {
            return p.price() >= min && p.price() <= max;
        }

        @Override
        public String toString() {
            return "PriceRange[$%.0f-$%.0f]".formatted(min, max);
        }
    }

    record InCategory(String category) implements Spec<Product> {
        @Override
        public boolean isSatisfiedBy(Product p) {
            return p.category().equalsIgnoreCase(category);
        }

        @Override
        public String toString() {
            return "InCategory[%s]".formatted(category);
        }
    }

    record InStock() implements Spec<Product> {
        @Override
        public boolean isSatisfiedBy(Product p) {
            return p.inStock();
        }

        @Override
        public String toString() {
            return "InStock";
        }
    }

    static <T> T retry(Supplier<T> action, int maxAttempts) {
        Exception last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                last = e;
                System.out.println("    attempt %d/%d failed: %s".formatted(i, maxAttempts, e.getMessage()));
            }
        }
        throw new RuntimeException("all %d attempts failed".formatted(maxAttempts), last);
    }

    static <T> T retry(Supplier<T> action, int maxAttempts, Supplier<T> fallback) {
        try {
            return retry(action, maxAttempts);
        } catch (RuntimeException e) {
            System.out.println("    using fallback");
            return fallback.get();
        }
    }

    public static void main(String[] args) {
        var products = List.of(
            new Product("RTX 5090", 2499.99, "GPU", true),
            new Product("RX 9070 XT", 549.99, "GPU", false),
            new Product("Keychron Q1", 169.00, "Keyboard", true),
            new Product("MX Keys", 119.99, "Keyboard", true),
            new Product("JBL Tune 520BT", 49.95, "Audio", false),
            new Product("AirPods Pro", 249.00, "Audio", true));

        System.out.println("=== named specifications ===");
        var affordable = new PriceRange(0, 200);
        var gpu = new InCategory("GPU");
        var keyboard = new InCategory("Keyboard");
        var inStock = new InStock();

        System.out.println("  " + affordable + ": " + products.stream()
            .filter(affordable::isSatisfiedBy).map(Product::name).toList());
        System.out.println("  " + gpu + ": " + products.stream()
            .filter(gpu::isSatisfiedBy).map(Product::name).toList());
        System.out.println("  " + inStock + ": " + products.stream()
            .filter(inStock::isSatisfiedBy).map(Product::name).toList());

        System.out.println("\n=== composed specifications ===");
        var affordableKeyboard = affordable.and(keyboard).and(inStock);
        System.out.println("  affordable in-stock keyboard: " + products.stream()
            .filter(affordableKeyboard::isSatisfiedBy).map(Product::name).toList());

        var gpuOrAudio = gpu.or(new InCategory("Audio"));
        System.out.println("  GPU or Audio: " + products.stream()
            .filter(gpuOrAudio::isSatisfiedBy).map(Product::name).toList());

        var notInStock = inStock.not();
        System.out.println("  out of stock: " + products.stream()
            .filter(notInStock::isSatisfiedBy).map(Product::name).toList());

        var expensiveInStockGpu = new PriceRange(500, 10000).and(gpu).and(inStock);
        System.out.println("  expensive in-stock GPU: " + products.stream()
            .filter(expensiveInStockGpu::isSatisfiedBy).map(Product::name).toList());

        System.out.println("\n=== retry (succeeds on 3rd attempt) ===");
        var counter = new AtomicInteger(0);
        var result = retry(() -> {
            int attempt = counter.incrementAndGet();
            if (attempt < 3) throw new RuntimeException("connection timeout");
            return "data loaded (attempt %d)".formatted(attempt);
        }, 5);
        System.out.println("  result: " + result);

        System.out.println("\n=== retry (all attempts fail, uses fallback) ===");
        var fallbackResult = retry(
            () -> { throw new RuntimeException("service unavailable"); },
            3,
            () -> "cached response");
        System.out.println("  result: " + fallbackResult);

        System.out.println("\n=== retry (succeeds immediately) ===");
        var instant = retry(() -> 42, 3);
        System.out.println("  result: " + instant);
    }
}
