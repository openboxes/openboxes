package org.pih.warehouse.importer

import org.pih.warehouse.core.localization.LocalizableMessage

/**
 * Wraps the errors that occur when custom validating bulk data.
 *
 * Exists solely for the purpose of making it more intuitive to implement {@link ConfiguresBulkDataValidator}
 * since we're only exposing a small number of methods here for constructing errors.
 */
class BulkDataValidationErrors {
    private List<BulkDataError> errors = []

    List<BulkDataError> getAllErrors() {
        return errors
    }

    /**
     * Mark a field of the object being validated as invalid.
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
                row: null,  // We rely on the BulkDataValidator to set this later
                column: null,  // We rely on the BulkDataValidator to set this later
                fieldName: fieldName,
                severity: severity,
                // We rely on the BulkDataValidator to localize this message later
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
     * @param errorCode The message key to use when localizing the message.
     * @param errorArgs Arguments for the errorCode.
     * @param severity The severity of the error.
     */
    void addObjectError(String errorCode,
                        Object[] errorArgs=null,
                        BulkDataErrorSeverity severity=BulkDataErrorSeverity.ERROR) {

        errors.add(new BulkDataError(
                row: null,  // We rely on the BulkDataValidator to set this later
                severity: severity,
                // We rely on the BulkDataValidator to localize this message later
                localizableMessage: new LocalizableMessage(
                        code: errorCode,
                        args: errorArgs,
                        defaultMessage: errorCode
                ),
        ))
    }
}
