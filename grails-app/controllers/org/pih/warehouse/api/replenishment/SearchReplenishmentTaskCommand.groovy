/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api.replenishment

import grails.validation.Validateable
import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.core.Location

class SearchReplenishmentTaskCommand implements Validateable {

    Location facility
    // The order id or order number of the replenishment list
    String orderId
    String assigneeId
    List<PickTaskStatus> status
    String outboundContainerId

    static constraints = {
        facility nullable: false
        orderId nullable: true
        assigneeId nullable: true
        status nullable: true
        outboundContainerId nullable: true
    }
}
