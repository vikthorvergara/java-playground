# Generic Type Resolution

## Purpose
Read generic type information at runtime via reflection using `ParameterizedType`, `WildcardType`, `TypeVariable`, and `GenericArrayType`.

## Patterns Used
- **Type Introspection** — recursive inspection of `java.lang.reflect.Type` hierarchy
- **Pattern Matching on Types** — branching on `ParameterizedType`, `WildcardType`, `TypeVariable`, `GenericArrayType`, and raw `Class`
- **Method Signature Analysis** — reading generic type parameters and bounds from method declarations
