/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.springframework.dao.DataIntegrityViolationException

@Transactional
class LocationGroupService {

    List getLocationGroups(Map params) {
        def locationGroups = LocationGroup.createCriteria().list(params){
            if (params.q) {
                or {
                    ilike("id", "${params.q}%")
                    ilike("name", "${params.q}%")
                }
            }
        }
        return locationGroups
    }

    LocationGroup getLocationGroup(String id) {
        LocationGroup locationGroup = LocationGroup.get(id)
        if (!locationGroup) {
            throw new IllegalArgumentException("No Location Group found for location group ID ${id}")
        }
        return locationGroup
    }

    LocationGroup createLocationGroup(Map params) {
        LocationGroup locationGroup = new LocationGroup(params)
        return locationGroup.save(failOnError: true, flush: true)
    }


    void deleteLocationGroup(String id) {
        LocationGroup locationGroup = getLocationGroup(id)
        locationGroup.delete(flush: true)
    }
}
