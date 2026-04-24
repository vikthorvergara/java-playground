package com.github.vikthorvergara.spring.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class InjectionAndPostProcessorPOC {

    public static class Repository {
        public String load(long id) {
            return "row#" + id;
        }
    }

    public static class Logger {
        private final String tag;

        public Logger(String tag) {
            this.tag = tag;
        }

        public void log(String msg) {
            System.out.println(tag + " " + msg);
        }
    }

    public static class Service {
        private final Repository repository;
        private Logger logger;

        public Service(Repository repository) {
            this.repository = repository;
        }

        @Autowired
        public void setLogger(Logger logger) {
            this.logger = logger;
        }

        public void handle(long id) {
            String row = repository.load(id);
            logger.log("Service handled " + row);
        }

        public Repository getRepository() {
            return repository;
        }

        public Logger getLogger() {
            return logger;
        }
    }

    public static class TimestampingBeanPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
            if (bean instanceof Service || bean instanceof Repository || bean instanceof Logger) {
                System.out.println("[BPP before-init] " + name + " -> " + bean.getClass().getSimpleName());
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
            if (bean instanceof Service || bean instanceof Repository || bean instanceof Logger) {
                System.out.println("[BPP after-init] " + name + " -> " + bean.getClass().getSimpleName());
            }
            return bean;
        }
    }

    @Configuration
    public static class AppConfig {
        @Bean
        public Repository repository() {
            return new Repository();
        }

        @Bean
        public Logger logger() {
            return new Logger("[L]");
        }

        @Bean
        public Service service(Repository repository) {
            return new Service(repository);
        }

        @Bean
        public static TimestampingBeanPostProcessor timestampingBpp() {
            return new TimestampingBeanPostProcessor();
        }
    }

    public static void main(String[] args) {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            constructorInjection(ctx);
            setterInjection(ctx);
            invokeService(ctx);
        }
    }

    static void constructorInjection(AnnotationConfigApplicationContext ctx) {
        System.out.println("\n--- constructor injection ---");
        Service service = ctx.getBean(Service.class);
        Repository repository = ctx.getBean(Repository.class);
        System.out.println("service.repository same as bean: " + (service.getRepository() == repository));
    }

    static void setterInjection(AnnotationConfigApplicationContext ctx) {
        System.out.println("\n--- setter injection ---");
        Service service = ctx.getBean(Service.class);
        Logger logger = ctx.getBean(Logger.class);
        System.out.println("service.logger non-null: " + (service.getLogger() != null));
        System.out.println("service.logger same as bean: " + (service.getLogger() == logger));
    }

    static void invokeService(AnnotationConfigApplicationContext ctx) {
        System.out.println("\n--- service call ---");
        ctx.getBean(Service.class).handle(42);
    }
}
