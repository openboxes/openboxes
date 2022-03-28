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
        loadDataService.importOrganizations(
                new URL(grailsApplication.config.openboxes.configurationWizard.dataFiles.organizations)
        )

        loadDataService.importLocationGroups(
                new URL(grailsApplication.config.openboxes.configurationWizard.dataFiles.locationGroups)
        )

        loadDataService.importLocations(
                new URL(grailsApplication.config.openboxes.configurationWizard.dataFiles.locations)
        )

        loadDataService.importLocations(
                new URL(grailsApplication.config.openboxes.configurationWizard.dataFiles.binLocations)
        )

        loadDataService.importCategories(
                new URL(grailsApplication.config.openboxes.configurationWizard.dataFiles.categories)
        )

        loadDataService.importProducts(
                new URL(grailsApplication.config.openboxes.configurationWizard.dataFiles.products)
        )

        render([] as JSON)
    }
}
