---
name: openboxes-domain-model
description: Map of the core OpenBoxes backend entities, their GORM relationships, and the services that own them. Use when building a feature that touches Products, Inventory, Locations, Requisitions, Shipments, Orders, Transactions, or Stock Movements — load this first to know which domain classes and services are in scope. Derived from the actual codebase, not invented.
---

# OpenBoxes Domain Model — entity / service map

This skill is a **file map**, not a schema reference. Open it when you need to answer: "which domain classes and services do I need to read before touching feature X?" Every entity listed below exists in the codebase as of the branch this was written from (`release/est/0.9.7`); follow the paths to the actual source for current field lists and constraints.

Conventions used throughout:

- Paths are repo-relative from `grails-app/domain/` and `grails-app/services/`.
- `hasMany` / `belongsTo` / `hasOne` declarations are quoted verbatim from the domain source (line number noted where helpful).
- "Owning service(s)" are the primary Grails services that mutate or load that entity — grep for `def <name>Service` in calling code to find more.

---

## Product catalogue

### Product
- `grails-app/domain/org/pih/warehouse/product/Product.groovy`
- The master SKU record — every movable item in the system is a `Product`. Implements `Validatable<ProductValidator>`.
- Relationships (line 243, 263):
  ```groovy
  static belongsTo = ProductGroup
  static hasMany = [
      categories, attributes, tags, documents, productGroups,
      packages, synonyms, inventoryLevels, inventoryItems,
      productComponents, productSuppliers, productCatalogItems,
      productAvailabilities
  ]
  ```
- Owning services: `ProductService`, `ProductDataService`, `ProductIdentifierService`, `ProductMergeService`, `ProductClassificationService`, `ProductTypeService` (all under `grails-app/services/org/pih/warehouse/product/`).
- Gotchas: `Product` has many join-table associations (`product_tag`, `product_category`, `product_attribute`, `product_document`, `product_group_product`) — GORM `joinTable:` mappings are in the `static mapping` block. A second-level cache (`cache true`) is enabled, so eviction matters on bulk updates. `productSuppliers` and `productComponents` use `cascade: 'all-delete-orphan'`.

### Category
- `grails-app/domain/org/pih/warehouse/product/Category.groovy`
- A hierarchical product grouping (`parentCategory` + `categories`) used for browsing and requisition grouping.
- Relationships (line 29, 31):
  ```groovy
  static hasMany = [categories: Category]
  static belongsTo = [parentCategory: Category]
  ```
- Owning services: `CategoryService`, `CategoryDataService`.
- Note: `isRoot` flags the top of the tree; `assigningParentToProductEnabled` gates a UI behavior.

### ProductGroup
- `grails-app/domain/org/pih/warehouse/product/ProductGroup.groovy`
- A generic-substitution group — "this product is interchangeable with these others for fulfillment purposes". Has a `Category category` reference.
- Owning services: `ProductGroupService`, `ProductGroupDataService`.
- Referenced from `Product` as both `belongsTo = ProductGroup` (class-form, meaning many-to-many via a join table) and `hasMany = [productGroups: ProductGroup]`.

---

## Inventory and stock

### Inventory
- `grails-app/domain/org/pih/warehouse/inventory/Inventory.groovy`
- A per-`Location` inventory ledger. One Inventory belongs to one warehouse `Location`, and has many `InventoryLevel` rows (one per configured product at that location).
- Relationships (line 26):
  ```groovy
  static belongsTo = [warehouse: Location]
  static hasMany = [configuredProducts: InventoryLevel]
  ```
- Owning services: `InventoryService` (the big one — 50+ methods), `InventoryLevelDataService`, `InventoryCountService`.
- Gotcha: the `belongsTo` is named `warehouse`, not `location`. When searching code, both names occur.

### InventoryItem
- `grails-app/domain/org/pih/warehouse/inventory/InventoryItem.groovy`
- A **lot-level** record: one row per `(product, lotNumber, expirationDate)` tuple. **Not** per-location — stock-on-hand at a location is tracked through `Transaction` + `TransactionEntry` (and cached in `ProductAvailability`).
- Relationships (line 64):
  ```groovy
  static belongsTo = [product: Product]
  ```
- Owning services: `InventoryItemDataService`, `InventoryService`.
- Fields include a denormalized `quantity` / `quantityOnHand` / `quantityAvailableToPromise` — treat these as cached aggregates.

