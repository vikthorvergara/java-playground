package com.github.vikthorvergara.functionalinterfaces.function;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionPOC {

    record Product(String name, double price) {}

    public static void main(String[] args) {
        var products = List.of(
            new Product("Laptop", 1200.00),
            new Product("Mouse", 25.99),
            new Product("Monitor", 450.00));

        System.out.println("=== apply ===");
        Function<Product, String> label = p -> "%s ($%.2f)".formatted(p.name(), p.price());
        System.out.println("Labels: " + products.stream().map(label).toList());

        System.out.println("\n=== andThen ===");
        Function<Product, String> upperLabel = label.andThen(String::toUpperCase);
        System.out.println("Upper: " + products.stream().map(upperLabel).toList());

        System.out.println("\n=== compose ===");
        Function<Double, Double> tax = price -> price * 1.1;
        Function<Double, String> format = price -> "$%.2f".formatted(price);
        Function<Double, String> priceWithTax = format.compose(tax);
        System.out.println("1200 with tax: " + priceWithTax.apply(1200.0));

        System.out.println("\n=== identity ===");
        System.out.println(Function.identity().apply("returns itself"));

        System.out.println("\n=== chaining ===");
        Function<String, String> slugify = ((Function<String, String>) String::trim)
            .andThen(String::toLowerCase)
            .andThen(s -> s.replace(" ", "-"));
        System.out.println("Slug: " + slugify.apply("  Hello World From Java  "));

        System.out.println("\n=== BiFunction ===");
        BiFunction<String, Double, String> formatProduct = (name, price) ->
            "%s costs $%.2f".formatted(name, price);
        products.forEach(p -> System.out.println("  " + formatProduct.apply(p.name(), p.price())));

        BiFunction<Double, Double, Double> applyDiscount = (price, percent) -> price * (1 - percent / 100);
        System.out.println("1200 with 15%% off: $%.2f".formatted(applyDiscount.apply(1200.0, 15.0)));

        BiFunction<Double, Double, String> discountAndFormat = applyDiscount.andThen(format);
        System.out.println("450 with 10% off: " + discountAndFormat.apply(450.0, 10.0));
    }
}
