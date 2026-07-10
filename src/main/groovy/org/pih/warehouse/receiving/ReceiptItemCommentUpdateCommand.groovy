package org.pih.warehouse.receiving

import org.pih.warehouse.core.validation.ObjectValidatable

/**
 * The request body for editing the existing comment of a single receipt item.
 */
class ReceiptItemCommentUpdateCommand extends ReceiptItemCommentSaveCommand
        implements ObjectValidatable<ReceiptItemCommentUpdateCommandValidator> {
}