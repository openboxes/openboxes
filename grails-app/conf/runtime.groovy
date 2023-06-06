import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.RoleType

// Organization identifier
openboxes.identifier.organization.format = Constants.DEFAULT_ORGANIZATION_NUMBER_FORMAT
openboxes.identifier.organization.minSize = 2
openboxes.identifier.organization.maxSize = 3

// Purchase Order identifier
openboxes.identifier.purchaseOrder.generatorType = IdentifierGeneratorTypeCode.SEQUENCE
openboxes.identifier.purchaseOrder.format = "PO-\${destinationPartyCode}-\${sequenceNumber}"
openboxes.identifier.purchaseOrder.properties = ["destinationPartyCode": "destinationParty.code"]

// Require approval on purchase orders
openboxes.purchasing.approval.enabled = false
openboxes.purchasing.approval.minimumAmount = 0.00
openboxes.purchasing.approval.defaultRoleTypes = [RoleType.ROLE_APPROVER]

// Experimental feature that approximates a costing method to provide a crude unit price used
// for inventory valuation.
//
// Possible values:
//  * UpdateUnitPriceMethodCode.USER_DEFINED_PRICE (default)
//  * UpdateUnitPriceMethodCode.AVERAGE_PURCHASE_PRICE
//  * UpdateUnitPriceMethodCode.FIRST_PURCHASE_PRICE
//  * UpdateUnitPriceMethodCode.LAST_PURCHASE_PRICE
openboxes.purchasing.updateUnitPrice.enabled = false
openboxes.purchasing.updateUnitPrice.method = UpdateUnitPriceMethodCode.USER_DEFINED_PRICE

// connection timeout milliseconds (OBPIH-5320)
openboxes.browser.connection.status.timeout = 8000

// Date configuration (OBPIH-5397)
openboxes.display.date.format = Constants.DISPLAY_DATE_FORMAT
openboxes.display.date.defaultValue = Constants.DISPLAY_DATE_DEFAULT_VALUE

// Notifications configuration (OBPIH-5355)

// delay is in ms
openboxes.client.notification.autohide.delay = 8000

// Autosave configuration (OBPIH-5493)
openboxes.client.autosave.enabled = false

// Merge Products (OBPIH-5453)
openboxes.products.merge.enabled = false

