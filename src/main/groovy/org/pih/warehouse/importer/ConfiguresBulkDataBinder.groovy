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
     * @return the data type for this configuration to be used for.
     */
    abstract BulkDataType getBulkDataType()

    /**
     * Performs any custom data binding that was not already automatically handled by the data binder.
     *
     * This method expects that the fields of each Importable row in BulkDataBinderResult.boundRows are already
     * populated with data if the field's config was marked with DataBindingMethod.AUTO. When custom binding, it
     * is safe to rely on the value of the AUTO bound fields of the Importable objects, as well as any field values
     * in the raw data.
     *
     * Any fields on the Importable objects in  BulkDataBinderResult.boundRows marked with the DataBindingMethod.MANUAL
     * config option will need to have their values set manually by this method.
     *
     * To provide custom binding logic, a feature will typically override customBindDataRow. However, if you need
     * the custom logic to be more complex, such as comparing data across rows, this method can also be overridden.
     */
    void customBindData(List<Map<String, BulkDataCell>> rawRows, BulkDataBinderResult<T> result) {
        // We assume that we are given the same number of raw rows and bound rows. There's a check for this in
        // the bulk data binder so we don't bother checking again here.
        List<T> boundRows = result.boundRows
        for (int rowIndex = 0; rowIndex < rawRows.size(); rowIndex++) {
            // We expect customBindDataRow to directly write to the Importable object for each row so we don't need
            // to do any further data binding. All we do here is collect any errors that occurred when custom binding.
            List<BulkDataError> errors = customBindDataRow(rawRows.get(rowIndex), boundRows.get(rowIndex))
            result.addErrors(errors)
        }
    }

    /**
     * Performs any custom data binding on a row that was not already automatically handled by the data binder.
     * This method is designed to be overridden by child implementations (unless no custom binding is required).
     *
     * We expect the fields of the boundRow object to be directly modified by this method.
     *
     * Any errors that occur when custom binding the row should be collected and returned by this method.
     *
     * @param rawRow The raw input data. Read only.
     * @param boundRow The strongly typed object that the raw data was bound to. Custom binding writes to this object.
     * @return The list of errors that occurred when custom binding the fields of the row.
     */
    List<BulkDataError> customBindDataRow(Map<String, BulkDataCell> rawRow, T boundRow) {
        return []  // Do nothing by default
    }
}
