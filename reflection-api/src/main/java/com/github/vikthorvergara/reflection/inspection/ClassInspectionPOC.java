package com.github.vikthorvergara.reflection.inspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClassInspectionPOC {

    static class Engine {
        private String type;
        private int horsepower;
        protected boolean turbo;

        public Engine() {
            this("V6", 300, false);
        }

        public Engine(String type, int horsepower, boolean turbo) {
            this.type = type;
            this.horsepower = horsepower;
            this.turbo = turbo;
        }

        public String getType() {
            return type;
        }

        public int getHorsepower() {
            return horsepower;
        }

        private String specs() {
            return type + " " + horsepower + "hp" + (turbo ? " turbo" : "");
        }

        @Override
        public String toString() {
            return specs();
        }
    }

    public static void main(String[] args) {
        Class<?> clazz = Engine.class;

        System.out.println("=== Class Info ===");
        System.out.println("Name: " + clazz.getName());
        System.out.println("Simple name: " + clazz.getSimpleName());
        System.out.println("Superclass: " + clazz.getSuperclass().getSimpleName());
        System.out.println("Enclosing class: " + clazz.getEnclosingClass().getSimpleName());
        System.out.println("Modifiers: " + Modifier.toString(clazz.getModifiers()));

        System.out.println("\n=== Fields ===");
        for (Field field : clazz.getDeclaredFields()) {
            System.out.printf("  %s %s %s%n",
                    Modifier.toString(field.getModifiers()),
                    field.getType().getSimpleName(),
                    field.getName());
        }

        System.out.println("\n=== Constructors ===");
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            var paramTypes = Arrays.stream(constructor.getParameterTypes())
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));
            System.out.printf("  %s(%s)%n", clazz.getSimpleName(), paramTypes);
        }

        System.out.println("\n=== Declared Methods ===");
        for (Method method : clazz.getDeclaredMethods()) {
            var paramTypes = Arrays.stream(method.getParameterTypes())
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));
            System.out.printf("  %s %s %s(%s)%n",
                    Modifier.toString(method.getModifiers()),
                    method.getReturnType().getSimpleName(),
                    method.getName(),
                    paramTypes);
        }

        System.out.println("\n=== Public Methods (inherited included) ===");
        var publicMethods = Arrays.stream(clazz.getMethods())
                .map(Method::getName)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        System.out.println("  " + publicMethods);

        System.out.println("\n=== Interfaces ===");
        System.out.println("  Implements: " +
                (clazz.getInterfaces().length == 0 ? "none" :
                        Arrays.stream(clazz.getInterfaces())
                                .map(Class::getSimpleName)
                                .collect(Collectors.joining(", "))));
    }
}
