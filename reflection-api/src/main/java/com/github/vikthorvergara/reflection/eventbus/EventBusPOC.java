package com.github.vikthorvergara.reflection.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBusPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Subscribe {}

    record UserRegistered(String name, int age) {}
    record OrderPlaced(String buyer, String product, double price) {}
    record OrderShipped(String orderId) {}

    record Handler(Object listener, Method method) {
        void invoke(Object event) {
            try {
                method.setAccessible(true);
                method.invoke(listener, event);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke handler: " + method.getName(), e);
            }
        }
    }

    static class EventBus {
        private final Map<Class<?>, List<Handler>> handlers = new HashMap<>();

        void register(Object listener) {
            var clazz = listener.getClass();
            System.out.printf("  Scanning %s...%n", clazz.getSimpleName());
            for (var method : clazz.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Subscribe.class)) continue;
                var paramTypes = method.getParameterTypes();
                if (paramTypes.length != 1) {
                    throw new IllegalArgumentException("@Subscribe method must have exactly one parameter: " + method.getName());
                }
                var eventType = paramTypes[0];
                handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(new Handler(listener, method));
                System.out.printf("    Registered %s(%s)%n", method.getName(), eventType.getSimpleName());
            }
        }

        void publish(Object event) {
            var eventType = event.getClass();
            var matchedHandlers = handlers.getOrDefault(eventType, List.of());
            System.out.printf("  Publishing %s -> %d handler(s) found%n", eventType.getSimpleName(), matchedHandlers.size());
            for (var handler : matchedHandlers) {
                handler.invoke(event);
            }
        }

        void printSubscriptions() {
            System.out.println("  Event subscriptions:");
            handlers.forEach((eventType, handlerList) -> {
                System.out.printf("    %s:%n", eventType.getSimpleName());
                for (var handler : handlerList) {
                    System.out.printf("      -> %s.%s()%n",
                            handler.listener().getClass().getSimpleName(),
                            handler.method().getName());
                }
            });
        }
    }

    static class NotificationListener {
        @Subscribe
        void onUserRegistered(UserRegistered event) {
            System.out.printf("    [NOTIFICATION] Welcome %s (age %d)! Your account is ready.%n", event.name(), event.age());
        }

        @Subscribe
        void onOrderPlaced(OrderPlaced event) {
            System.out.printf("    [NOTIFICATION] %s, your order for %s ($%.2f) has been confirmed.%n",
                    event.buyer(), event.product(), event.price());
        }
    }

    static class AuditListener {
        @Subscribe
        void auditUserRegistered(UserRegistered event) {
            System.out.printf("    [AUDIT] New user registered: %s, age %d%n", event.name(), event.age());
        }

        @Subscribe
        void auditOrderPlaced(OrderPlaced event) {
            System.out.printf("    [AUDIT] Order placed by %s: %s at $%.2f%n", event.buyer(), event.product(), event.price());
        }

        @Subscribe
        void auditOrderShipped(OrderShipped event) {
            System.out.printf("    [AUDIT] Order shipped: %s%n", event.orderId());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Event Bus Registration ===");
        var bus = new EventBus();
        var notificationListener = new NotificationListener();
        var auditListener = new AuditListener();
        bus.register(notificationListener);
        bus.register(auditListener);

        System.out.println("\n=== Subscription Map ===");
        bus.printSubscriptions();

        System.out.println("\n=== Publishing UserRegistered Events ===");
        bus.publish(new UserRegistered("Vikthor", 30));
        System.out.println();
        bus.publish(new UserRegistered("Alice", 30));

        System.out.println("\n=== Publishing OrderPlaced Event ===");
        bus.publish(new OrderPlaced("Bob", "RTX 5090", 2499.99));

        System.out.println("\n=== Publishing OrderShipped Event ===");
        bus.publish(new OrderShipped("ORD-1001"));

        System.out.println("\n=== Publishing Unhandled Event ===");
        bus.publish("This is a String event with no subscribers");

        System.out.println("\n=== Annotation Inspection ===");
        for (var listenerClass : new Class<?>[]{NotificationListener.class, AuditListener.class}) {
            System.out.printf("  %s:%n", listenerClass.getSimpleName());
            for (var method : listenerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Subscribe.class)) {
                    var paramType = method.getParameterTypes()[0];
                    System.out.printf("    @Subscribe %s(%s)%n", method.getName(), paramType.getSimpleName());
                }
            }
        }
    }
}
