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
     * For use when we want to rely on the default validation configuration for a given bulk data type.
     *
     * @param bulkDataType Determines which configurer to use when validating the data.
     * @param toValidateList The objects to be validated.
     * @return The result of validating the Importable data.
     */
    BulkDataValidatorResult validate(BulkDataType bulkDataType, List<Importable> toValidateList) {
        ConfiguresBulkDataValidator validateConfigurer = componentResolver.getBulkDataValidatorConfigurer(bulkDataType)
        if (!validateConfigurer) {
            throw new RuntimeException("No bulk data validator configurer was found for type ${bulkDataType}")
        }
        return validate(bulkDataType, validateConfigurer.bulkDataValidatorConfig, toValidateList)
    }

    /**
     * Validates a List of Importable objects, collecting any validation errors that occur.
     *
     * For use when we want to provide custom validation configuration that overrides the default.
     *
     * @param bulkDataType Determines which configurer to use when validating the data.
     * @param config Configuration for validating the data.
     * @param toValidateList The objects to be validated.
     * @return The result of validating the Importable data.
     */
    BulkDataValidatorResult validate(
            BulkDataType bulkDataType, BulkDataValidatorConfig config, List<Importable> toValidateList) {

        Map<String, String> columnByFieldName = config.columnByFieldName

        BulkDataValidatorResult result = new BulkDataValidatorResult()
        for (int rowIndex = 0; rowIndex < toValidateList.size(); rowIndex++) {
            List<ObjectError> errors = validateRow(toValidateList.get(rowIndex))
            result.validationErrors.addErrors(convertObjectErrorsToBulkDataErrors(errors, rowIndex, columnByFieldName))
        }
        result.validationErrors.addErrors(customValidate(bulkDataType, columnByFieldName, toValidateList))

        return result
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

    private List<BulkDataError> convertObjectErrorsToBulkDataErrors(
            List<ObjectError> errors, int rowIndex, Map<String, String> columnByFieldName) {
        List<BulkDataError> bulkDataErrors = []
        for (error in errors) {
            String fieldName = error instanceof FieldError ? error.field : null
            bulkDataErrors.add(new BulkDataError(
                    row: rowIndex,
                    column: columnByFieldName.get(fieldName),
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
    private List<BulkDataError> customValidate(
            BulkDataType bulkDataType, Map<String, String> columnByFieldName, List<Importable> toValidateList) {

        ConfiguresBulkDataValidator configuresValidator = componentResolver.getBulkDataValidatorConfigurer(bulkDataType)
        if (!configuresValidator) {
            return []
        }

        List<BulkDataError> customErrors = configuresValidator.customValidate(toValidateList)

        // For the sake of convenience during custom validation, set some of the fields on the error automatically.
        for (customError in customErrors) {
            if (customError.column == null) {
                customError.column = columnByFieldName.get(customError.fieldName)
            }

            // We may not have performed localization on custom errors yet, so make sure to do so.
            if (customError.localizedMessage == null && customError.localizableMessage != null) {
                customError.localizedMessage = messageLocalizer.localize(customError.localizableMessage)
            }
        }

        return customErrors
    }
}
