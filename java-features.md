# Modern Java Features Guide

This document introduces key modern Java features, focusing on enhancements that improve developer productivity, code readability, and system performance. These align with the principle of "extreme simplicity" and modern syntax preferences.

## 1. Local Variable Type Inference (`var`) - Java 10
Reduces boilerplate when declaring local variables, allowing the compiler to infer the type.

```java
// Before
List<Map<String, String>> list = new ArrayList<>();

// After
var list = new ArrayList<Map<String, String>>();
```

## 2. Switch Expressions - Java 14
Simplifies `switch` statements, preventing fall-through bugs and allowing the switch to return a value.

```java
int numLetters = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> 6;
    case TUESDAY                -> 7;
    default                     -> 0;
};
```

## 3. Text Blocks - Java 15
Makes multi-line strings (like JSON, SQL, or HTML) much easier to write and read without escaping quotes and adding newlines manually.

```java
String query = """
    SELECT id, name, email
    FROM users
    WHERE status = 'ACTIVE'
    ORDER BY created_at DESC
    """;
```

## 4. Records - Java 16
Provides a compact syntax for declaring classes that are transparent carriers for immutable data, automatically generating getters, `equals()`, `hashCode()`, and `toString()`.

```java
public record UserDTO(Long id, String username, String email) {}
```

## 5. Pattern Matching for `instanceof` - Java 16
Removes the need for explicit casting after an `instanceof` check.

```java
// Before
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}

// After
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

## 6. Sealed Classes - Java 17
Restricts which other classes or interfaces may extend or implement them, providing better control over inheritance hierarchies.

```java
public sealed interface Shape permits Circle, Rectangle, Square {}
```

## 7. Pattern Matching for `switch` - Java 21
Allows testing an expression against a number of patterns, each with a specific action, so that complex data-oriented queries can be expressed concisely and safely.

```java
String formatted = switch (obj) {
    case Integer i -> String.format("int %d", i);
    case Long l    -> String.format("long %d", l);
    case Double d  -> String.format("double %f", d);
    case String s  -> String.format("String %s", s);
    default        -> obj.toString();
};
```

## 8. Virtual Threads - Java 21
Lightweight threads that dramatically reduce the effort of writing, maintaining, and observing high-throughput concurrent applications.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10_000).forEach(i -> {
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return i;
        });
    });
}
```

## 9. Unnamed Variables & Patterns (`_`) - Java 22
Improves code readability by allowing the use of the underscore character (`_`) to denote variables or patterns that are required by the syntax but whose values are never used.

```java
// Before
try {
    int number = Integer.parseInt("123");
} catch (NumberFormatException ex) {
    System.out.println("Invalid format");
}

// After
try {
    int number = Integer.parseInt("123");
} catch (NumberFormatException _) {
    System.out.println("Invalid format");
}
```

## 10. Markdown Documentation Comments - Java 23
Allows writing JavaDoc comments in Markdown rather than a mix of HTML and JavaDoc tags, making documentation easier to write and read.

```java
/// Returns the user associated with the given ID.
/// 
/// # Usage
/// ```java
/// User user = userService.findById(123L);
/// ```
/// @param id the unique identifier of the user
/// @return the associated user, or null if not found
public User findById(Long id) {
    // implementation
    return null;
}
```

## 11. Stream Gatherers - Java 24
Enhances the Stream API to support custom intermediate operations. This allows stream pipelines to transform data in ways that are difficult to express with existing built-in intermediate operations.

```java
// Using a built-in gatherer like windowFixed
Stream.iterate(0, i -> i + 1)
      .gather(Gatherers.windowFixed(3))
      .limit(2)
      .forEach(System.out::println);
// Output: [0, 1, 2]
//         [3, 4, 5]
```

## 12. Module Import Declarations - Java 25
Simplifies the reuse of modular libraries by enabling developers to succinctly import all of the packages exported by a module, without requiring the importing code to be in a module itself.

```java
import module java.base;

