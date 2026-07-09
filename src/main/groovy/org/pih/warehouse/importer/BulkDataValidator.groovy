package org.pih.warehouse.importer

import grails.validation.Validateable
import org.grails.datastore.gorm.GormValidateable
import org.springframework.stereotype.Component
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import org.pih.warehouse.core.localization.MessageLocalizer

/**
 * Validates a List of Importable objects, collecting any validation errors that occur.
 */
@Component
class BulkDataValidator {

    BulkDataImportComponentResolver componentResolver
    MessageLocalizer messageLocalizer

    BulkDataValidator(final BulkDataImportComponentResolver componentResolver,
                      final MessageLocalizer messageLocalizer) {
        this.componentResolver = componentResolver
        this.messageLocalizer = messageLocalizer
    }

    /**
     * Validates a List of Importable objects, collecting any validation errors that occur.
     *
     * @param bulkDataType Determines which configurer to use when validating the data.
     * @param toValidateList The objects to be validated.
     * @return The list of validation errors that occurred.
     */
    List<BulkDataError> validate(BulkDataType bulkDataType, List<Importable> toValidateList) {
        List<BulkDataError> bulkDataErrors = []
        for (int rowIndex = 0; rowIndex < toValidateList.size(); rowIndex++) {
            List<ObjectError> errors = validateRow(toValidateList.get(rowIndex))
            bulkDataErrors.addAll(convertObjectErrorsToBulkDataErrors(errors, rowIndex))
        }

        bulkDataErrors.addAll(customValidate(bulkDataType, toValidateList))

        return bulkDataErrors
    }

    /**
     * Perform any validation that is defined on the Importable class itself.
     */
    private List<ObjectError> validateRow(Importable toValidate) {
        // ie a Hibernate/GORM domain entity
        if (toValidate instanceof GormValidateable) {
            toValidate.validate()
            return toValidate.errors.allErrors
        }

        // ie a non-domain object (such as a Command Object or DTO)
        if (toValidate instanceof Validateable) {
            toValidate.validate()
            return toValidate.errors.allErrors
        }

        // Otherwise we have no standard validation to perform.
        return []
    }

    private List<BulkDataError> convertObjectErrorsToBulkDataErrors(List<ObjectError> errors, int rowIndex) {
        List<BulkDataError> bulkDataErrors = []
        for (error in errors) {
            String fieldName = error instanceof FieldError ? error.field : null
            bulkDataErrors.add(new BulkDataError(
                    row: rowIndex,
                    column: null,  // TODO: we can get this from the config using fieldName!
                    fieldName: fieldName,
                    severity: BulkDataErrorSeverity.ERROR,  // We assume all standard validation failures are errors.
                    localizedMessage: messageLocalizer.localize(error),
            ))
        }
        return bulkDataErrors
    }

    /**
     * Perform any custom validation as declared by the configurer for the given bulk data type.
     */
    private List<BulkDataError> customValidate(BulkDataType bulkDataType, List<Importable> toValidateList) {
        ConfiguresBulkDataValidator configuresValidator = componentResolver.getBulkDataValidatorConfigurer(bulkDataType)
        if (!configuresValidator) {
            return []
        }

        List<BulkDataError> customErrors = configuresValidator.customValidate(toValidateList)
        for (customError in customErrors) {
            // We may not have performed localization on custom errors yet, so make sure to do so.
            if (customError.localizedMessage == null && customError.localizableMessage != null) {
                customError.localizedMessage = messageLocalizer.localize(customError.localizableMessage)
            }
        }

        return customErrors
    }
}
