package org.pih.warehouse.replenishment

import grails.validation.Validateable
import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.core.Location

class SearchReplenishmentTaskCommand implements Validateable {

    Location facility
    String assigneeId
    List<PickTaskStatus> status
    String outboundContainerId
    String orderId

    static constraints = {
        facility nullable: false
        assigneeId nullable: true
        status nullable: true
        outboundContainerId nullable: true
        orderId nullable: true
    }
}