### ProductAvailability
- `grails-app/domain/org/pih/warehouse/product/ProductAvailability.groovy`
- The **materialized view** of on-hand quantities: `(product, location, lotNumber, binLocationName) -> quantityOnHand / quantityAllocated / quantityOnHold / quantityAvailableToPromise / quantityNotPicked`. This is what most read-paths hit.
- Owning services: `ProductAvailabilityService`, `CycleCountProductAvailabilityService`, `RefreshProductAvailabilityEventService`.
- Gotcha: refreshed by event listeners and scheduled jobs — do **not** mutate it directly from feature code; call the refresh service or append a transaction.

### Transaction
- `grails-app/domain/org/pih/warehouse/inventory/Transaction.groovy`
- The source-of-truth stock event log (receive, issue, transfer, adjustment, cycle count). Every stock movement ultimately lands here as a `Transaction` + one or more `TransactionEntry`.
- Relationships (line 119):
  ```groovy
  static hasMany = [transactionEntries: TransactionEntry]
  static belongsTo = [LocalTransfer, Requisition, Shipment]
  ```
- Owning services: `InventoryService`, `ProductInventoryTransactionService`, `CycleCountProductInventoryTransactionService`, `CycleCountTransactionService`, `InventoryTransactionMigrationService`, `InventoryTransactionSummaryService`, `TransactionIdentifierService`, `TransactionEntryDataService`, `RecordStockProductInventoryTransactionService`.
- Gotcha: `belongsTo` is class-form (no property name), so a Transaction can be owned by any of three parents — check which FK is populated before assuming.

### TransactionEntry
- `grails-app/domain/org/pih/warehouse/inventory/TransactionEntry.groovy`
- A single `(product, quantity, reason)` line within a `Transaction`.
- Relationships (line 28):
  ```groovy
  static belongsTo = [transaction: Transaction]
  ```
- Owning service: `TransactionEntryDataService`.

### TransactionType
- `grails-app/domain/org/pih/warehouse/inventory/TransactionType.groovy`
- Enum-like lookup (receive / issue / transfer-in / transfer-out / cycle-count / adjustment / etc.) with a `compareName` helper and an `isAdjustment()` flag.
- Not owned by a dedicated service — read by `InventoryService` and the various migration services.

### InventoryLevel
- `grails-app/domain/org/pih/warehouse/inventory/InventoryLevel.groovy`
- Per-product, per-location configuration: min/max/reorder quantities, ABC class, status (supported / not supported), bin location. **Not** a stock balance — that's `ProductAvailability`.
- Owning services: `InventoryLevelDataService`, `InventoryService`.

### StockMovement (NOT a GORM entity)
- `src/main/groovy/org/pih/warehouse/api/StockMovement.groovy`
- A `grails.validation.Validateable` **command / facade** class — not persisted. It wraps an underlying `Requisition` (for inbound/outbound stock requests), an `Order` (for returns/transfers), or a `Shipment`, and exposes a unified API-friendly shape for the React frontend.
- Direction / type / status are in `src/main/groovy/org/pih/warehouse/api/{StockMovementDirection,StockMovementType,StockMovementStatusContext}.groovy` and `src/main/groovy/util/StockMovementStatusResolver.groovy`.
- Owning services: `StockMovementService` (`grails-app/services/org/pih/warehouse/inventory/`), `OutboundStockMovementService`.
- **Critical**: when you need to "create a stock movement" you are actually creating a `Requisition` (or `Order` for returns) and projecting it through `StockMovement`. Do not try to instantiate a GORM entity called `StockMovement` — there isn't one.
- There is a persisted `OutboundStockMovement` / `OutboundStockMovementListItem` domain pair under `grails-app/domain/org/pih/warehouse/inventory/` used as a read-model for outbound list views.

---

## Location / party / org

### Location
- `grails-app/domain/org/pih/warehouse/core/Location.groovy`
- Warehouse, zone, bin, or virtual location. Self-referential tree (parent/children) plus a zone ref.
- Relationships (line 70):
  ```groovy
  static belongsTo = [parentLocation: Location, organization: Organization]
  static hasMany = [locations: Location, supportedActivities: String, employees: User]
  ```
- Has direct refs to `Address address`, `LocationType locationType`, `LocationGroup locationGroup` (in the class body).
- Owning services: `LocationService`, `LocationDataService`, `LocationGroupService`, `LocationGroupDataService`, `LocationRoleDataService`, `LocationTypeDataService`, `LocationIdentifierService`.
- Gotcha: `supportedActivities` is a `hasMany` of `String` — a set of activity codes (see `ActivityCode` in the core package) that gates which features the location participates in (e.g. `RECEIVE_STOCK`, `SHIP_STOCK`).

### LocationType
- `grails-app/domain/org/pih/warehouse/core/LocationType.groovy`
- Typed location (Depot, Warehouse, Zone, Bin, Supplier, Ward, ...). Has `supports(ActivityCode)` — feature-gating lookup.
- Owning service: `LocationTypeDataService`.

