package org.pih.warehouse.importer

import org.pih.warehouse.core.localization.LocalizableMessage

/**
 * Wraps the errors that occur when performing custom, feature-specific bulk data operations such as
 * in {@link ConfiguresBulkDataBinder} and {@link ConfiguresBulkDataValidator}.
 *
 * The purpose of this object is to make it more intuitive to raise new bulk data errors since
 * we're only exposing a small number of methods here for constructing errors.
 */
class CustomBulkDataErrors {
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
                row: null,  // We rely on the caller to set this later
                column: null,  // We rely on the caller to set this later
                fieldName: fieldName,
                severity: severity,
                // We rely on the caller to localize this message later
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
                row: null,  // We rely on the caller to set this later
                severity: severity,
                // We rely on the caller to localize this message later
                localizableMessage: new LocalizableMessage(
                        code: errorCode,
                        args: errorArgs,
                        defaultMessage: errorCode
                ),
        ))
    }
}
