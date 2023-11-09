/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.importer

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization

@Transactional
class LocationImportDataService implements ImportDataService {
    LocationService locationService

    void validateData(ImportDataCommand command) {

        def locationNamesToImport = command.data.collect {it.name}
        def locationNumbersToImport = command.data.collect {it.locationNumber}

        command.data.eachWithIndex { params, index ->
            Location location = params.id ? Location.findById(params.id) : null
            Location parentLocation = params.parentLocation ? Location.findByName(params.parentLocation) : null
            LocationGroup locationGroup = params.locationGroup ? LocationGroup.findByName(params.locationGroup) : null
            String locationTypeName = LocalizationUtil.getDefaultString(params.locationType as String)
            // TODO: Replace with a single GORM .find with Closure when in Grails 3 (available since Grails 2.0)
            LocationType locationType = LocationType
                    .findAllByNameLike(locationTypeName + "%")
                    .find{ LocalizationUtil.getDefaultString(it.name) == locationTypeName}
            List<Organization> organizations = params.organization ? Organization.findAllByCodeOrName(params.organization, params.organization) : null

            if (params.id && !location) {
                command.errors.reject("Row ${index + 1}: Id '${params.id}' do not exists in the system.")
            }

            // Location name should be unique unless we are editing location (id should be added)
            if (Location.findByName(params.name) && !(params.id && params.name == location?.name)) {
                command.errors.reject("Row ${index + 1}: '${params.name}' already exists in the system. Choose a unique name")
            }

            if (!params.active && !(params.active instanceof Boolean)) {
                command.errors.reject("Row ${index + 1}: Active field is obligatory. Please fill with TRUE or FALSE")
            }

            if (params.active && !(params.active instanceof Boolean) && !"true".equalsIgnoreCase(params.active) && !"false".equalsIgnoreCase(params.active)) {
                command.errors.reject("Row ${index + 1}: Active field is incorrectly filled. Please fill with TRUE or FALSE")
            }

            // Location number should be unique unless we are editing location
            if (params.locationNumber && Location.findByLocationNumber(params.locationNumber) && !(params.id && params.locationNumber == location?.locationNumber)) {
                command.errors.reject("Row ${index + 1}: '${params.locationNumber}' already exists in the system. Choose a unique location number")
            }

            if (locationNamesToImport.findAll { it == params.name }.size() > 1) {
                command.errors.reject("Row ${index + 1}: '${params.name}' is not a unique name in imported data")
            }

            if (params.locationNumber && locationNumbersToImport.findAll { it == params.locationNumber }.size() > 1) {
                command.errors.reject("Row ${index + 1}: '${params.locationNumber}' is not a unique location number in imported data")
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

            if (organizations && !organizations.first()?.active) {
                command.errors.reject("Row ${index + 1}: Organization ${organizations.first().name} is inactive. You can't assign it to any location")
            }

            // Do not allow to change internal type location to non-internal
            if (params.locationType && location && (location.locationType.isInternalLocation() || location.locationType.isZone()) && !(LocationType.findByNameLike(params.locationType + "%").isInternalLocation() || LocationType.findByNameLike(params.locationType + "%").isZone())) {
                command.errors.reject("Row ${index + 1}: Changing Location Type from internal to '${params.locationType}' is not possible")
            }
        }
    }

    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            Location location = locationService.createOrUpdateLocation(params)
            if (!location.validate() || !location.save(failOnError: true)) {
                throw new ValidationException("Invalid location ${location.name}", location.errors)
            }
        }
    }
}
