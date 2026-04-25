package com.github.vikthorvergara.spring.core.value;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@SpringBootApplication
public class ValueAndEnvironmentPOC {

    @Component
    public static class Holder {
        @Value("${app.name:fallback}")
        private String appName;

        @Value("${missing.key:absent}")
        private String missing;

        @Value("${app.version}")
        private String version;

        @Value("${app.feature-flags}")
        private List<String> flags;

        @Value("#{T(java.lang.Math).PI * 2}")
        private double tau;

        @Value("#{'${app.feature-flags}'.toUpperCase()}")
        private String upperFlags;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ValueAndEnvironmentPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            Holder holder = ctx.getBean(Holder.class);
            valueResolution(holder);
            environmentApi(ctx.getEnvironment());
        }
    }

    static void valueResolution(Holder h) {
        System.out.println("--- @Value resolution ---");
        System.out.println("app.name = " + h.appName);
        System.out.println("missing.key = " + h.missing);
        System.out.println("app.version = " + h.version);
        System.out.println("app.feature-flags = " + h.flags);
        System.out.println("SpEL math: 2pi = " + h.tau);
        System.out.println("SpEL upper: " + h.upperFlags);
    }

    static void environmentApi(Environment env) {
        System.out.println("\n--- Environment API ---");
        System.out.println("env.getProperty(app.name) = " + env.getProperty("app.name"));
        System.out.println("env.getProperty(missing.key, fallback) = " + env.getProperty("missing.key", "fallback"));
        System.out.println("env.containsProperty(app.version) = " + env.containsProperty("app.version"));
        System.out.println("active profiles = " + java.util.Arrays.toString(env.getActiveProfiles()));
        System.out.println("default profiles = " + java.util.Arrays.toString(env.getDefaultProfiles()));
    }
}
