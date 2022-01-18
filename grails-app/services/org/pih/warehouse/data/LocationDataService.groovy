/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.data

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.importer.ImportDataCommand

class LocationDataService {

    def organizationService
    def identifierService

    /**
     * Validate inventory levels
     */
    Boolean validateData(ImportDataCommand command) {

        def locationNamesToImport = command.data.collect {it.name}

        command.data.eachWithIndex { params, index ->
            Location parentLocation = params.parentLocation ? Location.findByName(params.parentLocation) : null
            LocationGroup locationGroup = params.locationGroup ? LocationGroup.findByName(params.locationGroup) : null
            LocationType locationType = params.locationType ? LocationType.findByName(params.locationType) : null
            List<Organization> organizations = params.organization ? Organization.findAllByCodeOrName(params.organization, params.organization) : null

            if (Location.findByName(params.name)) {
                command.errors.reject("Row ${index + 1}: '${params.name}' already exists in the system. Choose a unique name")
            }

            if (locationNamesToImport.findAll { it == params.name }.size() > 1) {
                command.errors.reject("Row ${index + 1}: '${params.name}' is not a unique name in imported data")
            }

            if (params.parentLocation && !parentLocation) {
                command.errors.reject("Row ${index + 1}: Parent Location '${params.parentLocation}' does not exist")
            }

            if (params.locationGroup && !locationGroup) {
                command.errors.reject("Row ${index + 1}: Location Group '${params.locationGroup}' does not exist")
            }

            if (!locationType) {
                command.errors.reject("Row ${index + 1}: Location Type '${params.locationType}' does not exist")
            }

            if ((locationType?.isInternalLocation() || locationType?.isZone()) && !parentLocation) {
                command.errors.reject("Row ${index + 1}: Location Type '${params.locationType}' requires a parent location")
            }

            if (!(locationType?.isInternalLocation() || locationType?.isZone()) && parentLocation) {
                command.errors.reject("Row ${index + 1}: Cannot assign a parent location to a location of type '${params.locationType}'")
            }

            if (!(locationType?.isInternalLocation() || locationType?.isZone()) && !params.organization) {
                command.errors.reject("Row ${index + 1}: Location Type '${params.locationType}' requires an organization")
            }

            if (organizations?.size() > 1) {
                command.errors.reject("Row ${index + 1}: '${organizations.size()}' records found for organization name '${params.organization}'. Please specify by entering the organization code instead")
            }
        }
    }

    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            Location location = createOrUpdateLocation(params)
            if (location.validate()) {
                location.save(failOnError: true)
            }
        }

    }

    Location createOrUpdateLocation(Map params) {
        Location location = Location.findByNameOrLocationNumber(params.name, params.locationNumber)
        if (!location) {
            location = new Location()
        }

        location.name = params.name
        location.locationNumber = params.locationNumber
        location.locationType = params.locationType ? LocationType.findByNameLike(params.locationType + "%") : null
        location.locationGroup = params.locationGroup ? LocationGroup.findByName(params.locationGroup) : null
        location.parentLocation = params.parentLocation ? Location.findByNameOrLocationNumber(params.parentLocation, params.parentLocation) : null
        location.organization = params.organization ? Organization.findByCodeOrName(params.organization, params.organization) : null

        // Add required association to organization for depots and suppliers
        if (!(location.locationType?.isInternalLocation() || location.locationType?.isZone()) && !location.organization) {
            def locationCode = identifierService.generateOrganizationIdentifier(params.organization)
            Location currentLocation = AuthService?.currentLocation?.get()
            Organization organization = (location.locationType?.locationTypeCode == LocationTypeCode.SUPPLIER) ?
                    organizationService.findOrCreateSupplierOrganization(params?.organization, locationCode) :
                    organizationService.findOrCreateOrganization(params?.organization, locationCode)

            location.organization = organization
        }

        return location
    }
}
