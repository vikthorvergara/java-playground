package com.github.vikthorvergara.functionalinterfaces.consumer;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConsumerPOC {

    record Product(String name, double price, String category) {}

    public static void main(String[] args) {
        var products = List.of(
            new Product("GPU", 2000.00, "Gaming"),
            new Product("Mouse", 48.99, "Electronics"),
            new Product("Coffee", 8.50, "Food"));

        System.out.println("=== accept ===");
        Consumer<Product> print = p -> System.out.println("  " + p.name());
        products.forEach(print);

        System.out.println("\n=== andThen ===");
        Consumer<Product> printWithPrice = print
            .andThen(p -> System.out.println("    -> $%.2f".formatted(p.price())));
        products.forEach(printWithPrice);

        System.out.println("\n=== BiConsumer ===");
        BiConsumer<String, Double> printEntry = (name, price) ->
            System.out.println("  %s costs $%.2f".formatted(name, price));
        products.forEach(p -> printEntry.accept(p.name(), p.price()));
    }
}
