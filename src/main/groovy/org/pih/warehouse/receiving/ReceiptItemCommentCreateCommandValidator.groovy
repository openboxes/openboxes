package org.pih.warehouse.receiving

import org.springframework.stereotype.Component
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.core.validation.ObjectValidator

@Component
class ReceiptItemCommentCreateCommandValidator implements ObjectValidator<ReceiptItemCommentCreateCommand> {

    @Override
    ObjectValidationResult doValidate(ReceiptItemCommentCreateCommand command) {
        return new ObjectValidationResult(
                validateReceiptItemHasNoComment(command),
        )
    }

    /**
     * Adding a comment is only allowed when the receipt item doesn't have one yet - editing an existing
     * comment goes through the update endpoint instead.
     */
    private ObjectError validateReceiptItemHasNoComment(ReceiptItemCommentCreateCommand command) {
        // A missing receipt item is already reported by the command's nullable constraint and the validator
        // still runs after that failure (there is no short-circuit), so guard against NPE first.
        if (!command.receiptItem) {
            return null
        }

        if (command.receiptItem.comment != null) {
            return rejectField("receiptItem", command.receiptItem,
                    "receiptItemCommentCreateCommand.receiptItem.commentAlreadyExists",
                    [command.receiptItem.id])
        }

        return null
    }
}
