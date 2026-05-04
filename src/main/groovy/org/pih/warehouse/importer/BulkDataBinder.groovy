package org.pih.warehouse.importer

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.parser.DefaultTypeParser

/**
 * Takes in a List of Map of bulk data and binds it to a list of strongly typed Importable objects.
 */
@Component
class BulkDataBinder {

    final private BulkDataImportComponentResolver componentResolver
    final private DefaultTypeParser defaultTypeParser
    final private MessageLocalizer messageLocalizer
    final private ApplicationContext context

    BulkDataBinder(final BulkDataImportComponentResolver componentResolver,
                   final DefaultTypeParser defaultTypeParser,
                   final MessageLocalizer messageLocalizer,
                   final ApplicationContext context) {
        this.componentResolver = componentResolver
        this.defaultTypeParser = defaultTypeParser
        this.messageLocalizer = messageLocalizer
        this.context = context
    }

    /**
     * Binds the given data to a list of strongly typed Importable objects by parsing each field in
     * the given data to a specified type.
     *
     * @param bulkDataType Used to determine which configurer to use when binding the data.
     * @param rawRows The rows to be bound. Requires a LinkedHashMap so that we can preserve the column order
     */
    BulkDataBinderResult bindData(BulkDataType bulkDataType, List<LinkedHashMap<String, Object>> rawRows) {
        // Fetch the configuration to use when binding the rows
        ConfiguresBulkDataBinder importConfigurer = componentResolver.getBulkDataBinderConfigurer(bulkDataType)
        Map<String, BulkDataBinderFieldConfig> fieldConfigs = importConfigurer.bulkDataBinderConfig.fields

        // Build a map of field names to the types that those columns should be bound to
        Map<String, Class> fieldNameToTypeMap = mapFieldNamesToType(importConfigurer.bulkDataBinderConfig.bindTo)

        BulkDataBinderResult result = new BulkDataBinderResult()
        for (int rowIndex = 0; rowIndex < rawRows.size(); rowIndex++) {
            LinkedHashMap<String, Object> rawRow = rawRows[rowIndex]

            Importable boundRow = importConfigurer.bulkDataBinderConfig.bindTo.newInstance()

            // TODO: Refactor the data reader to product a List<Map<ColumnIndex, Object>> where ColumnIndex is a
            //       multi-key POJO holding the field name + column index. That way we don't need to rely on the column
            //       ordering of the LinkedHashMap, which only coincidentally works and is quite brittle. If we read
            //       columns if a different order for whatever reason, things will break.

            // Loop the columns in the order that they were added to the map so that we can preserve the column index
            List<Map.Entry<String, Object>> rawRowAsList = rawRow.entrySet().toList()
            for (int columnIndex = 0; columnIndex < rawRowAsList.size(); columnIndex++) {
                Map.Entry<String, Object> rawColumnEntry = rawRowAsList[columnIndex]
                String columnName = rawColumnEntry.key
                Object columnValue = rawColumnEntry.value

                // Only auto bind fields that are marked for auto-binding
                BulkDataBinderFieldConfig fieldConfig = fieldConfigs.get(columnName)
                if (fieldConfig == null || fieldConfig.dataBindingMethod != DataBindingMethod.AUTO) {
                    continue
                }

                if (!boundRow.hasProperty(columnName)) {
                    throw new RuntimeException("${boundRow.class.simpleName} does not have a field ${columnName}. Check your data binding config.")
                }

                try {
                    def parsedValue = parseField(columnValue, fieldNameToTypeMap.get(columnName), fieldConfig)
                    boundRow.setProperty(columnName, parsedValue)
                } catch (Exception e) {
                    result.addError(new BulkDataError(
                            row: rowIndex,
                            column: columnIndex,
                            localizedMessage: messageLocalizer.localize("bulkData.binder.error", [columnName, columnValue, boundRow.class.simpleName]),
                            exception: e,
                            severity: BulkDataErrorSeverity.ERROR,
                    ))
                }
            }
            result.boundRows.add(boundRow)
        }

        if (rawRows.size() != result.boundRows.size()) {
            // This should be impossible since we don't skip rows when binding, but check anyways just in case.
            throw new RuntimeException("Something went wrong during the data binding process. Processed ${rawRows.size()} raw rows but ended up with ${result.boundRows.size()} bound rows.")
        }

        // Now that the automatic data binding has completed, we can perform any custom data binding.
        // We rely on customBindData to modify the result object itself with any data changes and errors.
        importConfigurer.customBindData(rawRows, result)

        return result
    }

    /**
     * Parse a field to the given type. If the parser to use was explicitly specified in the config, fetch and use it,
     * otherwise use the default parser associated with the given type.
     */
    private def parseField(Object fieldValue, Class fieldType, BulkDataBinderFieldConfig fieldConfig) {
        return fieldConfig.parser != null ?
                context.getBean(fieldConfig.parser).parse(fieldValue, fieldConfig.parserContext) :
                defaultTypeParser.parse(fieldValue, fieldType, fieldConfig.parserContext)
    }

    private Map<String, Class> mapFieldNamesToType(Class clazz) {
        return clazz.getDeclaredFields().collectEntries { [it.name, it.type] }
    }
}
