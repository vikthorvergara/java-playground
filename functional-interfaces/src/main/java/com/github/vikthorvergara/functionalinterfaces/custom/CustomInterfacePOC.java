package com.github.vikthorvergara.functionalinterfaces.custom;

import java.util.List;

public class CustomInterfacePOC {

    record Product(String name, double price, String category) {}

    @FunctionalInterface
    interface Validator<T> {
        boolean validate(T item);

        default Validator<T> and(Validator<T> other) {
            return item -> validate(item) && other.validate(item);
        }

        default Validator<T> or(Validator<T> other) {
            return item -> validate(item) || other.validate(item);
        }

        default Validator<T> negate() {
            return item -> !validate(item);
        }
    }

    @FunctionalInterface
    interface Transformer<T, R> {
        R transform(T input);

        default <V> Transformer<T, V> andThen(Transformer<R, V> after) {
            return input -> after.transform(transform(input));
        }
    }

    @FunctionalInterface
    interface Action<T> {
        void execute(T item);

        default Action<T> then(Action<T> next) {
            return item -> {
                execute(item);
                next.execute(item);
            };
        }
    }

    public static void main(String[] args) {
        var products = List.of(
            new Product("GPU", 2000.00, "Gaming"),
            new Product("Mouse", 48.99, "Electronics"),
            new Product("Coffee", 8.50, "Food"),
            new Product("Monitor", 450.00, "Electronics"),
            new Product("Egg", 0.49, "Food"));

        System.out.println("=== Validator ===");
        Validator<Product> isExpensive = p -> p.price() > 100;
        Validator<Product> isFood = p -> p.category().equals("Food");
        Validator<Product> cheapFood = isFood.and(isExpensive.negate());

        products.forEach(p -> {
            if (cheapFood.validate(p)) System.out.println("  Cheap food: " + p.name());
        });

        System.out.println("\n=== Transformer ===");
        Transformer<Product, String> toLabel = p -> "%s ($%.2f)".formatted(p.name(), p.price());
        Transformer<Product, String> toUpperLabel = toLabel.andThen(String::toUpperCase);

        products.stream().map(toUpperLabel::transform).forEach(l -> System.out.println("  " + l));

        System.out.println("\n=== Transformer chain ===");
        Transformer<String, String> slugify = ((Transformer<String, String>) String::trim)
            .andThen(String::toLowerCase)
            .andThen(s -> s.replace(" ", "-"));
        System.out.println("  " + slugify.transform("  Hello World  "));

        System.out.println("\n=== Action ===");
        Action<Product> log = p -> System.out.print("  [LOG] " + p.name());
        Action<Product> price = p -> System.out.printf(" -> $%.2f%n", p.price());
        Action<Product> pipeline = log.then(price);

        products.forEach(pipeline::execute);

        System.out.println("\n=== combining all three ===");
        Validator<Product> filter = isExpensive;
        Transformer<Product, String> format = toLabel;
        Action<String> print = s -> System.out.println("  " + s);

        products.stream()
            .filter(filter::validate)
            .map(format::transform)
            .forEach(print::execute);
    }
}
