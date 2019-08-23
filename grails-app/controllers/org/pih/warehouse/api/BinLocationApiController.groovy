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
import org.hibernate.UnresolvableObjectException
import org.pih.warehouse.core.Location

class BinLocationApiController {

    def locationService

    def list = {
        String locationId = params?.parentLocation?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        if (!location) {
            throw new UnresolvableObjectException("No bin locations for location ${params?.parentLocation?.id}", params?.parentLocation?.id, Location.class.toString())
        }

        render([data: location?.binLocations] as JSON)
    }

    def read = {
        Location binLocation = Location.get(params.id)
        if (!binLocation) {
            throw new ObjectNotFoundException(params.id, Location.class.toString())
        }
        render([data: binLocation] as JSON)
    }


}
