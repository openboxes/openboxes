package org.pih.warehouse.inventory

import grails.validation.Validateable
import java.time.LocalDate

import org.pih.warehouse.core.Person
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.ReasonCode

class CycleCountUpdateItemCommand implements Validateable {

    String id

    boolean recount

    Integer quantityCounted

    ReasonCode discrepancyReasonCode

    String comment

    CycleCountItem cycleCountItem

    Person assignee

    LocalDate dateCounted

    def beforeValidate() {
        String cycleCountItemId = RequestContextHolder.getRequestAttributes().params?.cycleCountItemId ?: id
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
        assignee(nullable: true)
        dateCounted(nullable: true)
    }
}
