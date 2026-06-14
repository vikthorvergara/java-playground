package com.github.vikthorvergara.spring.events.chaining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class EventChainingPOC {

    public record OrderPlaced(String sku) {
    }

    public record InvoiceRequested(String sku) {
    }

    public record InvoicePaid(String sku) {
    }

    @Component
    public static class Handlers {

        @EventListener
        public InvoiceRequested onOrder(OrderPlaced event) {
            System.out.println("  onOrder(" + event.sku() + ") -> returns InvoiceRequested");
            return new InvoiceRequested(event.sku());
        }

        @EventListener
        public InvoicePaid onInvoiceRequested(InvoiceRequested event) {
            System.out.println("  onInvoiceRequested(" + event.sku() + ") -> returns InvoicePaid");
            return new InvoicePaid(event.sku());
        }

        @EventListener
        public void onInvoicePaid(InvoicePaid event) {
            System.out.println("  onInvoicePaid(" + event.sku() + ") -> chain terminates (void)");
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EventChainingPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            nonVoidReturnIsRepublishedAsTheNextEvent(ctx);
        }
    }

    static void nonVoidReturnIsRepublishedAsTheNextEvent(ConfigurableApplicationContext ctx) {
        System.out.println("--- one publish triggers a chain: each listener's return is republished ---");
        ctx.publishEvent(new OrderPlaced("WIDGET-1"));
        System.out.println("chain done from a single publishEvent(OrderPlaced)");
    }
}