openboxes.security.rbac.rules = [
    [controller: '*', actions: ['delete'], accessRules: [ minimumRequiredRole: RoleType.ROLE_SUPERUSER ]],
    [controller: '*', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_SUPERUSER ]],
    [controller: '*', actions: ['removeItem'],  accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
    // We probably need a way to handle wildcard in actions as well
    //[controller: '*', actions: ['remove*'], access: [RoleType.ROLE_SUPERUSER]],
    //[controller: '*', actions: ['delete*'], access: [RoleType.ROLE_SUPERUSER]]
    // ... otherwise we'll need to include explicit rules for everything
    [controller: 'order', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_ASSISTANT ]],
    [controller: 'order', actions: ['removeOrderItem'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
    [controller: 'order', actions: ['deleteDocument'], accessRules: [ minimumRequiredRole: RoleType.ROLE_ADMIN ]],
    [controller: 'invoice', actions: ['eraseInvoice'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
    [controller: 'invoiceApi', actions: ['removeItem'],  accessRules: [  minimumRequiredRole: RoleType.ROLE_MANAGER, supplementalRoles: [RoleType.ROLE_INVOICE] ]],
    [controller: 'stockTransfer', actions: ['eraseStockTransfer'],  accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
    [controller: 'stockMovementItemApi', actions: ['eraseItem'],  accessRules: [ minimumRequiredRole: RoleType.ROLE_ASSISTANT ]],
    [controller: 'stockMovement', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_ASSISTANT ]],
    [controller: 'stockRequest', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_ASSISTANT]],
    [controller: 'glAccount', actions: ['delete'], accessRules: [minimumRequiredRole: RoleType.ROLE_SUPERUSER]],
    [controller: 'glAccountType', actions: ['delete'], accessRules: [minimumRequiredRole: RoleType.ROLE_SUPERUSER]],
    [controller: 'preferenceType', actions: ['delete'], accessRules: [minimumRequiredRole: RoleType.ROLE_SUPERUSER]],
    [controller: 'purchaseOrderApi', actions: ['delete'], accessRules: [ minimumRequiredRole: RoleType.ROLE_ASSISTANT]],
    [controller: 'purchaseOrderApi', actions: ['rollback'], accessRules: [ supplementalRoles: [RoleType.ROLE_APPROVER]]],
    [controller: 'stockTransferApi', actions: ['delete'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER]],
    [controller: 'stockMovementApi', actions: ['delete'], accessRules: [ minimumRequiredRole: RoleType.ROLE_ASSISTANT]],
    [controller: 'product', actions: ['merge'], accessRules: [ minimumRequiredRole: RoleType.ROLE_ADMIN]],
    // Other controller actions that might need explicit rules
    //[controller: 'putawayItemApi', actions: ['removingItem'], access: [RoleType.ROLE_MANAGER]],
]

// Global megamenu configuration

openboxes.menuSectionsUrlParts = [
    inventory: ["inventory", "inventoryItem", "stockTransfer"],
    products: ["product"],
    purchasing: ["purchaseOrder"],
    invoicing: ["invoice"],
    outbound: ["verifyRequest"],
    requisitionTemplate: ["requisitionTemplate"],
    configuration: ["locationsConfiguration"],
    // for inbound / outbound and purchasing / putaway the same url is used,
    // so it is underlined directly from stockMovement/show.gsp
    injectedDirectly: ["stockMovement", "order"]
]

// TODO: Clean up and add all missing message.properties
openboxes {
    megamenu {
        dashboard {
            enabled = true
            label = "dashboard.label"
            defaultLabel = "Dashboard"
            href = "/dashboard/index"
        }
        analytics {
            enabled = false
            minimumRequiredRole = RoleType.ROLE_ADMIN
            label = "analytics.label"
            defaultLabel = "Analytics"
            menuItems = [
                // TODO: Add option to include label 'beta'
                [label: "inventory.browse.label", defaultLabel: "Browse Inventory", href: "/inventoryBrowser/index"],
                [label: "inventory.snapshot.label", defaultLabel: "Inventory Snapshots", href: "/snapshot/list"],
                [label: "consumption.report.label", defaultLabel: "Consumption Report", href: "/consumption/list"]
            ]
        }
        inventory {
            enabled = true
            label = "inventory.label"
            defaultLabel = "Inventory"
            requiredActivitiesAny = [ActivityCode.MANAGE_INVENTORY]
            subsections = [
                [
                    label: "inventory.browse.label",
                    defaultLabel: "Browse Inventory",
                    menuItems: [
                        [label: "inventory.browse.label", defaultLabel: "Browse Inventory", href: "/inventory/browse?resetSearch=true"],
                        // TODO: (Future improvement) Probably further options should be generated dynamicaly (with item count in bracket)...
                    ],
                ],
                [
                    label: "inventory.manage.label",
                    defaultLabel: "Manage Inventory",
                    menuItems: [
                        [label: "inventory.manage.label", defaultLabel: "Manage Inventory", href: "/inventory/manage"],
                        [label: "inventory.import.label", defaultLabel: "Import Inventory", href: "/batch/importData?type=inventory&execution=e1s1"],
                        [label: "inventory.createStockTransfer.label", defaultLabel: "Create Stock Transfer", requiredActivitiesAll: ActivityCode.binTrackingList(), href: "/stockTransfer/create"],
                        [label: "inventory.listStockTransfers.label", defaultLabel: "List Stock Transfers", requiredActivitiesAll: ActivityCode.binTrackingList(), href: "/stockTransfer/list"],
                        [label: "inventory.createReplenishment.label", defaultLabel: "Create Replenishment", requiredActivitiesAll: ActivityCode.binTrackingList(), href: "/replenishment/create"]
                    ]
                ]
            ]
        }
        purchasing {
            enabled = true
            label = "order.purchasing.label"
            defaultLabel = "Purchasing"
            subsections = [
                [
                    label: "",
                    defaultLabel: "Purchasing",
                    menuItems: [
                        [label: "order.createPurchase.label", defaultLabel: "Create Purchase Order", href: "/purchaseOrder/create", requiredActivitiesAny: [ActivityCode.PLACE_ORDER]],
                        [label: "order.listPurchase.label", defaultLabel: "List Purchase Orders", href: "/purchaseOrder/list"],
                        [label: "location.listSuppliers.label", defaultLabel: "List Suppliers", href: "/supplier/list"],
                        [label: "shipment.shipfromPO.label", defaultLabel: "Ship from Purchase Order", href: "/stockMovement/createCombinedShipments?direction=INBOUND"],
                        [label: "dashboard.supplierDashboard.label", defaultLabel: "Supplier Dashboard", href: "/dashboard/supplier"]
                    ]
                ]
            ]
        }
        invoicing {
            enabled = true
            supplementalRoles = [RoleType.ROLE_INVOICE]
            label = "react.invoicing.label"
            defaultLabel = "Invoicing"
            subsections = [
                [
                    label: "react.invoicing.label",
                    defaultLabel: "Invoicing",
                    menuItems: [
                        [label: "react.invoice.createInvoice.label", defaultLabel: "Create Invoice", href: "/invoice/create"],
                        [label: "react.invoice.list.label", defaultLabel: "List Invoices", href: "/invoice/list"],
                    ]
                ]
            ]
        }
        inbound {
            enabled = true
            label = "default.inbound.label"
            defaultLabel = "Inbound"
            requiredActivitiesAny = [ActivityCode.RECEIVE_STOCK]
            subsections = [
                [
                    label: "stockMovements.label",
                    defaultLabel: "Stock Movements",
                    menuItems: [
                        [label: "inbound.create.label", defaultLabel: "Create Inbound Movement", href: "/stockMovement/createInbound?direction=INBOUND"],
                        [label: "stockRequest.create.label", defaultLabel: "Create Stock Request", href: "/stockMovement/createRequest"],
                        [label: "inbound.list.label", defaultLabel: "List Inbound Movements", href: "/stockMovement/list?direction=INBOUND"],
                        [label: "inboundReturns.create.label", defaultLabel: "Create Inbound Return", href: "/stockTransfer/createInboundReturn"]
                    ]
                ],
                [
                    label: "putAways.label",
                    defaultLabel: "Putaways",
                    requiredActivitiesAll: ActivityCode.binTrackingList(),
                    menuItems: [
                        [label: "react.putAway.createPutAway.label", defaultLabel: "Create Putaway", href: "/putAway/create"],
                        [label: "react.putAway.list.label", defaultLabel: "List Putaways", href: "/order/list?orderType=PUTAWAY_ORDER&status=PENDING"]
                    ]
                ],
                [
                    // DEPRECATED!
                    label: "receiving.label",
                    defaultLabel: "Receiving",
                    enabled: false,
                    menuItems: [
                        [label: "shipping.createIncomingShipment.label", defaultLabel: "Create inbound shipment", href: "/createShipmentWorkflow/createShipment?type=INCOMING"],
                        [label: "shipping.listIncoming.label", defaultLabel: "List Inbound Shipments", href: "/shipment/list?type=incoming"],
                        [label: "default.all.label", defaultLabel: "All", href: "/shipment/list?type=incoming"],
                    ]
                ]
            ]
        }
        outbound {
            enabled = true
            label = "default.outbound.label"
            defaultLabel = "Outbound"
            requiredActivitiesAny = [ActivityCode.SEND_STOCK]
            subsections = [
                [
                    label: "",
                    defaultLabel: "Stock Movement",
                    menuItems: [
                        [label: "outbound.create.label", defaultLabel: "Create Outbound Movements", href: "/stockMovement/createOutbound?direction=OUTBOUND"],
                        [label: "outbound.list.label", defaultLabel: "List Outbound Movements", href: "/stockMovement/list?direction=OUTBOUND"],
                        [label: "requests.list.label", defaultLabel: "List Requests", href: "/stockMovement/list?direction=OUTBOUND&sourceType=ELECTRONIC"],
                        [label: "outboundReturns.create.label", defaultLabel: "Create Outbound Return", href: "/stockTransfer/createOutboundReturn"]
                    ]
                ],
                [
                    // DEPRECATED!
                    label: "shipping.label",
                    defaultLabel: "Shipping",
                    enabled: false,
                    menuItems: [
                        [label: "shipping.createOutgoingShipment.label", defaultLabel: "Create outbound shipment", href: "/createShipmentWorkflow/createShipment?type=OUTGOING"],
                        [label: "shipping.listOutgoing.label", defaultLabel: "List Outbound Shipments", href: "/shipment/list?type=outgoing"],
                        [label: "default.all.label", defaultLabel: "All", href: "/shipment/list?type=outgoing"],
                    ]
                ]
            ]
        }
        reporting {
            enabled = true
            label = "report.label"
            defaultLabel = "Reporting"
            subsections = [
                [
                    label: "report.inventoryReports.label",
                    defaultLabel: "Inventory Reports",
                    menuItems: [
                        [label: "report.inStockReport.label", defaultLabel: "In Stock Report", href: "/inventory/listInStock"],
                        [label: "report.binLocationReport.label", defaultLabel: "Bin Location Report", href: "/report/showBinLocationReport"],
                        [label: "report.expiredStockReport.label", defaultLabel: "Expired Stock Report", href: "/inventory/listExpiredStock"],
                        [label: "report.expiringStockReport.label", defaultLabel: "Expiring Stock Report", href: "/inventory/listExpiringStock"],
                        [label: "report.inventoryByLocationReport.label", defaultLabel: "Inventory By Location Report", href: "/report/showInventoryByLocationReport"],
                        [label: "report.cycleCount.label", defaultLabel: "Cycle Count Report", href: "/report/showCycleCountReport"],
                        [label: "report.baselineQohReport.label", defaultLabel: "Baseline QoH Report", href: "/inventory/show"],
                        [label: "report.onOrderReport.label", defaultLabel: "On Order Report", href: "/report/showOnOrderReport"]
                    ]
                ],
                [
                    label: "report.orderReports.label",
                    defaultLabel: "Order Reports",
                    menuItems: [
                        [label: "report.forecastReport.label", defaultLabel: "Forecast Report", href: "/report/showForecastReport"],
                        [label: "report.reorderReport.label", defaultLabel: "Reorder Report", href: "/inventory/reorderReport"],
                        [label: "report.amountOutstandingReport.label", defaultLabel: "Amount Outstanding Report", href: "/report/amountOutstandingOnOrdersReport", supplementalRoles: [RoleType.ROLE_FINANCE]],
                    ]
                ],
                [
                    label: "report.transactionReports.label",
                    defaultLabel: "Transaction Reports",
                    menuItems: [
                        [label: "report.showTransactionReport.label", defaultLabel: "Transaction Report", href: "/report/showTransactionReport"],
                        [label: "report.consumption.label", defaultLabel: "Consumption Report", href: "/consumption/show"],
                        [label: "report.requestDetailReport.label", defaultLabel: "Request Detail Report", href: "/report/showRequestDetailReport"],
                    ]
                ],
                [
                    label: "dataExports.label",
                    defaultLabel: "Data Exports",
                    menuItems: [
                        [label: "product.exportAsCsv.label", defaultLabel: "Export products", href: "/product/exportAsCsv"],
                        [label: "export.productSources.label", defaultLabel: "Export product sources", href: "/productSupplier/export"],
                        [label: "export.latestInventory.label", defaultLabel: "Export latest inventory date", href: "/inventory/exportLatestInventoryDate"],
                        [label: "export.inventoryLevels.label", defaultLabel: "Export inventory levels", href: "/inventoryLevel/export"],
                        [label: "export.requisitions.label", defaultLabel: "Export requisitions", href: "/requisition/export"],
                        [label: "export.binLocations.label", defaultLabel: "Export bin locations", href: "/report/exportBinLocation?downloadFormat=csv"],
                        [label: "export.productDemand.label", defaultLabel: "Export product demand", href: "/report/exportDemandReport?downloadFormat=csv"],
                        [label: "export.custom.label", defaultLabel: "Custom data exports", href: "/dataExport/index"]
                    ]
                ]
            ]
        }
        products {
            enabled = true
            label = "products.label"
            defaultLabel = "Products"
            requiredActivitiesAny = [ActivityCode.MANAGE_INVENTORY]
            subsections = [
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "product.create.label", defaultLabel: "Create product", href: "/product/create", minimumRequiredRole: RoleType.ROLE_ADMIN],
                        [label: "products.list.label", defaultLabel: "List Products", href: "/product/list"],
                        [label: "product.batchEdit.label", defaultLabel: "Batch edit product", href: "/product/batchEdit", minimumRequiredRole: RoleType.ROLE_ADMIN],
                        [label: "product.importAsCsv.label", defaultLabel: "Import products", href: "/product/importAsCsv", minimumRequiredRole: RoleType.ROLE_ADMIN],
                        [label: "product.exportAsCsv.label", defaultLabel: "Export products", href: "/product/exportAsCsv", minimumRequiredRole: RoleType.ROLE_ADMIN],
                        [label: "productType.label", defaultLabel: "Product Type", href: "/productType/list", minimumRequiredRole: RoleType.ROLE_SUPERUSER]
                    ]
                ],
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "categories.label", defaultLabel: "Categories", href: "/category/tree"],
                        [label: "product.catalogs.label", defaultLabel: "Catalogs", href: "/productCatalog/list"],
                        [label: "product.tags.label", defaultLabel: "Tags", href: "/tag/list"],
                        [label: "attributes.label", defaultLabel: "Attributes", href: "/attribute/list"],
                        [label: "product.associations.label", defaultLabel: "Associations", href: "/productAssociation/list"],
                    ]
                ],
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "productSuppliers.label", defaultLabel: "Products Sources", href: "/productSupplier/list"],
                        [label: "product.components.label", defaultLabel: "Components", href: "/productComponent"],
                        [label: "productGroups.label", defaultLabel: "Generic Products", href: "/productGroup/list"],
                        [label: "unitOfMeasure.label", defaultLabel: "Unit of Measure", href: "/unitOfMeasure"],
                        [label: "unitOfMeasureClass.label", defaultLabel: "Uom Class", href: "/unitOfMeasureClass"],
                        [label: "unitOfMeasureConversion.label", defaultLabel: "Uom Conversion", href: "/unitOfMeasureConversion/list"]
                    ]
                ]
            ]
        }
        requisitionTemplate {
            enabled = true
            label = "requisitionTemplates.label"
            defaultLabel = "Stock Lists"
            menuItems = [
                [label: "requisitionTemplates.list.label", defaultLabel: "List stock lists", href: "/requisitionTemplate/list"],
                [label: "requisitionTemplates.create.label", defaultLabel: "Create stock list", href: "/requisitionTemplate/create", minimumRequiredRole: RoleType.ROLE_ADMIN],
            ]
        }
        configuration {
            enabled = true
            minimumRequiredRole = RoleType.ROLE_ADMIN
            label = "configuration.label"
            defaultLabel = "Configuration"
            subsections = [
                [
                    label: "admin.label",
                    defaultLabel: "Administration",
                    menuItems: [
                        [label: "default.settings.label", defaultLabel: "Settings", href: "/admin/showSettings"],
                        [label: "cache.label", defaultLabel: "Cache", href: "/admin/cache"],
                        [label: "console.label", defaultLabel: "Console", href: "/console/index"],
                        [label: "dataImport.label", defaultLabel: "Data Import", href: "/batch/importData"],
                        [label: "dataMigration.label", defaultLabel: "Data Migration", href: "/migration/index"],
                        [label: "email.label", defaultLabel: "Email", href: "/admin/sendMail"],
                        [label: "localization.label", defaultLabel: "Localization", href: "/localization/list"]
                    ]
                ],
                [
                    label: "locations.label",
                    defaultLabel: "Locations",
                    menuItems: [
                        [label: "locations.label", defaultLabel: "Locations", href: "/location/list"],
                        [label: "locationGroups.label", defaultLabel: "Location groups", href: "/locationGroup/list"],
                        [label: "locationTypes.label", defaultLabel: "Location types", href: "/locationType/list"],
                        [label: "organizations.label", defaultLabel: "Organizations", href: "/organization/list"],
                        [label: "partyRoles.label", defaultLabel: "Party roles", href: "/partyRole/list"],
                        [label: "partyTypes.label", defaultLabel: "Party types", href: "/partyType/list"],
                        [label: "person.list.label", defaultLabel: "People", href: "/person/list"],
                        [label: "roles.label", defaultLabel: "Roles", href: "/role"],
                        [label: "users.label", defaultLabel: "Users", href: "/user/list"],
                    ]
                ],
                [
                    label: "transactions.label",
                    defaultLabel: "Transactions",
                    menuItems: [
                        [label: "transactionsTypes.label", defaultLabel: "Transactions Types", href: "/transactionType"],
                        [label: "transactions.label", defaultLabel: "Transactions", href: "/inventory/listAllTransactions"],
                        [label: "transaction.add.label", defaultLabel: "Add transaction", href: "/inventory/editTransaction"],
                        [label: "import.inventory.label", defaultLabel: "Import Inventory", href: "/batch/importData?type=inventory"],
                        [label: "import.inventoryLevel.label", defaultLabel: "Import Inventory Level", href: "/batch/importData?type=inventoryLevel"]
                    ]
                ],
                [
                    label: "default.other.label",
                    defaultLabel: "Other",
                    menuItems: [
                        [label: "budgetCode.label", defaultLabel: "Budget Code", href: "/budgetCode/list"],
                        [label: "containerTypes.label", defaultLabel: "Container Types", href: "/containerType"],
                        [label: "documents.label", defaultLabel: "Documents", href: "/document/list"],
                        [label: "documentTypes.label", defaultLabel: "Document Types", href: "/documentType"],
                        [label: "eventTypes.label", defaultLabel: "Event Types", href: "/eventType/list"],
                        [label: "glAccountType.label", defaultLabel: "GL Account Type", href: "/glAccountType/list"],
                        [label: "glAccount.label", defaultLabel: "GL Account", href: "/glAccount/list"],
                        [label: "orderAdjustmentType.label", defaultLabel: "Order Adjustment Type", href: "/orderAdjustmentType/list"],
                        [label: "paymentMethodTypes.label", defaultLabel: "Payment Method Types", href: "/paymentMethodType"],
                        [label: "paymentTerms.label", defaultLabel: "Payment Terms", href: "/paymentTerm/list"],
                        [label: "preferenceType.label", defaultLabel: "Preference Type", href: "/preferenceType/list"],
                        [label: "shippers.label", defaultLabel: "Shippers", href: "/shipper"],
                        [label: "shipmentWorkflows.label", defaultLabel: "Shipment Workflows", href: "/shipmentWorkflow/list"],
                        [label: "productsConfiguration.label", defaultLabel: "Categories and Products Configuration", href: "/productsConfiguration/index"],
                        [label: "locationsConfiguration.label", defaultLabel: "Locations Configuration", href: "/locationsConfiguration/index"],
                        [label: "loadData.label", defaultLabel: "Load Data", href: "/loadData/index"],
                        [label: "resetInstance.label", defaultLabel: "Reset your instance", href: "/resettingInstanceInfo/index"]
                    ]
                ]
            ]
        }
        customLinks {
            enabled = true
            label = "customLinks.label"
            defaultLabel = "Custom Links"
            menuItems = [
                //[label: "requestItemCreation.label", defaultLabel: "Request Item Creation", href: "", target: "_blank"],
            ]
        }

        // deprecated megamenu configuration
        requisitions {
            enabled = false
            label = "requisitions.label"
            defaultLabel = "Requisitions"
            subsections = [
                [
                    label: "requisition.list.label",
                    defaultLabel: "List Requisitions",
                    menuItems: [
                        [label: "requisition.allIncoming.label", defaultLabel: "All", href: "/requisition/list"],
                        // TODO: (Future improvement) Probably further options should be generated dynamicaly (with item count in bracket)...
                    ],
                ],
                [
                    label: "requisition.create.subsection.label",
                    defaultLabel: "Create Requisitions",
                    menuItems: [
                        [label: "requisition.create.stock.label", defaultLabel: "Create stock requisition", href: "/requisition/chooseTemplate?type=STOCK"],
                        [label: "requisition.create.nonstock.label", defaultLabel: "Create non-stock requisition", href: "/requisition/create?type=NON_STOCK"],
                        [label: "requisition.create.adhoc.stock.label", defaultLabel: "Create adhoc stock requisition", href: "/requisition/create?type=ADHOC"],
                    ]
                ]
            ]
        }
        shipping {
            enabled = false
            label = "shipping.label"
            defaultLabel = "Shipping"
        }
        receiving {
            enabled = false
            label = "receiving.label"
            defaultLabel = "Receiving"
        }

        orders {
            enabled = true
            label = "orders.label"
            defaultLabel = "Orders"
        }

        stockRequest {
            enabled = true
            label = "stockRequests.label"
            defaultLabel = "Stock Requests"
        }

        stockMovement {
            enabled = true
            label = "stockMovements.label"
            defaultLabel = "Stock Movements"
        }

        putaways {
            enabled = true
            label = "putaways.label"
            defaultLabel = "Putaways"
        }
    }
    requestorMegamenu {
        request {
            enabled = true
            supplementalRoles = [RoleType.ROLE_REQUESTOR]
            label = "default.inbound.label"
            defaultLabel = "Inbound"
            subsections = [
                [
                    label       : "stockMovements.label",
                    defaultLabel: "Stock Movements",
                    menuItems   : [
                        [label: "stockRequest.create.label", defaultLabel: "Create Stock Request", href: "/stockMovement/createRequest"],
                        [label: "inbound.list.label", defaultLabel: "List Inbound Movements", href: "/stockMovement/list?direction=INBOUND"],
                    ]
                ]
            ]
        }
    }
}

