# Record Introspection

## Purpose
Explore Java records via reflection — discover record components, canonical constructor, accessor methods, and dynamically build records from maps.

## Patterns Used
- **Record Component API** — `getRecordComponents()` to inspect name, type, and accessor of each component
- **Dynamic Instantiation** — finding the canonical constructor via component types and invoking it reflectively
- **Serialization/Deserialization** — converting records to `Map<String, Object>` and back via reflection for round-trip verification
