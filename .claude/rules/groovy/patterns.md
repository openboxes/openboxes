---
paths:
  - "grails-app/**/*.groovy"
  - "src/main/groovy/**/*.groovy"
  - "src/test/groovy/**/*.groovy"
---

# Grails / Groovy Patterns — OpenBoxes

> **Stack:** Grails 3.3.16 on Spring Boot, Groovy 2.4.21, GORM / Hibernate 5.2.18, MySQL 8 / MariaDB 10.3, Liquibase (via Grails Database Migration plugin).

## Language Floor: Groovy 2.4

Groovy 2.4 is the last 2.x release and is **feature-frozen**. Do not use Groovy 3.0+ features:

- **No method references** (`String::toUpperCase`) — use closures (`{ it.toUpperCase() }`)
- **No `!in` / `!instanceof`** — use `!(x in y)` / `!(x instanceof Y)`
- **No `final` as a method modifier with closures** — parser quirks
- **No Groovy 3 type inference** (`var`) — declare types or use `def`

Also: **OpenJDK 8** is the JVM. See `rules/java/patterns.md` for the forbidden Java features — they're also forbidden in `.groovy` code because they produce the same bytecode.

## The Four Load-Bearing Grails Conventions

If you only internalize four things, make it these:

### 1. Services are transactional by default

```groovy
class OrderService {
    // EVERY public method runs in a transaction automatically.
    // This is a HUGE footgun for devs coming from Spring Boot.

    Order place(OrderCommand cmd) {
        // inside a transaction, even though there's no @Transactional
    }
}
```

- To **disable** transactions on a single method: `@NotTransactional` (rare; use only when you explicitly want it non-transactional).
- To mark a service entirely **read-only**: `static transactional = false` at the class level, then annotate individual methods with `@Transactional(readOnly = true)` or `@Transactional` as needed.
- To mark a single method **read-only**: `@Transactional(readOnly = true)`.
- **Never sprinkle `@Transactional` everywhere** — Grails services already wrap you. Only use the annotation to override defaults (read-only, propagation, noRollbackFor).

### 2. Dependency injection is property-based and convention-driven

```groovy
class OrderService {
    // Just declare a property named after the bean — Grails wires it automatically.
    def inventoryService
    def productService
    def grailsApplication   // access to config
    def messageSource       // i18n
    def springSecurityService  // current user, etc.
}
```

- **No `@Autowired`, no `@Inject`, no constructor injection.**
- **Typed `def` is OK too**: `InventoryService inventoryService` — same effect, better IDE support.
- If a collaborator isn't in the Grails beans registry, register it in `grails-app/conf/spring/resources.groovy`.

### 3. Domain classes use GORM mappings, not JPA

```groovy
class Product {
    String name
    String productCode
    String description
    Category category
    BigDecimal pricePerUnit

    static hasMany = [
        synonyms: ProductSynonym,
        attributes: ProductAttribute,
    ]

    static belongsTo = [category: Category]

    static constraints = {
        name blank: false, maxSize: 255
        productCode blank: false, unique: true, maxSize: 100
        description nullable: true, maxSize: 2000
        pricePerUnit nullable: true, min: 0.0, scale: 4
    }

    static mapping = {
        table 'product'
        id generator: 'uuid'
        version false
        category fetch: 'join'
    }
}
```

- **No `@Entity`, `@Table`, `@Column`, `@Id`, `@OneToMany`** — GORM derives all of this from the class and `static mapping` / `static constraints` blocks.
- **`static constraints`** replaces Bean Validation (`@NotNull`, `@NotBlank`, `@Size`).
- **`static mapping`** replaces `@Table`, `@Column`, `@ManyToOne(fetch = ...)`, etc.
- **`static hasMany` / `static belongsTo` / `static hasOne`** replace `@OneToMany` / `@ManyToOne` / `@OneToOne`.
- **Domain package layout:** subdomains live in `grails-app/domain/org/pih/warehouse/<subdomain>/` (`core/`, `inventory/`, `order/`, `product/`, `shipping/`, `requisition/`, …). New custom domain classes go under `grails-app/domain/org/pih/warehouse/custom/<feature>/`.

