package org.pih.warehouse.shipping

import org.pih.warehouse.core.http.ResponseBodyFormattable
import org.pih.warehouse.location.LocationSimpleDto
import org.pih.warehouse.product.lot.ProductLotDto

class ShipmentItemDto implements ResponseBodyFormattable {
    String id
    String shipmentId
    String requisitionItemId
    ProductLotDto productLot
    LocationSimpleDto binLocation
    Integer quantity
    String recipientId
    String donorId
    ContainerSimpleDto container
    Date dateCreated
    Date lastUpdated

    static ShipmentItemDto from(ShipmentItem shipmentItem) {
        return !shipmentItem ? null : new ShipmentItemDto(
                id: shipmentItem.id,
                shipmentId: shipmentItem.shipmentId,
                requisitionItemId: shipmentItem.requisitionItemId,
                productLot: ProductLotDto.from(shipmentItem.inventoryItem),
                binLocation: LocationSimpleDto.from(shipmentItem.binLocation),
                quantity: shipmentItem.quantity,
                recipientId: shipmentItem.recipientId,
                donorId: shipmentItem.donorId,
                container: ContainerSimpleDto.from(shipmentItem.container),
                dateCreated: shipmentItem.dateCreated,
                lastUpdated: shipmentItem.lastUpdated,
        )
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                id: id,
                shipmentId: shipmentId,
                requisitionItemId: requisitionItemId,
                productLot: productLot,
                binLocation: binLocation,
                quantity: quantity,
                recipientId: recipientId,
                donorId: donorId,
                container: container,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
        ]
    }
}
