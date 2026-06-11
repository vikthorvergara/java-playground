package com.github.vikthorvergara.spring.aop.selfinvocation;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.aop.framework.AopContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class SelfInvocationPOC {

    @Aspect
    @Component
    public static class CountingAspect {
        final AtomicInteger advised = new AtomicInteger();

        @Before("execution(* com.github.vikthorvergara.spring.aop.selfinvocation.SelfInvocationPOC.WorkService.step())")
        public void count() {
            advised.incrementAndGet();
        }
    }

    @Service
    public static class WorkService {
        public void step() {
        }

        public void runViaSelfCall() {
            this.step();
        }

        public void runViaExposedProxy() {
            ((WorkService) AopContext.currentProxy()).step();
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SelfInvocationPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            WorkService work = ctx.getBean(WorkService.class);
            CountingAspect aspect = ctx.getBean(CountingAspect.class);

            selfCallBypassesProxyAdvice(work, aspect);
            exposedProxyCallTriggersAdvice(work, aspect);
        }
    }

    static void selfCallBypassesProxyAdvice(WorkService work, CountingAspect aspect) {
        System.out.println("--- internal this.step() bypasses the proxy, advice does not fire ---");
        work.runViaSelfCall();
        System.out.println("advised count = " + aspect.advised.get() + " (0, self-invocation skipped proxy)");
    }

    static void exposedProxyCallTriggersAdvice(WorkService work, CountingAspect aspect) {
        System.out.println("\n--- AopContext.currentProxy().step() goes through the proxy, advice fires ---");
        work.runViaExposedProxy();
        System.out.println("advised count = " + aspect.advised.get() + " (1, exposed proxy routed through advice)");
    }
}
