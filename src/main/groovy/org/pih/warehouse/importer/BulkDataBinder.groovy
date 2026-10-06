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
     * For use when we want to rely on the default data binder configuration for a given bulk data type.
     *
     * @param bulkDataType Determines which configurer to use when binding the data.
     * @param readerResult The output of the BulkDataReader.
     * @return The result of binding the bulk data.
     */
    BulkDataBinderResult bindData(BulkDataType bulkDataType, BulkDataReaderResult readerResult) {
        ConfiguresBulkDataBinder importConfigurer = componentResolver.getBulkDataBinderConfigurer(bulkDataType)
        if (!importConfigurer) {
            throw new RuntimeException("No bulk data binder config was found for type ${bulkDataType}")
        }
        return bindData(importConfigurer.bulkDataBinderConfig, readerResult)
    }

    /**
     * Takes in the result of having read in bulk data and binds it into a list of strongly typed Importable objects
     * by parsing each field in the given data to a specified type.
     *
     * For use when we want to provide a custom data binder configuration that overrides the default.
     *
     * @param config Configuration for binding the data.
     * @param readerResult The output of the BulkDataReader.
     * @return The result of binding the bulk data.
     */
    BulkDataBinderResult bindData(BulkDataBinderConfig config, BulkDataReaderResult readerResult) {
        Map<String, BulkDataBinderFieldConfig> fieldConfigs = config.fields
        Class<Importable> bindTo = config.bindTo
        BulkDataType bulkDataType = config.bulkDataType
        Map<String, String> columnByFieldName = config.columnByFieldName
        EpochDate epochDate = readerResult.epochDate
        List<Map<String, BulkDataCell>> rawRows = readerResult.rows

        // Build a map of field names to the types that those columns should be bound to
        Map<String, Class> fieldNameToTypeMap = mapFieldNamesToType(bindTo)

        BulkDataBinderResult result = new BulkDataBinderResult()
        for (Map<String, BulkDataCell> rawRow in rawRows) {
            Importable boundRow = bindTo.newInstance()
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
                    def parsedValue = parseField(cell.value, fieldNameToTypeMap.get(columnName), fieldConfig, epochDate)
                    boundRow.setProperty(columnName, parsedValue)
                } catch (Exception e) {
                    result.bindErrors.add(new BulkDataError(
                            row: cell.row,
                            column: cell.column,
                            fieldName: columnName,
                            localizedMessage: messageLocalizer.localize(
                                    "import.binder.error", [columnName, cell.value, boundRow.class.simpleName]),
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

        // The custom data binding directly modifies the rows, so the only thing left do is collect the errors.
        result.bindErrors.addAll(customBindData(bulkDataType, columnByFieldName, rawRows, result.boundRows))

        return result
    }

    /**
     * Perform any custom data binding as declared by the configurer for the given bulk data type.
     */
    private List<BulkDataError> customBindData(BulkDataType bulkDataType,
                                               Map<String, String> columnByFieldName,
                                               List<Map<String, BulkDataCell>> rawRows,
                                               List<Importable> boundRows) {
        ConfiguresBulkDataBinder configuresDataBinder = componentResolver.getBulkDataBinderConfigurer(bulkDataType)
        if (!configuresDataBinder) {
            return []
        }

        List<BulkDataError> errors = []

        // We provide two hook-ins for configuring custom data binding. One for binding across rows...
        errors.addAll(configuresDataBinder.customBindDataAcrossRows(rawRows, boundRows)?.allErrors ?: [])

        // And one for binding rows individually.
        errors.addAll(customBindEachRow(configuresDataBinder, rawRows, boundRows))

        for (customError in errors) {
            // To make it simpler to implement custom data binding for a feature, we set the column index on the errors
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

    private List<BulkDataError> customBindEachRow(ConfiguresBulkDataBinder configuresDataBinder,
                                                  List<Map<String, BulkDataCell>> rawRows,
                                                  List<Importable> boundRows) {
        List<BulkDataError> errors = []
        for (int rowIndex = 0; rowIndex < rawRows.size(); rowIndex++) {
            // We assume that we are given the same number of raw rows and bound rows. There's a check for this
            // earlier in the data binder so we don't bother checking again here.
            Map<String, BulkDataCell> rawRow = rawRows.get(rowIndex)
            Importable boundRow = boundRows.get(rowIndex)

            List<BulkDataError> rowErrors = configuresDataBinder.customBindDataRow(rawRow, boundRow)?.allErrors
            if (!rowErrors) {
                continue
            }

            // To make it simpler to implement custom data binding for a feature, we set the row index on the errors
            // here instead of requiring the custom configuration to know how to set the field itself.
            rowErrors.each { it.row = rowIndex }
            errors.addAll(rowErrors)
        }
        return errors
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
