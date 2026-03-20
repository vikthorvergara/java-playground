package com.github.vikthorvergara.jdk;

import java.util.concurrent.StructuredTaskScope;

public class ScopedValuesPOC {

    record User(String id, String name, String role) {}
    record AuditEntry(String action, String userId, String detail) {}

    static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();
    static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    static final ScopedValue<String> TENANT = ScopedValue.newInstance();

    static class OrderService {
        static String placeOrder(String product) {
            var user = CURRENT_USER.get();
            var requestId = REQUEST_ID.get();
            System.out.println("  [OrderService] requestId=" + requestId + " user=" + user.name() + " placing order for: " + product);
            AuditService.log("PLACE_ORDER", "product=" + product);
            return "ORD-" + System.nanoTime() % 10000;
        }
    }

    static class AuditService {
        static void log(String action, String detail) {
            var user = CURRENT_USER.get();
            var requestId = REQUEST_ID.orElse("N/A");
            var entry = new AuditEntry(action, user.id(), detail);
            System.out.println("  [AuditService] requestId=" + requestId + " audit=" + entry);
        }
    }

    static class InventoryService {
        static int checkStock(String product) {
            var user = CURRENT_USER.get();
            System.out.println("  [InventoryService] user=" + user.name() + " checking stock for: " + product);
            return 42;
        }
    }

    static class TenantAwareService {
        static String getConfig(String key) {
            var tenant = TENANT.get();
            return "config[" + tenant + "." + key + "]=enabled";
        }
    }

    static void handleRequest(User user, String requestId) {
        ScopedValue.where(CURRENT_USER, user)
                .where(REQUEST_ID, requestId)
                .run(() -> {
                    System.out.println("  Processing request " + requestId + " for " + user.name());
                    var orderId = OrderService.placeOrder("RTX 5090");
                    var stock = InventoryService.checkStock("RTX 5090");
                    System.out.println("  Order " + orderId + " placed, stock remaining: " + stock);
                });
    }

    static void handleMultiTenantRequest(User user) {
        ScopedValue.where(CURRENT_USER, user).run(() -> {
            ScopedValue.where(TENANT, "acme-corp").run(() -> {
                var config = TenantAwareService.getConfig("feature.beta");
                System.out.println("  Tenant ACME: " + config);
            });

            ScopedValue.where(TENANT, "globex-inc").run(() -> {
                var config = TenantAwareService.getConfig("feature.beta");
                System.out.println("  Tenant GLOBEX: " + config);
            });
        });
    }

    static void handleWithRebinding() {
        var admin = new User("admin-1", "Admin", "ADMIN");
        var serviceAccount = new User("svc-1", "ServiceAccount", "SERVICE");

        ScopedValue.where(CURRENT_USER, admin).run(() -> {
            System.out.println("  Outer scope user: " + CURRENT_USER.get().name());
            AuditService.log("ADMIN_ACTION", "starting maintenance");

            ScopedValue.where(CURRENT_USER, serviceAccount).run(() -> {
                System.out.println("  Inner scope user (rebound): " + CURRENT_USER.get().name());
                AuditService.log("SERVICE_ACTION", "automated cleanup");
            });

            System.out.println("  Back to outer scope: " + CURRENT_USER.get().name());
            AuditService.log("ADMIN_ACTION", "maintenance complete");
        });
    }

    static void handleConcurrentWithScopedValues() throws Exception {
        var user = new User("user-77", "Viktor", "USER");

        ScopedValue.where(CURRENT_USER, user)
                .where(REQUEST_ID, "REQ-CONCURRENT")
                .run(() -> {
                    try (var scope = StructuredTaskScope.open()) {
                        scope.fork(() -> {
                            System.out.println("  [Thread-1] user=" + CURRENT_USER.get().name()
                                    + " requestId=" + REQUEST_ID.get());
                            return OrderService.placeOrder("Monitor");
                        });

                        scope.fork(() -> {
                            System.out.println("  [Thread-2] user=" + CURRENT_USER.get().name()
                                    + " requestId=" + REQUEST_ID.get());
                            return InventoryService.checkStock("Monitor");
                        });

                        scope.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    static void demonstrateUnboundCheck() {
        System.out.println("  CURRENT_USER is bound: " + CURRENT_USER.isBound());
        System.out.println("  Using orElse: " + CURRENT_USER.orElse(new User("anonymous", "Guest", "NONE")));

        ScopedValue.where(CURRENT_USER, new User("u1", "Bound User", "USER")).run(() -> {
            System.out.println("  Inside scope - CURRENT_USER is bound: " + CURRENT_USER.isBound());
            System.out.println("  Value: " + CURRENT_USER.get());
        });

        System.out.println("  After scope - CURRENT_USER is bound: " + CURRENT_USER.isBound());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Scoped Values: Request Context Propagation ===");
        handleRequest(new User("u-1", "Alice", "USER"), "REQ-001");
        handleRequest(new User("u-2", "Bob", "ADMIN"), "REQ-002");

        System.out.println("\n=== Scoped Values: Multi-Tenant Isolation ===");
        handleMultiTenantRequest(new User("u-3", "Charlie", "USER"));

        System.out.println("\n=== Scoped Values: Rebinding in Nested Scopes ===");
        handleWithRebinding();

        System.out.println("\n=== Scoped Values: Inherited by Virtual Threads ===");
        handleConcurrentWithScopedValues();

        System.out.println("\n=== Scoped Values: Unbound Checks and orElse ===");
        demonstrateUnboundCheck();
    }
}
