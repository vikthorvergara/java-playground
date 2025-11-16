# Design Patterns in Java

A comprehensive collection of software design pattern implementations in Java, organized by category.

## Prerequisites

- Java 21 or higher
- Gradle 8.12 or higher (or use the included Gradle wrapper)

## Project Structure

```
src/main/java/com/github/vikthorvergara/designpatterns/
├── creational/
│   ├── builder/         - Builder Pattern
│   ├── prototype/       - Prototype Pattern
│   ├── singleton/       - Singleton Pattern
│   ├── factorymethod/   - Factory Method Pattern
│   └── abstractfactory/ - Abstract Factory Pattern
└── structural/
    └── adapter/         - Adapter Pattern
```

## Building the Project

### Using Gradle Wrapper (Recommended)

```bash
./gradlew build
```

### Using System Gradle

```bash
gradle build
```

## Running Examples

Each design pattern has a dedicated Gradle task for easy execution:

### Creational Patterns

```bash
# Builder Pattern
./gradlew runBuilder

# Prototype Pattern
./gradlew runPrototype

# Singleton Pattern
./gradlew runSingleton

# Factory Method Pattern
./gradlew runFactoryMethod

# Abstract Factory Pattern
./gradlew runAbstractFactory
```

### Structural Patterns

```bash
# Adapter Pattern
./gradlew runAdapter
```

## Design Patterns Implemented

### Creational Patterns

1. **Builder** - Constructs complex objects step by step
   - Example: Building gaming and office computers with different specifications

2. **Prototype** - Creates new objects by cloning existing ones
   - Example: Shape registry with cloneable shapes (Circle, Rectangle, Square)

3. **Singleton** - Ensures a class has only one instance
   - Example: DatabaseConnection and ConfigurationManager

4. **Factory Method** - Defines an interface for creating objects
   - Example: Blob storage systems (LocalSystem, S3, GCS)

5. **Abstract Factory** - Creates families of related objects
   - Example: Cloud provider factories (AWS, GCP) for storage and database

### Structural Patterns

1. **Adapter** - Converts one interface to another
   - Example: Media player adapting to different audio formats (MP3, MP4, VLC)

## Running All Tests

```bash
./gradlew test
```

## Cleaning Build Artifacts

```bash
./gradlew clean
```

## IDE Setup

This project can be imported into any Java IDE that supports Gradle:

- **IntelliJ IDEA**: File → Open → Select project directory
- **Eclipse**: File → Import → Existing Gradle Project
- **VS Code**: Install Java Extension Pack and open the project folder

## License

This project is for educational purposes.
