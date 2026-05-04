package org.pih.warehouse.importer

import org.springframework.context.MessageSourceResolvable

/**
 * Represents an error that occurred in the import / bulk data binding process.
 */
class BulkDataError {

    /**
     * The zero-indexed row that the error occurred in.
     * Will be null if the error is not directly associated with a particular row.
     */
    Integer row

    /**
     * The zero-indexed column that the error occurred in.
     * Will be null if the error is not directly associated with a particular column.
     */
    Integer column

    /**
     * The "importance" of the error. Higher severity errors may be used to block the flow from continuing.
     */
    BulkDataErrorSeverity severity

    /**
     * The localized error message to display.
     */
    String localizedMessage

    /**
     * An error message that has not yet been localized. Useful for holding the messages from the validate step,
     * which relies on the org.springframework.validation.Errors that are added to a grails.validation.Validateable
     * object after calling validate() on it.
     *
     * We expect localizable messages to be localized prior to display / returning a request response. As a part
     * of this process, we should populate the localizedMessage field with the result of localizing this message.
     */
    MessageSourceResolvable localizableMessage

    /**
     * The Exception that was throw when the error was triggered.
     * Not to be directly displayed to users but can be useful for debugging.
     */
    Exception exception
}
