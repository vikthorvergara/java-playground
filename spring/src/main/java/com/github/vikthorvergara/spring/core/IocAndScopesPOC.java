package com.github.vikthorvergara.spring.core;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

public class IocAndScopesPOC {

    public static class Greeter {
        private final String label;

        public Greeter(String label) {
            this.label = label;
        }

        public String greet(String who) {
            return label + " hello to " + who + " from " + System.identityHashCode(this);
        }
    }

    public static class Counter {
        private int n = 0;

        public int next() {
            return ++n;
        }

        @Override
        public String toString() {
            return "Counter@" + System.identityHashCode(this) + " n=" + n;
        }
    }

    @Configuration
    public static class AppConfig {
        @Bean
        public Greeter greeter() {
            return new Greeter("[singleton]");
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Counter counter() {
            return new Counter();
        }
    }

    public static void main(String[] args) {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            registeredBeans(ctx);
            singletonScope(ctx);
            prototypeScope(ctx);
        }
    }

    static void registeredBeans(AnnotationConfigApplicationContext ctx) {
        System.out.println("--- registered beans ---");
        for (String name : ctx.getBeanDefinitionNames()) {
            if (!name.startsWith("org.springframework")) {
                System.out.println(name + " -> " + ctx.getBean(name).getClass().getSimpleName());
            }
        }
    }

    static void singletonScope(AnnotationConfigApplicationContext ctx) {
        System.out.println("\n--- singleton scope ---");
        Greeter a = ctx.getBean(Greeter.class);
        Greeter b = ctx.getBean(Greeter.class);
        System.out.println(a.greet("first"));
        System.out.println(b.greet("second"));
        System.out.println("same instance: " + (a == b));
    }

    static void prototypeScope(AnnotationConfigApplicationContext ctx) {
        System.out.println("\n--- prototype scope ---");
        Counter a = ctx.getBean(Counter.class);
        Counter b = ctx.getBean(Counter.class);
        a.next();
        a.next();
        b.next();
        System.out.println(a);
        System.out.println(b);
        System.out.println("same instance: " + (a == b));
    }
}
