---
paths:
  - "src/test/groovy/**"
  - "src/integration-test/groovy/**"
  - "test/**/*.groovy"
---

# Grails / Groovy Testing — OpenBoxes

## Stack

- **Spock** — primary test framework for Grails services, controllers, and domain classes
- **Grails test harness** — `grails-app/conf` unit test base classes provide autowiring, in-memory GORM, etc.
- **JUnit 4/5** — allowed for plain Java helpers in `src/main/java/`, but Groovy code uses Spock
- **Jacoco** — coverage via `./gradlew jacocoTestReport`
- **H2 / in-memory** for unit tests; **MySQL** for integration tests

## Test Types

| Type | Location | Base class | Runs with |
|---|---|---|---|
| Unit | `src/test/groovy/**` | `Specification` (+ Grails test mixins) | `./gradlew test` |
| Integration | `src/integration-test/groovy/**` | `Specification` + `@Integration` | `./gradlew integrationTest` |
| Functional | `src/test/functional/**` | Geb specs | rare; check if still wired |

Match the location of the production code you're testing:

```
grails-app/services/org/pih/warehouse/custom/foo/FooService.groovy
src/test/groovy/org/pih/warehouse/custom/foo/FooServiceSpec.groovy
```

## Spock Spec Anatomy

```groovy
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class FooServiceSpec extends Specification implements ServiceUnitTest<FooService> {

    def setup() {
        // runs before each feature method
    }

    def "createFoo persists a new Foo with the given name"() {
        given:
        String name = 'widget-42'

        when:
        Foo foo = service.createFoo(name)

        then:
        foo.id != null
        foo.name == name
    }

    def "createFoo rejects blank names"() {
        when:
        service.createFoo('')

        then:
        IllegalArgumentException ex = thrown()
        ex.message.contains('name')
    }
}
```

### Spock blocks

- **`given:`** — test setup (fixtures, inputs)
- **`when:`** — the action under test
- **`then:`** — assertions (implicit — each line is an assertion)
- **`expect:`** — combines when+then for simple cases
- **`where:`** — data-driven tables
- **`cleanup:`** — teardown

### Data-driven tests

```groovy
def "discount is applied correctly"(BigDecimal price, int pct, BigDecimal expected) {
    expect:
    pricingService.discount(price, pct) == expected

    where:
    price        | pct  || expected
    100.00G      | 10   || 90.00G
    50.00G       | 0    || 50.00G
    200.00G      | 25   || 150.00G
}
```

Use `G` suffix for `BigDecimal` literals — Groovy's default for decimal literals.

## Mocking

Spock has built-in mocking — do not introduce Mockito for Groovy tests.

```groovy
def "sends confirmation email on order placed"() {
    given:
    EmailService emailService = Mock()
    service.emailService = emailService

    when:
    service.placeOrder(new Order(customerEmail: 'a@b.com'))

    then:
    1 * emailService.send({ it.to == 'a@b.com' })
}
```

- **`Mock()`** — strict mock; unexpected invocations are test failures
- **`Stub()`** — loose stub; records calls but doesn't fail on unexpected ones
- **`Spy()`** — wraps a real object, allowing partial mocking

### Interaction verification

```groovy
then:
1 * emailService.send(_)         // exactly once, any argument
(1.._) * emailService.send(_)    // at least once
_ * emailService.send(_)         // any number of times
0 * _                            // no other interactions on anything
```

## Unit Test Mixins (Grails-specific)

| Mixin | Purpose |
|---|---|
| `ServiceUnitTest<T>` | Service unit tests — autowires the service, provides `service` field |
| `ControllerUnitTest<T>` | Controller unit tests — autowires the controller, provides `controller` and `params`/`response` |
| `DomainUnitTest<T>` | Domain class unit tests — provides an in-memory GORM for the single domain |
| `DataTest` | Multi-domain — `setupData { mockDomains(A, B, C) }` |
| `TagLibUnitTest<T>` | Taglib unit tests |

```groovy
class ProductControllerSpec extends Specification implements ControllerUnitTest<ProductController>, DataTest {

    def setupSpec() {
        mockDomains(Product, Category)
    }

    def "show renders a product"() {
        given:
        Product product = new Product(name: 'Widget', productCode: 'W001').save(failOnError: true)

        when:
        params.id = product.id
        controller.show()

        then:
        response.status == 200
        response.json.name == 'Widget'
    }
}
```

## Integration Tests

```groovy
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class FooServiceIntegrationSpec extends Specification {

    FooService fooService

    def "integration: createFoo persists to MySQL"() {
        when:
        Foo foo = fooService.createFoo('widget-42')

        then:
        Foo.count() == 1
        Foo.findByName('widget-42').id == foo.id
    }
}
```

- Integration tests run against a **real Hibernate session** and a **real database** (MySQL or H2 depending on config).
- They roll back by default after each feature method.
- Use for anything that needs GORM's full machinery (criteria, HQL, cascades, transactions).

## Assertions

- **Assert concrete values.** Never use `assert result != null` when you can assert the value.
  ```groovy
  // BAD
  result != null
  result.items.size() > 0

  // GOOD
  result.items.size() == 3
  result.items*.name == ['widget', 'gadget', 'gizmo']
  ```
- **Spock's power assertion** shows every intermediate value when a line fails — lean on it.
- **Spread operator (`*.`)** for asserting on collection properties.
- **`with`** block for grouped assertions on one object:
  ```groovy
  then:
  with(result) {
      status == 'ACTIVE'
      items.size() == 3
      total == 150.00G
  }
  ```

## Test Data

- **Domain object creation:** `new Product(name: 'x', productCode: 'p1').save(failOnError: true)` inside `setup()` or `given:`.
- **Fixture files:** `src/test/resources/` for complex JSON/CSV fixtures.
- **Builder pattern** for repeated domain setup — extract a helper class under `src/test/groovy/org/pih/warehouse/custom/<feature>/<Feature>TestData.groovy`.
- **Don't share state between tests** — each feature method should be independent.

## Coverage

- Target: **80%+** on custom feature code (`org.pih.warehouse.custom.**`).
- Upstream code is exempted — don't chase coverage on files you don't own.
- Run `./gradlew test jacocoTestReport` and open `build/reports/jacoco/test/html/index.html`.

## What to Avoid

- **Mockito** — use Spock mocks; don't mix frameworks.
- **JUnit `@Test` annotations in Groovy** — use Spock `def "..."()` feature methods.
- **Testing upstream code** — they have their own tests. Focus on `org.pih.warehouse.custom.**`.
- **Tests that sleep** — use `PollingConditions` for async assertions.
- **Tests that depend on order** — use `@Stepwise` only when genuinely necessary and document why.
- **Shared mutable fixtures** — each test must be independent.
- **Asserting on strings that include dates / IDs** — use matchers or extract the volatile part.

## Running Tests

```bash
./gradlew test                                   # all unit tests
./gradlew test --tests "*.custom.foo.*"          # filter by pattern
./gradlew integrationTest                        # integration tests
./gradlew test jacocoTestReport                  # tests + coverage report
./gradlew check                                  # tests + static analysis
```
