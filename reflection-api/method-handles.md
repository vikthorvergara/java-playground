# MethodHandles vs Reflection

## Purpose
Compare `java.lang.invoke.MethodHandles` with traditional `java.lang.reflect` for field access and method invocation, benchmarking performance differences.

## Patterns Used
- **MethodHandle Lookup** — `MethodHandles.lookup()` and `MethodHandles.privateLookupIn()` for obtaining handles
- **Unreflect** — converting reflection `Field`/`Method` objects into MethodHandles via `unreflectGetter`
- **Microbenchmarking** — warmup iterations followed by measured runs to compare invocation overhead
