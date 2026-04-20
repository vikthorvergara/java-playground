package com.github.vikthorvergara.jdk.basics;

import java.util.List;

public class PatternMatchingPOC {

    sealed interface Shape permits Circle, Square, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Square(double side) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    sealed interface Json permits JsonNull, JsonBool, JsonNumber, JsonString, JsonArray, JsonObject {}
    record JsonNull() implements Json {}
    record JsonBool(boolean value) implements Json {}
    record JsonNumber(double value) implements Json {}
    record JsonString(String value) implements Json {}
    record JsonArray(List<Json> items) implements Json {}
    record JsonObject(java.util.Map<String, Json> fields) implements Json {}

    static String describeOld(Object obj) {
        if (obj instanceof Integer) {
            Integer i = (Integer) obj;
            return "int " + (i * 2);
        } else if (obj instanceof String) {
            String s = (String) obj;
            return "string len=" + s.length();
        } else {
            return "other";
        }
    }

    static String describeNew(Object obj) {
        if (obj instanceof Integer i) {
            return "int " + (i * 2);
        } else if (obj instanceof String s) {
            return "string len=" + s.length();
        } else {
            return "other";
        }
    }

    static void instanceofPatterns() {
        Object a = 42;
        Object b = "hello world";
        Object c = 3.14;

        System.out.println("old style int -> " + describeOld(a));
        System.out.println("new style int -> " + describeNew(a));
        System.out.println("new style str -> " + describeNew(b));
        System.out.println("new style other -> " + describeNew(c));
    }

    static String classify(Object obj) {
        return switch (obj) {
            case Integer i when i < 0 -> "negative int " + i;
            case Integer i when i == 0 -> "zero";
            case Integer i -> "positive int " + i;
            case String s when s.isEmpty() -> "empty string";
            case String s -> "string of length " + s.length();
            case int[] arr -> "int array of size " + arr.length;
            case null -> "null value";
            default -> "unknown " + obj.getClass().getSimpleName();
        };
    }

    static void switchPatterns() {
        System.out.println("classify(-5) -> " + classify(-5));
        System.out.println("classify(0) -> " + classify(0));
        System.out.println("classify(7) -> " + classify(7));
        System.out.println("classify(\"\") -> " + classify(""));
        System.out.println("classify(\"java\") -> " + classify("java"));
        System.out.println("classify(int[3]) -> " + classify(new int[]{1, 2, 3}));
        System.out.println("classify(null) -> " + classify(null));
        System.out.println("classify(3.14) -> " + classify(3.14));
    }

    static double area(Shape shape) {
        return switch (shape) {
            case Circle(double r) -> Math.PI * r * r;
            case Square(double s) -> s * s;
            case Rectangle(double w, double h) -> w * h;
            case Triangle(double b, double h) -> 0.5 * b * h;
        };
    }

    static void recordPatterns() {
        List<Shape> shapes = List.of(
                new Circle(2),
                new Square(3),
                new Rectangle(4, 5),
                new Triangle(6, 4)
        );
        for (Shape s : shapes) {
            System.out.println(s + " area=" + area(s));
        }
    }

    record Point(int x, int y) {}
    record Line(Point start, Point end) {}

    static String describeLine(Line line) {
        return switch (line) {
            case Line(Point(int x1, int y1), Point(int x2, int y2)) when x1 == x2 -> "vertical at x=" + x1;
            case Line(Point(int x1, int y1), Point(int x2, int y2)) when y1 == y2 -> "horizontal at y=" + y1;
            case Line(Point(int x1, int y1), Point(int x2, int y2)) ->
                    "diagonal from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")";
        };
    }

    static void nestedRecordPatterns() {
        var lines = List.of(
                new Line(new Point(2, 1), new Point(2, 7)),
                new Line(new Point(0, 4), new Point(8, 4)),
                new Line(new Point(0, 0), new Point(5, 5))
        );
        for (Line l : lines) {
            System.out.println(l + " -> " + describeLine(l));
        }
    }

    static String render(Json json) {
        return switch (json) {
            case JsonNull() -> "null";
            case JsonBool(boolean b) -> Boolean.toString(b);
            case JsonNumber(double n) -> Double.toString(n);
            case JsonString(String s) -> "\"" + s + "\"";
            case JsonArray(List<Json> items) -> items.stream().map(PatternMatchingPOC::render).toList().toString();
            case JsonObject(var fields) -> fields.entrySet().stream()
                    .map(e -> "\"" + e.getKey() + "\":" + render(e.getValue()))
                    .toList().toString();
        };
    }

    static void exhaustiveSealedDispatch() {
        Json sample = new JsonObject(java.util.Map.of(
                "name", new JsonString("vikthor"),
                "active", new JsonBool(true),
                "score", new JsonNumber(42),
                "tags", new JsonArray(List.of(new JsonString("java"), new JsonString("streams"))),
                "nothing", new JsonNull()
        ));
        System.out.println(render(sample));
    }

    public static void main(String[] args) {
        System.out.println("--- instanceof patterns ---");
        instanceofPatterns();

        System.out.println("\n--- switch patterns with guards ---");
        switchPatterns();

        System.out.println("\n--- record patterns + sealed ---");
        recordPatterns();

        System.out.println("\n--- nested record patterns ---");
        nestedRecordPatterns();

        System.out.println("\n--- exhaustive sealed dispatch ---");
        exhaustiveSealedDispatch();
    }
}
