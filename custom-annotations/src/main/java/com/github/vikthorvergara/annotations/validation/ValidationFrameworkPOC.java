package com.github.vikthorvergara.annotations.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationFrameworkPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotBlank {
        String message() default "must not be blank";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Min {
        long value();
        String message() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Max {
        long value();
        String message() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Email {
        String message() default "must be a valid email address";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface PatternConstraint {
        String regexp();
        String message() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Size {
        int min() default 0;
        int max() default Integer.MAX_VALUE;
        String message() default "";
    }

    static class Validator {
        static List<String> validate(Object obj) {
            var violations = new ArrayList<String>();
            var clazz = obj.getClass();

            for (var field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value;
                try {
                    value = field.get(obj);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if (field.isAnnotationPresent(NotBlank.class)) {
                    var ann = field.getAnnotation(NotBlank.class);
                    if (value == null || value.toString().isBlank()) {
                        violations.add(field.getName() + ": " + ann.message());
                    }
                }

                if (field.isAnnotationPresent(Email.class)) {
                    var ann = field.getAnnotation(Email.class);
                    if (value == null || !value.toString().contains("@") || !value.toString().contains(".")) {
                        violations.add(field.getName() + ": " + ann.message());
                    }
                }

                if (field.isAnnotationPresent(Min.class)) {
                    var ann = field.getAnnotation(Min.class);
                    if (value instanceof Number num && num.longValue() < ann.value()) {
                        var msg = ann.message().isEmpty()
                                ? "must be >= " + ann.value()
                                : ann.message();
                        violations.add(field.getName() + ": " + msg);
                    }
                }

                if (field.isAnnotationPresent(Max.class)) {
                    var ann = field.getAnnotation(Max.class);
                    if (value instanceof Number num && num.longValue() > ann.value()) {
                        var msg = ann.message().isEmpty()
                                ? "must be <= " + ann.value()
                                : ann.message();
                        violations.add(field.getName() + ": " + msg);
                    }
                }

                if (field.isAnnotationPresent(PatternConstraint.class)) {
                    var ann = field.getAnnotation(PatternConstraint.class);
                    if (value != null && !Pattern.matches(ann.regexp(), value.toString())) {
                        var msg = ann.message().isEmpty()
                                ? "must match pattern " + ann.regexp()
                                : ann.message();
                        violations.add(field.getName() + ": " + msg);
                    }
                }

                if (field.isAnnotationPresent(Size.class)) {
                    var ann = field.getAnnotation(Size.class);
                    int size = -1;
                    if (value instanceof String s) {
                        size = s.length();
                    } else if (value instanceof Collection<?> c) {
                        size = c.size();
                    }
                    if (size >= 0 && (size < ann.min() || size > ann.max())) {
                        var msg = ann.message().isEmpty()
                                ? "size must be between " + ann.min() + " and " + ann.max()
                                : ann.message();
                        violations.add(field.getName() + ": " + msg);
                    }
                }
            }

            return violations;
        }
    }

    static class UserRegistration {
        @NotBlank
        final String name;

        @Email
        final String email;

        @Min(0) @Max(150)
        final int age;

        @PatternConstraint(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$")
        final String password;

        @Size(min = 1, max = 5)
        final List<String> hobbies;

        UserRegistration(String name, String email, int age, String password, List<String> hobbies) {
            this.name = name;
            this.email = email;
            this.age = age;
            this.password = password;
            this.hobbies = hobbies;
        }

        @Override
        public String toString() {
            return "UserRegistration{name='%s', email='%s', age=%d, password='%s', hobbies=%s}"
                    .formatted(name, email, age, password, hobbies);
        }
    }

    static void printValidationResult(String label, UserRegistration user) {
        System.out.println("=== " + label + " ===");
        System.out.println("Input: " + user);
        var violations = Validator.validate(user);
        if (violations.isEmpty()) {
            System.out.println("Result: VALID - no violations found");
        } else {
            System.out.println("Result: INVALID - " + violations.size() + " violation(s)");
            for (var v : violations) {
                System.out.println("  - " + v);
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        var valid = new UserRegistration(
                "Vikthor", "vikthor@email.com", 30, "Str0ngPass", List.of("coding", "gaming"));

        var invalid = new UserRegistration(
                "", "not-an-email", 200, "weak", List.of());

        var partiallyInvalid = new UserRegistration(
                "Alice", "alice@mail.com", -5, "NoDigits!", List.of("a", "b", "c", "d", "e", "f"));

        printValidationResult("Valid User", valid);
        printValidationResult("Invalid User", invalid);
        printValidationResult("Partially Invalid User", partiallyInvalid);
    }
}