### Organization
- `grails-app/domain/org/pih/warehouse/core/Organization.groovy`
- Extends `Party` (core party/role model). A `Location` can belong to one `Organization`; one `Organization` has many `Location`s.
- Relationships (line 29):
  ```groovy
  static hasMany = [locations: Location]
  ```
- Owning services: `OrganizationService`, `OrganizationDataService`, `OrganizationIdentifierService`.
- `hasRoleType(RoleType)` is a common call-site — orgs carry role flags (SUPPLIER, MANUFACTURER, CUSTOMER, HOLDER, ...).

---

## Requisitions (stock requests)

### Requisition
- `grails-app/domain/org/pih/warehouse/requisition/Requisition.groovy`
- A stock request from a destination location to an origin location. The core entity for the internal replenishment workflow and — via `StockMovement` — the API-level representation of an inbound/outbound movement.
- Relationships (line 137):
  ```groovy
  static hasOne = [picklist: Picklist]
  static hasMany = [
      requisitionItems: RequisitionItem,
      transactions: Transaction,
      shipments: Shipment,
      comments: Comment,
      events: Event,
      approvers: Person,
  ]
  ```
- Owning services: `RequisitionService`, `RequisitionDataService`, `RequisitionIdentifierService`, `RequisitionTemplateService`, `RequisitionStatusTransitionEventService`.
- Gotcha: `requisitionItems` is `cascade: "all-delete-orphan"`, sorted by `orderIndex` asc, with `batchSize: 100`. Status transitions are event-driven (see `RequisitionStatusTransitionEventService`).

### RequisitionItem
- `grails-app/domain/org/pih/warehouse/requisition/RequisitionItem.groovy`
- A single product line within a `Requisition`. Supports substitution, cancellation, split lots, pallet/box/bin assignment.
- Relationships (line 113):
  ```groovy
  static belongsTo = [requisition: Requisition]
  static hasMany = [requisitionItems: RequisitionItem, picklistItems: PicklistItem]
  ```
- Self-referential `hasMany` is the **parent/child item** pattern — a single requested line can be split into multiple fulfilled sub-lines (different lots, substitutions).

---

## Shipments

### Shipment
- `grails-app/domain/org/pih/warehouse/shipping/Shipment.groovy`
- A physical movement of goods between two locations. Implements `Historizable`.
- Relationships (line 129):
  ```groovy
  static hasMany = [
      events, comments, containers, documents, receipts,
      shipmentItems, referenceNumbers,
      outgoingTransactions: Transaction,
      incomingTransactions: Transaction
  ]
  ```
- Note the dual `Transaction` associations: a shipment produces an `outgoingTransaction` (issue at origin) and an `incomingTransaction` (receive at destination). Both point at the same `Transaction` domain class but use different FK names — see the class-top comment about GORM ordering and join-table generation.
- Owning services: `ShipmentService`, `CombinedShipmentService`, `ShipmentIdentifierService`, `ShipmentStatusTransitionEventService`.
- Related: `ShipmentType`, `ShipmentMethod`, `ShipmentWorkflow`, `Shipper`, `ShipperService` (domain, not service — sic), `ReferenceNumber`, `ReferenceNumberType` — all under `grails-app/domain/org/pih/warehouse/shipping/`.

### ShipmentItem
- `grails-app/domain/org/pih/warehouse/shipping/ShipmentItem.groovy`
- A single line on a shipment. References `Shipment shipment` and `Container container` as top-level properties (no `belongsTo` — see the class-top comment about Hibernate HHH-4394).
- Loose-coupled to inventory by `lotNumber` + `expirationDate` (string/date, not FK).

### Container
- `grails-app/domain/org/pih/warehouse/shipping/Container.groovy`
- A pallet / box / tote inside a `Shipment`. Self-nesting via child containers.
- Relationships (line 41):
  ```groovy
  static belongsTo = [shipment: Shipment]
  static hasMany = [containers: Container]
  ```

---

## Orders (Purchase / Transfer / Return)

### Order
- `grails-app/domain/org/pih/warehouse/order/Order.groovy`
- A purchase order, transfer order, or return order (differentiated by `OrderType`). Table is `` `order` `` (backticked — `order` is a SQL reserved word).
- Relationships (line 131):
  ```groovy
  static hasMany = [
      orderItems, comments, documents, events, orderAdjustments
  ]
  static hasOne = [picklist: Picklist]
  ```
- Owning services: `OrderService`, `OrderIdentifierService`, `OrderSummaryService`, `OrderStatusEventService`, `PurchaseOrderIdentifierService`, `RefreshOrderSummaryEventService`.
- `orderItems` / `comments` / `documents` / `events` all `cascade: "all-delete-orphan"`. `OrderType` discriminates PO vs transfer vs return.