public class Main {
    public static void main(String[] args) {
        // We can use List, Map, Set, Stream, etc. without individual imports
        List<String> list = List.of("Java", "25");
        list.forEach(System.out::println);
    }
}
```

## 13. Primitive Types in Patterns, instanceof, and switch - Java 26
Enhances pattern matching by allowing primitive types in all pattern contexts, and extends `instanceof` and `switch` to work seamlessly with all primitive types.

```java
// Checking and casting primitive types safely
if (value instanceof byte b) {
    // value is safely cast to byte b
    System.out.println("Fits in a byte: " + b);
}

// Switching on primitive types
String result = switch (value) {
    case int i when i > 100 -> "Large integer";
    case byte b -> "A byte";
    case long l -> "A long";
    default -> "Other primitive";
};
```

---

## Version Summary (Java 8 - 26)

### The Modern Baseline
*   **Java 8 (2014 - LTS):** Introduced functional programming to Java. Main features include **Lambda Expressions**, the **Stream API**, `Optional`, the new **Date/Time API**, and **Default Methods** in interfaces.
*   **Java 9 (2017):** Brought modularity to the platform with the **Java Module System (Project Jigsaw)**. Also introduced **JShell** (REPL), factory methods for collections (`List.of()`, `Set.of()`), and private methods in interfaces.
*   **Java 10 (2018):** Introduced **Local-Variable Type Inference (`var`)** to reduce boilerplate code.

### The Java 11 LTS Era
*   **Java 11 (2018 - LTS):** Added the new standard **HTTP Client API** (supporting HTTP/2 and WebSockets), local-variable syntax for lambda parameters (`(var s) -> s.toLowerCase()`), and new utility methods for `String` (`isBlank()`, `lines()`, `repeat()`).
*   **Java 12 (2019):** Introduced **Switch Expressions** (Preview) and updates to the Garbage Collector.
*   **Java 13 (2019):** Introduced **Text Blocks** (Preview) to handle multiline strings easily.
*   **Java 14 (2020):** Finalized **Switch Expressions**, introduced **Helpful NullPointerExceptions** (telling you exactly *what* was null), and previewed **Records**.
*   **Java 15 (2020):** Finalized **Text Blocks**, previewed **Sealed Classes**, and hid the Nashorn JavaScript engine.
*   **Java 16 (2021):** Finalized **Records** (immutable data carriers) and **Pattern Matching for `instanceof`** (eliminating redundant casting).

### The Java 17 LTS Era
*   **Java 17 (2021 - LTS):** Finalized **Sealed Classes** (restricting which classes can extend a class/interface), and strongly encapsulated JDK internals.
*   **Java 18 (2022):** Made **UTF-8 the default charset** across all platforms and introduced a **Simple Web Server** for prototyping.
*   **Java 19 (2022):** Introduced **Virtual Threads** (Preview) and **Structured Concurrency** (Incubator) for high-throughput, lightweight concurrency.
*   **Java 20 (2023):** Introduced **Scoped Values** (Incubator) as a modern alternative to `ThreadLocal`.

### The Java 21 LTS Era
*   **Java 21 (2023 - LTS):** A massive release. Finalized **Virtual Threads**, **Pattern Matching for `switch`**, and **Record Patterns**. Also introduced **Sequenced Collections** (interfaces representing collections with a defined encounter order).
*   **Java 22 (2024):** Finalized **Unnamed Variables & Patterns (`_`)**, allowed statements before `super(...)` in constructors, and introduced the Foreign Function & Memory API.
*   **Java 23 (2024):** Introduced **Markdown Documentation Comments** (allowing `///` markdown syntax for JavaDoc) and simplified primitive type patterns.

### The New Frontier (Java 24 - 26)
*   **Java 24 (2025):** Focuses on **Stream Gatherers** (extending the Stream API for custom intermediate operations) and standardizing the **Class-File API**.
*   **Java 25 (2025 - LTS):** Finalizes **Module Import Declarations** (e.g., `import module java.base;` to import all packages in a module at once) and matures Structured Concurrency.
*   **Java 26 (2026):** Finalizes **Primitive Types in Patterns, `instanceof`, and `switch`** (allowing safe checks and casting of primitives), introduces Lazy Constants, and brings major GC/Performance optimizations.
