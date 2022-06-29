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
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode

class InternalLocationApiController {

    def zebraService
    def documentService
    def locationService
    def inventoryService
    def putawayService
    def productAvailabilityService

    def list = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location parentLocation = locationId ? Location.get(locationId) : null
        if (!parentLocation) {
            throw new IllegalArgumentException("Must provide location.id as a request parameter")
        }
        Location currentLocation = Location.get(session.warehouse.id)
        Boolean excludeUnavailable = params.excludeUnavailable ? params.boolean("excludeUnavailable") : Boolean.FALSE
        Boolean includeInactive = params.includeInactive ? params.boolean("includeInactive") : Boolean.FALSE
        ActivityCode[] activityCodes = params.activityCode ? params.list("activityCode") as ActivityCode [] : null
        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") :
                [LocationTypeCode.INTERNAL, LocationTypeCode.BIN_LOCATION]

        List<Location> locations = locationService.getInternalLocations(parentLocation, locationTypeCodes, activityCodes, includeInactive)

        // FIXME Exclude unavailable by default but we should make this more generic since search is used in other places besides putaway
        if (currentLocation.supports(ActivityCode.PUTAWAY_STRATEGY_EMPTY_LOCATIONS) && excludeUnavailable) {
            List<Location> availableLocations = putawayService.getAvailableLocations(currentLocation, null, activityCodes.toList())
            locations = availableLocations.intersect(locations)
        }

        render([data: locations?.collect { it.toJson(it?.locationType?.locationTypeCode) }] as JSON)
    }

    def search = {
        log.info "search " + params
        Location currentLocation = Location.get(session?.warehouse?.id)
        if (!currentLocation) {
            throw new IllegalArgumentException("User must be logged into a location")
        }
        Boolean excludeUnavailable = params.excludeUnavailable ? params.boolean("excludeUnavailable") : Boolean.TRUE
        ActivityCode [] activityCodes = params.activityCode ? params.list("activityCode") :
                [ActivityCode.RECEIVE_STOCK, ActivityCode.PUTAWAY_STOCK]
        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") :
                [LocationTypeCode.INTERNAL, LocationTypeCode.BIN_LOCATION]
        List<Location> locations = locationService.searchInternalLocations(params.searchTerm, locationTypeCodes, params)

        // FIXME Exclude unavailable by default but we should make this more generic since search is used in other places besides putaway
        if (currentLocation.supports(ActivityCode.PUTAWAY_STRATEGY_EMPTY_LOCATIONS) && excludeUnavailable) {
            List<Location> availableLocations = putawayService.getAvailableLocations(currentLocation, null, activityCodes.toList())
            locations = availableLocations.intersect(locations)
        }
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

        ActivityCode[] activityCodes = params.activityCode ? params.list("activityCode") : [ActivityCode.RECEIVE_STOCK]
        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") : [LocationTypeCode.BIN_LOCATION, LocationTypeCode.INTERNAL]

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

        String parentLocationId = params?.location?.id?:session?.warehouse?.id
        Location internalLocation = locationService.getInternalLocation(parentLocationId, params.id)
        if (!internalLocation) {
           throw new ObjectNotFoundException(params.id, Location.class.simpleName)
        }

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
        data.defaultBarcodeLabelUrl = documentService.getInternalLocationBarcodeLabel(internalLocation)
        render([data: data] as JSON)
    }

    def renderLabel = {
        Location internalLocation = locationService.getInternalLocation(session?.warehouse?.id, params.id)
        Document document = Document.get(params.documentId)
        if (!document) {
            throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
        }
        response.contentType = "image/png"
        response.outputStream << zebraService.renderDocument(document, [internalLocation: internalLocation])
    }

    def printLabel = {
        try {
            Document document = Document.get(params.documentId)
            if (!document) {
                throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
            }

            Location internalLocation = locationService.getInternalLocation(session?.warehouse?.id, params.id)
            Map model = [internalLocation: internalLocation]
            zebraService.printDocument(document, [internalLocation:internalLocation])

            render([data: "Barcode label has been printed"] as JSON)
            return
        } catch (Exception e) {
            render([errorCode: 500, cause: e?.class, errorMessage: e?.message] as JSON)
        }
    }


}
