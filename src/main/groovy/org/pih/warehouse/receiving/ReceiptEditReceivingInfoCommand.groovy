package org.pih.warehouse.receiving

import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.validation.ObjectValidatable
import org.pih.warehouse.shipping.ShipmentItem

/**
 * The request body for editing the receiving info of the receipt items of a single shipment item.
 *
 * The receipt and shipment item are identified by the URL path rather than the request body, so they are bound in
 * {@link #beforeValidate} (the data binding source only contains the JSON body, not the URL path variables). Binding
 * them here means they are available to the validator (e.g. to check that the receipt is still pending) and don't
 * have to be threaded through to the service as separate arguments. Both are required (the default for command object
 * properties), so a missing/unknown identifier yields the conventional "cannot be null" validation error.
 */
class ReceiptEditReceivingInfoCommand implements ObjectValidatable<ReceiptEditReceivingInfoCommandValidator> {

    // Bound from the URL path variables in beforeValidate(), not from the request body.
    Receipt receipt
    ShipmentItem shipmentItem

    // Receipt items to create (receiptItem == null) or update (receiptItem != null).
    List<ReceiptItemEditReceivingInfoRequest> itemsToSave = []

    def beforeValidate() {
        Map<String, Object> params = RequestContextHolder.getRequestAttributes().params
        receipt = Receipt.get(params?.receiptId)
        shipmentItem = ShipmentItem.get(params?.shipmentItemId)
    }
}
