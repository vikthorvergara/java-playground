package com.github.vikthorvergara.reflection.mapper;

import java.lang.reflect.Field;
import java.util.List;

public class ObjectMapperPOC {

    static class UserEntity {
        private String name;
        private int age;
        private String email;

        UserEntity(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        @Override
        public String toString() {
            return "UserEntity{name='%s', age=%d, email='%s'}".formatted(name, age, email);
        }
    }

    static class UserDTO {
        private String name;
        private int age;
        private String email;

        @Override
        public String toString() {
            return "UserDTO{name='%s', age=%d, email='%s'}".formatted(name, age, email);
        }
    }

    static class ProductEntity {
        private String name;
        private double price;
        private String category;

        ProductEntity(String name, double price, String category) {
            this.name = name;
            this.price = price;
            this.category = category;
        }

        @Override
        public String toString() {
            return "ProductEntity{name='%s', price=%.2f, category='%s'}".formatted(name, price, category);
        }
    }

    static class ProductDTO {
        private String name;
        private double price;

        @Override
        public String toString() {
            return "ProductDTO{name='%s', price=%.2f}".formatted(name, price);
        }
    }

    static <T> T map(Object source, Class<T> targetClass) throws Exception {
        var target = targetClass.getDeclaredConstructor().newInstance();
        var sourceFields = source.getClass().getDeclaredFields();

        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);
            try {
                var targetField = targetClass.getDeclaredField(sourceField.getName());
                if (targetField.getType().equals(sourceField.getType())) {
                    targetField.setAccessible(true);
                    targetField.set(target, sourceField.get(source));
                }
            } catch (NoSuchFieldException ignored) {
            }
        }

        return target;
    }

    public static void main(String[] args) throws Exception {
        var users = List.of(
                new UserEntity("Alice", 30, "alice@mail.com"),
                new UserEntity("Bob", 35, "bob@mail.com"),
                new UserEntity("Martin", 62, "martin@mail.com"),
                new UserEntity("Vikthor", 30, "vikthor@mail.com")
        );

        var products = List.of(
                new ProductEntity("Laptop", 1200.00, "Electronics"),
                new ProductEntity("Mouse", 25.99, "Electronics"),
                new ProductEntity("Monitor", 450.00, "Electronics"),
                new ProductEntity("RTX 5090", 2499.99, "Gaming")
        );

        System.out.println("=== User Mapping (UserEntity -> UserDTO) ===");
        for (var user : users) {
            System.out.println("Source: " + user);
            var dto = map(user, UserDTO.class);
            System.out.println("Target: " + dto);
            System.out.println();
        }

        System.out.println("=== Product Mapping (ProductEntity -> ProductDTO) ===");
        for (var product : products) {
            System.out.println("Source: " + product);
            var dto = map(product, ProductDTO.class);
            System.out.println("Target: " + dto);
            System.out.println();
        }

        System.out.println("=== Field Coverage Report ===");
        System.out.printf("UserEntity fields: %d -> UserDTO fields: %d (all mapped)%n",
                UserEntity.class.getDeclaredFields().length,
                UserDTO.class.getDeclaredFields().length);
        System.out.printf("ProductEntity fields: %d -> ProductDTO fields: %d (category excluded)%n",
                ProductEntity.class.getDeclaredFields().length,
                ProductDTO.class.getDeclaredFields().length);
    }
}
