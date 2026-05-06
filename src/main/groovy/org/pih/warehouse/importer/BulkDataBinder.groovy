package org.pih.warehouse.importer

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.DateParserContext
import org.pih.warehouse.core.date.EpochDate
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.parser.DefaultTypeParser
import org.pih.warehouse.core.parser.Parser
import org.pih.warehouse.core.parser.ParserContext

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
     * Takes in the result of having read in bulk data and binds it into a list of strongly typed Importable objects
     * by parsing each field in the given data to a specified type.
     *
     * @param bulkDataType Determines which configurer to use when binding the data.
     * @param readerResult The output of the BulkDataReader.
     * @param rawRows The rows to be bound.
     */
    BulkDataBinderResult bindData(BulkDataType bulkDataType, BulkDataReaderResult readerResult) {
        return bindData(bulkDataType, readerResult.rows, readerResult.epochDate)
    }

    /**
     * Binds the given list of raw, bulk data into a list of strongly typed Importable objects by parsing each field in
     * the given data to a specified type.
     *
     * @param bulkDataType Determines which configurer to use when binding the data.
     * @param rawRows The rows to be bound.
     */
    BulkDataBinderResult bindData(
            BulkDataType bulkDataType, List<Map<String, BulkDataCell>> rawRows, EpochDate epochDate) {

        // Fetch the configuration to use when binding the rows
        ConfiguresBulkDataBinder importConfigurer = componentResolver.getBulkDataBinderConfigurer(bulkDataType)
        Map<String, BulkDataBinderFieldConfig> fieldConfigs = importConfigurer.bulkDataBinderConfig.fields

        // Build a map of field names to the types that those columns should be bound to
        Map<String, Class> fieldNameToTypeMap = mapFieldNamesToType(importConfigurer.bulkDataBinderConfig.bindTo)

        BulkDataBinderResult result = new BulkDataBinderResult()
        for (Map<String, BulkDataCell> rawRow in rawRows) {
            Importable boundRow = importConfigurer.bulkDataBinderConfig.bindTo.newInstance()
            for (BulkDataCell cell in rawRow.values()) {
                String columnName = cell.fieldName

                // Only auto bind fields that are marked for auto-binding
                BulkDataBinderFieldConfig fieldConfig = fieldConfigs.get(columnName)
                if (fieldConfig == null || fieldConfig.dataBindingMethod != DataBindingMethod.AUTO) {
                    continue
                }

                if (!boundRow.hasProperty(columnName)) {
                    throw new RuntimeException("${boundRow.class.simpleName} does not have a field ${columnName}. Check your data binding config.")
                }

                try {
                    def parsedValue = parseField(
                            cell.value, fieldNameToTypeMap.get(columnName), fieldConfig, epochDate)
                    boundRow.setProperty(columnName, parsedValue)
                } catch (Exception e) {
                    result.addError(new BulkDataError(
                            row: cell.row,
                            column: cell.column,
                            localizedMessage: messageLocalizer.localize(
                                    "bulkData.binder.error", [columnName, cell.value, boundRow.class.simpleName]),
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
    private def parseField(
            Object fieldValue, Class fieldType, BulkDataBinderFieldConfig fieldConfig, EpochDate epochDate) {

        Parser parser = fieldConfig.parser != null ?
                context.getBean(fieldConfig.parser) :
                defaultTypeParser.getDefaultParser(fieldType)

        // When binding data that is coming from an Excel file, we need to know the epoch date that the file uses
        // (which differs depending on your OS). This is only relevant for date fields.
        ParserContext context = fieldConfig.parserContext ?: parser.getDefaultContext()
        if (context instanceof DateParserContext) {
            context.epochDate = epochDate
        }

        return parser.parse(fieldValue, context)
    }

    private Map<String, Class> mapFieldNamesToType(Class clazz) {
        return clazz.getDeclaredFields().collectEntries { [it.name, it.type] }
    }
}
