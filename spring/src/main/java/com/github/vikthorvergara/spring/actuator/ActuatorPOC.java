package com.github.vikthorvergara.spring.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
public class ActuatorPOC {

    public static final AtomicBoolean MARKET_DOWN = new AtomicBoolean(false);

    @Component
    public static class MarketHealth implements HealthIndicator {
        @Override
        public Health health() {
            if (MARKET_DOWN.get()) {
                return Health.down().withDetail("reason", "market closed").build();
            }
            return Health.up().withDetail("reason", "market open").build();
        }
    }

    @Service
    public static class OrderService {
        private final Counter placed;
        private final Counter rejected;

        public OrderService(MeterRegistry registry) {
            this.placed = Counter.builder("orders.placed").description("orders placed").register(registry);
            this.rejected = Counter.builder("orders.rejected").description("orders rejected").register(registry);
        }

        public String place(int qty) {
            if (MARKET_DOWN.get()) {
                rejected.increment();
                return "rejected";
            }
            placed.increment(qty);
            return "ok x" + qty;
        }
    }

    @RestController
    public static class Endpoints {
        private final OrderService orders;

        public Endpoints(OrderService orders) {
            this.orders = orders;
        }

        @GetMapping("/place")
        public String place(@RequestParam(defaultValue = "1") int qty) {
            return orders.place(qty);
        }
    }

    @Bean
    public SecurityFilterChain permitAll(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .csrf(c -> c.disable())
                .build();
    }

    @Bean
    PortHolder portHolder() {
        return new PortHolder();
    }

    public static class PortHolder implements ApplicationListener<WebServerInitializedEvent> {
        volatile int port;

        @Override
        public void onApplicationEvent(WebServerInitializedEvent event) {
            this.port = event.getWebServer().getPort();
        }
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ActuatorPOC.class,
                "--server.port=0",
                "--management.endpoints.web.exposure.include=health,metrics",
                "--management.endpoint.health.show-details=always")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;

            healthUpWhenMarketOpen(base);
            placeSomeOrdersIncrementCounter(base);
            metricsEndpointShowsCounter(base);
            healthDownWhenMarketClosed(base);
            placeAfterDownIncrementsRejectedCounter(base);
        }
    }

    static RestTemplate rt() {
        return new RestTemplate();
    }

    static void healthUpWhenMarketOpen(String base) {
        System.out.println("--- GET /actuator/health (market open) ---");
        ResponseEntity<String> res = rt().getForEntity(base + "/actuator/health", String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void placeSomeOrdersIncrementCounter(String base) {
        System.out.println("\n--- POST equivalents: place 3 orders, 2 orders, 5 orders ---");
        System.out.println(rt().getForObject(base + "/place?qty=3", String.class));
        System.out.println(rt().getForObject(base + "/place?qty=2", String.class));
        System.out.println(rt().getForObject(base + "/place?qty=5", String.class));
    }

    static void metricsEndpointShowsCounter(String base) {
        System.out.println("\n--- GET /actuator/metrics/orders.placed ---");
        ResponseEntity<String> res = rt().getForEntity(base + "/actuator/metrics/orders.placed", String.class);
        System.out.println("body=" + res.getBody());
    }

    static void healthDownWhenMarketClosed(String base) {
        System.out.println("\n--- flip MARKET_DOWN=true -> health DOWN (503) ---");
        MARKET_DOWN.set(true);
        try {
            ResponseEntity<String> res = rt().getForEntity(base + "/actuator/health", String.class);
            System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.out.println("status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
        }
    }

    static void placeAfterDownIncrementsRejectedCounter(String base) {
        System.out.println("\n--- /place while DOWN increments orders.rejected ---");
        System.out.println(rt().getForObject(base + "/place?qty=1", String.class));
        ResponseEntity<String> res = rt().getForEntity(base + "/actuator/metrics/orders.rejected", String.class);
        System.out.println("body=" + res.getBody());
    }
}
