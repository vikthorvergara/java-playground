package com.github.vikthorvergara.spring.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAsync
public class EventsAsyncPOC {

    public record OrderPlaced(String id, int qty, long publishedNanos) {
    }

    @Component
    public static class Publisher {
        private final ApplicationEventPublisher events;

        public Publisher(ApplicationEventPublisher events) {
            this.events = events;
        }

        public void place(String id, int qty) {
            System.out.println("[publisher thread=" + Thread.currentThread().getName() + "] publishing " + id);
            events.publishEvent(new OrderPlaced(id, qty, System.nanoTime()));
            System.out.println("[publisher thread=" + Thread.currentThread().getName() + "] returned from publishEvent for " + id);
        }
    }

    @Component
    public static class SyncListener {
        @EventListener
        public void onPlaced(OrderPlaced ev) {
            System.out.println("[sync-listener thread=" + Thread.currentThread().getName() + "] received " + ev.id());
            sleep(80);
            System.out.println("[sync-listener thread=" + Thread.currentThread().getName() + "] done " + ev.id());
        }

        static void sleep(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Component
    public static class AsyncListener {
        public static final CountDownLatch LATCH = new CountDownLatch(1);

        @Async
        @EventListener
        public void onPlaced(OrderPlaced ev) {
            System.out.println("[async-listener thread=" + Thread.currentThread().getName() + "] received " + ev.id());
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("[async-listener thread=" + Thread.currentThread().getName() + "] done " + ev.id());
            LATCH.countDown();
        }
    }

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("evt-async-");
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication app = new SpringApplication(EventsAsyncPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            Publisher pub = ctx.getBean(Publisher.class);

            syncListenerBlocksPublisher(pub);
            asyncListenerRunsOnSeparateThread(pub);
        }
    }

    static void syncListenerBlocksPublisher(Publisher pub) {
        System.out.println("--- sync @EventListener runs on caller thread ---");
        long start = System.nanoTime();
        pub.place("ORD-1", 3);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("publisher saw elapsedMs=" + elapsedMs + " (includes sync listener's 80ms sleep)");
    }

    static void asyncListenerRunsOnSeparateThread(Publisher pub) throws InterruptedException {
        System.out.println("\n--- @Async + @EventListener does NOT block publisher ---");
        long start = System.nanoTime();
        pub.place("ORD-2", 1);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("publisher returned elapsedMs=" + elapsedMs + " (sync listener still blocked here; async ran in parallel)");
        boolean ok = AsyncListener.LATCH.await(2, TimeUnit.SECONDS);
        System.out.println("async listener latch reached = " + ok);
    }
}
