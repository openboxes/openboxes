package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.User
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.ReasonCode

class CycleCountUpdateItemCommand implements Validateable {

    boolean recount

    Integer quantityCounted

    ReasonCode discrepancyReasonCode

    String comment

    CycleCountItem cycleCountItem

    User assignee

    def beforeValidate() {
        String cycleCountItemId = RequestContextHolder.getRequestAttributes().params?.cycleCountItemId
        cycleCountItem = CycleCountItem.get(cycleCountItemId)
    }

    static constraints = {
        quantityCounted(nullable: true)
        discrepancyReasonCode(nullable: true, validator: { ReasonCode discrepancyReasonCode ->
            if (!discrepancyReasonCode) {
                return true
            }

            return ReasonCode.listInventoryAdjustmentReasonCodes().contains(discrepancyReasonCode) ?
                    true :
                    ['cycleCountUpdateItemCommand.discrepancyReasonCode.invalid']
        })
        comment(nullable: true, blank: true)
    }
}
