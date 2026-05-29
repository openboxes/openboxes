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
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationGroupService

class LocationGroupApiController extends BaseDomainApiController {

    LocationGroupService locationGroupService

    def list() {
        List<LocationGroup> locationGroups = locationGroupService.getLocationGroups(params)
        render([data: locationGroups] as JSON)
    }

    def read() {
        LocationGroup locationGroup = locationGroupService.getLocationGroup(params.id)
        render([data: locationGroup] as JSON)
    }

    def create() {
        LocationGroup locationGroup = locationGroupService.createLocationGroup(request.JSON as Map)
        render([data: [id: locationGroup.id]] as JSON)
    }

    def delete() {
        locationGroupService.deleteLocationGroup(params.id)
        render status: 204
    }
}
