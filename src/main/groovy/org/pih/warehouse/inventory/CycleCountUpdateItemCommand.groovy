package org.pih.warehouse.inventory

import grails.databinding.BindUsing
import grails.validation.Validateable
import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.ReasonCode

class CycleCountUpdateItemCommand implements Validateable {

    boolean recount

    @BindUsing({ obj, source ->
        def productId = source['inventoryItem']['product'] instanceof Map ? source['inventoryItem']['product']['id'] : source['inventoryItem']['product']
        Product product = Product.read(productId)
        InventoryItem inventoryItem = InventoryItem.findByProductAndLotNumber(product, source['inventoryItem']['lotNumber'])
        return inventoryItem ?: new InventoryItem(
                product: product,
                lotNumber: source['inventoryItem']['lotNumber'],
                expirationDate: source['inventoryItem']['expirationDate'] ? DateUtil.asDate(source['inventoryItem']['expirationDate'].toString()) : null
        )
    })

    InventoryItem inventoryItem

    Integer quantityCounted

    ReasonCode discrepancyReasonCode

    String comment

    CycleCountItem cycleCountItem

    Location binLocation

    Person assignee

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
        assignee(nullable: true)
        binLocation(nullable: true)
    }
}
