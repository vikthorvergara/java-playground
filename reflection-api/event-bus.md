# Reflective Event Bus

## Purpose
Build a publish/subscribe event bus where listeners register via a custom `@Subscribe` annotation. The bus discovers annotated methods via reflection and dispatches events by matching parameter types.

## Patterns Used
- **Observer Pattern** — decoupled publishers and subscribers communicating through events
- **Annotation-Driven Registration** — `@Subscribe` marks handler methods, discovered at runtime via reflection
- **Type-Based Dispatch** — events are routed to handlers by matching the method parameter type against the published event's class
