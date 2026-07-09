package org.pih.warehouse.importer

import org.pih.warehouse.core.localization.LocalizableMessage

/**
 * Wraps the errors that occurred in the import / bulk data binding process.
 */
class BulkDataErrors {
    private List<BulkDataError> errors = []

    List<BulkDataError> getAllErrors() {
        return errors
    }

    boolean hasErrors() {
        return !errors.empty
    }

    /**
     * @return the highest severity error that has been raised.
     */
    BulkDataErrorSeverity getHighestSeverity() {
        return errors.severity.max()
    }

    /**
     * Mark a field of the object being validated as invalid.
     *
     * @param fieldName The name of the field that failed validation
     * @param severity The severity of the error.
     * @param errorCode The message key to use when localizing the message.
     * @param errorArgs Arguments for the errorCode.
     */
    void addFieldError(String fieldName,
                       BulkDataErrorSeverity severity,
                       String errorCode,
                       Object[] errorArgs=null) {

        errors.add(new BulkDataError(
                row: null,  // We rely on customValidate to set this later
                column: null,  // TODO: we can get this from the config using fieldName!
                fieldName: fieldName,
                severity: severity,
                localizableMessage: new LocalizableMessage(
                        code: errorCode,
                        args: errorArgs,
                        defaultMessage: errorCode
                ),
        ))
    }

    /**
     * Mark the object itself as invalid. For use when not validating a specific field.
     *
     * @param severity The severity of the error.
     * @param errorCode The message key to use when localizing the message.
     * @param errorArgs Arguments for the errorCode.
     */
    void addObjectError(BulkDataErrorSeverity severity,
                        String errorCode,
                        Object[] errorArgs=null) {

        errors.add(new BulkDataError(
                row: null,  // We rely on customValidate to set this later
                severity: severity,
                localizableMessage: new LocalizableMessage(
                        code: errorCode,
                        args: errorArgs,
                        defaultMessage: errorCode
                ),
        ))
    }
}
