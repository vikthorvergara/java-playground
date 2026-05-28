package com.github.vikthorvergara.spring.aop.around;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class AroundRetryPOC {

    @Service
    public static class Flaky {
        private final AtomicInteger attempts = new AtomicInteger();

        public String call() {
            int n = attempts.incrementAndGet();
            if (n < 3) {
                throw new RuntimeException("boom on attempt " + n);
            }
            return "ok on attempt " + n;
        }

        public int attempts() {
            return attempts.get();
        }
    }

    @Aspect
    @Component
    @Order(1)
    public static class RetryAspect {
        @Around("execution(* com.github.vikthorvergara.spring.aop.around.AroundRetryPOC.Flaky.call(..))")
        public Object retry(ProceedingJoinPoint pjp) throws Throwable {
            int max = 3;
            Throwable last = null;
            for (int i = 1; i <= max; i++) {
                try {
                    return pjp.proceed();
                } catch (RuntimeException e) {
                    last = e;
                    System.out.println("  [retry order=1] attempt " + i + " failed: " + e.getMessage());
                }
            }
            throw last;
        }
    }

    @Aspect
    @Component
    @Order(2)
    public static class LoggingAspect {
        @Around("execution(* com.github.vikthorvergara.spring.aop.around.AroundRetryPOC.Flaky.call(..))")
        public Object log(ProceedingJoinPoint pjp) throws Throwable {
            System.out.println("    [log  order=2] entering " + pjp.getSignature().getName());
            Object out = pjp.proceed();
            System.out.println("    [log  order=2] returned " + out);
            return out;
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AroundRetryPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            Flaky flaky = ctx.getBean(Flaky.class);

            aroundRetrySwallowsFailuresUntilSuccess(flaky);
        }
    }

    static void aroundRetrySwallowsFailuresUntilSuccess(Flaky flaky) {
        System.out.println("--- @Order(1) Retry wraps @Order(2) Log: log fires for every attempt ---");
        String result = flaky.call();
        System.out.println("final result: " + result + " (Flaky.attempts=" + flaky.attempts() + ")");
    }
}
