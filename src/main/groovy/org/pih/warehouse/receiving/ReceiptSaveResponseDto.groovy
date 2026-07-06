package org.pih.warehouse.receiving

import org.pih.warehouse.core.http.ResponseBodyFormattable

/**
 * The response body returned after saving (creating/updating) a batch of receipt items.
 */
class ReceiptSaveResponseDto implements ResponseBodyFormattable {

    // The receipt items that were created or updated as a part of the batch.
    List<ReceiptItemSaveDto> updatedLines = []

    @Override
    Map<String, Object> asResponseBody() {
        return [
                updatedLines: updatedLines*.asResponseBody(),
        ]
    }
}
