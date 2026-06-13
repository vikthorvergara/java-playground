package com.github.vikthorvergara.spring.data.cache.keygen;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableCaching
public class KeyGeneratorPOC {

    @Bean("caseInsensitiveKeyGen")
    public KeyGenerator caseInsensitiveKeyGen() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder(method.getName());
            for (Object p : params) {
                key.append(':').append(p == null ? "null" : p.toString().toLowerCase());
            }
            return key.toString();
        };
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("profiles", Caffeine.newBuilder().build());
        return manager;
    }

    @Service
    public static class ProfileService {
        private final AtomicInteger loads = new AtomicInteger();

        public int loadCount() {
            return loads.get();
        }

        @Cacheable(cacheNames = "profiles", keyGenerator = "caseInsensitiveKeyGen")
        public String lookup(String username) {
            loads.incrementAndGet();
            return "profile<" + username + ">";
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(KeyGeneratorPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            ProfileService profiles = ctx.getBean(ProfileService.class);
            customKeyCollapsesCaseVariantsToOneEntry(profiles);
            differentNameStillMissesAndLoads(profiles);
        }
    }

    static void customKeyCollapsesCaseVariantsToOneEntry(ProfileService profiles) {
        System.out.println("--- custom KeyGenerator lowercases args: case variants share one entry ---");
        System.out.println("  lookup(\"Alice\") = " + profiles.lookup("Alice"));
        System.out.println("  lookup(\"alice\") = " + profiles.lookup("alice"));
        System.out.println("  lookup(\"ALICE\") = " + profiles.lookup("ALICE"));
        System.out.println("loads = " + profiles.loadCount() + " (1, all three keys collapsed)");
    }

    static void differentNameStillMissesAndLoads(ProfileService profiles) {
        System.out.println("\n--- a genuinely different arg generates a different key -> cold load ---");
        System.out.println("  lookup(\"bob\")   = " + profiles.lookup("bob"));
        System.out.println("loads = " + profiles.loadCount() + " (2, bob is a new key)");
    }
}
