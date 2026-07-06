package org.pih.warehouse.receiving

import java.time.LocalDate

import grails.validation.Validateable
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product

/**
 * A single line of a {@link ReceiptEditReceivingInfoCommand}: the editable receiving info of one receipt item.
 *
 * Unlike {@link ReceiptItemUpsertRequest} this intentionally does NOT expose the bin location (which is not editable
 * through this endpoint) and instead carries the product lot fields. The product, lotNumber and expirationDate are
 * used to find or create the InventoryItem that the receipt item points at, which is how the lot can be swapped.
 */
class ReceiptItemEditReceivingInfoRequest implements Validateable {

    // Existing receipt item to update. When not provided, a new receipt item is created.
    ReceiptItem receiptItem

    // Client-side identifier of the row (e.g. "temp-12345"), used to correlate the request with the response.
    String rowId

    // The product being received. Combined with the lot number and expiration date to resolve the inventory item.
    Product product

    // Lot number of the inventory item to receive against. Used (together with the product and expiration date) to
    // find or create the inventory item that the receipt item points at.
    String lotNumber

    // Expiration date of the lot. Only used when we need to create a new inventory item for the given lot number.
    // A date-only LocalDate (not Date) so that "03/01/2028" is unambiguous and never gets shifted by the server's
    // timezone offset when bound or serialized (see LocalDateValueConverter / the LocalDate JSON marshaller).
    LocalDate expirationDate

    Person recipient

    Integer quantityReceiving

    // Marks a receipt line that was split off from the shipment item's line while receiving.
    Boolean isSplitItem = Boolean.FALSE

    static constraints = {
        receiptItem(nullable: true)
        rowId(nullable: true)
        lotNumber(nullable: true, maxSize: 255)
        expirationDate(nullable: true)
        recipient(nullable: true)
        quantityReceiving(nullable: true)
        isSplitItem(nullable: true)
    }
}
