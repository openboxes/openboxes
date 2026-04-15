---
paths:
  - "src/main/java/**/*.java"
---

# Java Patterns — OpenBoxes `src/main/java/`

> **Scope:** only the Java helper/utility code under `src/main/java/`. The main application is **Grails 3 / Groovy** — see `rules/groovy/patterns.md` for everything else.

## Language Floor: Java 8

OpenBoxes runs on **OpenJDK 8**. The following language features do **not** exist and will not compile:

- `var` local-variable inference (Java 10+)
- `record` types (Java 14+ preview, 16+ stable)
- `sealed` / `non-sealed` classes (Java 15+ preview, 17+ stable)
- Pattern matching for `instanceof` (Java 16+)
- Switch expressions with arrow syntax (Java 14+)
- Text blocks (`"""..."""`) (Java 15+)
- `List.of()`, `Map.of()`, `Set.of()` (Java 9+)
- `String.isBlank()`, `String.strip()`, `String.lines()` (Java 11+)
- `Optional.isEmpty()` (Java 11+) — use `!opt.isPresent()`
- `Collectors.toUnmodifiableList()` / `Stream.toList()` (Java 16+) — use `Collectors.toList()` and wrap with `Collections.unmodifiableList`

**Stay on Java 8 syntax for this codebase.** Upstream OpenBoxes pins the JDK via Docker (OpenJDK 8 base) and there is no plan to upgrade.

## Where Java lives

- `src/main/java/` — utility classes, framework extensions, base classes referenced by Groovy
- `src/main/java/util/` — helpers
- `src/main/java/org/pih/warehouse/` — supporting Java code (e.g. interceptors, annotation classes)

**New custom Java code goes under `src/main/java/org/pih/warehouse/custom/<feature>/`** per the Upstream Compatibility rules. See `rules/custom-package-isolation.md`.

## Framework Context

The Java helpers run inside a **Grails 3 application on Spring Boot**. Beans are wired by Grails' convention-based injection, not by Spring stereotypes. This means:

- **No `@Service`, `@Repository`, `@Component`, `@Controller`** on new Java classes — Grails does not scan them. If a Java class needs to be a Spring bean, register it in `grails-app/conf/spring/resources.groovy`.
- **No `@Autowired`** — Grails expects property-based injection from Groovy callers. If a Java bean has dependencies, declare a setter and let Spring set it, or pass them via constructor and wire it in `resources.groovy`.
- **No JPA.** GORM owns persistence. A Java helper should never define a `@Entity` — if you need a domain class, write it as a Groovy GORM class in `grails-app/domain/`.

## Immutability

- Mark fields `final` by default.
- Return defensive copies from public getters: `Collections.unmodifiableList(items)`.
- Do not expose mutable internals.

```java
public final class OrderSummary {
    private final Long id;
    private final String customerName;
    private final BigDecimal total;

    public OrderSummary(Long id, String customerName, BigDecimal total) {
        this.id = id;
        this.customerName = customerName;
        this.total = total;
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotal() { return total; }
}
```

No records. Write a plain immutable class with final fields and a public constructor.

## Optional Usage

- `Optional<T>` for return types of methods that may return "no result".
- Use `map()`, `flatMap()`, `orElseThrow()`, `ifPresent()` — never call `get()` without a preceding `isPresent()` check (or better, don't call `get()` at all).
- Do **not** use `Optional` as a field type, method parameter, or collection element.

```java
public Optional<Order> findById(Long id) {
    return Optional.ofNullable(orders.get(id));
}

// Usage
Order order = findById(id).orElseThrow(
    () -> new IllegalStateException("Order not found: " + id));
```

## Streams (keep short and simple)

```java
List<String> names = orders.stream()
    .map(Order::getCustomerName)
    .filter(Objects::nonNull)
    .collect(Collectors.toList());
```

- 3–4 operations max; if it's longer, extract a helper method.
- Avoid side effects in stream operations — no mutation of shared state inside `.forEach()`.
- **No `toList()` terminal op** — that's Java 16+. Use `collect(Collectors.toList())`.
- For small collections, a plain `for` loop is fine and often clearer.

## Error Handling

- Prefer unchecked exceptions (`RuntimeException` subclasses) for domain errors.
- Create specific exception types with context: `new OrderNotFoundException(id)`.
- Avoid catching `Exception` unless rethrowing or logging at a top-level boundary.
- Include the original cause when wrapping: `new RuntimeException(msg, cause)`.

```java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order not found: id=" + id);
    }
}
```

## Logging

Use SLF4J — it's already on the classpath via Grails:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderHelper {
    private static final Logger log = LoggerFactory.getLogger(OrderHelper.class);

    public void process(Long id) {
        log.info("process_order id={}", id);
        try {
            // ...
        } catch (RuntimeException ex) {
            log.error("process_order_failed id={}", id, ex);
            throw ex;
        }
    }
}
```

Do **not** use `System.out.println`.

## What to Avoid

- **JPA annotations** (`@Entity`, `@Table`, `@Column`, `@Id`) — use GORM in Groovy domain classes.
- **Spring stereotypes** (`@Service`, `@Repository`, `@Component`) on new Java classes — register beans in `resources.groovy` instead.
- **`@Autowired` / constructor injection** without a matching `resources.groovy` entry — Grails won't wire it automatically for arbitrary Java classes.
- **Java 9+ features** — none of them compile.
- **Raw types** (`List` instead of `List<String>`) — always parameterize.

## References

- For Groovy / Grails code, see `rules/groovy/patterns.md`.
- For custom-code isolation, see `rules/custom-package-isolation.md`.
