package com.github.vikthorvergara.reflection.invocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodInvocationPOC {

    static class Calculator {
        private int memory = 0;

        public int add(int a, int b) {
            return a + b;
        }

        public int multiply(int a, int b) {
            return a * b;
        }

        private int factorial(int n) {
            if (n <= 1) return 1;
            return n * factorial(n - 1);
        }

        public void storeInMemory(int value) {
            this.memory = value;
        }

        public int recallMemory() {
            return memory;
        }

        public static double circleArea(double radius) {
            return Math.PI * radius * radius;
        }

        public String formatResult(String label, int... values) {
            return label + ": " + Arrays.toString(values);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Invoking Public Methods ===");
        Class<?> clazz = Calculator.class;
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        Object calc = constructor.newInstance();

        Method addMethod = clazz.getMethod("add", int.class, int.class);
        int sum = (int) addMethod.invoke(calc, 15, 27);
        System.out.println("add(15, 27) = " + sum);

        Method multiplyMethod = clazz.getMethod("multiply", int.class, int.class);
        int product = (int) multiplyMethod.invoke(calc, 6, 7);
        System.out.println("multiply(6, 7) = " + product);

        System.out.println("\n=== Invoking Private Methods ===");
        Method factorialMethod = clazz.getDeclaredMethod("factorial", int.class);
        factorialMethod.setAccessible(true);
        int result = (int) factorialMethod.invoke(calc, 8);
        System.out.println("factorial(8) = " + result);

        System.out.println("\n=== Invoking Static Methods ===");
        Method areaMethod = clazz.getMethod("circleArea", double.class);
        double area = (double) areaMethod.invoke(null, 5.0);
        System.out.printf("circleArea(5.0) = %.4f%n", area);

        System.out.println("\n=== Invoking Varargs Methods ===");
        Method formatMethod = clazz.getMethod("formatResult", String.class, int[].class);
        String formatted = (String) formatMethod.invoke(calc, "Scores", new int[]{95, 87, 92});
        System.out.println(formatted);

        System.out.println("\n=== Invoking by Name Dynamically ===");
        String[] operations = {"add", "multiply"};
        int a = 10, b = 5;
        for (String op : operations) {
            Method m = clazz.getMethod(op, int.class, int.class);
            int res = (int) m.invoke(calc, a, b);
            System.out.printf("%s(%d, %d) = %d%n", op, a, b, res);
        }

        System.out.println("\n=== Chaining Reflective Calls ===");
        Method store = clazz.getMethod("storeInMemory", int.class);
        Method recall = clazz.getMethod("recallMemory");
        store.invoke(calc, 42);
        int recalled = (int) recall.invoke(calc);
        System.out.println("Stored 42, recalled: " + recalled);
    }
}
