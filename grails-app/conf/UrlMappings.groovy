import grails.validation.ValidationException
import org.apache.http.auth.AuthenticationException
import org.hibernate.ObjectNotFoundException

/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
class UrlMappings {
    static mappings = {

        "/snapshot/$action?"(controller: "inventorySnapshot")

        "/inventoryItem/delete/$id**?" {
            controller = "inventoryItem"
            action = "delete"
        }

        "/stockMovement/$action/$id**?" {
            controller = "stockMovement"
        }

        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        // REST APIs with complex resource names or subresources

        "/api/categories"(parseRequest: true) {
            controller = { "categoryApi" }
            action = [GET: "list", POST: "save"]
        }
        "/api/categories/$id"(parseRequest: true) {
            controller = { "categoryApi" }
            action = [GET: "read", POST: "save", PUT: "save", DELETE: "delete"]
        }
        "/api/products/$id/$action" {
            controller = { "productApi" }
        }

        "/api/locations/locationTypes" {
            controller = { "locationApi" }
            action = [GET: "locationTypes"]
        }

        "/api/locations/supportedActivities" {
            controller = { "locationApi" }
            action = [GET: "supportedActivities"]
        }

        "/api/locations/binLocations/template" {
            controller = { "locationApi" }
            action = [GET: "downloadBinLocationTemplate"]
        }

        "/api/locations/$id/binLocations/import"(parseRequest: true) {
            controller = { "locationApi" }
            action = [POST: "importBinLocations"]
        }

        "/api/locations/template" {
            controller = { "locationApi" }
            action = [GET: "downloadTemplate"]
        }

        "/api/locations/importCsv" {
            controller = { "locationApi" }
            action = [POST: "importCsv"]
        }

        "/api/locations/$id/$action" {
            controller = { "locationApi" }
        }

        "/api/config/data/demo"(parseRequest: true) {
            controller = "loadDataApi"
            action = [GET: "load"]
        }

        "/api/helpscout/configuration" {
            controller = { "helpScoutApi" }
            action = [GET: "configuration"]
        }

        // Stock Movement Item API

        "/api/stockMovementItems"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [GET: "list"]
        }

