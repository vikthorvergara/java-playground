package com.github.vikthorvergara.spring.data.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableCaching
public class CacheEvictPutPOC {

    public record Product(Long id, String name) {
    }

    @Service
    public static class CatalogService {
        private final Map<Long, Product> store = new ConcurrentHashMap<>(Map.of(
                1L, new Product(1L, "keyboard"),
                2L, new Product(2L, "monitor")));
        private final AtomicInteger loads = new AtomicInteger();

        @Cacheable("products")
        public Product findById(Long id) {
            loads.incrementAndGet();
            return store.get(id);
        }

        @CachePut(value = "products", key = "#product.id")
        public Product update(Product product) {
            store.put(product.id(), product);
            return product;
        }

        @CacheEvict(value = "products", key = "#id")
        public void evictOne(Long id) {
        }

        @CacheEvict(value = "products", allEntries = true)
        public void evictAll() {
        }

        public int loadCount() {
            return loads.get();
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CacheEvictPutPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            CatalogService catalog = ctx.getBean(CatalogService.class);

            cacheableStoresOnFirstLoad(catalog);
            cachePutRefreshesCacheWithoutReloading(catalog);
            cacheEvictKeyForcesReload(catalog);
            cacheEvictAllEntriesClearsEverything(catalog);
        }
    }

    static void cacheableStoresOnFirstLoad(CatalogService catalog) {
        System.out.println("--- @Cacheable: first load misses, second hits ---");
        System.out.println("load(1) = " + catalog.findById(1L) + " loads=" + catalog.loadCount());
        System.out.println("load(1) = " + catalog.findById(1L) + " loads=" + catalog.loadCount() + " (cache hit, no reload)");
    }

    static void cachePutRefreshesCacheWithoutReloading(CatalogService catalog) {
        System.out.println("\n--- @CachePut: writes new value straight into cache ---");
        System.out.println("update(1 -> keyboard-pro): " + catalog.update(new Product(1L, "keyboard-pro")));
        System.out.println("load(1) = " + catalog.findById(1L) + " loads=" + catalog.loadCount() + " (served updated value, loader not called)");
    }

    static void cacheEvictKeyForcesReload(CatalogService catalog) {
        System.out.println("\n--- @CacheEvict(key): single entry dropped, next load misses ---");
        catalog.evictOne(1L);
        System.out.println("load(1) = " + catalog.findById(1L) + " loads=" + catalog.loadCount() + " (reloaded from store)");
    }

    static void cacheEvictAllEntriesClearsEverything(CatalogService catalog) {
        System.out.println("\n--- @CacheEvict(allEntries=true): whole cache cleared ---");
        catalog.findById(2L);
        int before = catalog.loadCount();
        catalog.evictAll();
        catalog.findById(1L);
        catalog.findById(2L);
        System.out.println("loads before evictAll=" + before + " after reloading both=" + catalog.loadCount() + " (+2 means both missed)");
    }
}