openboxes {
    dashboard {
        yearTypes {
            fiscalYear {
                start = "07/01" // format: MM/DD, For PIH and the Govt of Dominica fiscal year start July 1
                end = "06/30" // format: MM/DD
                labelYearPrefix = "FY "
                yearFormat = "yy"
            }
            calendarYear {
                start = "01/01"
                end = "12/31"
                labelYearPrefix = ""
                yearFormat = "yyyy"
            }
        }
    }
}

openboxes {
    dashboardConfig {
        mainDashboardId = "mainDashboard"
        dashboards {
            mainDashboard {
                personal {
                    name = "My Dashboard"
                    filters {}
                    widgets = [
                        [
                            widgetId: "inventoryByLotAndBin",
                            order   : 1
                        ],
                        [
                            widgetId: "receivingBin",
                            order   : 2
                        ],
                        [
                            widgetId: "inProgressShipments",
                            order   : 3
                        ],
                        [
                            widgetId: "inProgressPutaways",
                            order   : 4
                        ],

                        [
                            widgetId: "inventorySummary",
                            order   : 1
                        ],
                        [
                            widgetId: "expirationSummary",
                            order   : 2
                        ],
                        [
                            widgetId: "incomingStock",
                            order   : 3
                        ],
                        [
                            widgetId: "outgoingStock",
                            order   : 4
                        ],
                        [
                            widgetId: "delayedShipments",
                            order   : 5
                        ],
                        [
                            widgetId: "discrepancy",
                            order   : 6
                        ]
                    ]
                }
                warehouse {
                    name = "Warehouse Management"
                    filters {}
                    widgets = [
                        [
                            widgetId: "inventoryByLotAndBin",
                            order   : 1
                        ],
                        [
                            widgetId: "receivingBin",
                            order   : 2
                        ],
                        [
                            widgetId: "inProgressShipments",
                            order   : 3
                        ],
                        [
                            widgetId: "inProgressPutaways",
                            order   : 4
                        ],

                        [
                            widgetId: "inventorySummary",
                            order   : 1
                        ],
                        [
                            widgetId: "expirationSummary",
                            order   : 2
                        ],
                        [
                            widgetId: "incomingStock",
                            order   : 3
                        ],
                        [
                            widgetId: "outgoingStock",
                            order   : 4
                        ],
                        [
                            widgetId: "delayedShipments",
                            order   : 5
                        ],
                        [
                            widgetId: "discrepancy",
                            order   : 6
                        ]
                    ]
                }
                inventory {
                    name = "Inventory Management"
                    filters {}
                    widgets = [
                        [
                            widgetId: "receivingBin",
                            order   : 1
                        ],
                        [
                            widgetId: "defaultBin",
                            order   : 2
                        ],
                        [
                            widgetId: "negativeInventory",
                            order   : 3
                        ],
                        [
                            widgetId: "expiredStock",
                            order   : 4
                        ],
                        [
                            widgetId: "openStockRequests",
                            order   : 5
                        ],

                        [
                            widgetId: "delayedShipments",
                            order   : 1
                        ],
                        [
                            widgetId: "productsInventoried",
                            order   : 2
                        ]
                    ]
                }
                transaction {
                    name = "Transaction Management"
                    filters {}
                    widgets = [
                        [
                            widgetId: "fillRateSnapshot",
                            order   : 1
                        ],

                        [
                            widgetId: "receivedStockMovements",
                            order   : 1
                        ],
                        [
                            widgetId: "sentStockMovements",
                            order   : 2
                        ],
                        [
                            widgetId: "lossCausedByExpiry",
                            order   : 3
                        ],
                        [
                            widgetId: "percentageAdHoc",
                            order   : 4
                        ],
                        [
                            widgetId: "fillRate",
                            order   : 5
                        ],
                        [
                            widgetId: "stockOutLastMonth",
                            order   : 6
                        ]
                    ]
                }
                fillRate {
                    name = "Fill Rate"
                    filters {
                        category {
                            endpoint = "/categoryApi/list"
                        }
                    }
                    widgets = [
                        [
                            widgetId: "fillRateSnapshot",
                            order   : 1
                        ],

                        [
                            widgetId: "fillRate",
                            order   : 1
                        ]
                    ]
                }

            }
            supplier {
                supplier {
                    name = "Supplier Dashboard"
                    filters {
                        supplier {
                            endpoint = "/api/locations?direction=INBOUND"
                        }
                    }
                    widgets = [
                        [
                            widgetId: "numberOfOpenPurchaseOrders",
                            order   : 1
                        ]
                    ]
                }
            }
        }
        // TODO: OBPIH-4384 Refactor indicator filters to be more generic (currently filters are hardcoded on the frontend, these should be defined here and rendered there basing on config)
        dashboardWidgets {
            inProgressPutaways {
                enabled = true
                title = "react.dashboard.inProgressPutaways.title.label"
                info = "react.dashboard.inProgressPutaways.info.label"
                subtitle = "react.dashboard.subtitle.putaways.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/inProgressPutaways"
            }
            inventoryByLotAndBin {
                enabled = true
                title = "react.dashboard.inventoryByLotAndBin.title.label"
                info = "react.dashboard.inventoryByLotAndBin.info.label"
                subtitle = "react.dashboard.subtitle.inStock.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/inventoryByLotAndBin"
            }
            inProgressShipments {
                enabled = true
                title = "react.dashboard.inProgressShipments.title.label"
                info = "react.dashboard.inProgressShipments.info.label"
                subtitle = "react.dashboard.subtitle.shipments.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/inProgressShipments"
            }
            receivingBin {
                enabled = true
                title = "react.dashboard.receivingBin.title.label"
                info = "react.dashboard.receivingBin.info.label"
                subtitle = "react.dashboard.subtitle.products.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/receivingBin"
            }
            itemsInventoried {
                enabled = true
                title = "react.dashboard.itemsInventoried.title.label"
                info = "react.dashboard.itemsInventoried.info.label"
                subtitle = "react.dashboard.subtitle.items.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/itemsInventoried"
            }
            defaultBin {
                enabled = true
                title = "react.dashboard.defaultBin.title.label"
                info = "react.dashboard.defaultBin.info.label"
                subtitle = "react.dashboard.subtitle.products.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/defaultBin"
            }
            negativeInventory {
                enabled = true
                title = "react.dashboard.productWithNegativeInventory.title.label"
                info = "react.dashboard.productWithNegativeInventory.info.label"
                subtitle = "react.dashboard.subtitle.products.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/productWithNegativeInventory"
            }
            expiredStock {
                enabled = true
                title = "react.dashboard.expiredProductsInStock.title.label"
                info = "react.dashboard.expiredProductsInStock.info.label"
                subtitle = "react.dashboard.subtitle.products.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/expiredProductsInStock"
            }
            openStockRequests {
                enabled = true
                title = "react.dashboard.openStockRequests.title.label"
                info = "react.dashboard.openStockRequests.info.label"
                subtitle = "react.dashboard.requests.subtitle.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/openStockRequests"
            }
            inventoryValue {
                enabled = true
                title = "react.dashboard.inventoryValue.title.label"
                info = ''
                subtitle = "react.dashboard.subtitle.inStock.label"
                numberType = 'dollars'
                type = 'number'
                endpoint = "/api/dashboard/inventoryValue"
            }

            fillRateSnapshot {
                enabled = true
                title = "react.dashboard.fillRateSnapshot.title.label"
                info = "react.dashboard.fillRateSnapshot.info.label"
                graphType = 'sparkline'
                type = 'number'
                endpoint = "/api/dashboard/fillRateSnapshot"
            }

            requisitionCountByYear {
                enabled = true
                title = "react.dashboard.requisitionCountByYear.title.label"
                info = "react.dashboard.requisitionCountByYear.info.label"
                graphType = "bar"
                type = 'graph'
                endpoint = "/api/dashboard/requisitionsByYear"
                yearTypeFilter {
                    parameter = "yearType"
                    defaultValue = "fiscalYear"
                    options = [
                        [label: "react.dashboard.fiscalYear.label", value: "fiscalYear"],
                        [label: "react.dashboard.calendarYear.label", value: "calendarYear"]
                    ]
                }
            }
            inventorySummary {
                enabled = true
                title = "react.dashboard.inventorySummaryData.title.label"
                info = "react.dashboard.inventorySummaryData.info.label"
                graphType = "horizontalBar"
                type = 'graph'
                endpoint = "/api/dashboard/inventorySummary"
                datalabel = true
                colors {
                    labels {
                        success = ["In stock"]
                        warning = ["Above maximum", "Below reorder", "Below minimum"]
                        error = ["No longer in stock"]
                    }
                }
            }
            expirationSummary {
                enabled = true
                title = "react.dashboard.expirationSummaryData.title.label"
                info = "react.dashboard.expirationSummaryData.info.label"
                graphType = "line"
                type = 'graph'
                endpoint = "/api/dashboard/expirationSummary"
                timeFilter = true
                colors {
                    datasets {
                        state6 = ["Expiration(s)"]
                    }
                    labels {
                        state5 = [
                            [code : "react.dashboard.timeline.today.label", message : "today"],
                            [code : "react.dashboard.timeline.within30Days.label", message : "within 30 days"],
                            [code : "react.dashboard.timeline.within90Days.label", message : "within 90 days"],
                            [code : "react.dashboard.timeline.within180Days.label", message : "within 180 days"],
                            [code : "react.dashboard.timeline.within360Days.label", message : "within 360 days"]
                        ]
                    }
                }
            }
            incomingStock {
                enabled = true
                title = "react.dashboard.incomingStock.title.label"
                info = "react.dashboard.incomingStock.info.label"
                graphType = "numbers"
                type = 'graph'
                endpoint = "/api/dashboard/incomingStock"
                colors {
                    datasets {
                        state6 = ["first"]
                        state7 = ["second"]
                        state8 = ["third"]
                    }
                }
            }
            outgoingStock {
                enabled = true
                title = "react.dashboard.outgoingStock.title.label"
                info = "react.dashboard.outgoingStock.info.label"
                graphType = "numbers"
                type = 'graph'
                endpoint = "/api/dashboard/outgoingStock"
                colors {
                    datasets {
                        success = ["first"]
                        warning = ["second"]
                        error = ["third"]
                    }
                }
            }
            receivedStockMovements {
                enabled = true
                title = "react.dashboard.receivedStockData.title.label"
                info = "react.dashboard.receivedStockData.info.label"
                graphType = "bar"
                type = 'graph'
                endpoint = "/api/dashboard/receivedStockMovements"
                timeFilter = true
                stacked = true
                datalabel = true
            }
            discrepancy {
                enabled = true
                title = "react.dashboard.discrepancy.title.label"
                info = "react.dashboard.discrepancy.info.label"
                graphType = "table"
                type = 'graph'
                endpoint = "/api/dashboard/discrepancy"
                timeFilter = true
            }
            delayedShipments {
                enabled = true
                title = "react.dashboard.delayedShipments.title.label"
                info = "react.dashboard.delayedShipments.info.label"
                graphType = "numberTable"
                type = 'graph'
                endpoint = "/api/dashboard/delayedShipments"
                colors {
                    datasets {
                        state5 = ["first"]
                        state4 = ["second"]
                        state3 = ["third"]
                    }
                }
            }
            sentStockMovements {
                enabled = true
                title = "react.dashboard.sentStockMovements.title.label"
                info = "react.dashboard.sentStockMovements.info.label"
                graphType = "bar"
                type = 'graph'
                endpoint = "/api/dashboard/sentStockMovements"
                timeFilter = true
                stacked = true
                datalabel = true
            }
            lossCausedByExpiry {
                enabled = false
                title = "react.dashboard.lossCausedByExpiry.title.label"
                info = ""
                graphType = "bar"
                type = 'graph'
                endpoint = "/api/dashboard/lossCausedByExpiry"
                timeFilter = true
                stacked = true
                colors {
                    datasets {
                        success = ["Inventory value not expired last day of month"]
                        warning = ["Inventory value expired last day of month"]
                        error = ["Inventory value removed due to expiry"]
                    }
                }
            }
            productsInventoried {
                enabled = false
                title = "react.dashboard.productsInventoried.title.label"
                info = ""
                graphType = "numbersCustomColors"
                type = 'graph'
                endpoint = "/api/dashboard/productsInventoried"
                colors {
                    datasets {
                        state6 = ["first"]
                        state7 = ["second"]
                        state8 = ["third"]
                    }
                }
            }
            percentageAdHoc {
                enabled = true
                title = "react.dashboard.percentageAdHoc.title.label"
                info = "react.dashboard.percentageAdHoc.info.label"
                graphType = "doughnut"
                type = 'graph'
                endpoint = "/api/dashboard/percentageAdHoc"
                legend = true
                datalabel = true
                colors {
                    labels {
                        state5 = ["STOCK"]
                        state4 = ["ADHOC"]
                    }
                }
            }
            fillRate {
                enabled = true
                title = "react.dashboard.fillRate.title.label"
                info = "react.dashboard.fillRate.info.label"
                graphType = "bar"
                type = 'graph'
                legend = true
                endpoint = "/api/dashboard/fillRate"
                timeFilter = true
                locationFilter = true
                timeLimit = 12
                doubleAxeY = true
                datalabel = false
                size = 'big'
                colors {
                    datasets {
                        state3 = ["Request lines submitted"]
                        state6 = ["Lines cancelled stock out"]
                        state2 = ["Average Fill Rate"]
                        state8 = ["Average of target Fill Rate"]
                    }
                }
            }
            stockOutLastMonth {
                enabled = true
                title = "react.dashboard.stockOutLastMonth.title.label"
                info = "react.dashboard.stockOutLastMonth.info.label"
                graphType = "doughnut"
                type = 'graph'
                endpoint = "/api/dashboard/stockOutLastMonth"
                legend = true
                datalabel = true
                colors {
                    labels {
                        success = ["Never"]
                        warning = ["Stocked out <1 week"]
                        state2  = ["Stocked out 1-2 weeks"]
                        state1  = ["Stocked out 2-3 weeks"]
                        error   = ["Stocked out 3-4 weeks"]
                    }
                }
            }
            numberOfOpenPurchaseOrders {
                enabled = true
                title = "react.dashboard.numberOfOpenPurchaseOrders.title.label"
                info = "react.dashboard.numberOfOpenPurchaseOrders.info.label"
                subtitle = "react.dashboard.subtitle.purchaseOrders.label"
                numberType = 'number'
                type = 'number'
                endpoint = "/api/dashboard/openPurchaseOrdersCount"
            }
        }
    }
}

