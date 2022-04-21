/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition

class LoadDataApiController extends BaseDomainApiController {

    def loadDataService;

    def listOfDemoData = {
        def listOfDemoData = grailsApplication.config.openboxes.configurationWizard.listOfDemoData

        render([data: listOfDemoData] as JSON)
    }

    def load = {
        def config = grailsApplication.config.openboxes.configurationWizard.dataInit;

        if (Boolean.valueOf(config.organizations.enabled)) {
            loadDataService.importOrganizations(new URL(config.organizations.url))
        }

        if (Boolean.valueOf(config.locationGroups.enabled)) {
            loadDataService.importLocationGroups(new URL(config.locationGroups.url))
        }

        if (Boolean.valueOf(config.locations.enabled)) {
            loadDataService.importLocations(new URL(config.locations.url))
        }

        if (Boolean.valueOf(config.binLocations.enabled)) {
            loadDataService.importLocations(new URL(config.binLocations.url))
        }

        if (Boolean.valueOf(config.categories.enabled)) {
            loadDataService.importCategories(new URL(config.categories.url))
        }

        if (Boolean.valueOf(config.products.enabled)) {
            loadDataService.importProducts(new URL(config.products.url))
        }

        if (Boolean.valueOf(config.productCatalog.enabled)) {
            loadDataService.importProductCatalog(new URL(config.productCatalog.url))
        }

        if (Boolean.valueOf(config.productCatalogItems.enabled)) {
            loadDataService.importProductCatalogItems(new URL(config.productCatalogItems.url))
        }

        if (Boolean.valueOf(config.productSuppliers.enabled)) {
            loadDataService.importProductSuppliers(new URL(config.productSuppliers.url))
        }

        if (Boolean.valueOf(config.mainWarehouseInventory.enabled)) {
            loadDataService.importInventory(
                    new URL(config.mainWarehouseInventory.url),
                    Location.findByName(config.mainWarehouseInventory.warehouseName)
            )
        }

        if (Boolean.valueOf(config.bostonWarehouseInventory.enabled)) {
            loadDataService.importInventory(
                    new URL(config.bostonWarehouseInventory.url),
                    Location.findByName(config.bostonWarehouseInventory.warehouseName)
            )
        }

        if (Boolean.valueOf(config.chicagoWarehouseInventory.enabled)) {
            loadDataService.importInventory(
                    new URL(config.chicagoWarehouseInventory.url),
                    Location.findByName(config.chicagoWarehouseInventory.warehouseName)
            )
        }

        if (Boolean.valueOf(config.inventoryLevels.enabled)) {
            loadDataService.importInventoryLevels(
                    new URL(config.inventoryLevels.url),
                    Location.findByName(config.inventoryLevels.warehouseName)
            )
        }

        if (Boolean.valueOf(config.users.enabled)) {
            loadDataService.importUsers(new URL(config.users.url))
        }

        if (Boolean.valueOf(config.persons.enabled)) {
            loadDataService.importPersons(new URL(config.persons.url))
        }

        if (Boolean.valueOf(config.chicagoStocklist.enabled)) {
            Requisition requisition = loadDataService.importStocklistTemplate(
                    new URL(config.chicagoStocklist.templateUrl)
            );

            loadDataService.importStocklistItems(
                    new URL(config.chicagoStocklist.itemsUrl),
                    requisition
            )
        }

        if (Boolean.valueOf(config.bostonStocklist.enabled)) {
            Requisition requisition = loadDataService.importStocklistTemplate(
                    new URL(config.bostonStocklist.templateUrl)
            )

            loadDataService.importStocklistItems(
                    new URL(config.bostonStocklist.itemsUrl),
                    requisition
            )
        }

        render([] as JSON)
    }
}
