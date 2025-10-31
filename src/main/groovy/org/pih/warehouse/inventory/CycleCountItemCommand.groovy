package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.validation.Validateable
import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.product.Product
import org.springframework.web.context.request.RequestContextHolder

class CycleCountItemCommand implements Validateable {

    boolean recount

    @BindUsing({ obj, source ->
        Product product = Product.read(source['inventoryItem']['product'])
        String lotNumber = source['inventoryItem']['lotNumber']
        InventoryItem inventoryItem = InventoryItem.findByProductAndLotNumber(product, lotNumber)

        // Currently default lotNumbers can be null or empty string,
        // to avoid the situation of not finding appropriate one
        // we are trying to find another inventoryItem with
        // empty lot
        if (!inventoryItem && !lotNumber) {
            String otherBlankLotNumber = lotNumber == null ? '' : null
            inventoryItem = InventoryItem.findByProductAndLotNumber(product, otherBlankLotNumber)
        }

        return inventoryItem ?: new InventoryItem(
                product: product,
                lotNumber: source['inventoryItem']['lotNumber'],
                expirationDate: source['inventoryItem']['expirationDate'] ? DateUtil.asDate(source['inventoryItem']['expirationDate'].toString()) : null
        )
    })
    InventoryItem inventoryItem

    Location binLocation

    Integer quantityCounted

    ReasonCode discrepancyReasonCode

    String comment

    CycleCount cycleCount

    Location facility

    Person assignee

    Date dateCounted

    def beforeValidate() {
        String cycleCountId = RequestContextHolder.getRequestAttributes().params?.cycleCountId
        String facilityId = RequestContextHolder.getRequestAttributes().params?.facility
        cycleCount = cycleCount ?: CycleCount.get(cycleCountId)
        facility = Location.read(facilityId)
    }

    static constraints = {
        quantityCounted(nullable: true)
        discrepancyReasonCode(nullable: true, validator: { ReasonCode discrepancyReasonCode ->
            if (!discrepancyReasonCode) {
                return true
            }

            return ReasonCode.listCycleCountReasonCodes().contains(discrepancyReasonCode) ? true : ['invalid']
        })
        comment(nullable: true, blank: true)
        assignee(nullable: true)
        binLocation(nullable: true)
        dateCounted(nullable: true)
    }
}
