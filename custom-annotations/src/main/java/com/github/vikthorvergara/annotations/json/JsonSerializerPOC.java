package com.github.vikthorvergara.annotations.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class JsonSerializerPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface JsonField {
        String name();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface JsonIgnore {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface JsonRequired {
    }

    public static class JsonSerializer {

        public static String serialize(Object obj) {
            var clazz = obj.getClass();
            var joiner = new StringJoiner(", ", "{", "}");

            for (var field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }

                field.setAccessible(true);

                var key = field.isAnnotationPresent(JsonField.class)
                        ? field.getAnnotation(JsonField.class).name()
                        : field.getName();

                try {
                    var value = field.get(obj);
                    joiner.add("\"" + key + "\": " + formatValue(value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            return joiner.toString();
        }

        private static String formatValue(Object value) {
            if (value == null) {
                return "null";
            }
            if (value instanceof String) {
                return "\"" + value + "\"";
            }
            return value.toString();
        }

        public static <T> T deserialize(String json, Class<T> clazz) {
            try {
                var instance = clazz.getDeclaredConstructor().newInstance();
                var parsed = parseJson(json);

                for (var field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(JsonIgnore.class)) {
                        continue;
                    }

                    var key = field.isAnnotationPresent(JsonField.class)
                            ? field.getAnnotation(JsonField.class).name()
                            : field.getName();

                    if (field.isAnnotationPresent(JsonRequired.class) && !parsed.containsKey(key)) {
                        throw new RuntimeException("Required field missing: " + key);
                    }

                    if (parsed.containsKey(key)) {
                        field.setAccessible(true);
                        var rawValue = parsed.get(key);
                        field.set(instance, convertValue(rawValue, field.getType()));
                    }
                }

                return instance;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        private static Map<String, String> parseJson(String json) {
            var map = new HashMap<String, String>();
            var trimmed = json.trim();
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();

            int i = 0;
            while (i < trimmed.length()) {
                int keyStart = trimmed.indexOf('"', i);
                if (keyStart == -1) break;
                int keyEnd = trimmed.indexOf('"', keyStart + 1);
                var key = trimmed.substring(keyStart + 1, keyEnd);

                int colonPos = trimmed.indexOf(':', keyEnd);
                int valueStart = colonPos + 1;

                while (valueStart < trimmed.length() && trimmed.charAt(valueStart) == ' ') {
                    valueStart++;
                }

                String value;
                if (trimmed.charAt(valueStart) == '"') {
                    int valueEnd = trimmed.indexOf('"', valueStart + 1);
                    value = trimmed.substring(valueStart + 1, valueEnd);
                    i = valueEnd + 1;
                } else {
                    int valueEnd = trimmed.indexOf(',', valueStart);
                    if (valueEnd == -1) {
                        valueEnd = trimmed.length();
                    }
                    value = trimmed.substring(valueStart, valueEnd).trim();
                    i = valueEnd + 1;
                }

                map.put(key, value);
            }

            return map;
        }

        private static Object convertValue(String raw, Class<?> type) {
            if (type == String.class) return raw;
            if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
            if (type == double.class || type == Double.class) return Double.parseDouble(raw);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
            return raw;
        }
    }

    public static class Product {
        @JsonField(name = "product_name")
        @JsonRequired
        private String name;

        @JsonField(name = "product_price")
        private double price;

        private String category;

        @JsonIgnore
        private String internalCode;

        public Product() {
        }

        public Product(String name, double price, String category, String internalCode) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.internalCode = internalCode;
        }

        @Override
        public String toString() {
            return "Product{name='" + name + "', price=" + price +
                    ", category='" + category + "', internalCode='" + internalCode + "'}";
        }
    }

    public static void main(String[] args) {
        var rtx5090 = new Product("RTX 5090", 2499.99, "Gaming", "NV-5090-INT");
        var keychronQ1 = new Product("Keychron Q1", 169.0, "Keyboard", "KC-Q1-INT");
        var airpodsPro = new Product("AirPods Pro", 249.0, "Audio", "APL-APP-INT");

        System.out.println("=== Serialization ===");
        var json1 = JsonSerializer.serialize(rtx5090);
        var json2 = JsonSerializer.serialize(keychronQ1);
        var json3 = JsonSerializer.serialize(airpodsPro);
        System.out.println(json1);
        System.out.println(json2);
        System.out.println(json3);

        System.out.println("\n=== Deserialization ===");
        var jsonInput = "{\"product_name\": \"RTX 5090\", \"product_price\": 2499.99, \"category\": \"Gaming\"}";
        System.out.println("Input: " + jsonInput);
        var deserialized = JsonSerializer.deserialize(jsonInput, Product.class);
        System.out.println("Output: " + deserialized);

        System.out.println("\n=== Required Field Validation ===");
        try {
            var missingRequired = "{\"product_price\": 999.99, \"category\": \"Test\"}";
            JsonSerializer.deserialize(missingRequired, Product.class);
        } catch (RuntimeException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }
    }
}
