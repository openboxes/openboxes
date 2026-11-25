package org.pih.warehouse.inventory

import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.dtos.DomainDto
import org.pih.warehouse.core.mapper.SmartMapper
import org.pih.warehouse.location.BinLocationDto
import org.pih.warehouse.location.FacilityDto
import org.pih.warehouse.product.ProductDto

class CycleCountItemDto implements DomainDto<CycleCountItem> {

    FacilityDto facility

    BinLocationDto binLocation

    ProductDto product

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
}
