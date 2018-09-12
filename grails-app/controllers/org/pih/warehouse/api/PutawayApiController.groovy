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
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location
/**
 * Should not extend BaseDomainApiController since stocklist is not a valid domain.
 */
class PutawayApiController {

    def putawayService
    def inventoryService
    def identifierService
    def pdfRenderingService

    def list = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        if (!location) {
            throw new IllegalArgumentException("Must provide location.id as request parameter")
        }
        List putawayItems = putawayService.getPutawayCandidates(location)
        render ([data:putawayItems.collect { it.toJson() }] as JSON)
	}

    def create = { Putaway putaway ->
        JSONObject jsonObject = request.JSON

        // Bind the putaway
        bindData(putaway, jsonObject)

        // Bind the putaway items
        jsonObject.putawayItems.each { putawayItemMap ->
            PutawayItem putawayItem = new PutawayItem()
            bindData(putawayItem, putawayItemMap)

            // Bind the split items
            putawayItemMap.splitItems.each { splitItemMap ->
                PutawayItem splitItem = new PutawayItem()
                bindData(splitItem, splitItemMap)
                putawayItem.splitItems.add(splitItem)
            }

            if (!putaway.putawayNumber) {
                putaway.putawayNumber = identifierService.generateOrderIdentifier()
            }

            putawayItem.availableItems =
                    inventoryService.getAvailableBinLocations(putawayItem.currentFacility, putawayItem.product)

            putaway.putawayItems.add(putawayItem)
        }

        // Putaway stock
        if (putaway?.putawayStatus?.equals(PutawayStatus.COMPLETE)) {
            // Need to process the split items
            putawayService.processSplitItems(putaway)
            putawayService.putawayStock(putaway)
        }

        render ([data:putaway?.toJson()] as JSON)
    }


}
