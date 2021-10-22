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
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.OrderItem

/**
 * Should not extend BaseDomainApiController since putawayItem is not a valid domain.
 */
class PutawayItemApiController {

    def putawayService

    def list = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        if (!location) {
            throw new IllegalArgumentException("Must provide location.id as request parameter")
        }
        List putawayItems = putawayService.getPutawayCandidates(location)
        render([data: putawayItems.collect { it.toJson() }] as JSON)
    }

    def removingItem = {
        putawayService.deletePutawayItem(params.id)

        render status: 204
    }
}
