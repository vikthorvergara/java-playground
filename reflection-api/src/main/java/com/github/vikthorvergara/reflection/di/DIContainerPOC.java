package com.github.vikthorvergara.reflection.di;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class DIContainerPOC {

    static class Container {
        private final Map<Class<?>, Class<?>> registry = new HashMap<>();
        private final Map<Class<?>, Object> instances = new HashMap<>();

        void register(Class<?> clazz) {
            registry.put(clazz, clazz);
        }

        @SuppressWarnings("unchecked")
        <T> T resolve(Class<T> clazz) {
            if (instances.containsKey(clazz)) {
                return (T) instances.get(clazz);
            }

            if (!registry.containsKey(clazz)) {
                throw new RuntimeException("Class not registered: " + clazz.getName());
            }

            try {
                var constructors = clazz.getDeclaredConstructors();
                Constructor<?> constructor = constructors[0];
                var paramTypes = constructor.getParameterTypes();
                var params = new Object[paramTypes.length];

                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = resolve(paramTypes[i]);
                }

                var instance = (T) constructor.newInstance(params);
                instances.put(clazz, instance);
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to resolve: " + clazz.getName(), e);
            }
        }

        void printRegistrations() {
            System.out.println("Registered classes:");
            registry.keySet().forEach(c -> System.out.println("  - " + c.getSimpleName()));
        }

        void printInstances() {
            System.out.println("Resolved instances:");
            instances.forEach((k, v) -> System.out.printf("  - %s -> %s%n", k.getSimpleName(), v));
        }
    }

    static class MessageFormatter {
        String format(String user, String message) {
            return "[To: " + user + "] " + message;
        }

        @Override
        public String toString() {
            return "MessageFormatter{}";
        }
    }

    static class NotificationService {
        private final MessageFormatter formatter;

        NotificationService(MessageFormatter formatter) {
            this.formatter = formatter;
        }

        String notify(String user, String message) {
            var formatted = formatter.format(user, message);
            return "NOTIFICATION SENT: " + formatted;
        }

        @Override
        public String toString() {
            return "NotificationService{formatter=" + formatter + "}";
        }
    }

    static class UserService {
        private final NotificationService notificationService;

        UserService(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        void welcomeUser(String name) {
            var result = notificationService.notify(name, "Welcome to the platform!");
            System.out.println("  " + result);
        }

        void alertUser(String name, String alert) {
            var result = notificationService.notify(name, alert);
            System.out.println("  " + result);
        }

        @Override
        public String toString() {
            return "UserService{notificationService=" + notificationService + "}";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== DI Container Setup ===");
        var container = new Container();
        container.register(MessageFormatter.class);
        container.register(NotificationService.class);
        container.register(UserService.class);
        container.printRegistrations();

        System.out.println("\n=== Resolving UserService ===");
        var userService = container.resolve(UserService.class);
        System.out.println("Resolved: " + userService);

        System.out.println("\n=== Dependency Chain in Action ===");
        userService.welcomeUser("Alice");
        userService.welcomeUser("Bob");
        userService.alertUser("Vikthor", "Your session is about to expire");
        userService.alertUser("Charlie", "New message received");

        System.out.println("\n=== Singleton Verification ===");
        var userService2 = container.resolve(UserService.class);
        System.out.println("Same instance: " + (userService == userService2));

        System.out.println("\n=== Container State ===");
        container.printInstances();

        System.out.println("\n=== Constructor Inspection ===");
        for (var clazz : new Class<?>[]{MessageFormatter.class, NotificationService.class, UserService.class}) {
            var ctor = clazz.getDeclaredConstructors()[0];
            var paramTypes = ctor.getParameterTypes();
            if (paramTypes.length == 0) {
                System.out.printf("  %s -> no dependencies%n", clazz.getSimpleName());
            } else {
                var deps = new StringBuilder();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) deps.append(", ");
                    deps.append(paramTypes[i].getSimpleName());
                }
                System.out.printf("  %s -> depends on [%s]%n", clazz.getSimpleName(), deps);
            }
        }

        System.out.println("\n=== Unregistered Class Error Handling ===");
        try {
            container.resolve(String.class);
        } catch (RuntimeException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }
    }
}
