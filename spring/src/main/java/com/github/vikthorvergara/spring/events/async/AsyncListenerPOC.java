package com.github.vikthorvergara.spring.events.async;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@EnableAsync
public class AsyncListenerPOC {

    public record JobEvent(String name) {
    }

    @Bean(name = "eventExecutor")
    public TaskExecutor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setThreadNamePrefix("evt-");
        executor.initialize();
        return executor;
    }

    @Component
    public static class Listeners {
        static final CountDownLatch latch = new CountDownLatch(1);

        @EventListener
        public void syncListener(JobEvent event) {
            System.out.println("  [sync]  job=" + event.name() + " thread=" + Thread.currentThread().getName());
        }

        @Async("eventExecutor")
        @EventListener
        public void asyncListener(JobEvent event) {
            System.out.println("  [async] job=" + event.name() + " thread=" + Thread.currentThread().getName());
            latch.countDown();
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(AsyncListenerPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            syncRunsOnPublisherThreadAsyncRunsOnExecutorThread(ctx);
        }
    }

    static void syncRunsOnPublisherThreadAsyncRunsOnExecutorThread(ConfigurableApplicationContext ctx) throws InterruptedException {
        System.out.println("--- publisher thread=" + Thread.currentThread().getName()
                + ": sync listener shares it, @Async listener runs on evt- pool ---");
        ctx.publishEvent(new JobEvent("reindex"));
        Listeners.latch.await();
        System.out.println("both listeners completed");
    }
}
