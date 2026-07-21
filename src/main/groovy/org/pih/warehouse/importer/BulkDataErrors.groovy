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

    /**
     * @return true if any errors have occurred.
     */
    boolean hasErrors() {
        return !errors.empty
    }

    /**
     * @return the highest severity error that has been raised.
     */
    BulkDataErrorSeverity getHighestSeverity() {
        return errors.severity.max()
    }

    void addError(BulkDataError error) {
        errors.add(error)
    }

    void addErrors(Collection<BulkDataError> errors) {
        this.errors.addAll(errors)
    }

    void addErrors(BulkDataErrors errors) {
        this.errors.addAll(errors.allErrors)
    }

    /**
     * Mark a field of the object being validated as invalid.
     *
     * To be used during the custom validate flow.
     *
     * @param fieldName The name of the field that failed validation
     * @param errorCode The message key to use when localizing the message.
     * @param errorArgs Arguments for the errorCode.
     * @param severity The severity of the error.
     */
    void addFieldError(String fieldName,
                       String errorCode,
                       Object[] errorArgs=null,
                       BulkDataErrorSeverity severity=BulkDataErrorSeverity.ERROR) {

        errors.add(new BulkDataError(
                row: null,  // We rely on customValidate to set this later
                column: null,  // We rely on customValidate to set this later
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
     * To be used during the custom validate flow.
     *
     * @param errorCode The message key to use when localizing the message.
     * @param errorArgs Arguments for the errorCode.
     * @param severity The severity of the error.
     */
    void addObjectError(String errorCode,
                        Object[] errorArgs=null,
                        BulkDataErrorSeverity severity=BulkDataErrorSeverity.ERROR) {

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
