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
     * Performs any custom data validation that was not already automatically handled by the data validator.
     *
     * To provide custom validation logic, a feature will typically override customValidateRow. However, if you need
     * the custom logic to be more complex, such as comparing data across rows, this method can also be overridden.
     *
     * If you do override this method, you'll likely still want to call super.customValidate(rows) to preserve
     * the below behaviour.
     */
    List<BulkDataError> customValidate(List<T> rows) {
        List<BulkDataError> errors = []
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<BulkDataError> rowErrors = customValidateRow(rows.get(rowIndex))?.allErrors
            if (!rowErrors) {
                continue
            }
            for (rowError in rowErrors) {
                // Set the rowIndex here entirely for convenience so that we don't need to provide it
                // as a method arg when calling BulkDataErrors.add*Error(...) in customValidateRow(row)
                rowError.row = rowIndex

                errors.add(rowError)
            }
        }
        return errors
    }

    /**
     * Performs any custom validation on a row that was not already automatically handled by the data validator.
     * This method is designed to be overridden by child implementations (unless no custom validation is required).
     *
     * Implementations can utilize the add*Error(...) convenience methods on BulkDataErrors to raise validation errors.
     */
    BulkDataErrors customValidateRow(T row) {
        return null  // Do nothing by default
    }
}