### OrderItem
- `grails-app/domain/org/pih/warehouse/order/OrderItem.groovy`
- A line on an `Order`. Self-references for sub-lines (returns can split lines). Cross-references `ShipmentItem`, `OrderAdjustment`, `PicklistItem`, `InvoiceItem`.
- Relationships (line 134):
  ```groovy
  static belongsTo = [order: Order, parentOrderItem: OrderItem]
  static hasMany = [
      orderItems: OrderItem, shipmentItems: ShipmentItem,
      orderAdjustments: OrderAdjustment, picklistItems: PicklistItem,
      invoiceItems: InvoiceItem
  ]
  ```

### OrderAdjustment
- `grails-app/domain/org/pih/warehouse/order/OrderAdjustment.groovy`
- A non-line charge on an order (freight, discount, tax). Typed by `OrderAdjustmentType`.

---

## Receiving

### Receipt
- `grails-app/domain/org/pih/warehouse/receiving/Receipt.groovy`
- A receiving event at the destination end of a `Shipment`. Implements `Historizable`.
- Relationships (line 49):
  ```groovy
  static hasOne = [transaction: Transaction]
  static hasMany = [receiptItems: ReceiptItem]
  static belongsTo = [shipment: Shipment]
  ```
- Owning services: `ReceiptService`, `ReceiptIdentifierService`.
- Note: a `Receipt` creates exactly one `Transaction` (the incoming stock event) via `hasOne`.

### ReceiptItem
- `grails-app/domain/org/pih/warehouse/receiving/ReceiptItem.groovy`
- A single received line, optionally linked back to a `ShipmentItem`.
- Relationships (line 53):
  ```groovy
  static belongsTo = [receipt: Receipt, shipmentItem: ShipmentItem]
  ```

---

## Users, people, roles

### Person
- `grails-app/domain/org/pih/warehouse/core/Person.groovy`
- Base-class for a human in the system — first/last/email/phone. `getName()` honors an `openboxes.anonymize.enabled` config flag (PII redaction for demo environments).

### User
- `grails-app/domain/org/pih/warehouse/core/User.groovy`
- Extends `Person`. Authenticatable.
- Relationships (line 38):
  ```groovy
  static hasMany = [roles: Role, locationRoles: LocationRole]
  ```
- Owning services: `UserService`, `UserDataService`.
- Note: `roles` are global; `locationRoles` are per-`Location` (distinct permission sets at each site).

### Role
- `grails-app/domain/org/pih/warehouse/core/Role.groovy`
- A named permission bundle. `RoleType` (enum) carries the semantic slot — `MANAGER`, `BROWSER`, `ADMIN`, ... `LocationRole` pairs a `User` + `Location` + `Role`.

---

## How to use this map

1. **Before opening a new feature**, find the entity that anchors it in the list above. Open the domain file to confirm fields and constraints.
2. **For stock-movement features**, always start from the relevant **service** in `grails-app/services/org/pih/warehouse/inventory/` — never manipulate `ProductAvailability` or `InventoryItem` directly.
3. **For API / React features**, check whether the operation goes through a `StockMovement` command (inbound/outbound requisition-backed flow) rather than creating a net-new domain.
4. **For customizations**, put your new service/domain under `org.pih.warehouse.custom.<feature>` per `rules/custom-package-isolation.md` — then inject the upstream services above as `def productService`, `def inventoryService`, etc.

## What is deliberately NOT in this map

- Full field lists (read the domain files).
- Every lookup/enum type (`ShipmentStatusCode`, `OrderStatus`, etc.) — grep in `src/main/groovy/org/pih/warehouse/`.
- Invoicing (`Invoice`, `InvoiceItem`) — its own subsystem under `grails-app/{domain,services}/org/pih/warehouse/invoice/`.
- Cycle count (`CycleCount*`) — its own subsystem under the inventory package; extensive service set prefixed `CycleCount`.
- Picklists (`Picklist`, `PicklistItem`) — referenced from `Requisition.hasOne` and `Order.hasOne`; live under `grails-app/{domain,services}/org/pih/warehouse/picklist/`.
- Fulfillment (`Fulfillment`, `FulfillmentItem`) — under `grails-app/{domain,services}/org/pih/warehouse/fulfillment/`; referenced from `Requisition`.
- Forecasting / replenishment / putaway / stockTransfer — service-only subsystems (no dedicated domains) under `grails-app/services/org/pih/warehouse/`.

If you need any of the above, walk the package directly — this map focuses on the **central spine** every feature touches.
