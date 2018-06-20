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
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode

class LocationApiController {

    def locationService

    def list = {
        def fields = params.fields ? params.fields.split(",") : null
        def locations = locationService.getAllLocations(fields)
		render ([data:locations] as JSON)
	}

    def read = {
        Location location = Location.get(params.id)
        if (!location) {
            throw new ObjectNotFoundException(params.id, Location.class.toString())
        }
        render ([data:location] as JSON)
    }


}