### 4. Controllers are thin and use Grails conventions

```groovy
class ProductController {
    def productService

    def show(Long id) {
        Product product = Product.get(id)
        if (!product) {
            notFound()
            return
        }
        respond product
    }

    def save(ProductCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, status: 400
            return
        }
        Product product = productService.save(cmd)
        respond product, status: 201
    }
}
```

- **Parameters come via `params`** (`params.id`, `params.name`) or via a command object with constraints.
- **`respond`** picks the right renderer (JSON, XML, HTML) based on the `Accept` header. `render` is the lower-level primitive.
- **Never put business logic in a controller.** Delegate to a service. Flag any controller method over ~15 lines.
- **API controllers** under `grails-app/controllers/org/pih/warehouse/api/` typically extend `BaseDomainApiController` — check inheritance before duplicating generic CRUD logic.

## GORM Query Decision Tree

For any read operation, pick the lowest rung that works:

1. **Dynamic finders** for simple single-field lookups:
   ```groovy
   Product.findByProductCode('ABC123')
   Product.findAllByCategoryAndActive(cat, true)
   Product.findByNameLike('%widget%')
   ```
2. **`where` queries** for type-safe multi-condition queries (Groovy DSL):
   ```groovy
   def results = Product.where {
       category == cat && pricePerUnit > minPrice
   }.list(max: 50, sort: 'name')
   ```
3. **Criteria queries** (`createCriteria()`) for complex dynamic filters:
   ```groovy
   def products = Product.createCriteria().list {
       eq('category', cat)
       between('pricePerUnit', min, max)
       fetchMode 'synonyms', org.hibernate.FetchMode.JOIN
       maxResults(50)
       order('name', 'asc')
   }
   ```
4. **HQL** via `Product.executeQuery(...)` for joins, projections, and aggregations that criteria can't express cleanly.
5. **Raw SQL** via `groovy.sql.Sql` — last resort. Requires a comment explaining *why* and MUST be parameterized (no string concatenation).

Escalate only when the lower rung can't express what you need.

## The N+1 Trap

Lazy loading is the default. A `.each { it.category.name }` across 1000 products fires 1000 queries. Defend with:

- `fetchMode 'category', FetchMode.JOIN` in criteria
- `Product.findAll("from Product p join fetch p.category")` in HQL
- `static mapping { category fetch: 'join' }` on the domain class (caution: eager-by-default across the codebase)

**Enable Hibernate SQL logging** in dev to catch N+1 early:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Constraints & Validation

- Validation runs **on save**. `entity.validate()` triggers it without saving.
- **Custom validators:**
  ```groovy
  static constraints = {
      productCode validator: { value, obj ->
          if (!value.startsWith('SKU-')) {
              return 'productCode.invalidPrefix'   // i18n key
          }
      }
  }
  ```
- Always define error messages as i18n keys in `grails-app/i18n/messages*.properties`, never hardcoded English.

## Command Objects

For complex controller inputs, use command objects instead of raw `params`:

```groovy
class CreateOrderCommand {
    String customerName
    Long locationId
    List<OrderItemCommand> items

    static constraints = {
        customerName blank: false, maxSize: 255
        locationId nullable: false
        items nullable: false, minSize: 1
    }
}
```

Validation happens automatically when the command object is passed to an action.

## Groovy Idioms (functional style)

The codebase already uses `.collect`, `.findAll`, `.groupBy`, `.inject`, `.find`, `.any`, `.every` ~8.7× more often than imperative `for` loops. **Match that.**

