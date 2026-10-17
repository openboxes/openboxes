# Regression suite

A broad regression suite for this branch: Spock unit and integration specs for the API, the services and the
warehouse task flows (allocation, backorders, auto-issuance, picking, putaway, sortation and cross-dock,
containers, receiving, jobs, events, labels), a contract check of every call the mobile app makes, and an OpenAPI
description of the API that the integration specs validate their calls against.

## Where it lives

| What | Where |
|---|---|
| Unit specs | `src/test/groovy/org/pih/warehouse/regression/**` |
| Integration specs | `src/integration-test/groovy/org/pih/warehouse/api/regression/**` |
| OpenAPI description of the API | `src/integration-test/resources/openapi/` |
| Mobile app API contract | `src/integration-test/resources/mobile-contract/mobile-contract.json` |
| Database settings for the suite | `src/test/resources/regression-testcontainers/my.cnf` |
| Gradle tasks | `gradle/regression-tests.gradle` (applied at the end of `build.gradle`) |

Package layout under `regression` (unit) and `api/regression` (integration):

- `platform`: the API, services, controllers, tag libraries and pages that this branch shares with `develop`.
  `platform/client` and `platform/builder` hold the API clients and test-data builders the API specs use;
  `platform/base` holds `RegressionApiSpec` (the base class of the API specs) and the call tracking and
  OpenAPI validation filters.
- `obaf`: the features of this branch (allocation, backorders, auto-issuance, jobs, tasks, events, labels).
- `mobile`: one contract spec per area of the mobile app's API calls.
- `apidocs`: the route inventory and the checks that keep the OpenAPI description complete.
- `support`: fixtures shared by the specs (users with given roles, runtime configuration overrides, loopback
  servers standing in for label and printer services, so no test reaches the internet).

## Running

The suite runs in its own test tasks, so OpenBoxes' `test` and `integrationTest` tasks (and `check` and
`jacocoTestReport`) keep their behaviour:

```sh
./gradlew regressionTest regressionIntegrationTest   # the whole suite
./gradlew regressionTest                    # unit specs (in parallel forks)
./gradlew regressionIntegrationTest         # integration specs (Testcontainers, needs Docker)
./gradlew regressionIntegrationTest -PregressionShard=2/6   # one sixth of the integration specs, as in CI
./gradlew regressionTest --tests 'org.pih.warehouse.regression.obaf.*'
```

The integration specs use the same database image as `integrationTest` (`TEST_DATABASE`, default
`mysql:8.0.36`). Pass `-PtestDatabase=<image>` to choose another one: a reused Gradle daemon keeps the
`TEST_DATABASE` it was started with. The database is started with `regression-testcontainers/my.cnf`, which is
upstream's test configuration plus the `sql-mode` the installation guide recommends for production servers (without
`ONLY_FULL_GROUP_BY`, which the cycle count candidate views need).

The test JVMs run in UTC with a 2 GB heap.

## CI

`.github/workflows/regression-tests.yml` runs on every pull request, next to "Test Pull Request": one job runs the
unit specs, and six jobs run a shard each of the integration specs on MySQL 8.0.36 (run the workflow by hand to
use MariaDB 10.3.39). Every push to the branch runs the shards on both databases. A shard that runs no tests fails.
The suite is not part of `jacocoTestReport`, so its coverage is not in the Codecov report. The jobs skip the webpack
bundle (`-x npm_run_bundle`): no spec needs it, and building it takes about 4 minutes on a runner.
`-PregressionShard=<i>/<n>` spreads the spec classes over the shards by their measured run time in
`gradle/regression-spec-times.txt`; a spec that is not listed there still runs, in a shard chosen by its name.
Refresh the file from `build/test-results/regressionIntegrationTest` when the shards drift apart.
`--tests` and `-PregressionShard` do not combine: a shard that does not own the selected spec has nothing to run.

## Known defects: `@PendingFeature`

A feature annotated with Spock's `@PendingFeature(reason = '...')` asserts the correct behaviour where the code
currently has a known defect; the reason says what goes wrong. While the defect is there, the feature fails and is
reported as skipped, and the build stays green. **When a change fixes the defect, the feature passes and Spock turns
that into a failure: remove the annotation in the same change.**

To see all of them at once, run them as normal features:

```sh
./gradlew regressionTest regressionIntegrationTest -PrunPending
```

Each run writes `build/reports/regression/pending/<task>.tsv`: OPEN (still failing), PASSING (fixed, remove the
annotation) and UNEXPECTED (any other failure).

## OpenAPI validation

The integration specs check their `/api` calls against the OpenAPI description (`SpecValidationFilter`): the
request and the response (status, content type and JSON body) must match the documented operation, and an error
must come in the documented error shape. A redirect or an HTML page from an `/api` call is recorded as such and its
body is not validated against the schema; security requirements are not checked (the specs log in with the session
cookie). Calls outside `/api` are not checked.

`-Pregression.specValidation` sets the mode:

- `report` (default): violations are only reported, in `build/reports/regression/spec-violations.jsonl` and
  `spec-violations-summary.txt` (written after `regressionIntegrationTest`). Specs annotated with
  `@EnforceSpecValidation` still fail a call that violates the description, except for the violations listed in the
  annotation's `tolerate` rules.
- `enforce`: every violation fails the call, in every spec.
- `report-only`: nothing fails, `@EnforceSpecValidation` included (to triage a spec's assertion failures).

`-PapiDocs.captureDir=<dir>` keeps one real request/response sample per operation, status and set of query
parameters in that directory, for drafting the documentation of an operation.

`./gradlew apiDocsGenerate` adds stub operations for API routes the description does not cover yet
(`-PapiDocs.prune` also removes operations no route answers).

## Isolation check

The API specs share one database and do not roll back. `apiIsolationTest` runs them twice against one persistent
database to show they do not depend on a clean database:

```sh
./gradlew apiIsolationTest -PapiIsolationDbUrl=jdbc:mysql://127.0.0.1:3306/<empty database> \
    [-PapiIsolationDbUser=openboxes -PapiIsolationDbPassword=openboxes] [-PapiIsolationTests=<pattern>]
```

## Mobile app contract

`mobile-contract.json` lists every API call the mobile app (`openboxes/openboxes-mobile`, at the commit recorded in
the file) makes: method, path, request fields and the response fields the app reads. The specs in `mobile/` call
each one and check the response has those fields. The file is maintained by hand: update it (and the recorded
commit) when the app's API calls change.
