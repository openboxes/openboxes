---
paths:
  - "src/main/java/**/*.java"
---

# Java Coding Style — OpenBoxes `src/main/java/`

> **Scope:** only the Java helper/utility code under `src/main/java/`. For Groovy/Grails code (the vast majority of the backend), see `rules/groovy/patterns.md` and `rules/groovy/testing.md`.

## Language Floor: Java 8

See `rules/java/patterns.md` for the full list of forbidden features. Short version: **no `var`, no records, no sealed types, no pattern matching, no `List.of()`, no text blocks, no `switch` arrow syntax**.

## Formatting

- Indent with **4 spaces** (match upstream; do not reformat existing files).
- One public top-level type per file.
- Member order: constants → instance fields → constructors → public methods → protected → private.
- Line length: stay under ~120 characters.

## Naming

- `PascalCase` for classes, interfaces, enums
- `camelCase` for methods, fields, parameters, local variables
- `SCREAMING_SNAKE_CASE` for `static final` constants
- Packages lowercase: `org.pih.warehouse.custom.<feature>`

## Immutability

```java
// GOOD — all fields final, no setters, defensive copies on exposure
public final class Price {
    private final BigDecimal amount;
    private final Currency currency;

    public Price(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }
}
```

## Optional

```java
// GOOD — Optional return, no get() calls, wrap with orElseThrow
return orderRepository.findById(id)
    .map(OrderDto::from)
    .orElseThrow(() -> new OrderNotFoundException(id));

// BAD — Optional as parameter
public void process(Optional<String> name) { /* ... */ }
```

## Streams

```java
// GOOD — short pipeline, named collector
List<String> activeNames = orders.stream()
    .filter(Order::isActive)
    .map(Order::getCustomerName)
    .collect(Collectors.toList());
```

- Max 3–4 operations per pipeline; extract helpers otherwise.
- Use method references (`Order::getCustomerName`) where they improve readability.
- **No `.toList()` terminal operation** — that's Java 16+.

## Error Handling

- Domain errors: specific `RuntimeException` subclasses.
- Avoid broad `catch (Exception e)` unless at a top-level handler.
- Always include context in exception messages.
- When wrapping, preserve the cause: `new RuntimeException(msg, ex)`.

## Null Handling

- Default to **non-null** for all parameters and return values.
- Document nullability with `@Nullable` / `@NonNull` when unavoidable (JSR-305 annotations are already on the classpath via Spring/Grails).
- `Objects.requireNonNull(arg, "arg must not be null")` at the top of public methods where appropriate.

## Logging (SLF4J)

```java
private static final Logger log = LoggerFactory.getLogger(OrderHelper.class);

log.info("fetch_order id={}", id);
log.warn("fetch_order_slow id={} durationMs={}", id, duration);
log.error("fetch_order_failed id={}", id, ex);
```

- Structured key=value logs, not English sentences.
- **Never** log secrets, tokens, passwords, or PII.
- **Never** use `System.out.println` in committed code.

## Project Layout

```
src/main/java/
└── org/pih/warehouse/
    ├── <upstream helpers — do not reformat>
    └── custom/                         # EyeSeeTea customizations
        └── <feature>/
            ├── Helper.java
            └── Dto.java
```

**New Java files go under `org.pih.warehouse.custom.<feature>`** per `rules/custom-package-isolation.md`.

## What Not to Do

- No `var`, no records, no sealed classes, no pattern matching, no text blocks.
- No `@Entity` / `@Column` / JPA annotations — GORM owns persistence.
- No `@Service` / `@Repository` / `@Component` on new classes — use `grails-app/conf/spring/resources.groovy` for bean registration.
- No `System.out.println` or `ex.printStackTrace()`.
- No reformatting of upstream files (Boy Scout Rule is suspended outside the `custom/` subpackage).

## References

- `rules/java/patterns.md` — architectural patterns
- `rules/java/security.md` — security rules
- `rules/groovy/patterns.md` — Grails/Groovy backend (the bulk of OpenBoxes)
- `rules/groovy/testing.md` — Spock + Grails test patterns
- `rules/custom-package-isolation.md` — where new files go
