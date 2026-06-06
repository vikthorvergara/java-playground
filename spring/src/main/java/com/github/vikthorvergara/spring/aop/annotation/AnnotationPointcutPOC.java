package com.github.vikthorvergara.spring.aop.annotation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SpringBootApplication
public class AnnotationPointcutPOC {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Audited {
        String value();
    }

    @Component
    public static class OrderService {
        @Audited("place-order")
        public String place(String item) {
            return "placed:" + item;
        }

        @Audited("cancel-order")
        public String cancel(String item) {
            return "cancelled:" + item;
        }

        public String browse(String item) {
            return "browsing:" + item;
        }
    }

    @Aspect
    @Component
    public static class AuditAspect {
        @Pointcut("within(com.github.vikthorvergara.spring.aop.annotation.AnnotationPointcutPOC.OrderService)")
        public void inOrderService() {
        }

        @Pointcut("@annotation(audited)")
        public void auditedMethod(Audited audited) {
        }

        @Before("inOrderService() && auditedMethod(audited)")
        public void recordAudit(JoinPoint jp, Audited audited) {
            System.out.println("  [before] action=" + audited.value() + " method=" + jp.getSignature().getName()
                    + " args=" + java.util.Arrays.toString(jp.getArgs()));
        }

        @AfterReturning(pointcut = "inOrderService() && auditedMethod(audited)", returning = "result")
        public void recordResult(Audited audited, Object result) {
            System.out.println("  [afterReturning] action=" + audited.value() + " result=" + result);
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AnnotationPointcutPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            OrderService service = ctx.getBean(OrderService.class);

            annotatedMethodsTriggerAdviceWithBoundAnnotation(service);
            unannotatedMethodIsNotAdvised(service);
        }
    }

    static void annotatedMethodsTriggerAdviceWithBoundAnnotation(OrderService service) {
        System.out.println("--- @Audited methods: advice fires, annotation value bound into advice ---");
        System.out.println("return=" + service.place("keyboard"));
        System.out.println("return=" + service.cancel("mouse"));
    }

    static void unannotatedMethodIsNotAdvised(OrderService service) {
        System.out.println("\n--- browse() has no @Audited: no advice output, only return ---");
        System.out.println("return=" + service.browse("monitor"));
    }
}
