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
import grails.gorm.transactions.Transactional
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.ZebraService

@Transactional
class InternalLocationApiController {

    LocationService locationService
    ZebraService zebraService

    def list() {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location parentLocation = locationId ? Location.get(locationId) : null
        if (!parentLocation) {
            throw new IllegalArgumentException("Must provide location.id as a request parameter")
        }

        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") : [LocationTypeCode.INTERNAL, LocationTypeCode.BIN_LOCATION]

        Map activityCodeParams = [:]
        activityCodeParams.allActivityCodes = params.allActivityCodes ? params.list("allActivityCodes") as ActivityCode[]: []
        activityCodeParams.anyActivityCodes = params.anyActivityCodes ? params.list("anyActivityCodes") as ActivityCode[]: []
        activityCodeParams.ignoreActivityCodes = params.ignoreActivityCodes ? params.list("ignoreActivityCodes") as ActivityCode[] : []

        List<Location> locations = locationService.getInternalLocations(parentLocation, locationTypeCodes, activityCodeParams)
        render([data: locations?.collect { [id: it.id, name: it.name, zoneId: it.zone?.id, zoneName: it.zone?.name] }] as JSON)
    }

    def search() {
        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") : [LocationTypeCode.INTERNAL, LocationTypeCode.BIN_LOCATION]
        List<Location> locations = locationService.searchInternalLocations(params, locationTypeCodes)
        render([data: locations, totalCount: locations?.totalCount] as JSON)
    }

    def listReceiving() {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location parentLocation = locationId ? Location.get(locationId) : null
        if (!parentLocation) {
            throw new IllegalArgumentException("Must provide location.id as a request parameter")
        }
        String shipmentNumber = params?.shipmentNumber
        if (!shipmentNumber) {
            throw new IllegalArgumentException("Must provide shipmentNumber as a request parameter")
        }

        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") : [LocationTypeCode.BIN_LOCATION]

        Map activityCodeParams = [:]
        activityCodeParams.allActivityCodes = params.allActivityCodes ? params.list("allActivityCodes") as ActivityCode[]: []
        activityCodeParams.anyActivityCodes = params.anyActivityCodes ? params.list("anyActivityCodes") as ActivityCode[]: []
        activityCodeParams.ignoreActivityCodes = params.ignoreActivityCodes ? params.list("ignoreActivityCodes") as ActivityCode[] : [ActivityCode.RECEIVE_STOCK]

        String[] receivingLocationNames = [locationService.getReceivingLocationName(shipmentNumber), "Receiving ${shipmentNumber}"]
        List<Location> locations = locationService.getInternalLocations(parentLocation, locationTypeCodes, activityCodeParams, receivingLocationNames)

        render([data: locations?.collect { [id: it.id, name: it.name, zoneId: it.zone?.id, zoneName: it.zone?.name] }] as JSON)
    }

    def read() {
        Location location = Location.get(params.id)
        render([data: location] as JSON)
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
            zebraService.printDocument(document, [internalLocation:internalLocation])

            render([data: "Barcode label has been printed"] as JSON)
            return
        } catch (Exception e) {
            render([errorCode: 500, cause: e?.class, errorMessage: e?.message] as JSON)
        }
    }

}
