package org.pih.warehouse.importer

/**
 * Customizes the bulk data validator, configuring it for a specific data type.
 */
trait ConfiguresBulkDataValidator<T extends Importable> {

    /**
     * @return the data type that this configuration is meant to be used for. Bulk data type has a one-to-one
     *         map to a configurer.
     */
    abstract BulkDataType getBulkDataType()

    /**
     * @return the configuration to use when validating the data.
     */
    abstract BulkDataValidatorConfig getBulkDataValidatorConfig()

    /**
     * Performs any cross-row validation that was not already automatically handled by the data validator.
     * Implementations can utilize the add*Error(...) methods on BulkDataValidationErrors to raise validation errors.
     *
     * This method is designed to be overridden when you need to perform validation that spans across multiple rows.
     */
    BulkDataValidationErrors customValidateAcrossRows(List<T> rows) {
        return null  // Do nothing by default
    }

    /**
     * Performs any custom validation on a row that was not already automatically handled by the data validator.
     * Implementations can utilize the add*Error(...) methods on BulkDataValidationErrors to raise validation errors.
     *
     * This method is designed to be overridden when you need to perform per row validation.
     */
    BulkDataValidationErrors customValidateRow(T row) {
        return null  // Do nothing by default
    }
}
