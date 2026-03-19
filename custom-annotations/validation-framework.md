# Validation Framework

## Purpose
Build a constraint validation framework using custom annotations processed via reflection, supporting multiple constraint types with clear error messages.

## Patterns Used
- **Constraint Annotations** — `@NotBlank`, `@Min`, `@Max`, `@Email`, `@PatternConstraint`, `@Size` define validation rules declaratively
- **Reflective Field Scanning** — `Validator` iterates over declared fields, checks for constraint annotations, and evaluates each rule
- **Separation of Concerns** — validation logic is decoupled from domain classes, driven entirely by annotation metadata
