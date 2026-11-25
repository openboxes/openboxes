package org.pih.warehouse.receiving

/**
 * The response body returned after saving (creating/updating) a batch of receipt items.
 */
class ReceiptSaveResponseDto {

    // The receipt items that were created or updated as a part of the batch.
    List<ReceiptItemSaveDto> updatedLines = []
}
