package com.github.vikthorvergara.reflection.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class DynamicProxyPOC {

    interface UserRepository {
        String findById(int id);
        void save(String name);
        void delete(int id);
    }

    interface Cacheable {
        String lookup(String key);
    }

    static class InMemoryUserRepository implements UserRepository {
        private final Map<Integer, String> store = new HashMap<>(Map.of(
                1, "Alice", 2, "Bob", 3, "Charlie"));
        private int nextId = 4;

        @Override
        public String findById(int id) {
            return store.getOrDefault(id, "not found");
        }

        @Override
        public void save(String name) {
            store.put(nextId++, name);
        }

        @Override
        public void delete(int id) {
            store.remove(id);
        }
    }

    static class LoggingHandler implements InvocationHandler {
        private final Object target;

        LoggingHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.printf("  -> %s(%s)%n", method.getName(), formatArgs(args));
            long start = System.nanoTime();
            Object result = method.invoke(target, args);
            long elapsed = System.nanoTime() - start;
            System.out.printf("  <- %s returned %s [%d us]%n",
                    method.getName(), result, elapsed / 1000);
            return result;
        }

        private String formatArgs(Object[] args) {
            if (args == null) return "";
            var sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(args[i]);
            }
            return sb.toString();
        }
    }

    static class CachingHandler implements InvocationHandler {
        private final Object target;
        private final Map<String, Object> cache = new HashMap<>();

        CachingHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String key = method.getName() + ":" + java.util.Arrays.toString(args);
            if (cache.containsKey(key)) {
                System.out.println("  [CACHE HIT] " + key);
                return cache.get(key);
            }
            System.out.println("  [CACHE MISS] " + key);
            Object result = method.invoke(target, args);
            cache.put(key, result);
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T createLoggingProxy(T target, Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new LoggingHandler(target));
    }

    @SuppressWarnings("unchecked")
    static <T> T createCachingProxy(T target, Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new CachingHandler(target));
    }

    public static void main(String[] args) {
        System.out.println("=== Logging Proxy ===");
        var repo = new InMemoryUserRepository();
        var loggedRepo = createLoggingProxy(repo, UserRepository.class);

        loggedRepo.findById(1);
        loggedRepo.save("Diana");
        loggedRepo.findById(4);
        loggedRepo.delete(2);

        System.out.println("\n=== Caching Proxy ===");
        var cachedRepo = createCachingProxy(repo, UserRepository.class);

        cachedRepo.findById(1);
        cachedRepo.findById(1);
        cachedRepo.findById(3);
        cachedRepo.findById(3);

        System.out.println("\n=== Virtual Implementation (no real class) ===");
        Cacheable fakeCache = (Cacheable) Proxy.newProxyInstance(
                Cacheable.class.getClassLoader(),
                new Class<?>[]{Cacheable.class},
                (proxy, method, methodArgs) -> {
                    if (method.getName().equals("lookup")) {
                        return "virtual-value-for-" + methodArgs[0];
                    }
                    if (method.getName().equals("toString")) {
                        return "VirtualCacheable";
                    }
                    return null;
                });

        System.out.println("lookup('key1') = " + fakeCache.lookup("key1"));
        System.out.println("lookup('key2') = " + fakeCache.lookup("key2"));
        System.out.println("toString() = " + fakeCache);

        System.out.println("\n=== Proxy Class Inspection ===");
        Class<?> proxyClass = loggedRepo.getClass();
        System.out.println("Proxy class: " + proxyClass.getName());
        System.out.println("Is proxy: " + Proxy.isProxyClass(proxyClass));
        System.out.println("Interfaces: " + java.util.Arrays.toString(proxyClass.getInterfaces()));
        System.out.println("InvocationHandler: " + Proxy.getInvocationHandler(loggedRepo).getClass().getSimpleName());
    }
}
