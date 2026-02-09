package com.github.vikthorvergara.functionalinterfaces.supplier;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SupplierPOC {

    record Product(String name, double price) {}

    public static void main(String[] args) {
        System.out.println("=== get ===");
        Supplier<Product> defaultProduct = () -> new Product("Unknown", 0.0);
        System.out.println("Default: " + defaultProduct.get());

        System.out.println("\n=== lazy evaluation ===");
        Supplier<Double> expensiveCalculation = () -> {
            System.out.println("  (calculating...)");
            return Math.sqrt(144) * Math.PI;
        };
        System.out.println("Result: " + expensiveCalculation.get());

        System.out.println("\n=== factory ===");
        Supplier<List<String>> listFactory = List::of;
        var list1 = listFactory.get();
        var list2 = listFactory.get();
        System.out.println("Same reference? " + (list1 == list2));

        System.out.println("\n=== Stream.generate ===");
        Supplier<Double> randomPrice = () -> Math.round(Math.random() * 10000) / 100.0;
        var prices = Stream.generate(randomPrice).limit(5).toList();
        System.out.println("Random prices: " + prices);

        System.out.println("\n=== orElseGet (deferred default) ===");
        List<Product> products = List.of();
        var first = products.stream().findFirst().orElseGet(defaultProduct);
        System.out.println("Fallback product: " + first);

        System.out.println("\n=== sequential ID generator ===");
        Supplier<String> idGenerator = new Supplier<>() {
            private int counter = 0;
            @Override
            public String get() {
                return "PROD-%04d".formatted(++counter);
            }
        };
        System.out.println(idGenerator.get());
        System.out.println(idGenerator.get());
        System.out.println(idGenerator.get());
    }
}
