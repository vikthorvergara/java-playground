# Mini ORM

## Purpose
Generate SQL statements (CREATE TABLE, INSERT, SELECT, UPDATE, DELETE) from annotated Java classes using custom annotations read via reflection.

## Patterns Used
- **Metadata Mapping** — `@Table`, `@Column`, `@Id`, `@AutoIncrement` annotations map Java classes to database schema
- **Code Generation** — reflection-based `QueryBuilder` generates SQL strings from annotated fields and their values
- **Type Mapping** — Java types (String, int, double, boolean) mapped to SQL column types (VARCHAR, INTEGER, DECIMAL, BOOLEAN)
