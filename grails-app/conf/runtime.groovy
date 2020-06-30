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

openboxes {
    megamenu {
        dashboard {
            enabled = true
            label = "dashboard.label"
            defaultLabel = "Dashboard"
            href = "/${appName}/dashboard/index"
        }
        analytics {
            enabled = true
            requiredRoles = [RoleType.ROLE_ADMIN]
            label = "analytics.label"
            defaultLabel = "Analytics"
            menuItems = [
                // TODO: Add option to include label 'beta'
                [label: "inventory.browse.label", defaultLabel: "Browse Inventory", href: "/${appName}/inventoryBrowser/index"],
                [label: "inventory.snapshots.label", defaultLabel: "Inventory Snapshots", href: "/${appName}/snapshot/list"],
                [label: "report.consumption.label", defaultLabel: "Consumption Report", href: "/${appName}/consumption/list"]
            ]
        }
        inventory {
            enabled = true
            label = "inventory.label"
            defaultLabel = "Inventory"
            requiredActivities = [ActivityCode.MANAGE_INVENTORY]
            subsections = [
                [
                    label: "inventory.browse.label",
                    defaultLabel: "Browse Inventory",
                    menuItems: [
                        [label: "inventory.browse.label", defaultLabel: "Browse Inventory", href: "/${appName}/inventory/browse?resetSearch=true"],
                        // TODO: (Future improvement) Probably further options should be generated dynamicaly (with item count in bracket)...
                    ],
                ],
                [
                    label: "inventory.manage.label",
                    defaultLabel: "Manage Inventory",
                    menuItems: [
                        [label: "inventory.manage.label", defaultLabel: "Manage Inventory", href: "/${appName}/inventory/manage"],
                        [label: "inventory.import.label", defaultLabel: "Import Inventory", href: "/${appName}/batch/importData?type=inventory&execution=e1s1"]
                    ]
                ]
            ]
        }
        inbound {
            enabled = true
            label = "inbound.label"
            defaultLabel = "Inbound"
            subsections = [
                [
                    label: "stockMovement.label",
                    defaultLabel: "Stock Movement",
                    menuItems: [
                        [label: "inbound.create.label", defaultLabel: "Create Inbound Movement", href: "/${appName}/stockMovement/createInbound?direction=INBOUND"],
                        [label: "stockRequest.create.label", defaultLabel: "Create Stock Request", href: "/${appName}/stockMovement/createRequest"],
                        [label: "inbound.list.label", defaultLabel: "List Inbound Movements", href: "/${appName}/stockMovement/list?direction=INBOUND"]
                    ]
                ],
                [
                    label: "purchaseOrders.label",
                    defaultLabel: "Purchase Orders",
                    menuItems: [
                        [label: "order.createPurchase.label", defaultLabel: "Create Purchase Order", href: "/${appName}/purchaseOrderWorkflow/index"],
                        [label: "order.listPurchase.label", defaultLabel: "List Purchase Orders", href: "/${appName}/order/list?orderTypeCode=PURCHASE_ORDER"]
                    ]
                ],
                [
                    label: "putAways.label",
                    defaultLabel: "Putaways",
                    menuItems: [
                        [label: "react.putAway.createPutAway.label", defaultLabel: "Create Putaway", href: "/${appName}/putAway/index"],
                        [label: "react.putAway.list.label", defaultLabel: "List Putaways", href: "/${appName}/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING"]
                    ]
                ]
            ]
        }
        outbound {
            enabled = true
            label = "outbound.label"
            defaultLabel = "Outbound"
            subsections = [
                [
                    label: "",
                    defaultLabel: "Stock Movement",
                    menuItems: [
                        [label: "outbound.create.label", defaultLabel: "Create Outbound Movements", href: "/${appName}/stockMovement/createOutbound?direction=OUTBOUND"],
                        [label: "outbound.list.label", defaultLabel: "List Outbound Movements", href: "/${appName}/stockMovement/list?direction=OUTBOUND"]
                    ]
                ]
            ]
        }
        reporting {
            enabled = true
            label = "reporting.label"
            defaultLabel = "Reporting"
            subsections = [
                [
                    label: "report.inventoryReports.label",
                    defaultLabel: "Inventory Reports",
                    menuItems: [
                        [label: "report.inStockReport.label", defaultLabel: "In Stock Report", href: "/${appName}/inventory/listInStock"],
                        [label: "report.binLocation.label", defaultLabel: "Bin Location Report", href: "/${appName}/report/showBinLocationReport"],
                        [label: "report.expiredStockReport.label", defaultLabel: "Expired Stock Report", href: "/${appName}/inventory/listExpiredStock"],
                        [label: "report.expiringStockReport.label", defaultLabel: "Expiring Stock Report", href: "/${appName}/inventory/listExpiringStock"],
                        [label: "report.inventoryByLocationReport.label", defaultLabel: "Inventory By Location Report", href: "/${appName}/report/showInventoryByLocationReport"],
                        [label: "report.cycleCount.label", defaultLabel: "Cycle Count Report", href: "/${appName}/cycleCount/exportAsCsv"],
                        [label: "report.baselineQoH.label", defaultLabel: "Baseline QoH Report", href: "/${appName}/inventory/show"],
                        [label: "report.order.label", defaultLabel: "Order Report", href: "/${appName}/report/showOnOrderReport"]
                    ]
                ],
                [
                    label: "report.transactionReports.label",
                    defaultLabel: "Transaction Reports",
                    menuItems: [
                        [label: "report.showTransactionReport.label", defaultLabel: "Transaction Report", href: "/${appName}/report/showTransactionReport"],
                        [label: "report.consumption.label", defaultLabel: "Consumption Report", href: "/${appName}/consumption/show"]
                    ]
                ],
                [
                    label: "dataExports.label",
                    defaultLabel: "Data Exports",
                    menuItems: [
                        [label: "product.exportAsCsv.label", defaultLabel: "Export products", href: "/${appName}/product/exportAsCsv"],
                        [label: "export.productSources.label", defaultLabel: "Export product sources", href: "/${appName}/productSupplier/export"],
                        [label: "export.latestInventory.label", defaultLabel: "Export latest inventory date", href: "/${appName}/inventory/exportLatestInventoryDate"],
                        [label: "export.inventoryLevels.label", defaultLabel: "Export inventory levels", href: "/${appName}/inventoryLevel/export"],
                        [label: "export.requisitions.label", defaultLabel: "Export requisitions", href: "/${appName}/requisition/export"],
                        [label: "export.requisitionItems.label", defaultLabel: "Export requisition items", href: "/${appName}/requisitionItem/listCanceled"],
                        [label: "export.binLocations.label", defaultLabel: "Export bin locations", href: "/${appName}/report/exportBinLocation?downloadFormat=csv"],
                        [label: "export.productDemand.label", defaultLabel: "Export product demand", href: "/${appName}/report/exportDemandReport?downloadFormat=csv"]
                    ]
                ]
            ]
        }
        products {
            enabled = true
            label = "products.label"
            defaultLabel = "Products"
            subsections = [
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "attributes.label", defaultLabel: "Attributes", href: "/${appName}/attribute/list"],
                        [label: "catalogs.label", defaultLabel: "Catalogs", href: "/${appName}/productCatalog/list"],
                        [label: "categories.label", defaultLabel: "Categories", href: "/${appName}/category/tree"],
                        [label: "components.label", defaultLabel: "Components", href: "/${appName}/productComponent/list"],
                        [label: "productGroups.label", defaultLabel: "Generic Products", href: "/${appName}/productGroup/list"],
                        [label: "inventoryLevels.label", defaultLabel: "Inventory Levels", href: "/${appName}/inventoryLevel/list"]
                    ]
                ],
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "products.label", defaultLabel: "Products", href: "/${appName}/product/list"],
                        [label: "productsSources.label", defaultLabel: "Products Sources", href: "/${appName}/productSupplier/list"],
                        [label: "productsAssociations.label", defaultLabel: "Products Associations", href: "/${appName}/productAssociation/list"],
                        [label: "tags.label", defaultLabel: "Tags", href: "/${appName}/tag/list"],
                        [label: "unitOfMeasure.label", defaultLabel: "Unit of Measure", href: "/${appName}/unitOfMeasure/list"],
                        [label: "unitOfMeasureClass.label", defaultLabel: "Uom Class", href: "/${appName}/unitOfMeasureClass/list"],
                        [label: "unitOfMeasureConversion.label", defaultLabel: "Uom Conversion", href: "/${appName}/unitOfMeasureConversion/list"]
                    ]
                ],
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "createProduct.label", defaultLabel: "Create new product", href: "/${appName}/product/create"],
                        [label: "product.batchEdit.label", defaultLabel: "Batch edit product", href: "/${appName}/product/batchEdit"],
                        [label: "product.import.label", defaultLabel: "Import products", href: "/${appName}/product/importAsCsv"],
                        [label: "product.exportAsCsv.label", defaultLabel: "Export products", href: "/${appName}/product/exportAsCsv"],
                        [label: "import.inventory.label", defaultLabel: "Import Inventory", href: "/${appName}/batch/importData?type=inventory"],
                        [label: "import.inventoryLevel.label", defaultLabel: "Import Inventory Level", href: "/${appName}/batch/importData?type=inventoryLevel"]
                    ]
                ]
            ]
        }
        requisitionTemplate {
            enabled = true
            label = "stocklists.label"
            defaultLabel = "Stocklists"
            menuItems = [
                [label: "requisitionTemplates.list.label", defaultLabel: "List stock lists", href: "/${appName}/requisitionTemplate/list"],
                [label: "requisitionTemplates.create.label", defaultLabel: "Create stock list", href: "/${appName}/requisitionTemplate/create"],
            ]
        }
        configuration {
            enabled = true
            label = "configuration.label"
            defaultLabel = "Configuration"
            subsections = [
                [
                    label: "admin.label",
                    defaultLabel: "Administration",
                    menuItems: [
                        [label: "default.settings.label", defaultLabel: "Settings", href: "/${appName}/admin/showSettings"],
                        [label: "dataMigration.label", defaultLabel: "Data Migration", href: "/${appName}/migration/index"],
                        [label: "console.label", defaultLabel: "Console", href: "/${appName}/console/index"],
                        [label: "cache.label", defaultLabel: "Cache", href: "/${appName}/admin/cache"],
                        [label: "email.label", defaultLabel: "Email", href: "/${appName}/admin/sendMail"],
                        [label: "importData.label", defaultLabel: "Import Data", href: "/${appName}/batch/importData"],
                        [label: "localization.label", defaultLabel: "Localization", href: "/${appName}/localization/list"]
                    ]
                ],
                [
                    label: "locations.label",
                    defaultLabel: "Locations",
                    menuItems: [
                        [label: "locations.label", defaultLabel: "Locations", href: "/${appName}/location/list"],
                        [label: "locationGroups.label", defaultLabel: "Location groups", href: "/${appName}/locationGroup/list"],
                        [label: "locationTypes.label", defaultLabel: "Location types", href: "/${appName}/locationType/list"]
                    ]
                ],
                [
                    label: "transactions.label",
                    defaultLabel: "Transactions",
                    menuItems: [
                        [label: "transactionsTypes.label", defaultLabel: "Transactions Types", href: "/${appName}/transactionType"],
                        [label: "transactions.label", defaultLabel: "Transactions", href: "/${appName}/inventory/listAllTransactions"],
                        [label: "transaction.add.label", defaultLabel: "Add transaction", href: "/${appName}/inventory/editTransaction"],
                        [label: "importInventory.label", defaultLabel: "Import Inventory", href: "/${appName}/batch/importData?type=inventory"],
                        [label: "importInventoryLevel.label", defaultLabel: "Import Inventory Level", href: "/${appName}/batch/importData?type=inventoryLevel"]
                    ]
                ],
                [
                    label: "parties.label",
                    defaultLabel: "Parties",
                    menuItems: [
                        [label: "partyTypes.label", defaultLabel: "Party types", href: "/${appName}/partyType/list"],
                        [label: "partyRoles.label", defaultLabel: "Party roles", href: "/${appName}/partyRole/list"],
                        [label: "organizations.label", defaultLabel: "Organizations", href: "/${appName}/organization/list"],
                        [label: "person.list.label", defaultLabel: "People", href: "/${appName}/person/list"],
                        [label: "users.label", defaultLabel: "Users", href: "/${appName}/user/list"],
                        [label: "roles.label", defaultLabel: "Roles", href: "/${appName}/role/list"]
                    ]
                ],
                [
                    label: "default.other.label",
                    defaultLabel: "Other",
                    menuItems: [
                        [label: "containerTypes.label", defaultLabel: "Container Types", href: "/${appName}/containerType/list"],
                        [label: "documents.label", defaultLabel: "Documents", href: "/${appName}/document/list"],
                        [label: "documentTypes.label", defaultLabel: "Document Types", href: "/${appName}/documentType/list"],
                        [label: "eventTypes.label", defaultLabel: "Event Types", href: "/${appName}/eventType/list"],
                        [label: "paymentMethodTypes.label", defaultLabel: "Payment Method Types", href: "/${appName}/paymentMethodType/list"],
                        [label: "paymentTerms.label", defaultLabel: "Payment Terms", href: "/${appName}/paymentTerm/list"],
                        [label: "shippers.label", defaultLabel: "Shippers", href: "/${appName}/shipper/list"],
                        [label: "shipmentWorkflows.label", defaultLabel: "Shipment Workflows", href: "/${appName}/shipmentWorkflow/list"]
                    ]
                ]
            ]
        }
        customLinks {
            enabled = false
            label = "customLinks.label"
            defaultLabel = "Custom Links"
            menuItems = [
                [label: "requestItemCreation.label", defaultLabel: "Request Item Creation", href: "", target: "_blank"], // Fill in href
                [label: "trainingVideos.label", defaultLabel: "Training Videos", href: "", target: "_blank"], // Fill in href
                [label: "tableroNuevo.label", defaultLabel: "Tablero Nuevo", href: "/${appName}/tablero"],
            ]
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

        // deprecated megamenu configuration
        requisitions {
            enabled = false
            label = "requisitions.label"
            defaultLabel = "Requisitions"
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
    }
}

