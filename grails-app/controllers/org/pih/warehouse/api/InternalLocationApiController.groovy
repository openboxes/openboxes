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
import org.pih.warehouse.core.InternalLocationSearchCommand
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.ReceivingLocationSearchCommand
import org.pih.warehouse.core.ZebraService

@Transactional
class InternalLocationApiController {

    LocationService locationService
    ZebraService zebraService

    def list(InternalLocationSearchCommand command) {
        command.location = getFacility(command)

        List<Location> locations = locationService.getInternalLocations(command)
        render([data: locations?.collect { [id: it.id, name: it.name, zoneId: it.zone?.id, zoneName: it.zone?.name] }] as JSON)
    }

    def search() {
        LocationTypeCode[] locationTypeCodes = params.locationTypeCode ? params.list("locationTypeCode") : [LocationTypeCode.INTERNAL, LocationTypeCode.BIN_LOCATION]
        List<Location> locations = locationService.searchInternalLocations(params, locationTypeCodes)
        render([data: locations, totalCount: locations?.totalCount] as JSON)
    }

    def listReceiving(ReceivingLocationSearchCommand command) {
        command.location = getFacility(command)

        // When searching receiving bins, we need different default search parameters. Mainly we want to include
        // the normal bin locations as well as only the receiving bins that match the given shipment number.
        command.locationTypeCode = params.locationTypeCode ? params.list("locationTypeCode") : [LocationTypeCode.BIN_LOCATION]
        command.ignoreActivityCodes = params.ignoreActivityCodes ? params.list("ignoreActivityCodes") : [ActivityCode.RECEIVE_STOCK]
        command.locationNames = [
                locationService.getReceivingLocationName(command.shipmentNumber),
                "Receiving ${command.shipmentNumber}"
        ]

        List<Location> locations = locationService.getInternalLocations(command)
        render([data: locations?.collect { [id: it.id, name: it.name, zoneId: it.zone?.id, zoneName: it.zone?.name] }] as JSON)
    }

    def read() {
        Location location = Location.get(params.id)
        render([data: location] as JSON)
    }

    /**
     * Computes the parent location, ie facility of a given internal location search request.
     */
    private Location getFacility(InternalLocationSearchCommand command) {
        if (command.location != null) {
            return command.location
        }

        // We need to do this fallback here instead of in a BindUsing in the command because the command doesn't
        // have access to the session.
        command.location = Location.get(session?.warehouse?.id)
        if (command.location == null) {
            throw new IllegalArgumentException("Must provide location.id as a request parameter")
        }
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
