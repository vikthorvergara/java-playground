package com.github.vikthorvergara.annotations.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

public class CliParserPOC {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Command {
        String name();
        String description() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Option {
        String name();
        String shortName() default "";
        String description() default "";
        boolean required() default false;
        String defaultValue() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Flag {
        String name();
        String shortName() default "";
        String description() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Positional {
        int index();
        String description() default "";
    }

    public static class CliParser<T> {
        private final Class<T> commandClass;

        public CliParser(Class<T> commandClass) {
            this.commandClass = commandClass;
        }

        public T parse(String[] args) {
            if (Arrays.asList(args).contains("--help") || Arrays.asList(args).contains("-h")) {
                System.out.println(generateUsage());
                return null;
            }

            try {
                var instance = commandClass.getDeclaredConstructor().newInstance();
                applyDefaults(instance);
                var positionals = new ArrayList<String>();
                var fields = commandClass.getDeclaredFields();

                int i = 0;
                while (i < args.length) {
                    var arg = args[i];

                    if (arg.startsWith("--") && arg.contains("=")) {
                        var parts = arg.substring(2).split("=", 2);
                        setNamedField(instance, fields, parts[0], parts[1]);
                        i++;
                    } else if (arg.startsWith("--")) {
                        var name = arg.substring(2);
                        if (isFlagField(fields, name)) {
                            setFlagField(instance, fields, name);
                            i++;
                        } else if (i + 1 < args.length) {
                            setNamedField(instance, fields, name, args[i + 1]);
                            i += 2;
                        } else {
                            throw new RuntimeException("Missing value for option: --" + name);
                        }
                    } else if (arg.startsWith("-") && arg.length() == 2) {
                        var shortName = arg.substring(1);
                        if (isFlagFieldShort(fields, shortName)) {
                            setFlagFieldShort(instance, fields, shortName);
                            i++;
                        } else if (i + 1 < args.length) {
                            setShortNamedField(instance, fields, shortName, args[i + 1]);
                            i += 2;
                        } else {
                            throw new RuntimeException("Missing value for option: -" + shortName);
                        }
                    } else {
                        positionals.add(arg);
                        i++;
                    }
                }

                for (int j = 0; j < positionals.size(); j++) {
                    setPositionalField(instance, fields, j, positionals.get(j));
                }

                validateRequired(instance, fields);
                return instance;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        private void applyDefaults(T instance) throws IllegalAccessException {
            for (var field : commandClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Option.class)) {
                    var option = field.getAnnotation(Option.class);
                    if (!option.defaultValue().isEmpty()) {
                        field.setAccessible(true);
                        field.set(instance, convertValue(option.defaultValue(), field.getType()));
                    }
                }
            }
        }

        private boolean isFlagField(Field[] fields, String name) {
            for (var field : fields) {
                if (field.isAnnotationPresent(Flag.class) && field.getAnnotation(Flag.class).name().equals(name)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isFlagFieldShort(Field[] fields, String shortName) {
            for (var field : fields) {
                if (field.isAnnotationPresent(Flag.class) && field.getAnnotation(Flag.class).shortName().equals(shortName)) {
                    return true;
                }
            }
            return false;
        }

        private void setFlagField(T instance, Field[] fields, String name) throws IllegalAccessException {
            for (var field : fields) {
                if (field.isAnnotationPresent(Flag.class) && field.getAnnotation(Flag.class).name().equals(name)) {
                    field.setAccessible(true);
                    field.set(instance, true);
                    return;
                }
            }
            throw new RuntimeException("Unknown flag: --" + name);
        }

        private void setFlagFieldShort(T instance, Field[] fields, String shortName) throws IllegalAccessException {
            for (var field : fields) {
                if (field.isAnnotationPresent(Flag.class) && field.getAnnotation(Flag.class).shortName().equals(shortName)) {
                    field.setAccessible(true);
                    field.set(instance, true);
                    return;
                }
            }
            throw new RuntimeException("Unknown flag: -" + shortName);
        }

        private void setNamedField(T instance, Field[] fields, String name, String value) throws IllegalAccessException {
            for (var field : fields) {
                if (field.isAnnotationPresent(Option.class) && field.getAnnotation(Option.class).name().equals(name)) {
                    field.setAccessible(true);
                    field.set(instance, convertValue(value, field.getType()));
                    return;
                }
            }
            throw new RuntimeException("Unknown option: --" + name);
        }

        private void setShortNamedField(T instance, Field[] fields, String shortName, String value) throws IllegalAccessException {
            for (var field : fields) {
                if (field.isAnnotationPresent(Option.class) && field.getAnnotation(Option.class).shortName().equals(shortName)) {
                    field.setAccessible(true);
                    field.set(instance, convertValue(value, field.getType()));
                    return;
                }
            }
            throw new RuntimeException("Unknown option: -" + shortName);
        }

        private void setPositionalField(T instance, Field[] fields, int index, String value) throws IllegalAccessException {
            for (var field : fields) {
                if (field.isAnnotationPresent(Positional.class) && field.getAnnotation(Positional.class).index() == index) {
                    field.setAccessible(true);
                    field.set(instance, convertValue(value, field.getType()));
                    return;
                }
            }
        }

        private void validateRequired(T instance, Field[] fields) throws IllegalAccessException {
            for (var field : fields) {
                if (field.isAnnotationPresent(Option.class) && field.getAnnotation(Option.class).required()) {
                    field.setAccessible(true);
                    var value = field.get(instance);
                    if (value == null || (value instanceof String s && s.isEmpty())) {
                        throw new RuntimeException("Required option missing: --" + field.getAnnotation(Option.class).name());
                    }
                }
            }
        }

        private Object convertValue(String raw, Class<?> type) {
            if (type == String.class) return raw;
            if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
            if (type == double.class || type == Double.class) return Double.parseDouble(raw);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
            if (type == long.class || type == Long.class) return Long.parseLong(raw);
            return raw;
        }

        public String generateUsage() {
            var sb = new StringBuilder();
            var command = commandClass.getAnnotation(Command.class);

            if (command != null) {
                sb.append("Usage: ").append(command.name());
            } else {
                sb.append("Usage: ").append(commandClass.getSimpleName().toLowerCase());
            }

            var positionalFields = new ArrayList<Field>();
            for (var field : commandClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Positional.class)) {
                    positionalFields.add(field);
                }
            }
            positionalFields.sort((a, b) -> a.getAnnotation(Positional.class).index() - b.getAnnotation(Positional.class).index());

            for (var field : positionalFields) {
                var pos = field.getAnnotation(Positional.class);
                sb.append(" <").append(field.getName()).append(">");
            }

            sb.append(" [OPTIONS]");

            if (command != null && !command.description().isEmpty()) {
                sb.append("\n\n").append(command.description());
            }

            if (!positionalFields.isEmpty()) {
                sb.append("\n\nPositional arguments:");
                for (var field : positionalFields) {
                    var pos = field.getAnnotation(Positional.class);
                    sb.append("\n  ").append(field.getName());
                    if (!pos.description().isEmpty()) {
                        sb.append("  ").append(pos.description());
                    }
                }
            }

            var optionFields = new ArrayList<Field>();
            var flagFields = new ArrayList<Field>();
            for (var field : commandClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Option.class)) optionFields.add(field);
                if (field.isAnnotationPresent(Flag.class)) flagFields.add(field);
            }

            if (!optionFields.isEmpty()) {
                sb.append("\n\nOptions:");
                for (var field : optionFields) {
                    var opt = field.getAnnotation(Option.class);
                    var line = new StringBuilder("  --").append(opt.name());
                    if (!opt.shortName().isEmpty()) {
                        line.append(", -").append(opt.shortName());
                    }
                    line.append(" <").append(field.getType().getSimpleName().toLowerCase()).append(">");
                    if (opt.required()) line.append(" (required)");
                    if (!opt.defaultValue().isEmpty()) line.append(" [default: ").append(opt.defaultValue()).append("]");
                    if (!opt.description().isEmpty()) line.append("  ").append(opt.description());
                    sb.append("\n").append(line);
                }
            }

            if (!flagFields.isEmpty()) {
                sb.append("\n\nFlags:");
                for (var field : flagFields) {
                    var flag = field.getAnnotation(Flag.class);
                    var line = new StringBuilder("  --").append(flag.name());
                    if (!flag.shortName().isEmpty()) {
                        line.append(", -").append(flag.shortName());
                    }
                    if (!flag.description().isEmpty()) line.append("  ").append(flag.description());
                    sb.append("\n").append(line);
                }
            }

            sb.append("\n\nGeneral:");
            sb.append("\n  --help, -h  Show this help message");

            return sb.toString();
        }
    }

    @Command(name = "search", description = "Search for products in the catalog")
    public static class SearchCommand {
        @Positional(index = 0, description = "Search term")
        String query;

        @Option(name = "category", shortName = "c", description = "Product category filter", defaultValue = "all")
        String category;

        @Option(name = "max-price", shortName = "p", description = "Maximum price filter")
        double maxPrice;

        @Option(name = "limit", shortName = "l", description = "Max number of results", defaultValue = "10")
        int limit;

        @Flag(name = "in-stock", shortName = "s", description = "Only show in-stock items")
        boolean inStockOnly;

        @Flag(name = "verbose", shortName = "v", description = "Enable verbose output")
        boolean verbose;

        @Override
        public String toString() {
            return "SearchCommand{" +
                    "query='" + query + '\'' +
                    ", category='" + category + '\'' +
                    ", maxPrice=" + maxPrice +
                    ", limit=" + limit +
                    ", inStockOnly=" + inStockOnly +
                    ", verbose=" + verbose +
                    '}';
        }
    }

    public static void main(String[] args) {
        var parser = new CliParser<>(SearchCommand.class);

        System.out.println("=== Parsing: RTX 5090 --category Gaming --max-price 3000 --in-stock ===");
        var cmd1 = parser.parse(new String[]{"RTX 5090", "--category", "Gaming", "--max-price", "3000", "--in-stock"});
        System.out.println(cmd1);

        System.out.println("\n=== Parsing: Keychron -c Keyboard -l 5 -v ===");
        var cmd2 = parser.parse(new String[]{"Keychron", "-c", "Keyboard", "-l", "5", "-v"});
        System.out.println(cmd2);

        System.out.println("\n=== Parsing: --help ===");
        parser.parse(new String[]{"--help"});
    }
}
