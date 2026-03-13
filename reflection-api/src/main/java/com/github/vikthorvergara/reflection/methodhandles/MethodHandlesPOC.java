package com.github.vikthorvergara.reflection.methodhandles;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MethodHandlesPOC {

    static class Product {
        private String name;
        private double price;
        private String category;

        Product(String name, double price, String category) {
            this.name = name;
            this.price = price;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public String getCategory() {
            return category;
        }

        public static String formatLabel(String name, String category) {
            return "[" + category + "] " + name;
        }

        @Override
        public String toString() {
            return name + " ($" + price + ", " + category + ")";
        }
    }

    static final int ITERATIONS = 1_000_000;

    public static void main(String[] args) throws Throwable {
        var rtx5090 = new Product("RTX 5090", 2499.99, "Gaming");
        var keychronQ1 = new Product("Keychron Q1", 169.00, "Keyboard");
        var airpodsPro = new Product("AirPods Pro", 249.00, "Audio");

        System.out.println("=== Products ===");
        System.out.println(rtx5090);
        System.out.println(keychronQ1);
        System.out.println(airpodsPro);

        System.out.println("\n=== Benchmark: Getter Invocation (x" + ITERATIONS + ") ===");
        benchmarkGetterInvocation(rtx5090);

        System.out.println("\n=== Benchmark: Private Field Access (x" + ITERATIONS + ") ===");
        benchmarkPrivateFieldAccess(keychronQ1);

        System.out.println("\n=== Benchmark: Static Method Invocation (x" + ITERATIONS + ") ===");
        benchmarkStaticMethod(airpodsPro);

        System.out.println("\n=== Verification: All Products via MethodHandles ===");
        verifyAllProducts(rtx5090, keychronQ1, airpodsPro);
    }

    static void benchmarkGetterInvocation(Product product) throws Throwable {
        Method reflectMethod = Product.class.getMethod("getName");

        var lookup = MethodHandles.lookup();
        MethodHandle mh = lookup.findVirtual(Product.class, "getName", MethodType.methodType(String.class));

        for (int i = 0; i < 10_000; i++) {
            reflectMethod.invoke(product);
            mh.invoke(product);
        }

        var reflectStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            reflectMethod.invoke(product);
        }
        var reflectTime = System.nanoTime() - reflectStart;

        var mhStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mh.invoke(product);
        }
        var mhTime = System.nanoTime() - mhStart;

        printResult("getName()", reflectTime, mhTime);
    }

    static void benchmarkPrivateFieldAccess(Product product) throws Throwable {
        Field reflectField = Product.class.getDeclaredField("price");
        reflectField.setAccessible(true);

        var lookup = MethodHandles.privateLookupIn(Product.class, MethodHandles.lookup());
        MethodHandle mh = lookup.unreflectGetter(reflectField);

        for (int i = 0; i < 10_000; i++) {
            reflectField.get(product);
            mh.invoke(product);
        }

        var reflectStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            reflectField.get(product);
        }
        var reflectTime = System.nanoTime() - reflectStart;

        var mhStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mh.invoke(product);
        }
        var mhTime = System.nanoTime() - mhStart;

        printResult("field 'price'", reflectTime, mhTime);
    }

    static void benchmarkStaticMethod(Product product) throws Throwable {
        Method reflectMethod = Product.class.getMethod("formatLabel", String.class, String.class);

        var lookup = MethodHandles.lookup();
        MethodHandle mh = lookup.findStatic(Product.class, "formatLabel",
                MethodType.methodType(String.class, String.class, String.class));

        for (int i = 0; i < 10_000; i++) {
            reflectMethod.invoke(null, product.getName(), product.getCategory());
            mh.invoke(product.getName(), product.getCategory());
        }

        var reflectStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            reflectMethod.invoke(null, product.getName(), product.getCategory());
        }
        var reflectTime = System.nanoTime() - reflectStart;

        var mhStart = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            mh.invoke(product.getName(), product.getCategory());
        }
        var mhTime = System.nanoTime() - mhStart;

        printResult("formatLabel()", reflectTime, mhTime);
    }

    static void verifyAllProducts(Product... products) throws Throwable {
        var lookup = MethodHandles.privateLookupIn(Product.class, MethodHandles.lookup());

        MethodHandle getName = lookup.findVirtual(Product.class, "getName", MethodType.methodType(String.class));
        MethodHandle getPrice = lookup.findVirtual(Product.class, "getPrice", MethodType.methodType(double.class));
        MethodHandle getCategory = lookup.findVirtual(Product.class, "getCategory", MethodType.methodType(String.class));
        MethodHandle formatLabel = lookup.findStatic(Product.class, "formatLabel",
                MethodType.methodType(String.class, String.class, String.class));

        Field priceField = Product.class.getDeclaredField("price");
        priceField.setAccessible(true);
        MethodHandle priceGetter = lookup.unreflectGetter(priceField);

        System.out.printf("%-25s %-12s %-15s %-10s%n", "Label", "Category", "Price (getter)", "Price (field)");
        System.out.println("-".repeat(65));

        for (var product : products) {
            var name = (String) getName.invoke(product);
            var category = (String) getCategory.invoke(product);
            var priceViaGetter = (double) getPrice.invoke(product);
            var priceViaField = (double) priceGetter.invoke(product);
            var label = (String) formatLabel.invoke(name, category);

            System.out.printf("%-25s %-12s $%-14.2f $%-9.2f%n", label, category, priceViaGetter, priceViaField);
        }
    }

    static void printResult(String operation, long reflectNanos, long mhNanos) {
        var reflectMs = reflectNanos / 1_000_000.0;
        var mhMs = mhNanos / 1_000_000.0;
        var faster = reflectNanos > mhNanos ? "MethodHandle" : "Reflection";
        var ratio = reflectNanos > mhNanos
                ? (double) reflectNanos / mhNanos
                : (double) mhNanos / reflectNanos;

        System.out.printf("%-20s | %-18s | %-18s | %-15s%n",
                "Operation", "Reflection (ms)", "MethodHandle (ms)", "Winner");
        System.out.println("-".repeat(78));
        System.out.printf("%-20s | %-18.2f | %-18.2f | %s (%.1fx)%n",
                operation, reflectMs, mhMs, faster, ratio);
    }
}
