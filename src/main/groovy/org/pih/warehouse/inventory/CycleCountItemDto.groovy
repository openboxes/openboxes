package org.pih.warehouse.inventory

import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.dtos.IdentifiableDto
import org.pih.warehouse.location.BinLocationDto
import org.pih.warehouse.location.FacilityDto
import org.pih.warehouse.product.ProductSimpleDto

class CycleCountItemDto implements IdentifiableDto {

    FacilityDto facility

    BinLocationDto binLocation

    ProductSimpleDto product

    InventoryItem inventoryItem

    Integer countIndex

    CycleCountItemStatus status

    Integer quantityOnHand

    Integer quantityCounted

    Integer quantityVariance

    ReasonCode discrepancyReasonCode

    String comment

    Boolean custom

    Date dateCounted

    Date dateCreated

    Person assignee

    static CycleCountItemDto from(CycleCountItem cycleCountItem) {
        return new CycleCountItemDto(
                id: cycleCountItem.id,
                facility: FacilityDto.from(cycleCountItem.facility),
                product: ProductSimpleDto.from(cycleCountItem.product),
                inventoryItem: cycleCountItem.inventoryItem,
                binLocation: BinLocationDto.from(cycleCountItem.location),
                countIndex: cycleCountItem.countIndex,
                status: cycleCountItem.status,
                quantityOnHand: cycleCountItem.quantityOnHand,
                quantityCounted: cycleCountItem.quantityCounted,
                quantityVariance: cycleCountItem.quantityVariance,
                discrepancyReasonCode: cycleCountItem.discrepancyReasonCode,
                dateCounted: cycleCountItem.dateCounted,
                dateCreated: cycleCountItem.dateCreated,
                comment: cycleCountItem.comment,
                custom: cycleCountItem.custom,
                assignee: cycleCountItem.assignee,
        )
    }
}