openboxes.supportLinks = [
    configureOrganizationsAndLocations: 'https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1291452471/Configure+Organizations+and+Locations',
    manageBinLocations: 'https://openboxes.atlassian.net/wiki/spaces/OBW/pages/1311572061/Manage+Bin+Locations',
    discussionForum: 'https://discuss.openboxes.com/',
    knowledgeBase: 'https://openboxes.helpscoutdocs.com/',
]

// Reset an instance

openboxes.resettingInstance.command = "wget https://raw.githubusercontent.com/openboxes/openboxes/develop/reset-database.sh | sh"

// Product configuration wizard
openboxes.configurationWizard.enabled = true
openboxes.configurationWizard.categoryOptions = [
    defaultCategories: [
        enabled: true,
        fileUrl: "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/default_OB_categories.csv",
        rootCategoryName: "ROOT",
        categoryNameColumnIndex: 0,
        parentCategoryNameColumnIndex: 1,
        title: "OpenBoxes Default Category List",
        description: "<div>A simple and flexible category tree with 25 categories organized into Equipment, Medicine, Supplies, Perishables, and Other. A good place to start for users  who aren’t sure exactly what they want. Can be edited after import. See a sample of the category tree below.</div>" +
            "<div class='category-list'>" +
            "  <ul>" +
            "    <li>Supplies" +
            "      <ul>" +
            "        <li>Office Supplies</li>" +
            "        <li>Medical Supplies" +
            "          <ul>" +
            "            <li>Dental</li>" +
            "            <li>Lab</li>" +
            "            <li>Surgical</li>" +
            "          </ul>" +
            "        </li>" +
            "      </ul>" +
            "    </li>" +
            "    <li>Equipment</li>" +
            "  </ul>" +
            "</div>",
    ],
    unspscCategories: [
        enabled: true,
        // TODO: add option to support 'classpath:'
        fileUrl: "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/UNSPSC_categories.csv",
        rootCategoryName: "ROOT", // needs to match the category from file
        categoryNameColumnIndex: 0,
        parentCategoryNameColumnIndex: 1,
        title: "UNSPSC Category List",
        description: "<div>A tree of 201 categories based on the <a target='_blank' rel='noopener noreferrer' href='https://www.unspsc.org'>United Nations Standard Products and Services Code</a>. This list takes some of the most commonly used sections and classes from the UNSPSC list, using the sections as parent categories for the classes. This is a good option for organizations who already use UNSPSC classifications or who want a very detailed tree. See a sample section of the tree below.</div>" +
            "<div class='category-list'>" +
            "  <ul>" +
            "    <li>Paper Materials and Products" +
            "      <ul>" +
            "        <li>Paper materials</li>" +
            "        <li>Paper Products</li>" +
            "        <li>Industrial use papers</li>" +
            "      </ul>" +
            "    </li>" +
            "    <li>Office Equipment and Accessories and Supplies" +
            "      <ul>" +
            "        <li>Office machines and their supplies and accessories</li>" +
            "      </ul>" +
            "    </li>" +
            "  </ul>" +
            "</div>",
    ],
    whoCategories: [
        enabled: true,
        // TODO: add option to support 'classpath:'
        fileUrl: "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/WHO_categories.csv",
        rootCategoryName: "ROOT", // needs to match the category from file
        categoryNameColumnIndex: 0,
        parentCategoryNameColumnIndex: 1,
        title: "WHO Category List",
        description: "<div>A system of medical categorization used by the <a target='_blank' rel='noopener noreferrer' href='https://www.who.int/groups/expert-committee-on-selection-and-use-of-essential-medicines/essential-medicines-lists'>WHO in their Essential Medicines List</a>. This categorization system is focused entirely on medication, and is best suited to healthcare organizations. Public health facilities that use the WHO list as the basis of their product catalogue will find that this is a good starting point to which medical items and other categories can be added. Users that want to import the WHO Essential Medicines List as their product list must select this category tree. See a sample of the tree below.</div>" +
            "<div class='category-list'>" +
            "  <ul>" +
            "    <li>Antileprosy Medicines</li>" +
            "    <li>Antimalarial Medicines" +
            "      <ul>" +
            "        <li>For Chemoprevention</li>" +
            "        <li>For Curative Treatment</li>" +
            "        <li>For Treatment of Acute Attack</li>" +
            "      </ul>" +
            "    </li>" +
            "    <li>Antimigraine Medicines</li>" +
            "  </ul>" +
            "</div>",
    ]
]

