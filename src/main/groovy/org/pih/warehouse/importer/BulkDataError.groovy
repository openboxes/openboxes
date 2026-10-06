package org.pih.warehouse.importer

import org.pih.warehouse.core.localization.LocalizableMessage

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
     * Either the zero-indexed column, or an Excel column letter that the error occurred in.
     * Will be null if the error is not directly associated with a particular column.
     */
    String column

    /**
     * The name of the field that the error occurred for.
     * Will be null if the error is not directly associated with a particular field.
     */
    String fieldName

    /**
     * The "importance" of the error. Higher severity errors may be used to block the flow from continuing.
     */
    BulkDataErrorSeverity severity

    /**
     * The localized error message to display.
     */
    String localizedMessage

    /**
     * Holds the context of an error message that has not yet been localized.
     *
     * We expect localizable messages to be localized prior to display / use in an API response. As a part
     * of this process, we should populate the localizedMessage field with the result of localizing this message.
     */
    LocalizableMessage localizableMessage

    /**
     * The Exception that was throw when the error was triggered.
     * Will be null if the error was not associated with an exception, such as a validation error.
     * Not to be directly displayed to users but can be useful for debugging.
     */
    Exception exception

    void setLocalizedMessage(String localizedMessage) {
        // We don't strictly need to do this, but it avoids potential confusion around having both a localized
        // and a localizable message. Once we have performed the localization, we no longer need the localizable.
        this.localizedMessage = localizedMessage
        this.localizableMessage = null
    }
}
