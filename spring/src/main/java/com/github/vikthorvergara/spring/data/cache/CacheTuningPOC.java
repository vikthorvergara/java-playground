package com.github.vikthorvergara.spring.data.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableCaching
public class CacheTuningPOC {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("bounded", Caffeine.newBuilder().maximumSize(2).build());
        manager.registerCustomCache("ttl", Caffeine.newBuilder().expireAfterWrite(Duration.ofMillis(300)).build());
        manager.registerCustomCache("results", Caffeine.newBuilder().build());
        return manager;
    }

    @Service
    public static class ComputeService {
        private final AtomicInteger boundedLoads = new AtomicInteger();
        private final AtomicInteger ttlLoads = new AtomicInteger();
        private final AtomicInteger computeRuns = new AtomicInteger();

        @Cacheable("bounded")
        public String loadBounded(long id) {
            boundedLoads.incrementAndGet();
            return "row-" + id;
        }

        @Cacheable("ttl")
        public String loadTtl(long id) {
            ttlLoads.incrementAndGet();
            return "row-" + id;
        }

        @Cacheable(value = "results", condition = "#n >= 0", unless = "#result > 100")
        public long compute(long n) {
            computeRuns.incrementAndGet();
            return n * 10;
        }

        public int boundedLoads() {
            return boundedLoads.get();
        }

        public int ttlLoads() {
            return ttlLoads.get();
        }

        public int computeRuns() {
            return computeRuns.get();
        }
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CacheTuningPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            ComputeService service = ctx.getBean(ComputeService.class);
            CacheManager cacheManager = ctx.getBean(CacheManager.class);

            maximumSizeEvictsOldestEntries(service, cacheManager);
            expireAfterWriteDropsEntryAfterTtl(service);
            conditionSkipsCachingForSomeArguments(service);
            unlessSkipsCachingForSomeResults(service);
        }
    }

    @SuppressWarnings("unchecked")
    static void maximumSizeEvictsOldestEntries(ComputeService service, CacheManager cacheManager) {
        System.out.println("--- maximumSize=2: third distinct key evicts one entry ---");
        service.loadBounded(1);
        service.loadBounded(2);
        service.loadBounded(3);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> native_ =
                (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cacheManager.getCache("bounded").getNativeCache();
        native_.cleanUp();
        System.out.println("loads after 3 distinct keys = " + service.boundedLoads() + ", cache estimatedSize = " + native_.estimatedSize() + " (capped at 2)");
    }

    static void expireAfterWriteDropsEntryAfterTtl(ComputeService service) {
        System.out.println("\n--- expireAfterWrite=300ms: entry expires, forcing a reload ---");
        service.loadTtl(1);
        service.loadTtl(1);
        System.out.println("loads right after caching = " + service.ttlLoads() + " (second call was a hit)");
        sleep(400);
        service.loadTtl(1);
        System.out.println("loads after waiting past ttl = " + service.ttlLoads() + " (expired -> reloaded)");
    }

    static void conditionSkipsCachingForSomeArguments(ComputeService service) {
        System.out.println("\n--- condition=\"#n >= 0\": negative args never enter the cache ---");
        int before = service.computeRuns();
        service.compute(-1);
        service.compute(-1);
        System.out.println("compute(-1) twice -> runs " + (service.computeRuns() - before) + " (condition false, never cached)");
    }

    static void unlessSkipsCachingForSomeResults(ComputeService service) {
        System.out.println("\n--- unless=\"#result > 100\": small results cached, large ones not ---");
        int beforeSmall = service.computeRuns();
        service.compute(5);
        service.compute(5);
        System.out.println("compute(5)=50 twice -> runs " + (service.computeRuns() - beforeSmall) + " (<=100 cached, second was a hit)");
        int beforeLarge = service.computeRuns();
        service.compute(50);
        service.compute(50);
        System.out.println("compute(50)=500 twice -> runs " + (service.computeRuns() - beforeLarge) + " (>100 not cached, both ran)");
    }
}
