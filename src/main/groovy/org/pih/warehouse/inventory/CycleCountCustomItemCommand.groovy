package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.springframework.web.context.request.RequestContextHolder

class CycleCountCustomItemCommand implements Validateable {

    boolean recount

    @BindUsing({ obj, source ->
        Product product = Product.read(source['inventoryItem']['product'])
        Date expirationDate = new Date(source['inventoryItem']['expirationDate'])
        return InventoryItem.findByProductAndLotNumberAndExpirationDate(product, source['inventoryItem']['lotNumber'], expirationDate)
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
        quantityCounted(nullable: true) // FIXME: should we allow for empty quantityCounted?
        comment(nullable: true, blank: true)
    }
}
