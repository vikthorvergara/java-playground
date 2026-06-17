package com.github.vikthorvergara.spring.aop.arguments;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ArgumentRewritePOC {

    @Aspect
    @Component
    public static class NormalizingAspect {
        @Around("execution(* com.github.vikthorvergara.spring.aop.arguments.ArgumentRewritePOC.GreetService.greet(..))")
        public Object normalizeFirstArg(ProceedingJoinPoint pjp) throws Throwable {
            Object[] args = pjp.getArgs();
            if (args.length > 0 && args[0] instanceof String s) {
                args[0] = s.strip().toUpperCase();
            }
            return pjp.proceed(args);
        }
    }

    @Service
    public static class GreetService {
        public String greet(String name) {
            return "hello " + name;
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ArgumentRewritePOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            GreetService greet = ctx.getBean(GreetService.class);
            aroundAdviceRewritesArgumentBeforeProceed(greet);
        }
    }

    static void aroundAdviceRewritesArgumentBeforeProceed(GreetService greet) {
        System.out.println("--- @Around proceeds with modified args: \"  bob \" normalized before method runs ---");
        System.out.println("greet(\"  bob \") = " + greet.greet("  bob ") + " (target saw \"BOB\")");
    }
}
