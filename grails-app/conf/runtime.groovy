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

openboxes {
    megamenu {
        dashboard {
            enabled = true
            label = "dashboard.label"
            defaultLabel = "Dashboard"
            href = "/dashboard/index"
        }
        analytics {
            enabled = true
            requiredRoles = [RoleType.ROLE_ADMIN, RoleType.ROLE_SUPERUSER]
            label = "analytics.label"
            defaultLabel = "Analytics"
            menuItems = [
                // TODO: Add option to include label 'beta'
                [label: "inventory.browse.label", defaultLabel: "Browse Inventory", href: "/inventoryBrowser"],
                [label: "inventory.snapshots.label", defaultLabel: "Inventory Snapshots", href: "/snapshot"],
                [label: "report.consumption.label", defaultLabel: "Consumption Report", href: "/consumption"]
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
                        [label: "inventory.browse.label", defaultLabel: "Browse Inventory", href: "/inventory/browse?resetSearch=true"],
                        // TODO: (Future improvement) Probably further options should be generated dynamicaly (with item count in bracket)...
                    ],
                ],
                [
                    label: "inventory.manage.label",
                    defaultLabel: "Manage Inventory",
                    menuItems: [
                        [label: "inventory.manage.label", defaultLabel: "Manage Inventory", href: "/inventory/manage"],
                        [label: "inventory.import.label", defaultLabel: "Import Inventory", href: "/batch/importData?type=inventory&execution=e1s1"]
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
                        [label: "inbound.create.label", defaultLabel: "Create Inbound Movement", href: "/stockMovement/createInbound?direction=INBOUND"],
                        [label: "stockRequest.create.label", defaultLabel: "Create Stock Request", href: "/stockMovement/createRequest"],
                        [label: "inbound.list.label", defaultLabel: "List Inbound Movements", href: "/stockMovement/list?direction=INBOUND"]
                    ]
                ],
                [
                    label: "purchaseOrders.label",
                    defaultLabel: "Purchase Orders",
                    menuItems: [
                        [label: "order.createPurchase.label", defaultLabel: "Create Purchase Order", href: "/purchaseOrderWorkflow/index"],
                        [label: "order.listPurchase.label", defaultLabel: "List Purchase Orders", href: "/order/list?orderTypeCode=PURCHASE_ORDER"]
                    ]
                ],
                [
                    label: "putAways.label",
                    defaultLabel: "Putaways",
                    menuItems: [
                        [label: "react.putAway.createPutAway.label", defaultLabel: "Create Putaway", href: "/putAway/index"],
                        [label: "react.putAway.list.label", defaultLabel: "List Putaways", href: "/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING"]
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
                        [label: "outbound.create.label", defaultLabel: "Create Outbound Movements", href: "/stockMovement/createOutbound?direction=OUTBOUND"],
                        [label: "outbound.list.label", defaultLabel: "List Outbound Movements", href: "/stockMovement/list?direction=OUTBOUND"]
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
                        [label: "report.inStockReport.label", defaultLabel: "In Stock Report", href: "/inventory/listInStock"],
                        [label: "report.binLocation.label", defaultLabel: "Bin Location Report", href: "/report/showBinLocationReport"],
                        [label: "report.expiredStockReport.label", defaultLabel: "Expired Stock Report", href: "/inventory/listExpiredStock"],
                        [label: "report.expiringStockReport.label", defaultLabel: "Expiring Stock Report", href: "/inventory/listExpiringStock"],
                        [label: "report.inventoryByLocationReport.label", defaultLabel: "Inventory By Location Report", href: "/report/showInventoryByLocationReport"],
                        [label: "report.cycleCount.label", defaultLabel: "Cycle Count Report", href: "/cycleCount/exportAsCsv"],
                        [label: "report.baselineQoH.label", defaultLabel: "Baseline QoH Report", href: "/inventory/show"],
                        [label: "report.order.label", defaultLabel: "Order Report", href: "/report/showOnOrderReport"]
                    ]
                ],
                [
                    label: "report.transactionReports.label",
                    defaultLabel: "Transaction Reports",
                    menuItems: [
                        [label: "report.showTransactionReport.label", defaultLabel: "Transaction Report", href: "/report/showTransactionReport"],
                        [label: "report.consumption.label", defaultLabel: "Consumption Report", href: "/consumption/show"]
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
                        [label: "export.requisitionItems.label", defaultLabel: "Export requisition items", href: "/requisitionItem/listCanceled"],
                        [label: "export.binLocations.label", defaultLabel: "Export bin locations", href: "/report/exportBinLocation?downloadFormat=csv"],
                        [label: "export.productDemand.label", defaultLabel: "Export product demand", href: "/report/exportDemandReport?downloadFormat=csv"]
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
                        [label: "attributes.label", defaultLabel: "Attributes", href: "/attribute"],
                        [label: "catalogs.label", defaultLabel: "Catalogs", href: "/productCatalog"],
                        [label: "categories.label", defaultLabel: "Categories", href: "/category/tree"],
                        [label: "components.label", defaultLabel: "Components", href: "/productComponent"],
                        [label: "productGroups.label", defaultLabel: "Generic Products", href: "/productGroup"],
                        [label: "inventoryLevels.label", defaultLabel: "Inventory Levels", href: "/inventoryLevel"]
                    ]
                ],
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "products.label", defaultLabel: "Products", href: "/product"],
                        [label: "productsSources.label", defaultLabel: "Products Sources", href: "/productSupplier"],
                        [label: "productsAssociations.label", defaultLabel: "Products Associations", href: "/productAssociation"],
                        [label: "tags.label", defaultLabel: "Tags", href: "/tag"],
                        [label: "unitOfMeasure.label", defaultLabel: "Unit of Measure", href: "/unitOfMeasure"],
                        [label: "unitOfMeasureClass.label", defaultLabel: "Uom Class", href: "/unitOfMeasureClass"],
                        [label: "unitOfMeasureConversion.label", defaultLabel: "Uom Conversion", href: "/unitOfMeasureConversion"]
                    ]
                ],
                [
                    label: "", // No label
                    defaultLabel: "", // No label
                    menuItems: [
                        [label: "createProduct.label", defaultLabel: "Create new product", href: "/product/create"],
                        [label: "product.batchEdit.label", defaultLabel: "Batch edit product", href: "/product/batchEdit"],
                        [label: "product.import.label", defaultLabel: "Import products", href: "/product/importAsCsv"],
                        [label: "product.exportAsCsv.label", defaultLabel: "Export products", href: "/product/exportAsCsv"],
                        [label: "import.inventory.label", defaultLabel: "Import Inventory", href: "/batch/importData?type=inventory"],
                        [label: "import.inventoryLevel.label", defaultLabel: "Import Inventory Level", href: "/batch/importData?type=inventoryLevel"]
                    ]
                ]
            ]
        }
        stocklists {
            enabled = true
            label = "stocklists.label"
            defaultLabel = "Stocklists"
            menuItems = [
                [label: "requisitionTemplates.list.label", defaultLabel: "List stock lists", href: "/requisitionTemplate"],
                [label: "requisitionTemplates.create.label", defaultLabel: "Create stock list", href: "/requisitionTemplate/create"],
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
                        [label: "default.settings.label", defaultLabel: "Settings", href: "/admin/showSettings"],
                        [label: "dataMigration.label", defaultLabel: "Data Migration", href: "/migration/index"],
                        [label: "console.label", defaultLabel: "Console", href: "/console/index"],
                        [label: "cache.label", defaultLabel: "Cache", href: "/admin/cache"],
                        [label: "email.label", defaultLabel: "Email", href: "/admin/sendMail"],
                        [label: "importData.label", defaultLabel: "Import Data", href: "/batch/importData"],
                        [label: "localization.label", defaultLabel: "Localization", href: "/localization"]
                    ]
                ],
                [
                    label: "locations.label",
                    defaultLabel: "Locations",
                    menuItems: [
                        [label: "locations.label", defaultLabel: "Locations", href: "/location"],
                        [label: "locationGroups.label", defaultLabel: "Location Groups", href: "/locationGroup"],
                        [label: "locationTypes.label", defaultLabel: "Location Types", href: "/locationType"]
                    ]
                ],
                [
                    label: "transactions.label",
                    defaultLabel: "Transactions",
                    menuItems: [
                        [label: "transactionsTypes.label", defaultLabel: "Transactions Types", href: "/transactionType"],
                        [label: "transactions.label", defaultLabel: "Transactions", href: "/inventory/listAllTransactions"],
                        [label: "transaction.add.label", defaultLabel: "Add transaction", href: "/inventory/editTransaction"],
                        [label: "importInventory.label", defaultLabel: "Import Inventory", href: "/batch/importData?type=inventory"],
                        [label: "importInventoryLevel.label", defaultLabel: "Import Inventory Level", href: "/batch/importData?type=inventoryLevel"]
                    ]
                ],
                [
                    label: "parties.label",
                    defaultLabel: "Parties",
                    menuItems: [
                        [label: "partyTypes.label", defaultLabel: "Party types", href: "/partyType"],
                        [label: "partyRoles.label", defaultLabel: "Party roles", href: "/partyRole"],
                        [label: "organizations.label", defaultLabel: "Organizations", href: "/organization"],
                        [label: "person.list.label", defaultLabel: "People", href: "/person"],
                        [label: "users.label", defaultLabel: "Users", href: "/user"],
                        [label: "roles.label", defaultLabel: "Roles", href: "/role"]
                    ]
                ],
                [
                    label: "default.other.label",
                    defaultLabel: "Other",
                    menuItems: [
                        [label: "containerTypes.label", defaultLabel: "Container Types", href: "/containerType"],
                        [label: "documents.label", defaultLabel: "Documents", href: "/document"],
                        [label: "documentTypes.label", defaultLabel: "Document Types", href: "/documentType"],
                        [label: "eventTypes.label", defaultLabel: "Event Types", href: "/eventType"],
                        [label: "paymentMethodTypes.label", defaultLabel: "Payment Method Types", href: "/paymentMethodType"],
                        [label: "paymentTerms.label", defaultLabel: "Payment Terms", href: "/paymentTerm"],
                        [label: "shippers.label", defaultLabel: "Shippers", href: "/shipper"],
                        [label: "shipmentWorkflows.label", defaultLabel: "Shipment Workflows", href: "/shipmentWorkflow"]
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
                [label: "tableroNuevo.label", defaultLabel: "Tablero Nuevo", href: "/tablero"],
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
                    endpoint = "/apitablero/getInProgressPutaways"
                    archived = ['inventory', 'transaction']
                    order = 4
                }
                inventoryByLotAndBin {
                    endpoint = "/apitablero/getInventoryByLotAndBin"
                    archived = ['inventory', 'transaction']
                    order = 1
                }
                inProgressShipments {
                    endpoint = "/apitablero/getInProgressShipments"
                    archived = ['inventory', 'transaction']
                    order = 3
                }
                receivingBin {
                    endpoint = "/apitablero/getReceivingBin"
                    archived = ['transaction']
                    order = 2
                }
                itemsInventoried {
                    endpoint = "/apitablero/getItemsInventoried"
                    archived = ['personal', 'warehouse', 'transaction']
                    order = 5
                }
                defaultBin {
                    endpoint = "/apitablero/getDefaultBin"
                    archived = ['personal', 'warehouse', 'transaction']
                    order = 6
                }
                negativeInventory {
                    endpoint = "/apitablero/getProductWithNegativeInventory"
                    archived = ['personal', 'warehouse', 'transaction']
                    order = 7
                }
                expiredStock {
                    endpoint = "/apitablero/getExpiredProductsInStock"
                    archived = ['personal', 'warehouse', 'transaction']
                    order = 8
                }
            }
            graph {
                inventorySummary {
                    endpoint = "/apitablero/getInventorySummary"
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
                    endpoint = "/apitablero/getExpirationSummary"
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
                    endpoint = "/apitablero/getIncomingStock"
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
                    endpoint = "/apitablero/getOutgoingStock"
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
                    endpoint = "/apitablero/getReceivedStockMovements"
                    archived = ['personal', 'warehouse', 'inventory']
                    filter = true
                    stacked = true
                    datalabel = true
                    order = 7
                }
                discrepancy {
                    endpoint = "/apitablero/getDiscrepancy"
                    archived = ['inventory', 'transaction']
                    filter = true
                    order = 6
                }
                delayedShipments {
                    endpoint = "/apitablero/getDelayedShipments"
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
                    endpoint = "/apitablero/getSentStockMovements"
                    archived = ['personal', 'warehouse', 'inventory']
                    filter = true
                    stacked = true
                    datalabel = true
                    order = 8
                }
            }
        }
    }
}
