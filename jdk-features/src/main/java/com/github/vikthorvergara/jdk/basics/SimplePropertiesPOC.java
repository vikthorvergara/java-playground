package com.github.vikthorvergara.jdk.basics;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SimplePropertiesPOC {

    static void setAndGet() {
        var props = new Properties();
        props.setProperty("app.name", "playground");
        props.setProperty("app.version", "1.0");
        props.setProperty("app.debug", "true");

        System.out.println("getProperty(app.name) -> " + props.getProperty("app.name"));
        System.out.println("getProperty(missing) -> " + props.getProperty("missing"));
        System.out.println("getProperty(missing, default) -> " + props.getProperty("missing", "fallback"));
    }

    static void storeAndLoad() throws IOException {
        var tmp = Files.createTempFile("props-poc-", ".properties");
        try {
            var out = new Properties();
            out.setProperty("db.host", "localhost");
            out.setProperty("db.port", "5432");
            out.setProperty("db.user", "admin");

            try (var fos = new FileOutputStream(tmp.toFile())) {
                out.store(fos, "playground properties");
            }
            System.out.println("stored to " + tmp);

            var in = new Properties();
            try (var fis = new FileInputStream(tmp.toFile())) {
                in.load(fis);
            }
            System.out.println("loaded db.host -> " + in.getProperty("db.host"));
            System.out.println("loaded db.port -> " + in.getProperty("db.port"));
            System.out.println("loaded db.user -> " + in.getProperty("db.user"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    static void propertiesWithDefaults() {
        var defaults = new Properties();
        defaults.setProperty("timeout.ms", "5000");
        defaults.setProperty("retries", "3");

        var overrides = new Properties(defaults);
        overrides.setProperty("retries", "5");

        System.out.println("timeout.ms (from defaults) -> " + overrides.getProperty("timeout.ms"));
        System.out.println("retries (overridden) -> " + overrides.getProperty("retries"));

        System.out.print("stringPropertyNames -> ");
        overrides.stringPropertyNames().stream().sorted().forEach(n -> System.out.print(n + " "));
        System.out.println();
    }

    static void systemProperties() {
        System.out.println("java.version -> " + System.getProperty("java.version"));
        System.out.println("os.name -> " + System.getProperty("os.name"));
        System.out.println("user.language -> " + System.getProperty("user.language"));

        System.setProperty("playground.custom", "hello");
        System.out.println("playground.custom after set -> " + System.getProperty("playground.custom"));

        long total = System.getProperties().stringPropertyNames().size();
        System.out.println("System.getProperties() count -> " + total);
    }

    static void xmlStoreAndLoad() throws IOException {
        var tmp = Files.createTempFile("props-poc-", ".xml");
        try {
            var out = new Properties();
            out.setProperty("xml.key", "xml-value");
            out.setProperty("xml.enabled", "true");

            try (var fos = new FileOutputStream(tmp.toFile())) {
                out.storeToXML(fos, "xml properties");
            }
            System.out.println("storeToXML to " + tmp);

            var in = new Properties();
            try (var fis = new FileInputStream(tmp.toFile())) {
                in.loadFromXML(fis);
            }
            System.out.println("loadFromXML xml.key -> " + in.getProperty("xml.key"));
            System.out.println("loadFromXML xml.enabled -> " + in.getProperty("xml.enabled"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("--- set and get ---");
        setAndGet();

        System.out.println("\n--- store and load ---");
        storeAndLoad();

        System.out.println("\n--- properties with defaults ---");
        propertiesWithDefaults();

        System.out.println("\n--- system properties ---");
        systemProperties();

        System.out.println("\n--- XML store and load ---");
        xmlStoreAndLoad();
    }
}