        "/api/stockMovementItems/$id"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [GET: "read"]
        }

        "/api/stockMovementItems/$id/details"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [GET: "details"]
        }

        "/api/stockMovementItems/$id/updatePicklist"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [POST: "updatePicklist"]
        }

        "/api/stockMovementItems/$id/createPicklist"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [POST: "createPicklist"]
        }

        "/api/stockMovementItems/$id/clearPicklist"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [POST: "clearPicklist"]
        }

        "/api/stockMovementItems/$id/substituteItem"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [POST: "substituteItem"]
        }

        "/api/stockMovementItems/$id/revertItem"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [POST: "revertItem"]
        }

        "/api/stockMovementItems/$id/cancelItem"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [POST: "cancelItem"]
        }

        "/api/stockMovementItems/$id/removeItem"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [DELETE: "eraseItem"]
        }

        "/api/stockMovements/$id/stockMovementItems"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [GET: "getStockMovementItems"]
        }

        "/api/stockMovements/$id/substitutionItems"(parseRequest: true) {
            controller = "stockMovementItemApi"
            action = [GET: "getSubstitutionItems"]
        }

        // Stock Movement API

        "/api/stockMovements/$id/removeAllItems"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [DELETE: "removeAllItems"]
        }

        "/api/stockMovements/$id/reviseItems"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "reviseItems"]
        }

        "/api/stockMovements/$id/updateItems"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "updateItems"]
        }

        "/api/stockMovements/$id/updateInventoryItems"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "updateInventoryItems"]
        }

        "/api/stockMovements/$id/updateShipmentItems"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "updateShipmentItems"]
        }

        "/api/stockMovements/$id/updateRequisition"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "updateRequisition"]
        }

        "/api/stockMovements/$id/updateShipment"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "updateShipment"]
        }

        "/api/stockMovements/$id/validatePicklist"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "validatePicklist"]
        }

        "/api/stockMovements/importPickListItems/$id"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "importPickListItems"]
        }

        "/api/stockMovements/exportPickListItems/$id"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "exportPickListItems"]
        }

        "/api/stockMovements/createPickList/$id"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "createPickList"]
        }

        "/api/stockMovements/pendingRequisitionDetails"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "getPendingRequisitionDetails"]
        }

        "/api/stockMovements/list"{
            controller = "stockMovementApi"
            action = [GET: "list"]
        }

        "/api/stockMovements/$id/updateAdjustedItems"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "updateAdjustedItems"]
        }

        // TODO Remove it later once all inbound types are shipment
        "/api/stockMovements/createCombinedShipments"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "createCombinedShipments"]
        }

        // Partial Receiving API

        "/api/partialReceiving"(parseRequest: true) {
            controller = "partialReceivingApi"
            action = [GET: "list", POST: "create"]
        }

        "/api/partialReceiving/$id"(parseRequest: true) {
            controller = "partialReceivingApi"
            action = [GET: "read", POST: "update"]
        }

        "/api/partialReceiving/importCsv/$id"(parseRequest: true) {
            controller = "partialReceivingApi"
            action = [POST: "importCsv"]
        }

        "/api/partialReceiving/exportCsv/$id"(parseRequest: true) {
            controller = "partialReceivingApi"
            action = [POST: "exportCsv"]
        }

        // Internal Locations API

        "/api/internalLocations/receiving"(parseRequest: true) {
            controller = "internalLocationApi"
            action = [GET: "listReceiving"]
        }

        "/api/internalLocations/search"(parseRequest: true) {
            controller = "internalLocationApi"
            action = [GET: "search"]
        }

        // Stocklist Item API

        "/api/stocklistItems/availableStocklists"(parseRequest: true) {
            controller = "stocklistItemApi"
            action = [GET: "availableStocklists"]
        }

        "/api/stocklistItems/$id"(parseRequest: true) {
            controller = "stocklistItemApi"
            action = [GET:"read", PUT:"update", DELETE:"remove", POST:"save"]
        }

        // Stocklist API

        "/api/stocklists/sendMail/$id"(parseRequest: true) {
            controller = "stocklistApi"
            action = [POST: "sendMail"]
        }

        // Putaway Item API

        "/api/putawayItems/$id"(parseRequest: true) {
            controller = "putawayItemApi"
            action = [DELETE: "removingItem"]
        }

        // Combined shipments

        "/api/orderNumberOptions"(parseRequest: true) {
            controller = "combinedShipmentItemApi"
            action = [GET: "getOrderOptions"]
        }

        "/api/combinedShipmentItems/findOrderItems"(parseRequest: true) {
            controller = "combinedShipmentItemApi"
            action = [POST:"findOrderItems"]
        }

        "/api/combinedShipmentItems/addToShipment/$id"(parseRequest: true) {
            controller = "combinedShipmentItemApi"
            action = [POST:"addItemsToShipment"]
        }

        "/api/combinedShipmentItems/importTemplate/$id"(parseRequest: true) {
            controller = "combinedShipmentItemApi"
            action = [POST:"importTemplate"]
        }

        "/api/combinedShipmentItems/getProductsInOrders"(parseRequest: true) {
            controller = "combinedShipmentItemApi"
            action = [GET:"getProductsInOrders"]
        }

        "/api/combinedShipmentItems/exportTemplate"(parseRequest: true) {
            controller = "combinedShipmentItemApi"
            action = [GET:"exportTemplate"]
        }

        "/api/unitOfMeasure/currencies"(parseRequest: true) {
            controller = "unitOfMeasureApi"
            action = [GET:"currencies"]
        }

        // Invoice API
        "/api/invoices/$id/items"(parseRequest: true) {
            controller = "invoiceApi"
            action = [POST: "updateItems", GET: "getInvoiceItems"]
        }

        "/api/invoices/$id/invoiceItemCandidates"(parseRequest: true) {
            controller = "invoiceApi"
            action = [POST: "getInvoiceItemCandidates"]
        }

        "/api/invoices/$id/orders"(parseRequest: true) {
            controller = "invoiceApi"
            action = [GET: "getOrderNumbers"]
        }

        "/api/invoices/$id/shipments"(parseRequest: true) {
            controller = "invoiceApi"
            action = [GET: "getShipmentNumbers"]
        }

        "/api/invoices/$id/removeItem"(parseRequest: true) {
            controller = "invoiceApi"
            action = [DELETE: "removeItem"]
        }

        "/api/invoices/$id/submit"(parseRequest: true) {
            controller = "invoiceApi"
            action = [POST: "submitInvoice"]
        }

        "/api/invoices/$id/post"(parseRequest: true) {
            controller = "invoiceApi"
            action = [POST: "postInvoice"]
        }

        "/api/invoices/$id/prepaymentItems"(parseRequest: true) {
            controller = "invoiceApi"
            action = [GET: "getPrepaymentItems"]
        }

        // Stock Transfer API

        "/api/stockTransfers/candidates"(parseRequest: true) {
            controller = { "stockTransferApi" }
            action = [GET: "stockTransferCandidates", POST: "returnCandidates"]
        }

        "/api/stockTransferItems/$id/"(parseRequest: true) {
            controller = { "stockTransferApi" }
            action = [DELETE: "removeItem"]
        }

        "/api/stockTransfers/$id/sendShipment"(parseRequest: true) {
            controller = { "stockTransferApi" }
            action = [POST: "sendShipment"]
        }

        "/api/stockTransfers/$id/rollback" {
            controller = { "stockTransferApi" }
            action = [POST: "rollback"]
        }

        "/api/stockTransfers/$id/removeAllItems"(parseRequest: true) {
            controller = { "stockTransferApi" }
            action = [DELETE: "removeAllItems"]
        }

        // Requirement API

        "/api/requirements"(parseRequest: true) {
            controller = { "replenishmentApi" }
            action = [GET: "requirements"]
        }

        // Replenishment API

        "/api/replenishments/statusOptions"(parseRequest: true) {
            controller = "replenishmentApi"
            action = [GET: "statusOptions"]
        }

        "/api/replenishments/$id/"(parseRequest: true) {
            controller = { "replenishmentApi" }
            action = [GET: "read", POST: "create", PUT: "update"]
        }

        "/api/replenishments/$id/removeItem"(parseRequest: true) {
            controller = { "replenishmentApi" }
            action = [DELETE: "removeItem"]
        }

        "/api/replenishments/$id/picklists"(parseRequest: true) {
            controller = { "replenishmentApi" }
            action = [GET: "getPicklist", POST: "createPicklist", PUT: "updatePicklist", DELETE: "deletePicklist"]
        }

        "/api/replenishments/$id/picklistItem"(parseRequest: true) {
            controller = { "replenishmentApi" }
            action = [POST: "createPicklistItem"]
        }

        // Dashboard API

        "/api/dashboard/config"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "config", POST: "updateConfig"]
        }

        "/api/dashboard/breadcrumbsConfig"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "breadcrumbsConfig"]
        }

        "/api/dashboard/inventoryByLotAndBin"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getInventoryByLotAndBin"]
        }

        "/api/dashboard/inProgressShipments"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getInProgressShipments"]
        }

        "/api/dashboard/inProgressPutaways"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getInProgressPutaways"]
        }

        "/api/dashboard/receivingBin"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getReceivingBin"]
        }

        "/api/dashboard/itemsInventoried"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getItemsInventoried"]
        }

        "/api/dashboard/defaultBin"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getDefaultBin"]
        }

        "/api/dashboard/expiredProductsInStock"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getExpiredProductsInStock"]
        }

        "/api/dashboard/expirationSummary"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getExpirationSummary"]
        }

        "/api/dashboard/fillRate"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getFillRate"]
        }

        "/api/dashboard/fillRateSnapshot"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getFillRateSnapshot"]
        }

        "/api/dashboard/fillRateDestinations"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getFillRateDestinations"]
        }

        "/api/dashboard/inventorySummary"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getInventorySummary"]
        }

        "/api/dashboard/requisitionsByYear"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getRequisitionsByYear"]
        }

        "/api/dashboard/sentStockMovements"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getSentStockMovements"]
        }

        "/api/dashboard/receivedStockMovements"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getReceivedStockMovements"]
        }

        "/api/dashboard/outgoingStock"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getOutgoingStock"]
        }

        "/api/dashboard/incomingStock"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getIncomingStock"]
        }

        "/api/dashboard/discrepancy"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getDiscrepancy"]
        }

        "/api/dashboard/delayedShipments"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getDelayedShipments"]
        }

        "/api/dashboard/productWithNegativeInventory"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getProductWithNegativeInventory"]
        }

        "/api/dashboard/lossCausedByExpiry"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getLossCausedByExpiry"]
        }

        "/api/dashboard/productsInventoried"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getProductsInventoried"]
        }

        "/api/dashboard/percentageAdHoc"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getPercentageAdHoc"]
        }

        "/api/dashboard/stockOutLastMonth"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getStockOutLastMonth"]
        }

        "/api/dashboard/openStockRequests"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getOpenStockRequests"]
        }

        "/api/dashboard/inventoryValue"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getInventoryValue"]
        }

        "/api/dashboard/openPurchaseOrdersCount"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getOpenPurchaseOrdersCount"]
        }

        "/api/productsConfiguration/importCategories"(parseRequest: true) {
            controller = { "productsConfigurationApi" }
            action = [POST: "importCategories"]
        }

        "/api/productsConfiguration/importCategoryCsv"(parseRequest: true) {
            controller = { "productsConfigurationApi" }
            action = [POST: "importCategoryCsv"]
        }

        "/api/productsConfiguration/downloadCategoryTemplate"(parseRequest: true) {
            controller = { "productsConfigurationApi" }
            action = [GET: "downloadCategoryTemplate"]
        }

        "/api/productsConfiguration/categoryOptions"(parseRequest: true) {
            controller = { "productsConfigurationApi" }
            action = [GET: "categoryOptions"]
        }

        "/api/productsConfiguration/productOptions"(parseRequest: true) {
            controller = { "productsConfigurationApi" }
            action = [GET: "productOptions"]
        }

        "/api/productsConfiguration/importProducts"(parseRequest: true) {
            controller = { "productsConfigurationApi" }
            action = [POST: "importProducts"]
        }

        "/api/productsConfiguration/categoriesCount"(parseRequest: true) {
            controller = { "productsConfigurationApi" }
            action = [GET: "getCategoriesCount"]
        }

        "/api/productsConfiguration/downloadCategories"(parseRequest: true) {
            controller = { "productsConfigurationApi" }
            action = [GET: "downloadCategories"]
        }

        // Load Data

        "/api/loadData/listOfDemoData"(parseRequest: true) {
            controller = { "loadDataApi" }
            action = [GET: "listOfDemoData"]
        }

        // Standard REST APIs

        "/api/${resource}s"(parseRequest: true) {
            controller = { "${params.resource}Api" }
            action = [GET: "list", POST: "create"]
        }

        "/api/${resource}s/$id/status"(parseRequest: true) {
            controller = { "${params.resource}Api" }
            action = [GET: "status", DELETE: "deleteStatus", POST: "updateStatus"]
        }

        "/api/${resource}s/$id"(parseRequest: true) {
            controller = { "${params.resource}Api" }
            action = [GET: "read", POST: "update", PUT: "update", DELETE: "delete"]
        }


        // Anonymous REST APIs like Status, Login, Logout

        "/api/$action/$id?"(controller: "api", parseRequest: false) {
            //action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
        }

        "/api/supportLinks"(parseRequest: true) {
            controller = { "api" }
            action = [GET: "getSupportLinks"]
        }

        "/api/resettingInstance/command"(parseRequest: true) {
            controller = { "api" }
            action = [GET: "getResettingInstanceCommand"]
        }

        // Generic API for all other resources

        "/api/generic/${resource}/"(parseRequest: false) {
            controller = "genericApi"
            action = [GET: "list", POST: "create"]
        }

        "/api/generic/${resource}/search"(parseRequest: false) {
            controller = "genericApi"
            action = [GET: "search", POST: "search"]
        }

        "/api/generic/${resource}/$id"(parseRequest: false) {
            controller = "genericApi"
            action = [GET: "read", POST: "update", PUT: "update", DELETE: "delete"]
        }

        // Error handling

        "401"(controller: "errors", action: "handleUnauthorized")
        "404"(controller: "errors", action: "handleNotFound")
        "405"(controller: "errors", action: "handleMethodNotAllowed")
        "500"(controller: "errors", action: "handleException")
        "500"(controller: "errors", action: "handleNotFound", exception: ObjectNotFoundException)
        "500"(controller: "errors", action: "handleValidationErrors", exception: ValidationException)
        "500"(controller: "errors", action: "handleUnauthorized", exception: AuthenticationException)
        "/"(controller: "dashboard", action: "index")
    }


}
