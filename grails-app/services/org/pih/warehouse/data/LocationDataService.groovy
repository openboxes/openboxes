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

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.importer.ImportDataCommand
import org.springframework.validation.BeanPropertyBindingResult

class LocationDataService {

    /**
     * Validate inventory levels
     */
    Boolean validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->

            Location location = createOrUpdateLocation(params)

            LocationGroup locationGroup = params.locationGroup ? LocationGroup.findByName(params.locationGroup) : null
            if (params.locationGroup && !locationGroup) {
                command.errors.reject("Row ${index + 1}: Location group ${params.locationGroup} for location ${params.name} does not exist")
            }

            if (!location.validate()) {
                location.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Location ${location.name} is invalid: ${error.getFieldError()}")
                }
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
        Location location = Location.findByNameOrLocationNumber(params.name, params.name)
        if (!location) {
            location = new Location()
        }

        location.name = params.name
        location.locationNumber = params.locationNumber
        location.locationType = params.locationType ? LocationType.findByNameLike(params.locationType + "%") : null
        location.locationGroup = params.locationGroup ? LocationGroup.findByName(params.locationGroup) : null
        location.parentLocation = params.parentLocation ? Location.findByNameOrLocationNumber(params.parentLocation, params.parentLocation) : null


        return location
    }
}
