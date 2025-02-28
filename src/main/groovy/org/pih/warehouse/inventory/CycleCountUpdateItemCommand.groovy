package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.User
import org.springframework.web.context.request.RequestContextHolder

class CycleCountUpdateItemCommand implements Validateable {

    boolean recount

    Integer quantityCounted

    String comment

    CycleCountItem cycleCountItem

    User assignee

    def beforeValidate() {
        String cycleCountItemId = RequestContextHolder.getRequestAttributes().params?.cycleCountItemId
        cycleCountItem = CycleCountItem.get(cycleCountItemId)
    }

    static constraints = {
        quantityCounted(nullable: true)
        comment(nullable: true, blank: true)
    }
}
