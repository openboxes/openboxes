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
     * @param rows The objects to be validated.
     * @return The result of validating the Importable data.
     */
    BulkDataValidatorResult validate(BulkDataType bulkDataType, List<Importable> rows) {
        ConfiguresBulkDataValidator validateConfigurer = componentResolver.getBulkDataValidatorConfigurer(bulkDataType)
        if (!validateConfigurer) {
            throw new RuntimeException("No bulk data validator configurer was found for type ${bulkDataType}")
        }
        return validate(bulkDataType, validateConfigurer.bulkDataValidatorConfig, rows)
    }

    /**
     * Validates a List of Importable objects, collecting any validation errors that occur.
     *
     * For use when we want to provide custom validation configuration that overrides the default.
     *
     * @param bulkDataType Determines which configurer to use when validating the data.
     * @param config Configuration for validating the data.
     * @param rows The objects to be validated.
     * @return The result of validating the Importable data.
     */
    BulkDataValidatorResult validate(
            BulkDataType bulkDataType, BulkDataValidatorConfig config, List<Importable> rows) {

        Map<String, String> columnByFieldName = config.columnByFieldName

        BulkDataValidatorResult result = new BulkDataValidatorResult()
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<ObjectError> errors = validateRow(rows.get(rowIndex))
            result.validationErrors.addAll(convertObjectErrorsToBulkDataErrors(errors, rowIndex, columnByFieldName))
        }
        result.validationErrors.addAll(customValidate(bulkDataType, columnByFieldName, rows))

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
            BulkDataType bulkDataType, Map<String, String> columnByFieldName, List<Importable> rows) {

        ConfiguresBulkDataValidator configuresValidator = componentResolver.getBulkDataValidatorConfigurer(bulkDataType)
        if (!configuresValidator) {
            return []
        }

        List<BulkDataError> errors = []

        // We provide two hook-ins for configuring custom validation. One for validating across rows...
        errors.addAll(configuresValidator.customValidateAcrossRows(rows)?.allErrors ?: [])

        // And one for validating rows individually.
        errors.addAll(customValidateEachRow(configuresValidator, rows))

        for (customError in errors) {
            // To make it simpler to implement custom validation for a feature, we set the column index on the errors
            // here instead of requiring the custom configuration to know how to set the field itself.
            if (customError.column == null && customError.fieldName != null) {
                customError.column = columnByFieldName.get(customError.fieldName)
            }

            // We may not have performed localization on custom errors yet, so make sure to do so. Again, this is
            // so that the custom configuration doesn't need to remember to do this.
            if (customError.localizedMessage == null && customError.localizableMessage != null) {
                customError.localizedMessage = messageLocalizer.localize(customError.localizableMessage)
            }
        }

        return errors
    }

    private List<BulkDataError> customValidateEachRow(ConfiguresBulkDataValidator configuresValidator,
                                                      List<Importable> rows) {
        List<BulkDataError> errors = []
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<BulkDataError> rowErrors = configuresValidator.customValidateRow(rows.get(rowIndex))?.allErrors
            if (!rowErrors) {
                continue
            }
            for (rowError in rowErrors) {
                // To make it simpler to implement custom validation for a feature, we set the row index on the errors
                // here instead of requiring the custom configuration to know how to set the field itself.
                rowError.row = rowIndex

                errors.add(rowError)
            }
        }
        return errors
    }
}
