package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class ReceiptItemCommentUpdateCommandValidator implements ObjectValidator<ReceiptItemCommentUpdateCommand> {

    @Override
    ObjectValidationResult doValidate(ReceiptItemCommentUpdateCommand command) {
        return new ObjectValidationResult(
                validateReceiptItemHasComment(command),
        )
    }

    /**
     * Editing a comment is only allowed when the receipt item already has one - adding a new
     * comment goes through the create endpoint instead.
     */
    private ObjectError validateReceiptItemHasComment(ReceiptItemCommentUpdateCommand command) {
        // A missing receipt item is already reported by the command's nullable constraint and the validator
        // still runs after that failure (there is no short-circuit), so guard against NPE first.
        if (!command.receiptItem) {
            return null
        }

        if (!command.receiptItem.comment) {
            return rejectField("receiptItem", command.receiptItem,
                    "receiptItemCommentUpdateCommand.receiptItem.commentNotFound",
                    [command.receiptItem.id])
        }

        return null
    }
}