openboxes.configurationWizard.productOptions = [
    whoProducts: [
        enabled: true,
        // TODO: add option to support 'classpath:'
        fileUrl: "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/WHO_products.csv",
        title: "WHO product list",
        description: "<div>This selection will import the WHO Essential Medicines List (Sept 2021 version) into your instance as products.  Public health facilities that use the WHO list as the basis of their product catalogue will find that this is a good starting point to building out their products in OpenBoxes.</div>" +
            "<div class='my-3'>In order to import this product list, you must have selected the corresponding WHO category tree in the previous step. This product list will not work with any other category tree. Go to <a target='_blank' rel='noopener noreferrer' href='https://list.essentialmeds.org/'>list.essentialmeds.org</a> to view the full WHO list that will be imported.</div>",
    ]
]

openboxes.configurationWizard.listOfDemoData = [
    title: "Summary of data to be loaded",
    description: "<ul>" +
        "  <li>57 products across 21 categories</li>" +
        "  <li>18 locations including 3 depots, 5 suppliers, and 10 dispensaries</li>" +
        "  <li>Inventory for 3 depots</li>" +
        "  <li>12 sample people and users</li>" +
        "  <li>2 sample stock lists</li>" +
        "<ul>",
]

openboxes {
    configurationWizard {
        dataInit {
            locations {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/locations.csv"
            }
            locationGroups {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/locationGroups.csv"
            }
            organizations {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/organizations.csv"
            }
            binLocations {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/binLocations.csv"
            }
            categories {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/default_OB_categories.csv"
            }
            products {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/products.csv"
            }
            productCatalog {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/productCatalog.csv"
            }
            productCatalogItems {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/productCatalogItems.csv"
            }
            productSuppliers {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/productSuppliers.csv"
            }
            mainWarehouseInventory {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/mainWarehouseInventory.csv"
                warehouseName = "Main Warehouse"
            }
            bostonWarehouseInventory {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/bostonWarehouseInventory.csv"
                warehouseName = "Boston Warehouse"
            }
            chicagoWarehouseInventory {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/chicagoWarehouseInventory.csv"
                warehouseName = "Chicago Warehouse"
            }
            inventoryLevels {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/inventoryLevels.csv"
                warehouseName = "Main Warehouse"
            }
            users {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/users.csv"
            }
            persons {
                enabled = true
                url = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/persons.csv"
            }
            chicagoStocklist {
                enabled = true
                templateUrl = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/chicagoStocklistTemplate.csv"
                itemsUrl = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/chicagoStocklistItems.csv"
            }
            bostonStocklist {
                enabled = true
                templateUrl = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/bostonStocklistTemplate.csv"
                itemsUrl = "https://raw.githubusercontent.com/openboxes/openboxes/develop/grails-app/conf/templates/configuration/bostonStocklistItems.csv"
            }
        }
    }
}

// Order number prefix for bin replenishment case
openboxes.stockTransfer.binReplenishment.prefix = Constants.REPLENISHMENT_PREFIX
