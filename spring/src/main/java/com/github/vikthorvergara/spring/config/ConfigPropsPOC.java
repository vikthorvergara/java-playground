package com.github.vikthorvergara.spring.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableConfigurationProperties(ConfigPropsPOC.MailProps.class)
public class ConfigPropsPOC {

    @ConfigurationProperties(prefix = "mail")
    public record MailProps(String host, int port, String from, Retry retry) {
        public record Retry(int attempts, int backoffMs) {
        }
    }

    public interface Notifier {
        String describe();
    }

    @Component
    @Profile("cfgdev")
    public static class DevNotifier implements Notifier {
        private final MailProps mail;

        public DevNotifier(MailProps mail) {
            this.mail = mail;
        }

        @Override
        public String describe() {
            return "dev-notifier -> " + mail.host() + ":" + mail.port() + " from=" + mail.from()
                    + " retry(" + mail.retry().attempts() + "x@" + mail.retry().backoffMs() + "ms)";
        }
    }

    @Component
    @Profile("cfgprod")
    public static class ProdNotifier implements Notifier {
        private final MailProps mail;

        public ProdNotifier(MailProps mail) {
            this.mail = mail;
        }

        @Override
        public String describe() {
            return "prod-notifier -> " + mail.host() + ":" + mail.port() + " from=" + mail.from()
                    + " retry(" + mail.retry().attempts() + "x@" + mail.retry().backoffMs() + "ms)";
        }
    }

    public static void main(String[] args) {
        runUnderProfile("cfgdev");
        runUnderProfile("cfgprod");
    }

    static void runUnderProfile(String profile) {
        System.out.println("\n--- profile=" + profile + " ---");
        SpringApplication app = new SpringApplication(ConfigPropsPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run("--spring.profiles.active=" + profile)) {
            MailProps mail = ctx.getBean(MailProps.class);
            Notifier notifier = ctx.getBean(Notifier.class);
            System.out.println("bound record: " + mail);
            System.out.println("active notifier bean class: " + notifier.getClass().getSimpleName());
            System.out.println(notifier.describe());
        }
    }
}
