package com.github.vikthorvergara.annotations.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestScannerPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Controller {
        String basePath();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Route {
        String method();
        String path();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface PathParam {
        String name();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface QueryParam {
        String name();
    }

    static class RouteEntry {
        final String httpMethod;
        final String fullPath;
        final Pattern pathPattern;
        final List<String> pathParamNames;
        final Method method;
        final Object controllerInstance;

        RouteEntry(String httpMethod, String fullPath, Method method, Object controllerInstance) {
            this.httpMethod = httpMethod;
            this.fullPath = fullPath;
            this.method = method;
            this.controllerInstance = controllerInstance;
            this.pathParamNames = new ArrayList<>();

            var regex = new StringBuilder();
            var matcher = Pattern.compile("\\{(\\w+)}").matcher(fullPath);
            while (matcher.find()) {
                pathParamNames.add(matcher.group(1));
            }
            var patternStr = fullPath.replaceAll("\\{\\w+}", "(\\\\w+)");
            this.pathPattern = Pattern.compile("^" + patternStr + "$");
        }

        @Override
        public String toString() {
            return httpMethod + " " + fullPath + " -> " + method.getName() + "()";
        }
    }

    static class RouteScanner {
        private final List<RouteEntry> routes = new ArrayList<>();

        void scan(Class<?>... classes) {
            for (var clazz : classes) {
                if (!clazz.isAnnotationPresent(Controller.class)) continue;

                var controller = clazz.getAnnotation(Controller.class);
                var basePath = controller.basePath();
                Object instance;
                try {
                    var constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    instance = constructor.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName(), e);
                }

                for (var method : clazz.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(Route.class)) continue;

                    var route = method.getAnnotation(Route.class);
                    var fullPath = basePath + route.path();
                    routes.add(new RouteEntry(route.method(), fullPath, method, instance));
                }
            }
        }

        void printRouteTable() {
            System.out.println("Discovered Routes:");
            for (var entry : routes) {
                System.out.println("  " + entry);
            }
        }

        Object dispatch(String httpMethod, String requestPath) throws Exception {
            var path = requestPath;
            var queryString = "";
            if (requestPath.contains("?")) {
                var parts = requestPath.split("\\?", 2);
                path = parts[0];
                queryString = parts[1];
            }

            var queryParams = parseQueryParams(queryString);

            for (var entry : routes) {
                if (!entry.httpMethod.equalsIgnoreCase(httpMethod)) continue;

                Matcher matcher = entry.pathPattern.matcher(path);
                if (!matcher.matches()) continue;

                var pathValues = new HashMap<String, String>();
                for (int i = 0; i < entry.pathParamNames.size(); i++) {
                    pathValues.put(entry.pathParamNames.get(i), matcher.group(i + 1));
                }

                var method = entry.method;
                var parameters = method.getParameters();
                var args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    var param = parameters[i];
                    if (param.isAnnotationPresent(PathParam.class)) {
                        var name = param.getAnnotation(PathParam.class).name();
                        args[i] = convertValue(pathValues.get(name), param.getType());
                    } else if (param.isAnnotationPresent(QueryParam.class)) {
                        var name = param.getAnnotation(QueryParam.class).name();
                        args[i] = convertValue(queryParams.get(name), param.getType());
                    }
                }

                method.setAccessible(true);
                return method.invoke(entry.controllerInstance, args);
            }
            throw new RuntimeException("No route found for " + httpMethod + " " + requestPath);
        }

        private Map<String, String> parseQueryParams(String queryString) {
            var params = new HashMap<String, String>();
            if (queryString == null || queryString.isEmpty()) return params;
            for (var pair : queryString.split("&")) {
                var kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
            }
            return params;
        }

        private Object convertValue(String value, Class<?> type) {
            if (value == null) return null;
            if (type == int.class || type == Integer.class) return Integer.parseInt(value);
            if (type == long.class || type == Long.class) return Long.parseLong(value);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
            return value;
        }
    }

    @Controller(basePath = "/users")
    static class UserController {
        private final List<Map<String, Object>> users = new ArrayList<>();

        UserController() {
            users.add(Map.of("id", 0, "name", "Alice", "age", 30));
            users.add(Map.of("id", 1, "name", "Bob", "age", 35));
            users.add(Map.of("id", 2, "name", "Charlie", "age", 28));
            users.add(Map.of("id", 3, "name", "Vikthor", "age", 30));
            users.add(Map.of("id", 4, "name", "Martin", "age", 62));
        }

        @Route(method = "GET", path = "/")
        public List<String> listUsers() {
            var names = new ArrayList<String>();
            for (var user : users) {
                names.add((String) user.get("name"));
            }
            return names;
        }

        @Route(method = "GET", path = "/{id}")
        public Map<String, Object> getUser(@PathParam(name = "id") int id) {
            if (id < 0 || id >= users.size()) {
                return Map.of("error", "User not found", "id", id);
            }
            return users.get(id);
        }

        @Route(method = "POST", path = "/")
        public Map<String, Object> createUser(@QueryParam(name = "name") String name, @QueryParam(name = "age") int age) {
            var newId = users.size();
            var user = new HashMap<String, Object>();
            user.put("id", newId);
            user.put("name", name);
            user.put("age", age);
            users.add(user);
            return user;
        }
    }

    public static void main(String[] args) throws Exception {
        var scanner = new RouteScanner();
        scanner.scan(UserController.class);

        System.out.println("=== Route Table ===");
        scanner.printRouteTable();

        System.out.println("\n=== GET /users/ ===");
        var allUsers = scanner.dispatch("GET", "/users/");
        System.out.println("Response: " + allUsers);

        System.out.println("\n=== GET /users/1 ===");
        var user = scanner.dispatch("GET", "/users/1");
        System.out.println("Response: " + user);

        System.out.println("\n=== POST /users/?name=Diana&age=28 ===");
        var created = scanner.dispatch("POST", "/users/?name=Diana&age=28");
        System.out.println("Response: " + created);

        System.out.println("\n=== GET /users/ (after create) ===");
        var updatedList = scanner.dispatch("GET", "/users/");
        System.out.println("Response: " + updatedList);
    }
}