```groovy
// GOOD — functional
List<String> names = products.collect { it.name }
List<Product> expensive = products.findAll { it.price > 100 }
Map<Category, List<Product>> byCategory = products.groupBy { it.category }
BigDecimal total = items.inject(BigDecimal.ZERO) { sum, item -> sum + item.total }
Product featured = products.find { it.featured }

// BAD — Java-style
List<String> names = []
for (Product p : products) {
    names.add(p.name)
}
```

- **No Java Streams** in Groovy (`.stream().map(...).collect(toList())`) — not idiomatic, and Groovy closures are simpler.
- **`.with { }` and `.tap { }` are OK** but use sparingly; clarity beats cleverness.
- **Null-safe navigation (`?.`), Elvis (`?:`), safe navigation indexing (`?[...]`)** — use freely.

## Groovy Truth

`if (thing) { ... }` is true when:
- `thing` is not `null`
- `thing` is a non-empty `String`, `Collection`, `Map`, `Matcher`
- `thing` is a non-zero `Number`
- `thing` is `true` (if it's a `Boolean`)

This is **different** from Java! `if ("")` is false in Groovy, `if (0)` is false, `if ([])` is false. **Do not** write `if (list != null && !list.isEmpty())` — write `if (list)`.

## Logging

Grails 3 provides a `log` field on every service/controller/domain class:

```groovy
class OrderService {
    def place(Order order) {
        log.info "place_order id=${order.id}"
        try {
            // ...
        } catch (Exception ex) {
            log.error "place_order_failed id=${order.id}", ex
            throw ex
        }
    }
}
```

- Use `log.info "..."` (no parens needed for Groovy method calls)
- Never use `println` in committed code
- Include structured key=value pairs; don't write English sentences

## Liquibase Migrations

- **Location:** `grails-app/migrations/` with the master changelog at `grails-app/migrations/changelog.groovy`.
- **Custom migrations** go under `grails-app/migrations/custom/<yyyy-mm-dd>-<description>.groovy` and are included from the master via an `include file: 'custom/...'` line added in a clearly-scoped commit.
- **Additive only:** `addColumn`, `createTable`, `addForeignKeyConstraint`. Never edit an existing upstream changeset.
- **Unique `id` + `author`** on every changeset (the Liquibase DSL requires it).
- **Preconditions** prevent re-running on already-migrated databases.
- **Rollback blocks** are encouraged for non-trivial changes.

```groovy
databaseChangeLog = {
    changeSet(id: '2026-04-13-01-add-custom-field', author: 'eyeseetea') {
        preConditions(onFail: 'MARK_RAN') {
            not { columnExists(tableName: 'product', columnName: 'custom_field') }
        }
        addColumn(tableName: 'product') {
            column(name: 'custom_field', type: 'varchar(255)') {
                constraints(nullable: true)
            }
        }
    }
}
```

## Where New Code Goes (Custom Package Isolation)

- **Domain:** `grails-app/domain/org/pih/warehouse/custom/<feature>/`
- **Services:** `grails-app/services/org/pih/warehouse/custom/<feature>/`
- **Controllers:** `grails-app/controllers/org/pih/warehouse/custom/<feature>/`
- **Views (GSP):** `grails-app/views/custom/<feature>/`
- **Migrations:** `grails-app/migrations/custom/`
- **Taglibs:** `grails-app/taglib/org/pih/warehouse/custom/<feature>/`

See `rules/custom-package-isolation.md` for the full rules and rationale.

## What to Avoid

- **JPA annotations** (`@Entity`, `@Column`, `@OneToMany`, `@Id`) — GORM
- **Spring stereotypes** (`@Service`, `@Repository`, `@Controller`) — Grails conventions handle this
- **`@Autowired`** — use property-based injection
- **Java Streams in Groovy** — use closures
- **`for` loops with mutable accumulators** — use functional collection methods
- **`println`** in committed code
- **Raw SQL** without a comment explaining why + parameterization
- **Modifying existing upstream changesets** — always add a new one under `migrations/custom/`
- **Boy Scout-style cleanups in upstream files** — suspended here; only apply inside `org.pih.warehouse.custom.*`