openboxes {
    tablero {
        enabled = true
        configurations {
            personal = "My Dashboard"
            warehouse = "Warehouse Management"
            inventory = "Inventory Management"
            transaction = "Transaction History"
        }
        endpoints {
            number {
                inProgressPutaways {
                    endpoint = "/${appName}/apitablero/getInProgressPutaways"
                    archived = ['inventory', 'transaction']
                    order = 4
                }
                inventoryByLotAndBin {
                    endpoint = "/${appName}/apitablero/getInventoryByLotAndBin"
                    archived = ['inventory', 'transaction']
                    order = 1
                }
                inProgressShipments {
                    endpoint = "/${appName}/apitablero/getInProgressShipments"
                    archived = ['inventory', 'transaction']
                    order = 3
                }
                receivingBin {
                    endpoint = "/${appName}/apitablero/getReceivingBin"
                    archived = ['transaction']
                    order = 2
                }
                itemsInventoried {
                    endpoint = "/${appName}/apitablero/getItemsInventoried"
                    archived = ['personal', 'warehouse', 'transaction']
                    order = 5
                }
                defaultBin {
                    endpoint = "/${appName}/apitablero/getDefaultBin"
                    archived = ['personal', 'warehouse', 'transaction']
                    order = 6
                }
                negativeInventory {
                    endpoint = "/${appName}/apitablero/getProductWithNegativeInventory"
                    archived = ['personal', 'warehouse', 'transaction']
                    order = 7
                }
                expiredStock {
                    endpoint = "/${appName}/apitablero/getExpiredProductsInStock"
                    archived = ['personal', 'warehouse', 'transaction']
                    order = 8
                }
            }
            graph {
                inventorySummary {
                    endpoint = "/${appName}/apitablero/getInventorySummary"
                    archived = ['inventory', 'transaction']
                    filter = false
                    datalabel = true
                    order = 1
                    colors {
                        labels {
                            success = ["In stock"]
                            warning = ["Above maximum", "Below reorder", "Below minimum"]
                            error = ["No longer in stock"]
                        }
                    }
                }
                expirationSummary {
                    endpoint = "/${appName}/apitablero/getExpirationSummary"
                    archived = ['inventory', 'transaction']
                    filter = true
                    order = 2
                    colors {
                        datasets {
                            state6 = ["Expiration(s)"]
                        }
                        labels {
                            state5 = ["today", "within 30 days", "within 90 days", "within 180 days", "within 360 days"]
                        }
                    }
                }
                incomingStock {
                    endpoint = "/${appName}/apitablero/getIncomingStock"
                    archived = ['inventory', 'transaction']
                    filter = false
                    archived = []
                    order = 3
                    colors {
                        datasets {
                            state6 = ["first"]
                            state7 = ["second"]
                            state8 = ["third"]
                        }
                    }
                }
                outgoingStock {
                    endpoint = "/${appName}/apitablero/getOutgoingStock"
                    archived = ['inventory', 'transaction']
                    filter = false
                    order = 4
                    colors {
                        datasets {
                            success = ["first"]
                            warning = ["second"]
                            error = ["third"]
                        }
                    }
                }
                receivedStockMovements {
                    endpoint = "/${appName}/apitablero/getReceivedStockMovements"
                    archived = ['personal', 'warehouse', 'inventory']
                    filter = true
                    stacked = true
                    datalabel = true
                    order = 7
                }
                discrepancy {
                    endpoint = "/${appName}/apitablero/getDiscrepancy"
                    archived = ['inventory', 'transaction']
                    filter = true
                    order = 6
                }
                delayedShipments {
                    endpoint = "/${appName}/apitablero/getDelayedShipments"
                    archived = ['transaction']
                    filter = false
                    order = 5
                    colors {
                        datasets {
                            state5 = ["first"]
                            state4 = ["second"]
                            state3 = ["third"]
                        }
                    }
                }
                sentStockMovements {
                    endpoint = "/${appName}/apitablero/getSentStockMovements"
                    archived = ['personal', 'warehouse', 'inventory']
                    filter = true
                    stacked = true
                    datalabel = true
                    order = 8
                }
                lossCausedByExpiry {
                    endpoint = "/${appName}/apitablero/getLossCausedByExpiry"
                    archived = ['personal', 'warehouse', 'inventory']
                    filter = true
                    stacked = true
                    order = 9
                    colors {
                        datasets {
                            success = ["Inventory value not expired last day of month"]
                            warning = ["Inventory value expired last day of month"]
                            error = ["Inventory value removed due to expiry"]
                        }
                    }
                }
                productsInventoried {
                    endpoint = "/${appName}/apitablero/getProductsInventoried"
                    archived = ['personal', 'warehouse', 'transaction']
                    filter = false
                    order = 10
                    colors {
                        datasets {
                            state6 = ["first"]
                            state7 = ["second"]
                            state8 = ["third"]
                        }
                    }
                }
            }
        }
    }
}
