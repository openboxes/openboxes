package org.pih.warehouse.product.lot

import org.pih.warehouse.core.http.ResponseBodyFormattable
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.LotStatusCode
import org.pih.warehouse.product.ProductSimpleDto

/**
 * Represents a specific lot number of a product.
 * The DTO form of an InventoryItem.
 */
class ProductLotDto implements ResponseBodyFormattable {

    ProductSimpleDto product
    String lotNumber
    Date expirationDate
    LotStatusCode lotStatus
    String comments

    static ProductLotDto from(InventoryItem inventoryItem) {
        return !inventoryItem ? null : new ProductLotDto(
                product: ProductSimpleDto.from(inventoryItem.product),
                lotNumber: inventoryItem.lotNumber,
                expirationDate: inventoryItem.expirationDate,
                lotStatus: inventoryItem.lotStatus,
                comments: inventoryItem.comments
        )
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                product: product.asResponseBody(),
                lotNumber: lotNumber,
                expirationDate: expirationDate,
                lotStatus: lotStatus,
                comments: comments,
        ]
    }
}
