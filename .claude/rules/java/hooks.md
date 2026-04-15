---
paths:
  - "src/main/java/**/*.java"
  - "src/test/java/**/*.java"
---

# Java Hooks — OpenBoxes

> The OpenBoxes backend is mostly Groovy/Grails. The `src/main/java/` tree only holds helpers and utilities. These hooks scope to that subtree only, not to upstream Java files.

## What we run on Java edits

- `./gradlew compileJava` — verify Java helpers still compile against the rest of the build.
- `./gradlew compileGroovy` — Groovy depends on Java; if Java fails, Groovy fails too.

These are **manual** by default — there are no auto-format / static-analysis hooks committed to the project (`google-java-format`, Checkstyle, SpotBugs are **not** configured in `build.gradle`). Do not introduce them as PreToolUse / PostToolUse hooks here without a team decision; they would touch upstream files and cause merge conflicts.

## What NOT to do

- Do not run Maven (`mvn`, `./mvnw`) — this project is Gradle-only.
- Do not target `pom.xml` or `*.gradle.kts` — neither exists here.
- Do not autoformat upstream Java files (Boy Scout Rule is suspended outside `org.pih.warehouse.custom.*`).
