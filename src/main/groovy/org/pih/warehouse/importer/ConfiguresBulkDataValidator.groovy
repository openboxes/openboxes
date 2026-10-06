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
     * Designed to be overridden when you need to perform custom validation that spans across multiple rows.
     * Implementations can utilize the add*Error(...) methods on CustomBulkDataErrors to raise validation errors.
     *
     * Note that this validation will be executed after the default validation is executed.
     */
    CustomBulkDataErrors customValidateAcrossRows(List<T> rows) {
        return CustomBulkDataErrors.NO_ERRORS  // Do nothing by default
    }

    /**
     * Designed to be overridden when you need to perform custom validation on each row individually.
     * Implementations can utilize the add*Error(...) methods on CustomBulkDataErrors to raise validation errors.
     *
     * Note that this validation will be executed after the default validation is executed.
     */
    CustomBulkDataErrors customValidateRow(T row) {
        return CustomBulkDataErrors.NO_ERRORS  // Do nothing by default
    }
}
