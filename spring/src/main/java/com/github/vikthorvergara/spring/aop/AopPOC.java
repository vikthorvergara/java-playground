package com.github.vikthorvergara.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SpringBootApplication
public class AopPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Timed {
    }

    @Service
    public static class Calculator {
        public int slowAdd(int a, int b) {
            sleep(40);
            return a + b;
        }

        @Timed
        public int slowMul(int a, int b) {
            sleep(60);
            return a * b;
        }

        public int fastNoop(int x) {
            return x;
        }

        static void sleep(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Aspect
    @Component
    public static class TimingAspect {
        @Pointcut("execution(* com.github.vikthorvergara.spring.aop.AopPOC.Calculator.*(..))")
        public void allServiceMethods() {
        }

        @Around("allServiceMethods()")
        public Object timeAll(ProceedingJoinPoint pjp) throws Throwable {
            long start = System.nanoTime();
            try {
                return pjp.proceed();
            } finally {
                long us = (System.nanoTime() - start) / 1_000;
                System.out.println("[around-all] " + pjp.getSignature().toShortString() + " took " + us + "us");
            }
        }

        @Around("@annotation(com.github.vikthorvergara.spring.aop.AopPOC.Timed)")
        public Object timeAnnotated(ProceedingJoinPoint pjp) throws Throwable {
            System.out.println("[around-@Timed] entering " + pjp.getSignature().toShortString());
            Object r = pjp.proceed();
            System.out.println("[around-@Timed] result=" + r);
            return r;
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AopPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            Calculator calc = ctx.getBean(Calculator.class);

            allMethodsAdvised(calc);
            annotatedMethodGetsBothAdvices(calc);
            proxyClassProvesCglib(calc);
        }
    }

    static void allMethodsAdvised(Calculator calc) {
        System.out.println("--- execution() pointcut advises every Calculator method ---");
        System.out.println("slowAdd(2,3) = " + calc.slowAdd(2, 3));
        System.out.println("fastNoop(7) = " + calc.fastNoop(7));
    }

    static void annotatedMethodGetsBothAdvices(Calculator calc) {
        System.out.println("\n--- @Timed method gets BOTH advices (execution + @annotation) ---");
        System.out.println("slowMul(4,5) = " + calc.slowMul(4, 5));
    }

    static void proxyClassProvesCglib(Calculator calc) {
        System.out.println("\n--- proxy in play ---");
        System.out.println("bean class = " + calc.getClass().getName());
    }
}
