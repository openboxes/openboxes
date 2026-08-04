package org.pih.warehouse.importer

/**
 * Customizes the bulk data binder, configuring it for a specific data type.
 */
trait ConfiguresBulkDataBinder<T extends Importable> {

    /**
     * @return the configuration to use when binding the data.
     */
    abstract BulkDataBinderConfig getBulkDataBinderConfig()

    /**
     * @return the data type that this configuration is meant to be used for. Bulk data type has a one-to-one
     *         map to a configurer.
     */
    abstract BulkDataType getBulkDataType()

    /**
     * Designed to be overridden when you need to perform custom data binding that spans across multiple rows.
     * Implementations can utilize the add*Error(...) methods on CustomBulkDataErrors to raise validation errors.
     *
     * Note that this validation will be executed after the default data binding is executed. This means that
     * the fields of the bound rows that are marked with the DataBindingMethod.AUTO config will already be populated.
     *
     * We expect the fields of the boundRow object to be directly modified by this method.
     *
     * @param rawRows The raw input data. Read only.
     * @param boundRows The strongly typed object that the raw data was bound to. Custom binding writes to this object.
     * @return The list of errors that occurred when custom binding the rows.
     */
    CustomBulkDataErrors customBindDataAcrossRows(List<Map<String, BulkDataCell>> rawRows, List<T> boundRows) {
        return null  // Do nothing by default
    }

    /**
     * Designed to be overridden when you need to perform custom data binding on each row individually.
     * Implementations can utilize the add*Error(...) methods on CustomBulkDataErrors to raise validation errors.
     *
     * Note that this validation will be executed after the default data binding is executed. This means that
     * the fields of the bound row that are marked with the DataBindingMethod.AUTO config will already be populated.
     *
     * We expect the fields of the boundRow object to be directly modified by this method.
     *
     * @param rawRow The raw input data. Read only.
     * @param boundRow The strongly typed object that the raw data was bound to. Custom binding writes to this object.
     * @return The list of errors that occurred when custom binding the fields of the row.
     */
    CustomBulkDataErrors customBindDataRow(Map<String, BulkDataCell> rawRow, T boundRow) {
        return null  // Do nothing by default
    }
}
