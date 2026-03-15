package com.github.vikthorvergara.reflection.generictypes;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenericTypesPOC {

    record Product(String name, double price) {}

    static class Catalog {
        List<String> names;
        Map<String, Double> prices;
        List<? extends Number> scores;
        Map<String, List<String>> categoryProducts;
        Optional<String> description;
        Comparable<Product>[] rankings;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Generic Types Reflection ===\n");

        var products = List.of(
                new Product("RTX 5090", 1999.99),
                new Product("MX Keys", 109.99),
                new Product("JBL Tune 520BT", 49.99)
        );

        System.out.println("Products:");
        for (var p : products) {
            System.out.println("  - %s ($%.2f)".formatted(p.name(), p.price()));
        }

        System.out.println("\n=== Inspecting Catalog Fields ===\n");

        for (var field : Catalog.class.getDeclaredFields()) {
            System.out.println("Field: " + field.getName());
            var genericType = field.getGenericType();
            inspectType(genericType, 1);
            System.out.println();
        }

        System.out.println("=== Inspecting Generic Method Signature ===\n");

        Method sortMethod = GenericTypesPOC.class.getDeclaredMethod("sort", List.class);
        System.out.println("Method: " + sortMethod.getName());

        var typeParams = sortMethod.getTypeParameters();
        System.out.println("  Type parameters: " + typeParams.length);
        for (var tp : typeParams) {
            System.out.println("    TypeVariable: " + tp.getName());
            for (var bound : tp.getBounds()) {
                System.out.println("      Bound: " + bound.getTypeName());
                inspectType(bound, 3);
            }
        }

        var returnType = sortMethod.getGenericReturnType();
        System.out.println("  Return type: " + returnType.getTypeName());
        inspectType(returnType, 2);

        var paramTypes = sortMethod.getGenericParameterTypes();
        for (var pt : paramTypes) {
            System.out.println("  Parameter type: " + pt.getTypeName());
            inspectType(pt, 2);
        }
    }

    private static void inspectType(Type type, int depth) {
        var indent = "  ".repeat(depth);

        System.out.println(indent + "Type class: " + type.getClass().getSimpleName());
        System.out.println(indent + "Type name: " + type.getTypeName());

        if (type instanceof ParameterizedType pt) {
            System.out.println(indent + "[ParameterizedType]");
            System.out.println(indent + "Raw type: " + pt.getRawType().getTypeName());
            var typeArgs = pt.getActualTypeArguments();
            System.out.println(indent + "Type arguments (" + typeArgs.length + "):");
            for (int i = 0; i < typeArgs.length; i++) {
                System.out.println(indent + "  [" + i + "] " + typeArgs[i].getTypeName());
                inspectType(typeArgs[i], depth + 2);
            }
        } else if (type instanceof WildcardType wt) {
            System.out.println(indent + "[WildcardType]");
            for (var upper : wt.getUpperBounds()) {
                System.out.println(indent + "Upper bound: " + upper.getTypeName());
            }
            for (var lower : wt.getLowerBounds()) {
                System.out.println(indent + "Lower bound: " + lower.getTypeName());
            }
        } else if (type instanceof TypeVariable<?> tv) {
            System.out.println(indent + "[TypeVariable]");
            System.out.println(indent + "Name: " + tv.getName());
            for (var bound : tv.getBounds()) {
                System.out.println(indent + "Bound: " + bound.getTypeName());
            }
        } else if (type instanceof GenericArrayType gat) {
            System.out.println(indent + "[GenericArrayType]");
            System.out.println(indent + "Component type: " + gat.getGenericComponentType().getTypeName());
            inspectType(gat.getGenericComponentType(), depth + 1);
        } else if (type instanceof Class<?> clazz) {
            System.out.println(indent + "[Class]");
            if (clazz.isArray()) {
                System.out.println(indent + "Array component type: " + clazz.getComponentType().getTypeName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable<T>> List<T> sort(List<T> items) {
        var sorted = new java.util.ArrayList<>(items);
        java.util.Collections.sort(sorted);
        return sorted;
    }
}
