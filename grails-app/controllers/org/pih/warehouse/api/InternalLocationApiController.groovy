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
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode

class InternalLocationApiController {

    def locationService
    def inventoryService
    def productAvailabilityService

    def list = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location parentLocation = locationId ? Location.get(locationId) : null
        if (!parentLocation) {
            throw new IllegalArgumentException("Must provide location.id as a request parameter")
        }

        ActivityCode[] activityCodes = params.activityCode ? params.list("activityCode") : null
        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") : [LocationTypeCode.INTERNAL, LocationTypeCode.BIN_LOCATION]
        List<Location> locations = locationService.getInternalLocations(parentLocation, locationTypeCodes, activityCodes)
        render([data: locations?.collect { it.toJson(it?.locationType?.locationTypeCode) }] as JSON)
    }

    def listReceiving = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location parentLocation = locationId ? Location.get(locationId) : null
        if (!parentLocation) {
            throw new IllegalArgumentException("Must provide location.id as a request parameter")
        }
        String shipmentNumber = params?.shipmentNumber
        if (!shipmentNumber) {
            throw new IllegalArgumentException("Must provide shipmentNumber as a request parameter")
        }

        ActivityCode[] activityCodes = params.activityCode ? params.list("activityCode") : null
        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") : [LocationTypeCode.BIN_LOCATION]

        List receivingLocationNames = locationService.getDefaultReceivingLocationNames()
        receivingLocationNames << locationService.getReceivingLocationName(shipmentNumber)
        receivingLocationNames << "Receiving ${shipmentNumber}"
        List<Location> locations = locationService.getInternalLocations(parentLocation, locationTypeCodes, activityCodes, (String[]) receivingLocationNames.toArray())

        render([data: locations?.collect { it.toJson(it.locationType?.locationTypeCode) }] as JSON)
    }

    def read = {
        Location location = locationService.getLocation(params.id)
        render([data: location] as JSON)
    }


    def details = {
        Location internalLocation = locationService.getLocation(params.id)
        Map data = internalLocation.toJson()
        def availableItems = productAvailabilityService.getAvailableItems(internalLocation.parentLocation, internalLocation)
        data.availableItems = availableItems.collect { AvailableItem availableItem ->
            [
                    "product.id" : availableItem?.inventoryItem?.product?.id,
                    "product.productCode" : availableItem?.inventoryItem?.product?.productCode,
                    "product.name" : availableItem?.inventoryItem?.product?.name,
                    "inventoryItem.id" : availableItem?.inventoryItem?.id,
                    "inventoryItem.lotNumber" : availableItem?.inventoryItem?.lotNumber,
                    "inventoryItem.expirationDate" : availableItem?.inventoryItem?.expirationDate,
                    "binLocation.id" : availableItem?.binLocation?.id,
                    "binLocation.name" : availableItem?.binLocation?.name,
                    "quantityAvailable" : availableItem?.quantityAvailable,
                    "quantityOnHand" : availableItem?.quantityOnHand,
            ]
        }
        render([data: data] as JSON)

    }

}
