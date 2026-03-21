# Retry Mechanism

## Purpose
Build a retry mechanism using custom annotations and reflection-based dynamic proxies. Methods annotated with `@Retry` are automatically retried on failure with configurable backoff and optional fallback.

## Patterns Used
- **Dynamic Proxy** — `java.lang.reflect.Proxy` intercepts method calls to add retry behavior transparently
- **Annotation-Driven Configuration** — `@Retry(maxAttempts, backoffMs, retryOn)` and `@Fallback(methodName)` control retry behavior declaratively
- **Resilience Pattern** — retry with backoff and fallback provides fault tolerance for unreliable operations
