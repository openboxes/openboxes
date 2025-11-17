package org.pih.warehouse

import grails.validation.ValidationException
import org.apache.http.auth.AuthenticationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.requisition.RequisitionSourceType

import java.sql.SQLIntegrityConstraintViolationException

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

        "/stockRequest/$action/$id**?" {
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

        "/api/categories:bulk"(parseRequest: true) {
            controller = { "categoryApi" }
            action = [GET: "list", POST: "save"]
        }

        "/api/categories/$id:sortToContainer"(parseRequest: true) {
            controller = { "categoryApi" }
            action = [GET: "list", PATCH: "list"]
        }


        // Category options for filters on  product list page
        "/api/categoryOptions"(parseRequest: true) {
            controller = { "selectOptionsApi" }
            action = [GET: "categoryOptions"]
        }

        // Catalog options for filters on  product list page
        "/api/catalogOptions"(parseRequest: true) {
            controller = { "selectOptionsApi" }
            action = [GET: "catalogOptions"]
        }

        // Product Group options for filters on  product list page
        "/api/productGroupOptions"(parseRequest: true) {
            controller = { "selectOptionsApi" }
            action = [GET: "productGroupOptions"]
        }

        // Tag options for filters on  product list page
        "/api/tagOptions"(parseRequest: true) {
            controller = { "selectOptionsApi" }
            action = [GET: "tagOptions"]
        }

        // Gl account options for filters on product list page
        "/api/glAccountOptions"(parseRequest: true) {
            controller = { "selectOptionsApi" }
            action = [GET: "glAccountOptions"]
        }

        "/api/paymentTermOptions"(parseRequest: true) {
            controller = { "selectOptionsApi" }
            action = [GET: "paymentTermOptions"]
        }

        "/api/users" {
            controller = { "selectOptionsApi" }
            action = [GET: "usersOptions"]
        }

        "/api/preferenceTypeOptions" {
            controller = { "selectOptionsApi" }
            action = [GET: "preferenceTypeOptions"]
        }

        "/api/ratingTypeCodeOptions" {
            controller = { "selectOptionsApi" }
            action = [GET: "ratingTypeCodeOptions"]
        }

        "/api/handlingRequirementsOptions" {
            controller = { "selectOptionsApi" }
            action = [GET: "handlingRequirementsOptions"]
        }

        "/api/products"(parseRequest: true) {
            controller = { "productApi" }
            action = [GET: "list", POST: "save"]
        }

        "/api/products/search"(parseRequest: true) {
            controller = { "productApi" }
            action = [GET: "search"]
        }

        "/api/products/$id/$action" {
            controller = { "productApi" }
        }

        "/api/products/$productId/inventoryItems/$lotNumber"(parseRequest: true) {
            controller = { "productApi" }
            action = [GET: "getInventoryItem"]
        }

        "/api/products/getLatestInventoryCountDate" {
            controller = { "productApi" }
            action = [GET: "getLatestInventoryCountDate"]
        }

        "/api/products/import" {
            controller = { "productApi" }
            action = [POST: "importCsv"]
        }

        "/api/products/inventoryItems/lotNumbersWithExpirationDate" {
            controller = { "productApi" }
            action = [GET: "getLotNumbersWithExpirationDate"]
        }

        "/api/facilities/$facilityId/products/classifications" {
            controller = "productClassificationApi"
            action = [GET: "list"]
        }

        "/api/facilities/$facilityId/inventory-levels(.$format)?" {
            controller = "inventoryLevelApi"
            action = [GET: "list"]
        }

        "/api/facilities/$facilityId/inventory-items"(parseRequest: true) {
            controller = "inventoryItemApi"
            action = [GET: "listByActivity"]
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

        "/api/stockMovementItems/$id/picklistItems" {
            controller = "stockMovementItemApi"
            action = [DELETE: "revertPick"]
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
            controller = "picklist"
            action = [POST: "importPickListItems"]
        }

        "/api/stockMovements/exportPickListItems/$id"(parseRequest: true) {
            controller = "picklist"
            action = [GET: "exportPicklistItems"]
        }

        "/api/stockMovements/picklistTemplate/$id"(parseRequest: true) {
            controller = "picklist"
            action = [GET: "exportPicklistTemplate"]
        }

        "/api/stockMovements/createPickList/$id"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "createPickList"]
        }

        "/api/stockMovements/pendingRequisitionDetails"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "getPendingRequisitionDetails"]
        }

        "/api/stockMovements"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "list", POST: "create"]
        }

        "/api/stockMovements/shippedItems"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "shippedItems"]
        }

        "/api/stockMovements/pendingRequisitionItems"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [GET: "pendingRequisitionItems"]
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

        "/api/stockMovements/$id/packingLocation"(parseRequest: true) {
            controller = "stockMovementApi"
            action = [POST: "packingLocation"]
        }

        "/api/stockMovements/shipmentStatusCodes" {
            controller = "stockMovementApi"
            action = [GET: "shipmentStatusCodes"]
        }

        "/api/stockMovements/requisitionsStatusCodes" {
            controller = "stockMovementApi"
            action = [GET: "requisitionStatusCodes"]
        }

        "/api/stockMovements/$id/rollbackApproval" {
            controller = "stockMovementApi"
            action = [PUT: "rollbackApproval"]
        }

        "/api/stockMovements/packingList/template" {
            controller = "stockMovementApi"
            action = [GET: "downloadPackingListTemplate"]
        }

        "/api/picklists/$id/items" {
            controller = "picklistApi"
            action = [DELETE: "clearPicklist"]
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

        "/api/partialReceiving/$id/receivingBinLocation"(parseRequest: true) {
            controller = "partialReceivingApi"
            action = [POST: "createReceivingBinLocation"]
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

        "/api/internalLocations/$id/labels/$documentId**?" {
            controller = { "internalLocationApi" }
            action = [GET: "renderLabel", POST: "printLabel", PUT: "printLabel"]
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

        "/api/stocklists/$id/export"(parseRequest: true) {
            controller = "stocklistApi"
            action = [GET: "export"]
        }

        "/api/stocklists/$id/clone"(parseRequest: true) {
            controller = "stocklistApi"
            action = [POST: "clone"]
        }

        "/api/stocklists/$id/publish"(parseRequest: true) {
            controller = "stocklistApi"
            action = [POST: "publish"]
        }

        "/api/stocklists/$id/unpublish"(parseRequest: true) {
            controller = "stocklistApi"
            action = [POST: "unpublish"]
        }

        "/api/stocklists/$id/clear"(parseRequest: true) {
            controller = "stocklistApi"
            action = [POST: "clear"]
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

        "/api/unitOfMeasures/options" {
            controller = "unitOfMeasureApi"
            action = [GET: "uomOptions"]
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

        "/api/invoiceStatuses"(parseRequest: true) {
            controller = { "invoiceApi" }
            action = [GET: "statusOptions"]
        }

        "/api/invoiceTypeCodes"(parseRequest: true) {
            controller = { "invoiceApi" }
            action = [GET: "invoiceTypeCodes"]
        }

        // TODO: Investigate the proper way to handle validation as a REST resource
        "/api/invoiceItems/$id/validation" {
            controller = "invoiceApi"
            action = [POST: "validateInvoiceItem"]
        }

        "/api/prepaymentInvoices/$id/invoiceItems" {
            controller = "prepaymentInvoiceApi"
            action = [POST: "updateItems"]
        }

        "/api/prepaymentInvoiceItems/$id" {
            controller = "prepaymentInvoiceItemApi"
            action = [POST: "update", DELETE: "delete"]
        }

        // Stock Transfer API

        "/api/stockTransfers/statusOptions"(parseRequest: true) {
            controller = { "stockTransferApi" }
            action = [GET: "statusOptions"]
        }

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
            action = [GET: "requirements", POST: "create"]
        }

        // Replenishment API

        "/api/replenishments/statusOptions"(parseRequest: true) {
            controller = "replenishmentApi"
            action = [GET: "statusOptions"]
        }

        "/api/replenishments/$id/"(parseRequest: true) {
            controller = { "replenishmentApi" }
            action = [GET: "read", POST: "update", PUT: "update"]
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
            action = [POST: "updateConfig"]
        }

        "/api/dashboard/$id/config"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "config"]
        }

        "/api/dashboard/$id/subdashboardKeys"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getSubdashboardKeys"]
        }

        "/api/dashboard/inventoryByLotAndBin"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getInventoryByLotAndBin"]
        }

        "/api/dashboard/lostAndFound"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getLostAndFoundInventoryItems"]
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

        "/api/dashboard/requestsPendingApproval"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getRequestsPendingApproval"]
        }

        "/api/dashboard/inventoryValue"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getInventoryValue"]
        }

        "/api/dashboard/openPurchaseOrdersCount"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getOpenPurchaseOrdersCount"]
        }

        "/api/dashboard/backdatedOutboundShipments" {
            controller = { "dashboardApi" }
            action = [GET: "getBackdatedOutboundShipments"]
        }

        "/api/dashboard/backdatedInboundShipments" {
            controller = { "dashboardApi" }
            action = [GET: "getBackdatedInboundShipments"]
        }

        "/api/dashboard/itemsWithBackdatedShipments" {
            controller = { "dashboardApi" }
            action = [GET: "getItemsWithBackdatedShipments"]
        }

        "/api/dashboard/openPutawayTasks"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getOpenPutawayTasks"]
        }

        "/api/dashboard/inboundSortationItems"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getInboundSortationItems"]
        }

        "/api/dashboard/averageInboundSortationTime"(parseRequest: true) {
            controller = { "dashboardApi" }
            action = [GET: "getAverageInboundSortationTime"]
        }

        /**
         * Inventory API endpoints
         */

        "/api/facilities/$facilityId/inventories/import" {
            controller = { "inventoryApi" }
            action = "importCsv"
        }

        /**
        * Purchase Orders API endpoints
        */

        "/api/orderSummaryStatus"(parseRequest: true) {
            controller = { "purchaseOrderApi" }
            action = [GET: "statusOptions"]
        }

        "/api/purchaseOrders/$id/rollback"(parseRequest: true) {
            controller = { "purchaseOrderApi" }
            action = [POST: "rollback"]
        }

        "/api/purchaseOrders"(parseRequest: true) {
            controller = { "purchaseOrderApi" }
            action = [GET: "list", POST: "create"]
        }

        "/api/purchaseOrders/$id"(parseRequest: true) {
            controller = { "purchaseOrderApi" }
            action = [GET: "read", DELETE: "delete", PUT: "update"]
        }

        /**
         * Products Configuration API endpoints
         */

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

        "/api/productSupplierPreferences/batch" {
            controller = { "productSupplierPreferenceApi" }
            action = [POST: "createOrUpdateBatch"]
        }

        "/api/productSupplierAttributes/batch" {
            controller = { "productSupplierAttributeApi" }
            action = [POST: "updateAttributes"]
        }

        "/api/productSuppliers/export" {
            controller = { "productSupplierApi" }
            action = [GET: "export"]
        }

        // Load Data

        "/api/loadData/listOfDemoData"(parseRequest: true) {
            controller = { "loadDataApi" }
            action = [GET: "listOfDemoData"]
        }

        "/api/fulfillments" {
            controller = { "fulfillmentApi" }
            action = [POST: "save"]
        }

        "/api/fulfillments/validate" {
            controller = { "fulfillmentApi" }
            action = [POST: "validate"]
        }

        // Custom MOBILE APIs
        "/api/mobile/putaways"(parseRequest: true) {
            controller = "putawayApi"
            action = [GET: "mobilePutaways"]
        }

        "/api/locations/$id/putawayCandidates" {
            controller = { "putawayItemApi" }
            action = [GET: "list"]
        }

        "/api/containers/$id/details"(parseRequest: true) {
            controller = "containerApi"
            action = [GET: "details"]
        }

        "/api/containers/$id/labels/$documentId**?" {
            controller = { "containerApi" }
            action = [GET: "renderLabel", POST: "printLabel", PUT: "printLabel"]
        }

        "/api/mobile/products/" {
            controller = { "mobileProductApi" }
            action = [GET: "list"]
        }

        "/api/mobile/products/search"(parseRequest: true) {
            controller = { "mobileProductApi" }
            action = [GET: "search", POST: "search"]
        }

        "/api/mobile/products/$id/" {
            controller = { "mobileProductApi" }
            action = [GET: "read"]
        }

        "/api/products/$id/labels/$documentId**?" {
            controller = { "mobileProductApi" }
            action = [GET: "renderLabel", POST: "printLabel", PUT: "printLabel"]
        }

        "/api/mobile/products/$id/details" {
            controller = { "mobileProductApi" }
            action = [GET: "details"]
        }

        "/api/mobile/products/$id/identifiers" {
            controller = { "mobileProductApi" }
            action = [PUT: "updateIdentifier"]
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

        "/api/facilities/$facilityId/cycle-counts/candidates" {
            controller = "cycleCountApi"
            action = [GET: "getCandidates"]
        }

        "/api/facilities/$facilityId/cycle-counts/requests/pending" {
            controller = "cycleCountApi"
            action = [GET: "getPendingCycleCountRequests"]
        }

        "/api/facilities/$facilityId/cycle-counts/requests/batch" {
            controller = "cycleCountApi"
            action = [POST: "createRequests", PATCH: "updateRequests", DELETE: "deleteRequests"]
        }

        "/api/facilities/$facility/cycle-counts/start/batch" {
            controller = "cycleCountApi"
            action = [POST: "startCycleCount"]
        }

        "/api/facilities/$facility/cycle-counts/recount/start/batch" {
            controller = "cycleCountApi"
            action = [POST: "startRecount"]
        }

        "/api/facilities/$facility/cycle-counts" {
            controller = "cycleCountApi"
            action = [GET: "list"]
        }

        "/api/facilities/$facility/cycle-counts/$cycleCountId/count" {
            controller = "cycleCountApi"
            action = [POST: "submitCount"]
        }

        "/api/facilities/$facility/cycle-counts/$cycleCountId/recount" {
            controller = "cycleCountApi"
            action = [POST: "submitRecount"]
        }

        "/api/facilities/$facility/cycle-counts/items/$cycleCountItemId" {
            controller = "cycleCountApi"
            action = [PATCH: "updateCycleCountItem", DELETE: "deleteCycleCountItem"]
        }

        "/api/facilities/$facility/cycle-counts/items/upload/count" {
            controller = "cycleCountApi"
            action = [POST: "uploadCycleCountItems"]
        }

        "/api/facilities/$facility/cycle-counts/$cycleCountId/items" {
            controller = "cycleCountApi"
            action = [POST: "createCycleCountItem"]
        }

        "/api/facilities/$facility/cycle-counts/$cycleCountId/items/batch" {
            controller = "cycleCountApi"
            action = [POST: "createCycleCountItemBatch", PATCH: "updateCycleCountItemBatch"]
        }

        "/api/facilities/$facility/cycle-counts/items/batch" {
            controller = "cycleCountApi"
            action = [POST: "createCycleCountItemBatch", PATCH: "updateCycleCountItemBatch"]
        }

        "/api/facilities/$facility/cycle-counts/$cycleCountId/refresh" {
            controller = "cycleCountApi"
            action = [POST: "refreshCycleCount"]
        }

        "/api/reports/cycle-count-details" {
            controller = "cycleCountApi"
            action = [POST: "getCycleCountDetails", GET: "getCycleCountDetails"]
        }

        "/api/reports/cycle-count-summary" {
            controller = "cycleCountApi"
            action = [POST: "getCycleCountSummary", GET: "getCycleCountSummary"]
        }

        "/api/reports/inventory-audit-details" {
            controller = "inventoryAuditReport"
            action = [POST: "getInventoryAuditDetails", GET: "getInventoryAuditDetails"]

        }

        "/api/reports/inventory-audit-summary(.$format)?" {
            controller = "inventoryAuditReport"
            action = [POST: "getInventoryAuditSummary", GET: "getInventoryAuditSummary"]
        }

        "/api/reports/inventory-transactions-summary" {
            controller = "inventoryTransactionSummaryApi"
            action = [GET: "getInventoryTransactionsSummary"]
        }

        "/api/reports/indicators/productsInventoried" {
            controller = "indicatorApi"
            action = [GET: "getProductsInventoried"]
        }

        "/api/reports/indicators/inventoryAccuracy" {
            controller = "indicatorApi"
            action = [GET: "getInventoryAccuracy"]
        }

        "/api/reports/indicators/inventoryShrinkage" {
            controller = "indicatorApi"
            action = [GET: "getInventoryShrinkage"]
        }

        "/api/facilities/$facility/inventory/record-stock/save" {
            controller = "recordStockApi"
            action = [POST: "saveRecordStock"]
        }

        "/api/reason-codes"(parseRequest: true) {
            controller = "reasonCodeApi"
            action = [GET: "list"]
        }

        // Error handling

        "401"(controller: "errors", action: "handleUnauthorized")
        //"404"(controller: "errors", action: "handleNotFound")
        "405"(controller: "errors", action: "handleMethodNotAllowed")
        "500"(controller: "errors", action: "handleException")
        "500"(controller: "errors", action: "handleNotFound", exception: ObjectNotFoundException)
        "500"(controller: "errors", action: "handleValidationErrors", exception: ValidationException)
        "500"(controller: "errors", action: "handleUnauthorized", exception: AuthenticationException)
        "500"(controller: "errors", action: "handleConstraintViolation", exception: SQLIntegrityConstraintViolationException)
        "/"(controller: "dashboard", action: "index")
    }


}
