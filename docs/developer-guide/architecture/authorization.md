# Authorization Design

OpenBoxes checks *which role* a user has but usually not *where* they have it. Role checks read the location from the
user's session, so a request that names a different facility in its URL or params is checked against the wrong
location. This design fixes that without adopting Spring Security, using one service and the existing RBAC rule
config (no database policy table).

- **Constraint:** works with the current session auth, `RoleInterceptor` and `openboxes.security.rbac.rules`.
- **Constraint:** every new piece maps 1:1 onto a Spring Security concept, so a later migration is a rename, not a
  rewrite.
- **First endpoint:** `GET /api/facilities/{facilityId}/available-items`
  ([#6289](https://github.com/openboxes/openboxes/pull/6289)). The user is checked for a role at the facility in the
  URL, not the facility in their session.

Related issues: [#3922](https://github.com/openboxes/openboxes/issues/3922),
[#244](https://github.com/openboxes/openboxes/issues/244),
[#1850](https://github.com/openboxes/openboxes/issues/1850),
[#2263](https://github.com/openboxes/openboxes/issues/2263).

## Design

Every authorization question is answered by one service, which always takes the location as an argument. Endpoints
declare which request param names their location in the existing RBAC rule config.

### 1. `AuthorizationService.hasRole` with an explicit location

```groovy
boolean hasRole(User user, Location location, RoleType minimumRole,
                Collection<RoleType> supplementalRoles = [])
```

- **Location is required.** The service never reads the session or the `AuthService` ThreadLocals, so it gives the
  same answer for UI requests, API requests and jobs. `null` means only global roles count.
- **Primary roles are hierarchical.** Superuser > Admin > Manager > Assistant > Browser > Authenticated, compared by
  sortOrder.
- **Supplemental roles need an exact match.** At least one listed role must be held. (`RoleType.expand()` compared
  them by sortOrder, so any primary role satisfied them.)
- **Inactive users never pass.**
- **Unchanged semantics:** effective roles are global roles plus roles at that location, so a global Browser can
  browse every facility. A location role can add access but never remove it.

### 2. `locationParam` in RBAC rules

One new optional key on an existing rule. When present, `RoleInterceptor` checks roles at the location named by that
request param instead of the session location:

```groovy
[controller: 'productAvailabilityApi', actions: ['list'],
 accessRules: [minimumRequiredRole: RoleType.ROLE_BROWSER, locationParam: 'facilityId']],
```

- A missing, blank or unknown id resolves to `null`, so only global roles count. A location-only user gets 403 rather
  than 400 and cannot probe whether ids exist.
- Every config rule, with or without `locationParam`, goes through `AuthorizationService`.
- **Behavior change:** `purchaseOrderApi.rollback` now really requires `ROLE_PURCHASE_APPROVER`, and
  `invoiceApi.removeItem` really requires `ROLE_INVOICE`.

### 3. How this maps to Spring Security later

| Today | Spring Security equivalent |
| --- | --- |
| `AuthorizationService.hasRole(user, location, role)` | Body of a custom `PermissionEvaluator`, or a `@Component` used in SpEL |
| RBAC rule with `locationParam` | `@PreAuthorize("@authz.hasRole(#facilityId, 'ROLE_BROWSER')")` on the action |
| `RoleInterceptor` rule lookup | `FilterSecurityInterceptor` / method security |
| `hasPermission(user, target, Permission)` (phase 2) | `PermissionEvaluator.hasPermission(auth, target, permission)` |
| `AccessDeniedException` (phase 2) | `org.springframework.security.access.AccessDeniedException` |

Callers depend only on the service, and the service depends only on `User`, `Location` and `RoleType`, so swapping
the engine underneath leaves call sites unchanged.

## Object ownership options (phase 2)

Recommendation: **C, policy classes**, with the role thresholds read from RBAC config. Ownership that depends on
status (a shipment belongs to its origin until it ships, then to its destination) is real code, and a policy class
keeps that code in one testable place without touching domain classes.

All three options share one entry point, `authorizationService.hasPermission(user, target, Permission.READ | WRITE |
DELETE)`, and a loader that replaces bare `.get(id)`:

```groovy
Shipment shipment = authorizationService.get(Shipment, params.id, Permission.WRITE)  // 404 or 403
```

### A. Interface on the domain class

```groovy
class Shipment implements LocationOwned {
    Set<Location> getAuthorizedLocations(Permission permission) {
        if (permission == Permission.READ) return [origin, destination] as Set
        return currentStatus < ShipmentStatusCode.SHIPPED ? [origin] as Set : [destination] as Set
    }
}
```

- **Pro:** rules sit next to the data; adding a domain forces a decision.
- **Con:** authorization logic leaks into domain classes; the role per permission still has to live somewhere else.

### B. Central RBAC config closures

```groovy
openboxes.security.authorization.resources = [
    shipment: [
        read : [minimumRequiredRole: ROLE_BROWSER, locations: { s -> [s.origin, s.destination] }],
        write: [minimumRequiredRole: ROLE_MANAGER, locations: { s -> s.isShipped() ? [s.destination] : [s.origin] }],
    ],
]
```

- **Pro:** one file to audit; implementers can override per install.
- **Con:** code in config is hard to test and type-check; closures in `runtime.groovy` are easy to break in an
  override.

### C. Policy classes (recommended)

```groovy
class ShipmentPolicy implements Policy<Shipment> {
    Set<Location> locationsFor(Shipment s, Permission p) {
        p == Permission.READ ? [s.origin, s.destination] as Set
                             : (s.isShipped() ? [s.destination] : [s.origin]) as Set
    }
}
// RBAC config keeps only the thresholds:
// openboxes.security.authorization.resources.shipment = [read: ROLE_BROWSER, write: ROLE_MANAGER, delete: ROLE_MANAGER]
```

- **Pro:** ownership logic is ordinary, unit-testable code; thresholds stay configurable; one class per resource maps
  directly onto a Spring `PermissionEvaluator` delegate.
- **Con:** one more class per resource family.

With any option, a resource type that has no policy is **denied**, so a forgotten domain fails closed.

## Generic API allowlist

The generic API stays until a real API replaces it, but only resources listed in config are reachable. Each is grouped
into a family that sets its role thresholds and the fields that may be written. Anything not listed returns 404.

```groovy
openboxes.api.generic.families = [
    referenceData: [
        resources: ['unitOfMeasure', 'unitOfMeasureClass', 'paymentTerm', 'reasonCode', 'glAccountType'],
        read  : RoleType.ROLE_BROWSER,
        write : RoleType.ROLE_ADMIN,
        delete: RoleType.ROLE_SUPERUSER,
    ],
    catalog: [
        resources: ['category', 'productCatalog', 'productType', 'tag'],
        read  : RoleType.ROLE_BROWSER,
        write : RoleType.ROLE_MANAGER,
        delete: RoleType.ROLE_ADMIN,
    ],
]
// Optional per resource: writableFields: ['name', 'description', 'code']
```

- **Deny by default:** `GenericApiService.getDomainClass` looks the resource up in this map first.
- **No location-owned resources:** shipments, orders, transactions and requisitions must go through their own
  controllers, where location checks live.
- **Field allowlist:** `bindData(obj, json, [include: writableFields])` replaces `obj.properties = json`, and `id`,
  `version`, `dateCreated` and audit fields are never writable.
- **Open question:** which resources are clients actually using today? Logs for `/api/generic/*` over 30 days would
  give the starting list.

## API tokens (later)

A sketch for [#2263](https://github.com/openboxes/openboxes/issues/2263). It only changes how the user is identified.
Every authorization check above stays the same, because they all take a `User` and a `Location`.

- **Domain `ApiToken`:** `user`, `name`, `tokenHash` (SHA-256, never the raw token), `prefix` (first 8 characters,
  shown in the UI), `expiresAt`, `lastUsedAt`, `revokedAt`, optional `maxRole` and `locations` to scope the token below
  the user's own roles.
- **Request:** `Authorization: Bearer ob_<prefix>_<secret>`. A new `ApiTokenInterceptor` runs before
  `SecurityInterceptor`, looks up the token by prefix, compares the hash in constant time, and sets
  `authService.currentUser` for that request only. It does not create a session.
- **Facility:** token requests must name the facility in the URL (`/api/facilities/{facilityId}/...`) and never rely
  on a session location. This is why `locationParam` comes first.
- **Spring Security later:** maps to a `BearerTokenAuthenticationFilter` plus an `AuthenticationProvider`.

## Rollout

Six small PRs, each shippable alone.

1. **Location-scoped rules.** `AuthorizationService.hasRole`, `locationParam`, rule for
   `/api/facilities/{facilityId}/available-items`.
2. **Location selection.** `ApiController.login`, `ApiController.chooseLocation` and the UI location chooser call
   `hasRole(user, location, ROLE_AUTHENTICATED)` ([#244](https://github.com/openboxes/openboxes/issues/244)).
3. **Generic API allowlist.** Config map by family, deny by default, field allowlist.
4. **`locationParam` rules for remaining `locationId` / `facilityId` endpoints.** Start with `DashboardApiController`.
   Move `/api/...?locationId=` endpoints to `/api/facilities/{facilityId}/...` where practical.
5. **Per-record checks.** `hasPermission` + policy classes, starting with Shipment, Order, Requisition, Invoice
   ([#3922](https://github.com/openboxes/openboxes/issues/3922)).
6. **Default deny for `*Api` controllers.** Log-only mode first: record every API action with no rule for two weeks,
   add rules, then switch to deny ([#1850](https://github.com/openboxes/openboxes/issues/1850)).

**Testing:** each PR adds unit specs for `AuthorizationService` plus an integration spec that logs in as a user with a
role at one facility only and expects 403 at another.

**Release note for PR 1:** two existing rules change. `purchaseOrderApi.rollback` requires an actual
`ROLE_PURCHASE_APPROVER`, and `invoiceApi.removeItem` an actual `ROLE_INVOICE`. Implementers who relied on the old
behavior must assign those roles.
