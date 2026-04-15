---
paths:
  - "src/main/java/**/*.java"
---

# Java Security — OpenBoxes `src/main/java/`

> **Scope:** Java helper code only. Main Grails application security is governed by the Grails spring-security-core plugin and the rules in `rules/groovy/security.md`.

## Secrets

- **Never hardcode** credentials, API keys, or tokens in source.
- Read from environment variables or the externalized Grails config:
  ```java
  String apiKey = System.getenv("INTEGRATION_API_KEY");
  Objects.requireNonNull(apiKey, "INTEGRATION_API_KEY must be set");
  ```
- Do not put secrets in `grails-app/conf/application.yml` or `application.groovy` — use an external config file path (`grails.config.locations`) or env vars.

## SQL Injection

- **Never concatenate user input into SQL.** Use `PreparedStatement` with positional parameters.
- Prefer going through Grails/GORM (in Groovy) rather than raw JDBC in Java helpers. If you're writing raw JDBC in a Java helper, you probably should have written it as a Groovy service using criteria queries.

```java
// BAD
stmt.executeQuery("SELECT * FROM products WHERE name = '" + name + "'");

// GOOD
PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE name = ?");
ps.setString(1, name);
ResultSet rs = ps.executeQuery();
```

## Input Validation

- Validate at the boundary. Use plain guard clauses:
  ```java
  if (customerName == null || customerName.trim().isEmpty()) {
      throw new IllegalArgumentException("customerName is required");
  }
  ```
- **Note:** `String.isBlank()` does not exist on Java 8 — use `.trim().isEmpty()`.
- For file paths, always normalize with `Paths.get(...).normalize()` and verify the result stays under an allowed root directory (path traversal defense).

## File I/O

- Never construct a `File` or `Path` from user input without validating it stays inside an allowed directory.
- Close resources with try-with-resources:
  ```java
  try (InputStream in = Files.newInputStream(path)) {
      // ...
  }
  ```

## Authentication / Authorization

- Auth is handled by the Grails spring-security-core plugin in the main app. Java helpers should never implement their own authentication.
- If a Java helper needs to check the current user, get it from the Grails security service via `resources.groovy` injection — do not reach into the `SecurityContextHolder` directly unless you know the Grails plugin has populated it.

## Error Messages

- **Never expose stack traces, internal paths, or raw exception messages to users.** Log server-side; return generic messages at the API boundary.
- Avoid `ex.getMessage()` in API responses — it often contains SQL fragments or file paths.

```java
try {
    return orderService.findById(id);
} catch (OrderNotFoundException ex) {
    log.warn("Order not found: id={}", id);
    throw new NotFoundApiException("Resource not found");  // generic, no internals
} catch (RuntimeException ex) {
    log.error("Unexpected error processing order id={}", id, ex);
    throw new InternalApiException("Internal server error");
}
```

## Dependency Hygiene

- New dependencies in `build.gradle` need justification — every transitive dep is a supply-chain surface.
- Run `./gradlew dependencyCheckAnalyze` before a release (OWASP Dependency Check is already configured).
- Keep Spring Boot / Grails on supported versions (bound by upstream).

## What to Avoid

- `eval`-like reflection on user-controlled class names
- XML parsing without XXE protection — use `XMLInputFactory` with `IS_SUPPORTING_EXTERNAL_ENTITIES = false`
- Insecure deserialization (`ObjectInputStream` on untrusted streams)
- Hardcoded passwords in test fixtures that might get copy-pasted into production

## References

- `rules/groovy/security.md` — main application security rules
- `rules/java/patterns.md` — Java 8 patterns and forbidden features
