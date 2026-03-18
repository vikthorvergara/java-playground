package com.github.vikthorvergara.annotations.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.StringJoiner;

public class MiniOrmPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Table {
        String name();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Column {
        String name() default "";
        boolean nullable() default false;
        boolean unique() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Id {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface AutoIncrement {
    }

    @Table(name = "products")
    public static class Product {
        @Id @AutoIncrement @Column
        private int id;

        @Column(unique = true)
        private String name;

        @Column
        private double price;

        @Column(nullable = true)
        private String category;

        public Product() {
        }

        public Product(String name, double price, String category) {
            this.name = name;
            this.price = price;
            this.category = category;
        }

        public Product(int id, String name, double price, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
        }
    }

    @Table(name = "persons")
    public static class Person {
        @Id @AutoIncrement @Column
        private int id;

        @Column(unique = true)
        private String name;

        @Column
        private int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static class QueryBuilder {

        public static String createTable(Class<?> clazz) {
            var table = clazz.getAnnotation(Table.class);
            if (table == null) {
                throw new RuntimeException("Class " + clazz.getSimpleName() + " is not annotated with @Table");
            }

            var columns = new StringJoiner(", ");
            for (var field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Column.class)) continue;

                var col = field.getAnnotation(Column.class);
                var colName = col.name().isEmpty() ? field.getName() : col.name();
                var sqlType = mapType(field.getType());

                var def = new StringBuilder(colName + " " + sqlType);

                if (field.isAnnotationPresent(Id.class)) {
                    def.append(" PRIMARY KEY");
                }
                if (field.isAnnotationPresent(AutoIncrement.class)) {
                    def.append(" AUTO_INCREMENT");
                }
                if (!col.nullable() && !field.isAnnotationPresent(Id.class)) {
                    def.append(" NOT NULL");
                }
                if (col.unique()) {
                    def.append(" UNIQUE");
                }

                columns.add(def.toString());
            }

            return "CREATE TABLE " + table.name() + " (" + columns + ");";
        }

        public static String insert(Object obj) {
            var clazz = obj.getClass();
            var table = clazz.getAnnotation(Table.class);
            if (table == null) {
                throw new RuntimeException("Class " + clazz.getSimpleName() + " is not annotated with @Table");
            }

            var columnNames = new StringJoiner(", ");
            var values = new StringJoiner(", ");

            for (var field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Column.class)) continue;
                if (field.isAnnotationPresent(AutoIncrement.class)) continue;

                field.setAccessible(true);
                var col = field.getAnnotation(Column.class);
                var colName = col.name().isEmpty() ? field.getName() : col.name();
                columnNames.add(colName);

                try {
                    var value = field.get(obj);
                    values.add(formatSqlValue(value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            return "INSERT INTO " + table.name() + " (" + columnNames + ") VALUES (" + values + ");";
        }

        public static String select(Class<?> clazz) {
            return select(clazz, null);
        }

        public static String select(Class<?> clazz, String whereClause) {
            var table = clazz.getAnnotation(Table.class);
            if (table == null) {
                throw new RuntimeException("Class " + clazz.getSimpleName() + " is not annotated with @Table");
            }

            var columns = new StringJoiner(", ");
            for (var field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Column.class)) continue;
                var col = field.getAnnotation(Column.class);
                columns.add(col.name().isEmpty() ? field.getName() : col.name());
            }

            var sql = "SELECT " + columns + " FROM " + table.name();
            if (whereClause != null && !whereClause.isEmpty()) {
                sql += " WHERE " + whereClause;
            }
            return sql + ";";
        }

        public static String update(Object obj) {
            var clazz = obj.getClass();
            var table = clazz.getAnnotation(Table.class);
            if (table == null) {
                throw new RuntimeException("Class " + clazz.getSimpleName() + " is not annotated with @Table");
            }

            String idColumn = null;
            Object idValue = null;
            var setClauses = new StringJoiner(", ");

            for (var field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Column.class)) continue;

                field.setAccessible(true);
                var col = field.getAnnotation(Column.class);
                var colName = col.name().isEmpty() ? field.getName() : col.name();

                try {
                    var value = field.get(obj);

                    if (field.isAnnotationPresent(Id.class)) {
                        idColumn = colName;
                        idValue = value;
                        continue;
                    }

                    setClauses.add(colName + " = " + formatSqlValue(value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            if (idColumn == null) {
                throw new RuntimeException("No @Id field found in " + clazz.getSimpleName());
            }

            return "UPDATE " + table.name() + " SET " + setClauses + " WHERE " + idColumn + " = " + formatSqlValue(idValue) + ";";
        }

        public static String delete(Class<?> clazz, Object idValue) {
            var table = clazz.getAnnotation(Table.class);
            if (table == null) {
                throw new RuntimeException("Class " + clazz.getSimpleName() + " is not annotated with @Table");
            }

            String idColumn = null;
            for (var field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                    var col = field.getAnnotation(Column.class);
                    idColumn = col.name().isEmpty() ? field.getName() : col.name();
                    break;
                }
            }

            if (idColumn == null) {
                throw new RuntimeException("No @Id field found in " + clazz.getSimpleName());
            }

            return "DELETE FROM " + table.name() + " WHERE " + idColumn + " = " + formatSqlValue(idValue) + ";";
        }

        private static String mapType(Class<?> type) {
            if (type == String.class) return "VARCHAR(255)";
            if (type == int.class || type == Integer.class) return "INTEGER";
            if (type == double.class || type == Double.class) return "DECIMAL(10,2)";
            if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
            return "VARCHAR(255)";
        }

        private static String formatSqlValue(Object value) {
            if (value == null) return "NULL";
            if (value instanceof String) return "'" + value + "'";
            return value.toString();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CREATE TABLE ===");
        System.out.println(QueryBuilder.createTable(Product.class));
        System.out.println(QueryBuilder.createTable(Person.class));

        System.out.println("\n=== INSERT ===");
        var rtx5090 = new Product("RTX 5090", 2499.99, "Gaming");
        var vikthor = new Person("Vikthor", 30);
        System.out.println(QueryBuilder.insert(rtx5090));
        System.out.println(QueryBuilder.insert(vikthor));

        System.out.println("\n=== SELECT ===");
        System.out.println(QueryBuilder.select(Product.class));
        System.out.println(QueryBuilder.select(Product.class, "category = 'Gaming'"));

        System.out.println("\n=== UPDATE ===");
        var updatedProduct = new Product(1, "RTX 5090", 1999.99, "Gaming");
        System.out.println(QueryBuilder.update(updatedProduct));

        System.out.println("\n=== DELETE ===");
        System.out.println(QueryBuilder.delete(Product.class, 1));
        System.out.println(QueryBuilder.delete(Person.class, 5));
    }
}
