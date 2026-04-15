package com.github.vikthorvergara.jdk.basics;

import java.util.List;
import java.util.stream.Collectors;

public class StreamCollectorsPOC {

    record Product(String name, String category, double price, int stock) {}

    static final List<Product> PRODUCTS = List.of(
            new Product("apple", "fruit", 1.20, 50),
            new Product("banana", "fruit", 0.50, 120),
            new Product("cherry", "fruit", 3.00, 30),
            new Product("carrot", "veggie", 0.80, 80),
            new Product("broccoli", "veggie", 2.10, 40),
            new Product("salmon", "fish", 12.50, 15),
            new Product("tuna", "fish", 9.00, 22)
    );

    static void reduceOperations() {
        var numbers = List.of(1, 2, 3, 4, 5);

        var sumWithIdentity = numbers.stream().reduce(0, Integer::sum);
        System.out.println("reduce(0, sum) -> " + sumWithIdentity);

        var productWithIdentity = numbers.stream().reduce(1, (a, b) -> a * b);
        System.out.println("reduce(1, *) -> " + productWithIdentity);

        var maxNoIdentity = numbers.stream().reduce(Integer::max);
        System.out.println("reduce(max) -> " + maxNoIdentity.orElseThrow());

        var words = List.of("hello", "streams", "and", "reduce");
        var totalChars = words.stream().reduce(0, (acc, w) -> acc + w.length(), Integer::sum);
        System.out.println("reduce(0, lenAccum, combiner) -> " + totalChars);
    }

    static void groupingByCollectors() {
        var byCategory = PRODUCTS.stream()
                .collect(Collectors.groupingBy(Product::category));
        System.out.println("groupingBy(category) -> " + byCategory);

        var countByCategory = PRODUCTS.stream()
                .collect(Collectors.groupingBy(Product::category, Collectors.counting()));
        System.out.println("groupingBy(category, counting) -> " + countByCategory);

        var totalStockByCategory = PRODUCTS.stream()
                .collect(Collectors.groupingBy(Product::category, Collectors.summingInt(Product::stock)));
        System.out.println("groupingBy(category, summingInt stock) -> " + totalStockByCategory);

        var namesByCategory = PRODUCTS.stream()
                .collect(Collectors.groupingBy(Product::category,
                        Collectors.mapping(Product::name, Collectors.toList())));
        System.out.println("groupingBy(category, mapping name) -> " + namesByCategory);
    }

    static void partitioningJoiningToMap() {
        var expensivePartition = PRODUCTS.stream()
                .collect(Collectors.partitioningBy(p -> p.price() > 2.0));
        System.out.println("partitioningBy(price > 2.0) -> " + expensivePartition);

        var joined = PRODUCTS.stream()
                .map(Product::name)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining(, [ ]) -> " + joined);

        var nameToPrice = PRODUCTS.stream()
                .collect(Collectors.toMap(Product::name, Product::price));
        System.out.println("toMap(name, price) -> " + nameToPrice);

        var productsWithDuplicates = List.of(
                new Product("apple", "fruit", 1.20, 50),
                new Product("apple", "fruit", 1.50, 10)
        );
        var mergedByName = productsWithDuplicates.stream()
                .collect(Collectors.toMap(Product::name, Product::price, (a, b) -> a + b));
        System.out.println("toMap(name, price, mergeSum) -> " + mergedByName);

        var unmodifiable = PRODUCTS.stream()
                .map(Product::name)
                .collect(Collectors.toUnmodifiableList());
        System.out.println("toUnmodifiableList() -> " + unmodifiable);
    }

    static void summaryStatistics() {
        long count = PRODUCTS.stream().collect(Collectors.counting());
        System.out.println("counting() -> " + count);

        int totalStock = PRODUCTS.stream().collect(Collectors.summingInt(Product::stock));
        System.out.println("summingInt(stock) -> " + totalStock);

        double avgPrice = PRODUCTS.stream().collect(Collectors.averagingDouble(Product::price));
        System.out.println("averagingDouble(price) -> " + avgPrice);

        var stats = PRODUCTS.stream().collect(Collectors.summarizingInt(Product::stock));
        System.out.println("summarizingInt(stock) -> " + stats);
    }

    public static void main(String[] args) {
        System.out.println("--- reduce operations ---");
        reduceOperations();

        System.out.println("\n--- groupingBy collectors ---");
        groupingByCollectors();

        System.out.println("\n--- partitioning, joining, toMap ---");
        partitioningJoiningToMap();

        System.out.println("\n--- summary statistics ---");
        summaryStatistics();
    }
}
