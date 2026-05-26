package com.github.vikthorvergara.spring.aop.advice;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class AdviceTypesPOC {

    @Service
    public static class Bank {
        private int balance;

        public void deposit(int amount) {
            balance += amount;
        }

        public void withdraw(int amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("negative withdraw: " + amount);
            }
            balance -= amount;
        }

        public int peek() {
            return balance;
        }
    }

    @Aspect
    @Component
    public static class AuditAspect {
        @Before("execution(* com.github.vikthorvergara.spring.aop.advice.AdviceTypesPOC.Bank.*(..)) && args(amount)")
        public void beforeAmount(int amount) {
            System.out.println("  [before] called with amount=" + amount);
        }

        @AfterReturning(pointcut = "execution(* com.github.vikthorvergara.spring.aop.advice.AdviceTypesPOC.Bank.peek())", returning = "result")
        public void afterPeek(int result) {
            System.out.println("  [afterReturning] peek -> " + result);
        }

        @AfterThrowing(pointcut = "execution(* com.github.vikthorvergara.spring.aop.advice.AdviceTypesPOC.Bank.*(..))", throwing = "ex")
        public void afterThrow(Throwable ex) {
            System.out.println("  [afterThrowing] " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AdviceTypesPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            Bank bank = ctx.getBean(Bank.class);

            beforeAdviceFiresOnAnyArgMethod(bank);
            afterReturningAdviceCapturesReturnValue(bank);
            afterThrowingAdviceCapturesException(bank);
        }
    }

    static void beforeAdviceFiresOnAnyArgMethod(Bank bank) {
        System.out.println("--- @Before on args(amount): fires for deposit and withdraw ---");
        bank.deposit(50);
        bank.withdraw(20);
    }

    static void afterReturningAdviceCapturesReturnValue(Bank bank) {
        System.out.println("\n--- @AfterReturning(returning=\"result\"): captures peek's return ---");
        int v = bank.peek();
        System.out.println("main saw peek=" + v);
    }

    static void afterThrowingAdviceCapturesException(Bank bank) {
        System.out.println("\n--- @AfterThrowing(throwing=\"ex\"): captures thrown exception ---");
        try {
            bank.withdraw(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("main caught: " + e.getMessage());
        }
    }
}
