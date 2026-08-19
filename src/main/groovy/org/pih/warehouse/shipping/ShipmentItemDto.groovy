package org.pih.warehouse.shipping

import org.pih.warehouse.location.LocationSimpleDto
import org.pih.warehouse.product.lot.ProductLotDto

class ShipmentItemDto {
    String id
    String shipmentId
    String requisitionItemId
    ProductLotDto productLot
    LocationSimpleDto binLocation
    Integer quantity
    String recipientId
    String donorId
    String supplierCode
    ContainerSimpleDto container
    String unitOfMeasure
    BigDecimal packSize
    Date dateCreated
    Date lastUpdated
    
    static ShipmentItemDto from(ShipmentItem shipmentItem, String supplierCode = null) {
        return !shipmentItem ? null : new ShipmentItemDto(
                id: shipmentItem.id,
                shipmentId: shipmentItem.shipmentId,
                requisitionItemId: shipmentItem.requisitionItemId,
                productLot: ProductLotDto.from(shipmentItem.inventoryItem),
                binLocation: LocationSimpleDto.from(shipmentItem.binLocation),
                quantity: shipmentItem.quantity,
                recipientId: shipmentItem.recipient?.id,
                donorId: shipmentItem.donorId,
                supplierCode: supplierCode,
                container: ContainerSimpleDto.from(shipmentItem.container),
                unitOfMeasure: shipmentItem.unitOfMeasure,
                packSize: shipmentItem.packSize,
                dateCreated: shipmentItem.dateCreated,
                lastUpdated: shipmentItem.lastUpdated,
        )
    }
}
