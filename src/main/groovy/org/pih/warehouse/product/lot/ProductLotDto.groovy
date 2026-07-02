package org.pih.warehouse.product.lot

import java.time.LocalDate

import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.date.LocalDateParser
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
    // A date-only LocalDate (not Date) so it serializes as e.g. "2028-03-01" instead of an instant that the server's
    // timezone offset can shift to the previous day (e.g. "2028-02-29T23:00:00Z"). See the LocalDate JSON marshaller.
    LocalDate expirationDate
    LotStatusCode lotStatus
    String comments

    static ProductLotDto from(InventoryItem inventoryItem) {
        return !inventoryItem ? null : new ProductLotDto(
                product: ProductSimpleDto.from(inventoryItem.product),
                lotNumber: inventoryItem.lotNumber,
                // InventoryItem.expirationDate is a (legacy) java.util.Date. Resolve it to a calendar date in the
                // system zone, mirroring how the InventoryItem JSON marshaller formats it (MM/dd/yyyy, system zone).
                expirationDate: LocalDateParser.asLocalDate(inventoryItem.expirationDate, DateUtil.systemZoneId),
                lotStatus: inventoryItem.lotStatus,
                comments: inventoryItem.comments
        )
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                product: product?.asResponseBody(),
                lotNumber: lotNumber,
                expirationDate: expirationDate,
                lotStatus: lotStatus,
                comments: comments,
        ]
    }
}
