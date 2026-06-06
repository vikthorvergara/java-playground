package com.github.vikthorvergara.spring.data.cache.sync;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableCaching
public class CacheSyncPOC {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("prices", Caffeine.newBuilder().build());
        manager.registerCustomCache("audit", Caffeine.newBuilder().build());
        return manager;
    }

    @Service
    public static class PriceService {
        final AtomicInteger loads = new AtomicInteger();

        public int loadCount() {
            return loads.get();
        }

        @Cacheable(cacheNames = "prices", sync = true)
        public int slowPrice(String sku) {
            loads.incrementAndGet();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return sku.length() * 10;
        }

        @Caching(
                put = @CachePut(cacheNames = "prices", key = "#sku"),
                evict = @CacheEvict(cacheNames = "audit", allEntries = true))
        public int reprice(String sku, int price) {
            return price;
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(CacheSyncPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            PriceService service = ctx.getBean(PriceService.class);

            syncSerializesConcurrentMissesIntoSingleLoad(service);
            cachingCombinesPutAndEvict(service);
        }
    }

    static void syncSerializesConcurrentMissesIntoSingleLoad(PriceService service) throws Exception {
        System.out.println("--- sync=true: 8 threads hit same cold key, only ONE computes ---");
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    service.slowPrice("ABCDE");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        done.await();
        System.out.println("loads = " + service.loadCount() + " (expected 1 despite 8 concurrent callers)");
    }

    static void cachingCombinesPutAndEvict(PriceService service) {
        System.out.println("\n--- @Caching: single call does a @CachePut and a @CacheEvict together ---");
        int before = service.loadCount();
        service.reprice("ABCDE", 999);
        int cached = service.slowPrice("ABCDE");
        System.out.println("price after reprice = " + cached + " (put value, served from cache)");
        System.out.println("extra loads = " + (service.loadCount() - before) + " (0, @CachePut refreshed the entry)");
    }
}
