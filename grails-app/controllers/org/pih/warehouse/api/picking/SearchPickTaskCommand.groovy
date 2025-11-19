package org.pih.warehouse.api.picking

import grails.validation.Validateable
import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location

class SearchPickTaskCommand implements Validateable {

    Location facility
    DeliveryTypeCode deliveryTypeCode
    Integer ordersCount
    String assigneeId
    List<PickTaskStatus> status
    Integer priority

    static constraints = {
        facility nullable: false
        deliveryTypeCode nullable: true
        ordersCount nullable: true
        assigneeId nullable: true
        status nullable: true
        priority nullable: true
    }
}
