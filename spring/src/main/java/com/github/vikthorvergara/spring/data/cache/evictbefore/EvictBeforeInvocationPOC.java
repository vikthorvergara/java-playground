package com.github.vikthorvergara.spring.data.cache.evictbefore;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableCaching
public class EvictBeforeInvocationPOC {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("items", Caffeine.newBuilder().build());
        return manager;
    }

    @Service
    public static class ItemService {
        private final AtomicInteger loads = new AtomicInteger();

        public int loadCount() {
            return loads.get();
        }

        @Cacheable(cacheNames = "items", key = "#key")
        public String load(String key) {
            loads.incrementAndGet();
            return "item<" + key + ">";
        }

        @CacheEvict(cacheNames = "items", key = "#key")
        public void evictThenFailDefault(String key) {
            throw new RuntimeException("boom after default-phase evict");
        }

        @CacheEvict(cacheNames = "items", key = "#key", beforeInvocation = true)
        public void evictThenFailEarly(String key) {
            throw new RuntimeException("boom after beforeInvocation evict");
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EvictBeforeInvocationPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            ItemService items = ctx.getBean(ItemService.class);
            warmCache(items);
            defaultEvictSkippedWhenMethodThrows(items);
            beforeInvocationEvictAppliesEvenWhenMethodThrows(items);
        }
    }

    static void warmCache(ItemService items) {
        System.out.println("--- warm cache: load(A) twice, second is a hit ---");
        items.load("A");
        items.load("A");
        System.out.println("loads = " + items.loadCount() + " (1, second served from cache)");
    }

    static void defaultEvictSkippedWhenMethodThrows(ItemService items) {
        System.out.println("\n--- default @CacheEvict: exception skips eviction, entry survives ---");
        try {
            items.evictThenFailDefault("A");
        } catch (RuntimeException e) {
            System.out.println("  caught: " + e.getMessage());
        }
        items.load("A");
        System.out.println("loads = " + items.loadCount() + " (1, still cached, evict was skipped)");
    }

    static void beforeInvocationEvictAppliesEvenWhenMethodThrows(ItemService items) {
        System.out.println("\n--- beforeInvocation=true: eviction happens before the throw ---");
        try {
            items.evictThenFailEarly("A");
        } catch (RuntimeException e) {
            System.out.println("  caught: " + e.getMessage());
        }
        items.load("A");
        System.out.println("loads = " + items.loadCount() + " (2, evicted before failure, reloaded)");
    }
}
