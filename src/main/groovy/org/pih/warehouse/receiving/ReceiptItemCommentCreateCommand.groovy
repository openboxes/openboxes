package org.pih.warehouse.receiving

import org.pih.warehouse.core.validation.ObjectValidatable

/**
 * The request body for adding a comment to a single receipt item.
 */
class ReceiptItemCommentCreateCommand extends ReceiptItemCommentSaveCommand
        implements ObjectValidatable<ReceiptItemCommentCreateCommandValidator> {
}
