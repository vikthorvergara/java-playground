# CLI Argument Parser

## Purpose
Map command-line arguments to annotated fields via reflection, supporting flags, named options with short/long forms, positional arguments, and auto-generated help text.

## Patterns Used
- **Declarative Configuration** — `@Command`, `@Option`, `@Flag`, `@Positional` annotations describe CLI structure without parsing logic in the command class
- **Reflective Field Binding** — parser scans annotated fields, matches them against args, converts types, and sets values via reflection
- **Self-Documenting Code** — help/usage text is generated directly from annotation metadata (descriptions, defaults, types)
