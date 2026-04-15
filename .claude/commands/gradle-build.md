---
description: Run and troubleshoot Gradle builds for the OpenBoxes Grails 3.3.16 project (JDK 8, Gradle 4.10.3). Delegates deeper diagnosis to the java-build-resolver agent.
---

# /gradle-build — OpenBoxes Grails build runbook

## Step 1: Verify environment

```bash
java -version          # MUST be 1.8.x — anything newer breaks gradle-git-properties + Gradle 4.10.3
./gradlew --version    # should report Gradle 4.10.3
```

If JDK is wrong, fix it first:

```bash
# user-global JDK pin (does not touch any repo file)
mkdir -p ~/.gradle
echo "org.gradle.java.home=/usr/lib/jvm/java-8-openjdk-amd64" >> ~/.gradle/gradle.properties
```

## Step 2: Pick the task

| What you want | Command |
|---|---|
| Compile Java helpers | `./gradlew compileJava` |
| Compile Groovy/Grails | `./gradlew compileGroovy` |
| Run unit tests (Spock) | `./gradlew test` |
| Run integration tests (`@Integration`) | `./gradlew integrationTest` |
| Run the app (dev) | `./gradlew bootRun` |
| Build the WAR | `./gradlew war` |
| Stage Docker build context | `./gradlew prepareDocker -Dgrails.env=prod` |
| Full assemble | `./gradlew assemble` |
| Inspect dependencies | `./gradlew dependencies --configuration runtime` |
| Why is this dependency on the classpath? | `./gradlew dependencyInsight --dependency <name> --configuration runtime` |
| Clean | `./gradlew clean` |
| Force re-resolve | `./gradlew --refresh-dependencies` |

Add `--console=plain` if you see jansi / `JansiLoader` errors. Add `--info` for verbose progress.

## Step 3: Frontend assets (when bundling for prod)

`prepareDocker` and `war` trigger the webpack build. The frontend `package.json` is at the **repo root** (not under `src/main/webapp/`), and the stack is **Node 14 + npm 6–7** — anything newer is unsupported.

```bash
node -v                # must be 14.x
npm install            # from repo root
npm run build          # production webpack build
npm run watch          # dev — rebuilds on change
```

If `prepareDocker` fails inside webpack, run the frontend build standalone first to see the real error.

## Step 4: When something fails

Hand the error to the `java-build-resolver` agent:

> The build agent already knows the stack constraints (JDK 8, Gradle 4.10.3, Grails 3.3.16, Groovy 2.4, Node 14), the most common Grails build errors, and the fork's upstream-isolation rules. Paste the failing task name and the last ~50 lines of output and let it triage.

## Step 5: Don't touch upstream build files

`build.gradle`, `gradle.properties`, `settings.gradle`, `gradle/wrapper/gradle-wrapper.properties` are **upstream-owned**. Edits cause future merge conflicts. Prefer:

1. `~/.gradle/gradle.properties` for user-global settings (JDK pin, proxy).
2. A new file in `gradle/init.d/` for project-scoped init scripts.
3. External config / env vars over `application.groovy` edits.

If you genuinely must edit an upstream build file, keep the change one or two lines and document it in the matching OpenSpec change's `design.md` under "Upstream touch points".

## Reference

- Stack constraints, error tables, and Grails-specific gotchas: `.claude/agents/java-build-resolver.md`
- Upstream-isolation rule (always applies): `.claude/rules/custom-package-isolation.md`
- Fork-merge recipes: `.claude/docs/FORK_MAINTENANCE.md`
