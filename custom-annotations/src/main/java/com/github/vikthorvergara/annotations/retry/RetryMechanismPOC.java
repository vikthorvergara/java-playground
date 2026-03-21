package com.github.vikthorvergara.annotations.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetryMechanismPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Retry {
        int maxAttempts() default 3;
        long backoffMs() default 50;
        Class<? extends Throwable>[] retryOn() default {RuntimeException.class};
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Fallback {
        String methodName();
    }

    public interface ProductService {
        String fetchProduct(String name);
        double fetchPrice(String name);
        List<String> listCategories();
    }

    public static class UnstableProductService implements ProductService {

        private final Map<String, Integer> callCounts = new HashMap<>();

        private int getAndIncrement(String key) {
            var count = callCounts.getOrDefault(key, 0);
            callCounts.put(key, count + 1);
            return count;
        }

        @Override
        @Retry(maxAttempts = 5, backoffMs = 10)
        public String fetchProduct(String name) {
            var attempt = getAndIncrement("fetchProduct");
            if (attempt < 2) {
                throw new RuntimeException("Service unavailable for fetchProduct (attempt " + (attempt + 1) + ")");
            }
            return "Product{name='" + name + "', price=$2499.99}";
        }

        @Override
        @Retry(maxAttempts = 3, backoffMs = 10)
        @Fallback(methodName = "fetchPriceFallback")
        public double fetchPrice(String name) {
            throw new RuntimeException("Price service is down for " + name);
        }

        @Override
        @Retry(maxAttempts = 3, backoffMs = 10)
        public List<String> listCategories() {
            var attempt = getAndIncrement("listCategories");
            if (attempt < 1) {
                throw new RuntimeException("Category service temporarily unavailable (attempt " + (attempt + 1) + ")");
            }
            return List.of("GPUs", "Audio", "Peripherals", "Storage");
        }

        public double fetchPriceFallback(String name) {
            System.out.println("  [FALLBACK] Returning cached price for " + name);
            return 249.00;
        }
    }

    public static class RetryHandler implements InvocationHandler {

        private final Object target;

        public RetryHandler(Object target) {
            this.target = target;
        }

        @SuppressWarnings("unchecked")
        public static <T> T createProxy(Class<T> iface, Object target) {
            return (T) Proxy.newProxyInstance(
                    iface.getClassLoader(),
                    new Class<?>[]{iface},
                    new RetryHandler(target)
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            var targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            var retry = targetMethod.getAnnotation(Retry.class);

            if (retry == null) {
                return targetMethod.invoke(target, args);
            }

            var maxAttempts = retry.maxAttempts();
            var backoffMs = retry.backoffMs();
            var retryOn = retry.retryOn();

            Throwable lastException = null;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    System.out.println("  [ATTEMPT " + attempt + "/" + maxAttempts + "] " + method.getName());
                    var result = targetMethod.invoke(target, args);
                    System.out.println("  [SUCCESS] " + method.getName() + " returned: " + result);
                    return result;
                } catch (InvocationTargetException e) {
                    lastException = e.getCause();
                    var shouldRetry = false;
                    for (var exClass : retryOn) {
                        if (exClass.isInstance(lastException)) {
                            shouldRetry = true;
                            break;
                        }
                    }

                    if (!shouldRetry) {
                        throw lastException;
                    }

                    System.out.println("  [FAILED] " + method.getName() + " - " + lastException.getMessage());

                    if (attempt < maxAttempts) {
                        Thread.sleep(backoffMs);
                    }
                }
            }

            System.out.println("  [EXHAUSTED] All " + maxAttempts + " attempts failed for " + method.getName());

            var fallback = targetMethod.getAnnotation(Fallback.class);
            if (fallback != null) {
                var fallbackMethodName = fallback.methodName();
                System.out.println("  [FALLBACK] Invoking " + fallbackMethodName);
                var fallbackMethod = target.getClass().getMethod(fallbackMethodName, method.getParameterTypes());
                return fallbackMethod.invoke(target, args);
            }

            throw lastException;
        }
    }

    public static void main(String[] args) {
        var realService = new UnstableProductService();
        var service = RetryHandler.createProxy(ProductService.class, realService);

        System.out.println("=== Fetch Product: RTX 5090 ===");
        var product = service.fetchProduct("RTX 5090");
        System.out.println("Result: " + product);

        System.out.println("\n=== Fetch Price: AirPods Pro ===");
        var price = service.fetchPrice("AirPods Pro");
        System.out.println("Result: $" + price);

        System.out.println("\n=== List Categories ===");
        var categories = service.listCategories();
        System.out.println("Result: " + categories);
    }
}
