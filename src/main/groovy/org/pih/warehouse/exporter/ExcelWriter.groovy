package org.pih.warehouse.exporter

import java.time.temporal.TemporalAccessor
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.DateParser
import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.parser.DoubleParser

/**
 * For converting objects into an Excel file.
 */
@Component
class ExcelWriter extends BulkDataWriter<Workbook, ExcelWriterConfig> {

    final DateParser dateParser
    final DoubleParser doubleParser

    // The component resolver is annotated with @Lazy because it wires in the writers, creating a circular dependency.
    // Fortunately the writer doesn't immediately use the component resolver so we can simply delay fetching it.
    ExcelWriter(final ApplicationContext context,
                @Lazy final BulkDataExportComponentResolver componentResolver,
                final DateParser dateParser,
                final DefaultTypeFormatter defaultTypeFormatter,
                final DoubleParser doubleParser,
                final MessageLocalizer messageLocalizer) {
        super(context, componentResolver, defaultTypeFormatter, messageLocalizer)
        this.dateParser = dateParser
        this.doubleParser = doubleParser
    }

    @Override
    List<ContentType> getSupportedContentTypes() {
        // TODO: Add ContentType.XLSX back in here once we remove the Grails plugin and can resolve the dependency
        //       hell around POI. We need to either upgrade to the latest POI or bring in the poi-ooxml dependency.
        return [ContentType.XLS]
    }

    @Override
    def getEmptyCellValue() {
        // Excel will ignore nulls so we need to set a non-null cell value to create a blank field.
        return ''
    }

    @Override
    BulkDataWriterResult<Workbook> doWrite(List<Map<String, Object>> rowsToWrite,
                                           ContentType contentType,
                                           ExcelWriterConfig config) {
        Workbook workbook = null
        try {
            workbook = getWorkbook(contentType)
            Sheet sheet = workbook.createSheet(config.sheetName)

            writeHeaderRow(rowsToWrite, config, workbook, sheet)
            writeDataRows(rowsToWrite, config, sheet)

            return new ExcelWriterResult(result: workbook)

        } catch (Exception e) {
            // If we fail to create the workbook, close it to preserve memory. We don't close it if no exception
            // is thrown because the caller still needs to use the workbook. We expect them to close it themselves.
            workbook?.close()
            throw e
        }
    }

    private Workbook getWorkbook(ContentType contentType) {
        return contentType == ContentType.XLSX ? new XSSFWorkbook() : new HSSFWorkbook()
    }

    private void writeHeaderRow(List<Map<String, Object>> rowsToWrite,
                                ExcelWriterConfig config,
                                Workbook workbook,
                                Sheet sheet) {
        if (!config.addHeaderRow) {
            return
        }

        // Construct the header row
        List<String> headerRow = buildHeaderRow(rowsToWrite, config)
        Row excelRow = sheet.createRow(0)
        for (int i = 0; i < headerRow.size(); i++) {
            Object headerValue = headerRow.get(i)
            excelRow.createCell(i).setCellValue(formatCellValueForExcel(headerValue, null) as String)
        }

        // Apply any relevant styling to the header row
        if (config.excelStyle == ExcelStyle.TABLE) {
            styleHeaderRowForTable(excelRow, workbook, sheet)
        }
    }

    private void styleHeaderRowForTable(Row excelRow, Workbook workbook, Sheet sheet) {
        // Freeze the top row
        sheet.createFreezePane(0, 1)

        // Make the top row bold
        Font headerFont = workbook.createFont()
        headerFont.setBold(true)

        CellStyle headerStyle = workbook.createCellStyle()
        headerStyle.setFont(headerFont)

        for (Cell cell in excelRow.cellIterator()) {
            cell.setCellStyle(headerStyle)
        }
    }

    private void writeDataRows(List<Map<String, Object>> rowsToWrite,
                               ExcelWriterConfig config,
                               Sheet sheet) {
        if (!rowsToWrite) {
            return
        }

        List<BulkDataWriterFieldConfig> fieldConfigsOrdered = getOrderedFieldConfigs(rowsToWrite, config)
        for (int i = 0; i < rowsToWrite.size(); i++) {
            int rowIndex = config.addHeaderRow ? i + 1 : i
            writeRow(rowIndex, rowsToWrite.get(i), sheet, fieldConfigsOrdered)
        }
    }

    private Row writeRow(int rowIndex,
                         Map<String, Object> rowToWrite,
                         Sheet sheet,
                         List<BulkDataWriterFieldConfig> fieldConfigsOrdered) {

        Row excelRow = sheet.createRow(rowIndex)
        for (int i = 0; i < fieldConfigsOrdered.size(); i++) {
            BulkDataWriterFieldConfig fieldConfig = fieldConfigsOrdered.get(i)
            Object cellValue = rowToWrite.get(fieldConfig.fieldName)
            excelRow.createCell(i).setCellValue(formatCellValueForExcel(cellValue, fieldConfig))
        }
        return excelRow
    }

    private def formatCellValueForExcel(Object value, BulkDataWriterFieldConfig config) {
        // POI can map some types to their associated types in Excel.
        if (value instanceof Number) {
            return doubleParser.parse(value)
        }
        if (value instanceof Boolean) {
            return value
        }
        if (value instanceof TemporalAccessor || value instanceof Date) {
            // Support for java.time was added to Apache POI in version 5.0 so until we can upgrade, we must
            // convert all date types to java.util.Date. Note that POI converts dates to a double because that
            // is how Excel represents dates.
            return dateParser.parseToDate(value)
        }

        // For all other types, simple use the default formatting behaviour.
        return formatCellValue(value, config)
    }
}
