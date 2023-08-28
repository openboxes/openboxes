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
import grails.validation.ValidationException
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationGroupDataService
import org.pih.warehouse.core.LocationGroupService

class LocationGroupApiController extends BaseDomainApiController {

    LocationGroupService locationGroupService
    LocationGroupDataService locationGroupDataService

    def list() {
        List<LocationGroup> locationGroups = locationGroupService.getLocationGroups(params)
        render ([data:locationGroups] as JSON)
    }

    def read() {
        LocationGroup locationGroup = locationGroupDataService.get(params.id)
        if (!locationGroup) {
            throw new IllegalArgumentException("No Location Group found for location group ID ${params.id}")
        }

        render([data: locationGroup] as JSON)
    }

    def create() {
        JSONObject jsonObject = request.JSON

        LocationGroup locationGroup = new LocationGroup()
        bindData(locationGroup, jsonObject)

        if (locationGroup.hasErrors() || !locationGroupDataService.save(locationGroup)) {
            throw new ValidationException("Invalid location group", locationGroup.errors)
        }

        render([data: [id: locationGroup.id]] as JSON)
    }

}
