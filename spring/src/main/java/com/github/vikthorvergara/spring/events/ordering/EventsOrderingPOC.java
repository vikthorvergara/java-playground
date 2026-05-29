package com.github.vikthorvergara.spring.events.ordering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class EventsOrderingPOC {

    public record AlertEvent(String level, String msg) {
    }

    @Component
    public static class Listeners {
        @Order(1)
        @EventListener(condition = "#event.level == 'ERROR'")
        public void errorOnly(AlertEvent event) {
            System.out.println("  [1 errorOnly]  level=" + event.level() + " msg=" + event.msg());
        }

        @Order(2)
        @EventListener
        public void auditAll(AlertEvent event) {
            System.out.println("  [2 auditAll]   level=" + event.level() + " msg=" + event.msg());
        }

        @Order(3)
        @EventListener(condition = "#event.msg.contains('disk')")
        public void diskOnly(AlertEvent event) {
            System.out.println("  [3 diskOnly]   level=" + event.level() + " msg=" + event.msg());
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EventsOrderingPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            conditionFiltersOutNonMatchingListeners(ctx);
            orderControlsListenerInvocationSequence(ctx);
            conditionAndOrderCombine(ctx);
        }
    }

    static void conditionFiltersOutNonMatchingListeners(ApplicationEventPublisher pub) {
        System.out.println("--- INFO event: only auditAll matches (errorOnly + diskOnly conditions false) ---");
        pub.publishEvent(new AlertEvent("INFO", "login ok"));
    }

    static void orderControlsListenerInvocationSequence(ApplicationEventPublisher pub) {
        System.out.println("\n--- ERROR + disk: all three fire, @Order dictates 1 -> 2 -> 3 ---");
        pub.publishEvent(new AlertEvent("ERROR", "disk full"));
    }

    static void conditionAndOrderCombine(ApplicationEventPublisher pub) {
        System.out.println("\n--- WARN + cpu: errorOnly skipped (level!=ERROR), diskOnly skipped (no 'disk') ---");
        pub.publishEvent(new AlertEvent("WARN", "cpu high"));
    }
}
