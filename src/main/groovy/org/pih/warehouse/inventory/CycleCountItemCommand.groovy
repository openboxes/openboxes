package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.util.Holders
import grails.validation.Validateable

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.springframework.web.context.request.RequestContextHolder

class CycleCountItemCommand implements Validateable {

    boolean recount

    @BindUsing({ obj, source ->
        CycleCountService cycleCountService = Holders.grailsApplication.mainContext.getBean(CycleCountService)
        return cycleCountService.bindInventoryItem(source as Map)
    })
    InventoryItem inventoryItem

    Location binLocation

    Integer quantityCounted

    ReasonCode discrepancyReasonCode

    String comment

    CycleCount cycleCount

    Location facility

    Person assignee

    def beforeValidate() {
        String cycleCountId = RequestContextHolder.getRequestAttributes().params?.cycleCountId
        String facilityId = RequestContextHolder.getRequestAttributes().params?.facility
        cycleCount = CycleCount.read(cycleCountId)
        facility = Location.read(facilityId)
    }

    static constraints = {
        quantityCounted(nullable: true)
        discrepancyReasonCode(nullable: true, validator: { ReasonCode discrepancyReasonCode ->
            if (!discrepancyReasonCode) {
                return true
            }

            return ReasonCode.listInventoryAdjustmentReasonCodes().contains(discrepancyReasonCode) ?
                    true :
                    ['cycleCountItemCommand.discrepancyReasonCode.invalid']
        })
        comment(nullable: true, blank: true)
        assignee(nullable: true)
        binLocation(nullable: true)
    }
}
