/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.shipping

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.api.BaseDomainApiController

class ShipmentItemApiController extends BaseDomainApiController {

    def shipmentService

    def update = {
        JSONObject jsonObject = request.JSON
        log.info "Update shipment " + jsonObject.toString(4)

        String action = jsonObject.has("action") ? jsonObject.get("action") : null
        if (!action) {
            forward(controller: "genericApi", action: "update")
            return
        }

        ShipmentItem shipmentItem = ShipmentItem.get(params.id)
        if (action.equalsIgnoreCase("PACK")) {
            String containerId = jsonObject.has("container.id") && !jsonObject.isNull("container.id") ?
                    jsonObject.get("container.id") : null

            Integer quantityToPack = jsonObject.has("quantityToPack") && !jsonObject.isNull("quantityToPack") ?
                    jsonObject.get("quantityToPack") : null

            shipmentItem = shipmentService.packItem(params.id, containerId, quantityToPack)
        }

        render ([data: shipmentItem] as JSON)
    }

}
