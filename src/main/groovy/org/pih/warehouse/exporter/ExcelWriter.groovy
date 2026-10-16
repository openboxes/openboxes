package org.pih.warehouse.exporter

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.apache.commons.lang.StringUtils
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

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.date.ZonedDateTimeParser
import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.formatter.Formatter
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.parser.DoubleParser
import org.pih.warehouse.core.session.SessionManager

/**
 * For converting objects into an Excel file.
 */
@Component
class ExcelWriter extends BulkDataWriter<Workbook, ExcelWriterConfig> {

    final DoubleParser doubleParser
    final SessionManager sessionManager
    final ConfigService configService

    // The component resolver is annotated with @Lazy because it wires in the writers, creating a circular dependency.
    // Fortunately the writer doesn't immediately use the component resolver so we can simply delay fetching it.
    ExcelWriter(final ApplicationContext context,
                @Lazy final BulkDataExportComponentResolver componentResolver,
                final DefaultTypeFormatter defaultTypeFormatter,
                final DoubleParser doubleParser,
                final MessageLocalizer messageLocalizer,
                final SessionManager sessionManager,
                final ConfigService configService) {
        super(context, componentResolver, defaultTypeFormatter, messageLocalizer)
        this.doubleParser = doubleParser
        this.sessionManager = sessionManager
        this.configService = configService
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
            String sheetName = getSheetName(config.sheetName)
            Sheet sheet = workbook.createSheet(sheetName)

            writeHeaderRow(rowsToWrite, config, workbook, sheet)
            writeDataRows(rowsToWrite, config, sheet, new ExcelCellStyles(workbook))

            return new ExcelWriterResult(result: workbook)

        } catch (Exception e) {
            // If we fail to create the workbook, close it to preserve memory. We don't close it if no exception
            // is thrown because the caller still needs to use the workbook. We expect them to close it themselves.
            workbook?.close()
            throw e
        }
    }

    private String getSheetName(String sheetNameOverride) {
        return StringUtils.isBlank(sheetNameOverride) ?
                configService.getProperty("openboxes.export.defaultSheetName") :
                sheetNameOverride
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
            excelRow.createCell(i).setCellValue(formatCellValue(headerValue, null) as String)
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
                               Sheet sheet,
                               ExcelCellStyles cellStyles) {
        if (!rowsToWrite) {
            return
        }

        // Datetimes are written in the timezone of the requesting user. This mirrors how dates are bound on import.
        ZoneId timezone = sessionManager.timezone.toZoneId()

        List<BulkDataWriterFieldConfig> fieldConfigsOrdered = getOrderedFieldConfigs(rowsToWrite, config)
        for (int i = 0; i < rowsToWrite.size(); i++) {
            int rowIndex = config.addHeaderRow ? i + 1 : i
            writeRow(rowIndex, rowsToWrite.get(i), sheet, fieldConfigsOrdered, cellStyles, timezone)
        }
    }

    private Row writeRow(int rowIndex,
                         Map<String, Object> rowToWrite,
                         Sheet sheet,
                         List<BulkDataWriterFieldConfig> fieldConfigsOrdered,
                         ExcelCellStyles cellStyles,
                         ZoneId timezone) {

        Row excelRow = sheet.createRow(rowIndex)
        for (int i = 0; i < fieldConfigsOrdered.size(); i++) {
            BulkDataWriterFieldConfig fieldConfig = fieldConfigsOrdered.get(i)
            Object cellValue = rowToWrite.get(fieldConfig.fieldName)
            Object excelCellValue = formatCellValueForExcel(cellValue, fieldConfig, timezone)

            Cell cell = excelRow.createCell(i)
            cell.setCellValue(excelCellValue)

            CellStyle style = cellStyles.getStyleForValue(cellValue?.class)
            if (style) {
                cell.setCellStyle(style)
            }
        }
        return excelRow
    }

    private def formatCellValueForExcel(Object value, BulkDataWriterFieldConfig config, ZoneId timezone) {

        // If the config contains a specific Formatter to use for the field, use it.
        if (value != null && config?.formatter != null) {
            Formatter formatter = context.getBean(config.formatter)
            return formatter.format(value, config.formatterContext)
        }

        switch (value) {
            // POI has some accepted types that it can automatically map to their associated types in Excel.
            case Number:
                return doubleParser.parse(value)
            case Boolean:
                return value

            // POI only supports java.time in version 5.0+. Our version of POI can handle java.util.Date, but it
            // converts the resulting string to the server's timezone. We want the cell to display in the user's local
            // timezone (which is what import does). We can't just format to a string because the field needs to be
            // formatted as an Excel date. If we instead give POI a Calendar, it pulls the date + time straight out of
            // the object without converting timezone. Once we upgrade to POI 5.0, this logic can be simplified.
            case LocalDate:
                // GregorianCalendar needs a ZonedDateTime. Since LocalDate has no time component, calling atStartOfDay
                // will always result in midnight in the given timezone. POI ignores the timezone, and the time will get
                // stripped off when we format the field to a string, so we can pick any non-changing timezone here.
                return GregorianCalendar.from(value.atStartOfDay(ZoneOffset.UTC))
            // TODO: Be smarter about Date since it can also be a date-only. Pass in a dateFormat config option that
            //       is set in the field config that we can use to conditionally format the date.
            case Date:
                return GregorianCalendar.from(ZonedDateTimeParser.asZonedDateTime(value, timezone))
            case Instant:
                return GregorianCalendar.from(ZonedDateTimeParser.asZonedDateTime(value, timezone))
            case ZonedDateTime:
                return GregorianCalendar.from(value.withZoneSameInstant(timezone))

            // For all other types, simply use the default formatting behaviour.
            default:
                return formatCellValue(value, config)
        }
    }
}
