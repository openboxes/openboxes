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

class LoadDataApiController extends BaseDomainApiController {

    def loadDataService;

    def listOfDemoData = {
        def listOfDemoData = grailsApplication.config.openboxes.configurationWizard.listOfDemoData

        render([data: listOfDemoData] as JSON)
    }

    def load = {
        def config = grailsApplication.config.openboxes.configurationWizard.dataInit;

        if (config.organizations.enabled) {
            loadDataService.importOrganizations(new URL(config.organizations.url))
        }

        if (config.locationGroups.enabled) {
            loadDataService.importLocationGroups(new URL(config.locationGroups.url))
        }

        if (config.locations.enabled) {
            loadDataService.importLocations(new URL(config.locations.url))
        }

        if (config.binLocations.enabled) {
            loadDataService.importLocations(new URL(config.binLocations.url))
        }

        if (config.categories.enabled) {
            loadDataService.importCategories(new URL(config.categories.url))
        }

        if (config.products.enabled) {
            loadDataService.importProducts(new URL(config.products.url))
        }

        if (config.productCatalog.enabled) {
            loadDataService.importProductCatalog(new URL(config.productCatalog.url))
        }

        if (config.productSuppliers.enabled) {
            loadDataService.importProductCatalogItems(new URL(config.productSuppliers.url))
        }

        if(config.productSuppliers.enabled) {
            loadDataService.importProductSuppliers(new URL(config.productSuppliers.url))
        }

        if(config.mainWarehouseInventory.enabled) {
            loadDataService.importInventory(new URL(config.mainWarehouseInventory.url))
        }

        if(config.bostonWarehouseInventory.enabled) {
            loadDataService.importInventory(new URL(config.bostonWarehouseInventory.url))
        }

        if(config.chicagoWarehouseInventory.enabled) {
            loadDataService.importInventory(new URL(config.chicagoWarehouseInventory.url))
        }

        if(config.users.enabled) {
            loadDataService.importUsers(new URL(config.users.url))
        }

        if(config.persons.enabled) {
            loadDataService.importPersons(new URL(config.persons.url))
        }

        render([] as JSON)
    }
}
