# Infrastructure changes to support horizontal scaling in OpenBoxes

## Quick overview

All OpenBoxes deployments presently comprise a single Tomcat application for the
web front end and a single database instance. This document details some,
hopefully most, and possibly all of the changes we'd need to make to the Tomcat
application to allow multiple instances to run at the same time.

## Critical blockers

1. [Convert Quartz scheduler to use JDBC, not main memory, to track job state; enable graceful shutdown.](#1-configure-quartz-to-support-multiple-instances)
2. [Analytics jobs that run at startup need to be aware that another instance may have just run them, especially on a rolling redeployment.](#2-prevent-thundering-herds-of-analytics-jobs-at-startup)
3. [Session management, currently in memory, should be stored in Redis.](#3-manage-session-data-with-redis)
4. [Nginx needs to support load-balancing across Tomcat instances.](#4-set-up-load-balancing-in-nginx)
5. [Prevent race conditions when assigning sequence numbers.](#5-prevent-race-conditions-when-assigning-sequence-numbers)
6. [Deprecate local caching (Ehcache, Hibernate) in favor of Redis.](#6-deprecate-local-caching-ehcache-hibernate-in-favor-of-redis)

## Important but non-critical improvements

7. [DB connection pool needs sizing adjustments to support four or more instances.](#7-optimize-db-settings-for-multiple-clients)
8. [Prevent memory leaks via ThreadLocal objects when attaching sessions.](#8-prevent-memory-leaks-in-threadlocal-storage)
   <br>(Technically this is an existing issue, but it may become more important at scale.)
9. [Don't spin up new instances because of API abuse / DoS / user error.](#9-enable-rate-limiting-to-prevent-runaway-instance-creation)
10. [Add functionality to tag each request with an ID to ease distributed debugging.](#10-support-distributed-tracing-by-injecting-request-ids)
11. [Expose liveness and readiness endpoints for Kubernetes.](#11-expose-liveness-and-readiness-endpoints-for-kubernetes)

## Unresolved questions

12. [Does local file storage (upload) need to be changed in any way?](#12-investigate-file-storage)
    <br>(Likely not: all files appear to be scraped and sent to DB after upload.)
13. [Should we add multi-instance support to docker-compose configuration?](#13-launch-multiple-instances-in-docker-compose)
    <br>(Could be helpful for dev/local testing, but production would use Ansible or K8s.)

## Things we don't need to worry about

14. [Liquibase already has a `DATABASECHANGELOGLOCK` that prevents two migrations from stepping on each others' toes.](#14-liquibase-migrations-are-already-safe)

# Critical blockers

## 1. Configure Quartz to support multiple instances

### What's the problem?

Actually, there are two.

1. The Quartz scheduler uses in-memory storage (`jdbcStore: false`). When multiple instances run:
- Jobs may execute on _all_ instances, possibly simultaneously;
- We may duplicate notifications, emails, data refreshes;
- 11 jobs marked `concurrent = false` have the opportunity to conflict.
2. We never shut down the Quartz scheduler gracefully:
- Horizontally-scaled instances have shorter lifespans than a monolith;
- During rolling deploys, Tomcat instances will receive SIGTERMs without warning.

### What's the solution?

1. Update the `quartz:` stanza of `grails-app/conf/application.yml` to enable
   `jdbcStore` and `waitForJobsToCompleteOnShutdown`.
2. Set `props` as required following best practices for `org.quartz.*`
   keys:
   - `org.quartz.jobStore.clusterCheckinInterval`
   - `org.quartz.jobStore.createTablesOnStartup`
   - `org.quartz.jobStore.isClustered`
   - `org.quartz.scheduler.instanceId`
   - &c.
3. Add a `destroy()` method to `org.pih.warehouse.BootStrap` that calls
   `scheduler.shutdown(true)`.

### Acceptance criteria

1. Logs show "Quartz scheduler shutdown complete" before process terminates.
2. During instance shutdown, running jobs complete before JVM exits.
3. No "job interrupted" or "connection closed" errors during rolling deploys.
4. Database shows no jobs stuck in "running" state after instance shutdown.

### Potential gotchas

[Quartz had a complex migration from 1.8 to 2.0](https://www.quartz-scheduler.org/documentation/2.3.1-SNAPSHOT/migration-guide.html) (search the page for "If you use JDBCJobStore, you will
need to make several changes to the database"). Future Quartz upgrades may be a
little tricky, although the docs currently say that no schema changes are planned
for 2.x to 3.0. If Quartz introduces a breaking change again, it may be simplest
to "migrate" as follows:

1. Make sure the system is quiescent and no jobs are running.
2. Shut down all instances.
3. Change Quartz's table prefix.
4. Just let Quartz build a whole new set of tables when the first new instance starts.

## 2. Prevent thundering herds of analytics jobs at startup

### What's the problem?

Analytics refresh jobs are kicked off on _every_ instance startup. That works fine
for a long-lived monolith but will cause severe loading on the DB whenever there
is a rolling restart on a cluster of instances.

### What's the solution?

1. `org.pih.warehouse.BootStrap` has several calls to `<<JOB>>.triggerNow()`.
   Instead, it should check the database for a last-run time, and perhaps a
   version marker (e.g. app version, to force refresh after system upgrades),
   to see if jobs need to be re-launched at startup. For example, suppose we are
   adding an instance to ease capacity problems. If it sees that an analytics
   job was run in the past hour, it probably should not launch a new one. On the
   other hand, suppose we are doing a rolling deploy of a new version. Perhaps
   the first new instance should run all analytics jobs.
2. We have a number of other cases of `triggerNow()` in the codebase. Each one
   should be checked for correctness to make sure we aren't launching too many,
   or too few, Quartz jobs.
3. It's worth pointing out that a short-term fix during development may be to
   simply set `refreshAnalyticsDataOnStartup.enabled: false`.

### Acceptance criteria

1. Deploy three instances in quick succession.
2. One instance must run analytics refresh.
3. Other two instances must instead log "Analytics were refreshed X minutes ago, skipping".
4. After cooldown period expires, next restart triggers refresh.

### Potential gotchas

Quartz jobs should already be reasonably idempotent, but it's possible as we
evaluate the work each one does that we realize we want to re-engineer certain
jobs, because we'll definitely need to break with the assumption that when an
instance launches, all analytics jobs are 100% up to date.

## 3. Manage session data with Redis

### What's the problem?

Sessions are stored in memory. Users will lose session data whenever the load
balancer switches them to different instance. They also will lose session data
whenever an instance is removed, either via graceful shutdown or crashing.

### What's the solution?

There are three, but I think one is clearly better. The easiest option is to
configure nginx to support sticky sessions. This will prevent the load balancer
from moving a session around from instance to instance, but will do nothing to
protect against instance removal. Another option is to store session data in the
database. However, my recommendation is to store session data in Redis:

- DB already is a performance bottleneck.
- DB-based session management requires a few schema migrations on the way to Grails 5.
- Sessions auto-expire in redis vs recurring cleanup required in DB.
- The `redis-session` upgrade path to Grails 5 is smooth.
- Looking ahead to Kubernetes, adding a Redis service is easy-peasy.
- [There are other places where Redis is going to help us anyway](#6-deprecate-local-caching-ehcache-hibernate-in-favor-of-redis).

To migrate from locally-stored sessions to Redis-backed ones, we need to:

1. Update our ansible scripts to launch a redis service with persistence.
2. Add `org.grails.plugins:redis-session:2.0.4` as a gradle dependency.
   - don't forget to inspect transitive dependency changes, our stack is fragile.
   - N.B., Grails 5 would use `spring-session-data-redis` with an easy migration.
3. Add a `grails.redis.*` stanza to `application.yml`.
4. Add a small redis instance to `docker-compose.yml` for local development.
5. Harmonize `grails.redis.session.timeout` and `server.session.timeout` in
   `application.yml`.

### Acceptance Criteria

- User stays logged in when load balancer switches instances.
- Session data persists across instance restarts.
- Redis handles session expiration automatically.
- No additional database load from sessions.

### Potential gotchas

We currently do some clever legerdemain with caching session-derived data in
`ThreadLocal<>()` objects in `org.pih.warehouse.auth.AuthService`, but that's
[its own can of worms](#8-prevent-memory-leaks-in-threadlocal-storage) that merits
its own section.

## 4. Set up load balancing in Nginx

### What's the problem?

Currently, Nginx as a reverse proxy has it easy. Every connection gets routed to
the one Tomcat instance we've got. We're going to want to configure nginx to
spread the wealth.

### What's the solution?

The `least_conn` config option seems reasonable enough for a first swing. It
assigns a new connection to whichever instance is handling the fewest connections.

### Acceptance criteria

- Nginx distributes connections across instances.

### Potential gotchas

Maybe there's a more sophisticated way to do this but that feels like premature
optimization at this point.

## 5. Prevent race conditions when assigning sequence numbers

### What's the problem?

Two services generate incremental ID's in a fashion that won't work when multiple
instances are running. One uses the `synchronized` keyword and an in-memory
counter (`ProductTypeService`) while the other hits the DB without any locking
(`PurchaseOrderIdentifierService`).

### What's the solution?

It depends on whether we need to maintain incrementing numbers for these classes.
If we do, then we need to use the DB as the source of truth and use locks to
supplement/supplant the `synchronized` keyword. If we do not, then we can use
randomly generated identifiers (as we do elsewhere in the codebase).

### Acceptance criteria

- ID's are generated not only thread-safely, but instance-safely.
- ID's have formats that are either identical to existing patterns, or reasonably close.
- We avoid UUID's: can't read them over the phone, can't print in existing
  output templates.

### Potential gotchas

I can think of two cases where this is occurring, which makes me worry there's
a third, and/or a fourth, a fifth... However, `IdentifierService.generate()`
generates short, random ID's, has a uniqueness check, sure looks safe for horizontal
scaling, and is widely inherited. So with luck we only have two cases to fix.

## 6. Deprecate local caching (Ehcache, Hibernate) in favor of Redis

### What's the problem?

Hibernate's L2 cache and Ehcache are both local in scope. The way we currently
use them means that two instances may display different data in dashboards.
What's more, we don't configure TTL for cache entries, meaning that over time
different instances could display markedly different data.

### What's the solution?

We should avoid using Hibernate and Ehcache. Fortunately, even though our codebase
includes hibernate cache directives, these have been ignored since November 2016
when `cache.use_second_level_cache=false` was added to `DataSource.groovy`. So,
stopping using Hibernate's L2 cache just takes the form of accepting that we
haven't been using it for many years.

As for Ehcache, if we replace our `cache-ehcache:3.0.0` dependency with
`cache-redis:3.0.0`, all of our `@Cacheable` annotations will work as before,
but globally; we already introduced a Redis dependency in
[Step 3](#3-manage-session-data-with-redis). Note that Hibernate will pull in
its own (older) ehcache dependency, which we can ignore because we've disabled
Hibernate caching.

### Acceptance criteria

- Dashboards show the same data regardless of instance.

### Potential gotchas

Redis requires a network hop compared to in-memory storage for Ehcache. So cache
hits will be perhaps a half-microsecond slower. However, since the cache is now
a global resource, each instance should experience 1/N as many cache misses as
they would if they had a local cache. What's more, correctness matters far more
than speed. We can investigate a local caching strategy (with short TTL) if
performance really becomes a problem.

One thing we'll want to do is set reasonable `ttl` values for each and every
`@Cacheable` annotation. If we do not, cache entries are eternal, which we
definitely do not want.

# Important but non-critical improvements

## 7. Optimize DB settings for multiple clients

### What's the problem?

We're currently lifting most of our DB connection pool settings from the Grails
documentation which assumes a single client. With `maxActive: 50` per instance,
four clients will overload our MySQL database (default max of 151 connections).

### What's the solution?

- Reduce `maxActive` to a more reasonable value (say, 20).
- Check out trusted documentation (like HikariCP) for good values for
  non-monolithic clients.
- Accept and document that even with horizontal scaling, our single DB instance
  has limits.
    - We can't have 1000 Tomcat instances hitting one MySQL DB no matter
  how well we horizontally scale it.

### Acceptance criteria

- Four clients can connect to the DB without raising errors.
- DB and client performance is not negatively impacted.

## 8. Prevent memory leaks in ThreadLocal storage

### What's the problem?

`AuthService` uses the `ThreadLocal` object but cleanup only happens in
`afterView()`. If an exception occurs before the page is rendered, `ThreadLocal`
contents might not be cleared. This isn't strictly a scalability blocker, but I
noticed it while looking for scalability bugs, and didn't want to lose track of
it.

### What's the solution?

- Use `after()`, not `afterView()`. The former is called after an exception, the
  latter only runs if the view renders successfully.
- There is a chance of a race condition with lazy initialization of the `ThreadLocal`
  variables. They should be declared like
  `private static final ThreadLocal<User> threadLocalUser = new ThreadLocal<>()`

### Acceptance criteria

- We haven't noticed any bugs in the current implementation.
- We shouldn't notice any bugs in the replacement.
- If it compiles and passes lint, we're 90% of the way there.

### Potential gotchas

I think there's a risk of privilege escalation if we accidentally use another
more authorized user's credentials either after a view rendering bug (`afterView`
doesn't clear state) or at lazy startup. But security issues are far beyond the
scope of this project. The changes proposed here, if they change our security
posture at all, should do so in a positive direction.

## 9. Enable rate limiting to prevent runaway instance creation

### What's the problem?

With horizontal scaling, especially in an autoscaling K8s context, we can handle
more traffic by spinning up additional instances. However, there are many cases
where we would _not_ want to do this:

- Excessive login attempts (sign of brute-force password guessing).
- Uneven load (single user generating heavy requests for reports).
- No backpressure (client hits Swagger API with a bunch of requests at once).

### What's the solution?

We can configure Nginx to rate limit requests even before they hit Tomcat, for
example, a stanza with `location ~ ^/openboxes/(auth|login)` can return 429
(try again later) after a few auth failures. Similarly we can return 429 to the
`api/` endpoints after a burst of 20 requests, etc.

### Acceptance criteria

- Rate-limited requests return 429.
- Legitimate traffic is unaffected.

### Potential gotchas

Obviously setting the limits too low could really inconvenience our users. We'll
want to take a look at existing production logs to get a feel for what limits
are reasonable.

## 10. Support distributed tracing by injecting request IDs

### What's the problem?

When a user reports an error in production, we need to trace that request through
logs on nginx, Tomcat, and possibly the database. With multiple Tomcat instances
we have even more work to do than before. For example, which instance handled
the request? Or, worse, did it hit multiple instances via a retry or reload?

### What's the solution?

1. Configure nginx to generate (or pass through) an `X-Request-ID` header for
   every request.
2. In a Grails interceptor, read this header and add it to the logging MDC
   (Mapped Diagnostic Context) so that every log line includes the request ID.
3. Make sure we emit logs with a consistent logging framework: MDC support can
   be logger-specific.
4. Include the request ID in error responses so users can report it.

### Acceptance criteria

- Every log line includes a request ID.
- Error pages display a request ID users can report.
- We can grep across all instance logs for a single request ID.

### Potential gotchas

I could see myself getting a little lost in the weeds wiring this up with Sentry
and New Relic. We should implement a bare-bones MVP and only expand it if we
find the initial implementation to be lacking in some way.

## 11. Expose liveness and readiness endpoints for Kubernetes

### What's the problem?

Kubernetes (and nginx) need to know whether an instance is healthy and ready to
receive traffic. Without health checks, the load balancer might send requests to
an instance that's still starting up or has lost its database connection.

### What's the solution?

We already have `/openboxes/api/status`, which tests database connectivity and
returns JSON. It doesn't require authentication and doesn't require a location
to be selected, so nginx and K8s can call it freely.

For now, this single endpoint can serve as both liveness and readiness probe.
If we later need to distinguish between "JVM is alive" (liveness) and "ready to
serve traffic" (readiness), we can add a simpler `/health` endpoint that just
returns 200 without hitting the database.

### Acceptance criteria

- `/openboxes/api/status` returns 200 when app is healthy.
- Nginx upstream health checks use this endpoint.
- K8s readiness/liveness probes configured in deployment manifests.

### Potential gotchas

The current endpoint calls `Product.count()` on every health check. Under
heavy polling (e.g., K8s checking every 5 seconds across many pods), this could
add up. If it becomes a problem, we can add a simpler liveness endpoint or cache
the database check for a few seconds, although caching it seems to defeat the
purpose of making sure the instance has a good connection to the DB.

# Unresolved questions

## 12. Investigate file storage

### What's the problem?

Uploads are currently pushed to `openboxes.uploads.location`, which defaults to
a local `uploads/` directory. If multiple instances have separate local
filesystems, an upload to Instance A wouldn't be visible to Instance B.

### What's the solution?

We probably don't need to do anything at all here, but I want to look more
closely to be sure.

1. `UploadService.createLocalFile()` creates a temporary file in the uploads
   directory for processing.
2. `Document.groovy` stores file contents as `byte[] fileContents` directly in
   the database (up to 10MB).
3. The local directory appears to be staging only, not permanent storage.

If my reading is correct, file uploads should work fine across multiple
instances since the database is the source of truth.

### Acceptance criteria

- Upload a file to Instance A.
- Verify the results of the upload are equally visible to Instance B.
- Confirm the local `uploads/` directory is only temporary.

### Potential gotchas

If I'm wrong about this, and some files are served from local disk rather than
the database, we'd need shared storage (S3, etc.). But the code suggests we're OK.

## 13. Launch multiple instances in Docker Compose

### What's the problem?

It would be nice to test horizontal scaling locally without deploying to a real
cluster. Docker Compose can run multiple instances of a service, but our current
configuration doesn't support this.

### What's the solution?

This is low priority, but if someone wants to set it up:

1. Use `docker-compose up --scale openboxes=3` (already works with no changes).
2. Or add `deploy.replicas: 3` to the service definition.
3. Ensure nginx upstream is configured to find all instances.
4. Add a Redis service for sessions and caching.

### Acceptance criteria

- `docker-compose up --scale openboxes=2` starts two instances.
- Nginx load-balances between them.
- Sessions persist across instances (requires Redis).

### Potential gotchas

This is useful for smoke testing but doesn't replicate production conditions
like rolling deploys, network partitions, or instance failures. The big risk
here is that "it works in docker-compose" gives devs false confidence.

# Things we don't need to worry about

## 14. Liquibase migrations are already safe

### What's the problem?

What if two instances start simultaneously and both try to run database
migrations? Could we end up with duplicate or conflicting schema changes?

### What's the solution?

Nothing on our side. Liquibase handles this automatically. It uses the
`DATABASECHANGELOGLOCK` table to ensure only one instance can run migrations at
a time. And we know it's using it because we have to delete it from time to time
in dev and staging! A competing instance will wait until the first one releases
the lock.

### Acceptance criteria

- Start 3 instances simultaneously with pending migrations.
- Only one instance runs migrations.
- Other instances wait or skip gracefully.

### Potential gotchas

This is a solved problem for Liquibase and has been for more than a decade.

The real gotcha for us is `liquibase.changeLogLockWaitTimeInMinutes`, which I
think defaults to 5 minutes. Our migrations take longer than that; we'll want to
configure it appropriately.
