package com.github.vikthorvergara.functionalinterfaces.predicate;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class PredicatePOC {

    record Product(String name, double price, String category) {}

    public static void main(String[] args) {
        var products = List.of(
            new Product("GPU", 2000.00, "Gaming"),
            new Product("Mouse", 48.99, "Electronics"),
            new Product("Coffee", 8.50, "Food"),
            new Product("Monitor", 450.00, "Electronics"),
            new Product("Egg", 0.49, "Food"));

        Predicate<Product> isExpensive = p -> p.price() > 100;
        Predicate<Product> isElectronics = p -> p.category().equals("Electronics");

        System.out.println("=== test ===");
        System.out.println("Expensive: " + products.stream().filter(isExpensive).map(Product::name).toList());

        System.out.println("\n=== negate ===");
        System.out.println("Cheap: " + products.stream().filter(isExpensive.negate()).map(Product::name).toList());

        System.out.println("\n=== and ===");
        System.out.println("Cheap electronics: " + products.stream().filter(isExpensive.negate().and(isElectronics)).map(Product::name).toList());

        System.out.println("\n=== or ===");
        System.out.println("Expensive or not eletronics: " + products.stream().filter(isExpensive.or(isElectronics.negate())).map(Product::name).toList());

        System.out.println("\n=== Predicate.not ===");
        var words = List.of("hello", "", "world", "  ", "java");
        System.out.println("Non-blank: " + words.stream().filter(Predicate.not(String::isBlank)).toList());

        System.out.println("\n=== Predicate.isEqual ===");
        Predicate<String> isJava = Predicate.isEqual("java");
        System.out.println("Is 'java': " + isJava.test("java"));
        System.out.println("Is 'python': " + isJava.test("python"));

        System.out.println("\n=== BiPredicate ===");
        BiPredicate<String, Double> isAffordable = (category, price) ->
            category.equals("Food") ? price < 5 : price < 100;
        products.forEach(p -> System.out.println("  %s affordable? %s".formatted(
            p.name(), isAffordable.test(p.category(), p.price()))));

        BiPredicate<String, Double> isGaming = (category, price) -> category.equals("Gaming");
        BiPredicate<String, Double> affordableOrGaming = isAffordable.or(isGaming);
        products.forEach(p -> System.out.println("  %s affordable or gaming? %s".formatted(
            p.name(), affordableOrGaming.test(p.category(), p.price()))));
    }
}
