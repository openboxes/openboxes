package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.springframework.web.context.request.RequestContextHolder

class CycleCountItemCommand implements Validateable {

    boolean recount

    @BindUsing({ obj, source ->
        Product product = Product.read(source['inventoryItem']['product'])
        return InventoryItem.findByProductAndLotNumber(product, source['inventoryItem']['lotNumber'])
    })
    InventoryItem inventoryItem

    Location binLocation

    Integer quantityCounted

    ReasonCode discrepancyReasonCode

    String comment

    CycleCount cycleCount

    Location facility

    User assignee

    def beforeValidate() {
        String cycleCountId = RequestContextHolder.getRequestAttributes().params?.cycleCountId
        String facilityId = RequestContextHolder.getRequestAttributes().params?.facility
        cycleCount = CycleCount.read(cycleCountId)
        facility = Location.read(facilityId)
    }

    static constraints = {
        quantityCounted(nullable: true)
        discrepancyReasonCode(nullable: true, validator: { ReasonCode discrepancyReasonCode ->
            return ReasonCode.listInventoryAdjustmentReasonCodes().contains(discrepancyReasonCode) ?
                    true :
                    ['cycleCountItemCommand.discrepancyReasonCode.invalid']
        })
        comment(nullable: true, blank: true)
    }
}
