package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.validation.Validateable
import org.pih.warehouse.core.Location
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

    String comment

    CycleCount cycleCount

    Location facility

    def beforeValidate() {
        String cycleCountId = RequestContextHolder.getRequestAttributes().params?.cycleCountId
        String facilityId = RequestContextHolder.getRequestAttributes().params?.facility
        cycleCount = CycleCount.read(cycleCountId)
        facility = Location.read(facilityId)
    }

    static constraints = {
        quantityCounted(nullable: true)
        comment(nullable: true, blank: true)
    }
}
