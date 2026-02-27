package com.github.vikthorvergara.reflection.modification;

import java.lang.reflect.Field;

public class FieldModificationPOC {

    static class AppConfig {
        private String environment = "development";
        private int maxConnections = 10;
        private boolean debugMode = false;
        private final String appName = "MyApp";
        private static int instanceCount = 0;

        public AppConfig() {
            instanceCount++;
        }

        @Override
        public String toString() {
            return "AppConfig{env='%s', maxConn=%d, debug=%s, app='%s', instances=%d}"
                    .formatted(environment, maxConnections, debugMode, appName, instanceCount);
        }
    }

    static class Credentials {
        private String username;
        private String password;
        private String[] roles;

        public Credentials(String username, String password, String... roles) {
            this.username = username;
            this.password = password;
            this.roles = roles;
        }

        @Override
        public String toString() {
            return "Credentials{user='%s', pass='%s', roles=%s}"
                    .formatted(username, password, java.util.Arrays.toString(roles));
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Reading Private Fields ===");
        var config = new AppConfig();
        Class<?> clazz = config.getClass();

        Field envField = clazz.getDeclaredField("environment");
        envField.setAccessible(true);
        System.out.println("environment = " + envField.get(config));

        Field maxConnField = clazz.getDeclaredField("maxConnections");
        maxConnField.setAccessible(true);
        System.out.println("maxConnections = " + maxConnField.getInt(config));

        Field debugField = clazz.getDeclaredField("debugMode");
        debugField.setAccessible(true);
        System.out.println("debugMode = " + debugField.getBoolean(config));

        System.out.println("\n=== Modifying Private Fields ===");
        System.out.println("Before: " + config);

        envField.set(config, "production");
        maxConnField.setInt(config, 100);
        debugField.setBoolean(config, true);

        System.out.println("After:  " + config);

        System.out.println("\n=== Modifying Final Fields ===");
        Field appNameField = clazz.getDeclaredField("appName");
        appNameField.setAccessible(true);
        System.out.println("appName (final) = " + appNameField.get(config));
        try {
            appNameField.set(config, "ChangedApp");
            System.out.println("Final field modified to: " + appNameField.get(config));
        } catch (IllegalAccessException e) {
            System.out.println("Cannot modify final field: " + e.getMessage());
        }

        System.out.println("\n=== Modifying Static Fields ===");
        Field countField = clazz.getDeclaredField("instanceCount");
        countField.setAccessible(true);
        System.out.println("instanceCount (static) = " + countField.getInt(null));
        countField.setInt(null, 999);
        System.out.println("instanceCount (after) = " + countField.getInt(null));

        System.out.println("\n=== Bulk Field Inspection and Copy ===");
        var creds = new Credentials("admin", "s3cret", "ADMIN", "USER");
        System.out.println("Original: " + creds);

        var copy = new Credentials("", "", "");
        for (Field field : creds.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(creds);
            field.set(copy, value);
        }
        System.out.println("Copy:     " + copy);

        System.out.println("\n=== Field Type Analysis ===");
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            System.out.printf("  %-15s type=%-10s primitive=%-5s value=%s%n",
                    field.getName(),
                    field.getType().getSimpleName(),
                    field.getType().isPrimitive(),
                    field.get(config));
        }
    }
}
