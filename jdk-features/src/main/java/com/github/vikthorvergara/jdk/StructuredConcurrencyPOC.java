package com.github.vikthorvergara.jdk;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.List;
import java.util.ArrayList;

public class StructuredConcurrencyPOC {

    record User(String name, String email) {}
    record Order(String id, String product, double price) {}
    record Recommendation(String product, String reason) {}
    record UserDashboard(User user, List<Order> orders, List<Recommendation> recommendations) {}

    static User fetchUser(String userId) throws InterruptedException {
        Thread.sleep(80);
        if (userId.equals("error")) {
            throw new RuntimeException("User service unavailable");
        }
        return new User("Viktor " + userId, userId + "@mail.com");
    }

    static List<Order> fetchOrders(String userId) throws InterruptedException {
        Thread.sleep(120);
        return List.of(
                new Order("ORD-001", "Mechanical Keyboard", 189.99),
                new Order("ORD-002", "Ultra-Wide Monitor", 1299.00),
                new Order("ORD-003", "USB-C Hub", 49.99)
        );
    }

    static List<Recommendation> fetchRecommendations(String userId) throws InterruptedException {
        Thread.sleep(100);
        return List.of(
                new Recommendation("Desk Mat", "Based on your keyboard purchase"),
                new Recommendation("Monitor Arm", "Pairs well with ultra-wide monitors")
        );
    }

    static UserDashboard loadDashboardShutdownOnFailure(String userId) throws Exception {
        try (var scope = StructuredTaskScope.open()) {
            Subtask<User> userTask = scope.fork(() -> fetchUser(userId));
            Subtask<List<Order>> ordersTask = scope.fork(() -> fetchOrders(userId));
            Subtask<List<Recommendation>> recsTask = scope.fork(() -> fetchRecommendations(userId));

            scope.join();

            return new UserDashboard(userTask.get(), ordersTask.get(), recsTask.get());
        }
    }

    static List<String> fetchFromMultipleProviders() throws Exception {
        try (var scope = StructuredTaskScope.open()) {
            List<Subtask<String>> tasks = new ArrayList<>();

            tasks.add(scope.fork(() -> {
                Thread.sleep(200);
                return "Provider A: $1,249.00";
            }));

            tasks.add(scope.fork(() -> {
                Thread.sleep(50);
                return "Provider B: $1,199.00";
            }));

            tasks.add(scope.fork(() -> {
                Thread.sleep(150);
                try {
                    throw new RuntimeException("Provider C timeout");
                } catch (RuntimeException e) {
                    return "[FAILED] " + e.getMessage();
                }
            }));

            scope.join();

            List<String> results = new ArrayList<>();
            for (var task : tasks) {
                results.add(task.get());
            }
            return results;
        }
    }

    static UserDashboard loadDashboardWithNestedScopes(String userId) throws Exception {
        try (var outerScope = StructuredTaskScope.open()) {

            Subtask<User> userTask = outerScope.fork(() -> fetchUser(userId));

            Subtask<List<Object>> aggregatedTask = outerScope.fork(() -> {
                try (var innerScope = StructuredTaskScope.open()) {
                    Subtask<List<Order>> ordersTask = innerScope.fork(() -> fetchOrders(userId));
                    Subtask<List<Recommendation>> recsTask = innerScope.fork(() -> fetchRecommendations(userId));
                    innerScope.join();
                    List<Object> combined = new ArrayList<>();
                    combined.add(ordersTask.get());
                    combined.add(recsTask.get());
                    return combined;
                }
            });

            outerScope.join();

            @SuppressWarnings("unchecked")
            var orders = (List<Order>) aggregatedTask.get().get(0);
            @SuppressWarnings("unchecked")
            var recs = (List<Recommendation>) aggregatedTask.get().get(1);

            return new UserDashboard(userTask.get(), orders, recs);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Structured Concurrency: Parallel Dashboard Load ===");
        var dashboard = loadDashboardShutdownOnFailure("42");
        System.out.println("User: " + dashboard.user());
        System.out.println("Orders:");
        dashboard.orders().forEach(o -> System.out.println("  - " + o.id() + ": " + o.product() + " ($" + o.price() + ")"));
        System.out.println("Recommendations:");
        dashboard.recommendations().forEach(r -> System.out.println("  - " + r.product() + " (" + r.reason() + ")"));

        System.out.println("\n=== Structured Concurrency: Failure Handling ===");
        try {
            loadDashboardShutdownOnFailure("error");
        } catch (Exception e) {
            System.out.println("Caught expected failure: " + e.getMessage());
        }

        System.out.println("\n=== Structured Concurrency: Multiple Providers (partial failure) ===");
        var results = fetchFromMultipleProviders();
        results.forEach(r -> System.out.println("  " + r));

        System.out.println("\n=== Structured Concurrency: Nested Scopes ===");
        var nestedDashboard = loadDashboardWithNestedScopes("99");
        System.out.println("User: " + nestedDashboard.user());
        System.out.println("Orders: " + nestedDashboard.orders().size() + " items");
        System.out.println("Recommendations: " + nestedDashboard.recommendations().size() + " items");
    }
}
