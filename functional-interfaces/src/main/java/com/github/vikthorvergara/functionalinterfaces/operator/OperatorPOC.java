package com.github.vikthorvergara.functionalinterfaces.operator;

import java.util.ArrayList;
import static java.util.Comparator.comparingDouble;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class OperatorPOC {

    record Product(String name, double price) {}

    public static void main(String[] args) {
        System.out.println("=== UnaryOperator - apply ===");
        UnaryOperator<Double> applyTax = price -> price * 1.1;
        UnaryOperator<Double> applyDiscount = price -> price * 0.85;
        System.out.println("100 with tax: " + applyTax.apply(100.0));
        System.out.println("100 with discount: " + applyDiscount.apply(100.0));

        System.out.println("\n=== UnaryOperator - andThen / compose ===");
        UnaryOperator<String> trim = String::trim;
        UnaryOperator<String> lower = String::toLowerCase;
        UnaryOperator<String> normalize = trim.andThen(lower)::apply;
        System.out.println("Normalized: '" + normalize.apply("  HELLO World  ") + "'");

        UnaryOperator<Double> taxThenDiscount = applyTax.andThen(applyDiscount)::apply;
        UnaryOperator<Double> discountThenTax = applyTax.compose(applyDiscount)::apply;
        System.out.println("1000 tax then discount: " + taxThenDiscount.apply(1000.0));
        System.out.println("1000 discount then tax: " + discountThenTax.apply(1000.0));

        System.out.println("\n=== UnaryOperator - identity ===");
        UnaryOperator<String> noChange = UnaryOperator.identity();
        System.out.println("Identity: " + noChange.apply("same"));

        System.out.println("\n=== UnaryOperator - List.replaceAll ===");
        var prices = new ArrayList<>(List.of(100.0, 200.0, 300.0));
        prices.replaceAll(applyTax::apply);
        System.out.println("Prices with tax: " + prices);

        System.out.println("\n=== BinaryOperator - apply ===");
        BinaryOperator<Double> sum = Double::sum;
        BinaryOperator<Double> max = Double::max;
        System.out.println("10 + 20 = " + sum.apply(10.0, 20.0));
        System.out.println("max(10, 20) = " + max.apply(10.0, 20.0));

        System.out.println("\n=== BinaryOperator - reduce ===");
        var products = List.of(
            new Product("Laptop", 1200.0),
            new Product("Mouse", 48.99),
            new Product("Monitor", 450.0));

        var total = products.stream().map(Product::price).reduce(sum).orElse(0.0);
        System.out.println("Total: $%.2f".formatted(total));

        var mostExpensive = products.stream()
            .reduce(BinaryOperator.maxBy(comparingDouble(Product::price)))
            .map(Product::name)
            .orElse("none");
        System.out.println("Most expensive: " + mostExpensive);

        var cheapest = products.stream()
            .reduce(BinaryOperator.minBy(comparingDouble(Product::price)))
            .map(Product::name)
            .orElse("none");
        System.out.println("Cheapest: " + cheapest);
    }
}
