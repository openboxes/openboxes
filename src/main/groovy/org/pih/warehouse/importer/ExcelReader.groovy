package org.pih.warehouse.importer

import org.apache.commons.lang3.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.CellValue
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.EpochDate
import org.pih.warehouse.core.http.ContentType

/**
 * Reads in an Excel file, converting the rows of one of the sheets in the file to a Java-friendly structure.
 */
@Component
class ExcelReader extends BulkDataReader<MultipartFileSource, ExcelReaderConfig> {

    @Override
    List<ContentType> getSupportedContentTypes() {
        // TODO: Add ContentType.XLSX back in here once we remove the Grails plugin and can resolve the dependency
        //       hell around POI. We need to either upgrade to the latest POI or bring in the poi-ooxml dependency.
        return [ContentType.XLS]
    }

    @Override
    protected BulkDataReaderResult doRead(MultipartFileSource source, ExcelReaderConfig config) {
        Workbook workbook = null
        try {
            workbook = getWorkbook(source)
            Sheet sheet = getSheet(workbook, config.sheetName)

            // Required to be able to resolve any cells that contain a formula
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator()

            Map<String, String> columnMapping = config.columnMapping
            List<Map<String, Object>> readRows = []
            for (Row row : sheet) {
                if (row.rowNum < config.linesToSkip) {  // rowNum is zero-index based
                    continue
                }

                Map<String, Object> readRow = [:]
                for (Cell cell : row) {
                    // Only bother importing cells whose columns are specified in the config
                    String fieldName = getFieldName(cell, columnMapping)
                    if (StringUtils.isBlank(fieldName)) {
                        continue
                    }

                    readRow.put(fieldName, getCellValue(cell, evaluator))
                }
                readRows.add(readRow)
            }

            // Extract the epoch date that the file uses so that we can properly parse dates in the data binding step.
            EpochDate epochDate = getEpochDate(workbook)

            return new BulkDataReaderResult(
                    rows: readRows,
                    epochDate: epochDate,
            )
        }
        finally {
            workbook?.close()
        }
    }

    /**
     * Extract the field name from the column mapping config for the given cell.
     *
     * We allow our columns in our mapping config to be represented as either zero-indexed numerical keys,
     * or as letters (as they appear in Excel). Ex: The first column can be represented as "0" or "A".
     */
    private String getFieldName(Cell cell, Map<String, String> columnMapping) {
        return columnMapping.get(cell.columnIndex) ?: columnMapping.get(getCellLetterIndex(cell))
    }

    private Sheet getSheet(Workbook workbook, String sheetName) {
        if (workbook.numberOfSheets == 1) {
            return workbook.getSheetAt(0)
        }

        if (StringUtils.isBlank(sheetName)) {
            throw new IllegalArgumentException("Excel file has multiple sheets. You must specify which sheet to use.")
        }

        Sheet sheet = workbook.getSheet(sheetName)
        if (!sheet) {
            throw new IllegalArgumentException("Excel file does not contain a sheet with name ${sheetName}")
        }
        return sheet
    }

    private String getCellLetterIndex(Cell cell) {
        return CellReference.convertNumToColString(cell.columnIndex)
    }

    private def getCellValue(Cell cell, FormulaEvaluator evaluator) {
        switch (cell.cellTypeEnum) {
            case CellType.STRING:
                return cell.stringCellValue
            case CellType.NUMERIC:
                // Note that date fields are represented in Excel as numerics, but at this point we don't know if this
                // field is a date or simply a number. We need to wait until the data binding step, where the expected
                // type is specified. For now we simply treat it as a double.
                return cell.numericCellValue
            case CellType.BOOLEAN:
                return cell.booleanCellValue
            case CellType.FORMULA:
                return getFormulaCellValue(cell, evaluator)
            case CellType.BLANK:
                return null
            default:
                // TODO: Handle this case more gracefully. This method should return a CellValue POJO containing the
                //       value and any errors that occurred when reading it. Then the caller can use this POJO
                //       to build a more user friendly error response.
                throw new IllegalArgumentException("Cell in row ${cell.rowIndex} and column ${cell.columnIndex} contains an invalid value.")
        }
    }

    private def getFormulaCellValue(Cell cell, FormulaEvaluator evaluator) {
        CellValue cellValue = evaluator.evaluate(cell)
        switch (cellValue.cellTypeEnum) {
            case CellType.STRING:
                return cellValue.stringValue
            case CellType.NUMERIC:
                return cellValue.numberValue
            case CellType.BOOLEAN:
                return cellValue.booleanValue
            case CellType.BLANK:
                return null
            case CellType.FORMULA:
                // TODO: Handle this case more gracefully. See other todo.
                // I don't know if this case is even possible, but error if the formula resolves to another formula
                // so that we don't get stuck in an infinite loop.
                throw new IllegalArgumentException("Cell in row ${cell.rowIndex} and column ${cell.columnIndex} contains an invalid formula.")
            default:  // ERROR
                // TODO: Handle this case more gracefully. This method should return a CellValue POJO containing the
                //       value and any errors that occurred when reading it. Then the caller can use this POJO
                //       to build a more user friendly error response.
                throw new IllegalArgumentException("Cell in row ${cell.rowIndex} and column ${cell.columnIndex} contains an invalid value.")
        }
    }

    private Workbook getWorkbook(MultipartFileSource source) {
        // This automatically determines the Excel workbook file type, creating an HSSFWorkbook for .xls files,
        // and an XSSFWorkbook for .xlsx files.
        return WorkbookFactory.create(source.asInputStream())
    }

    /**
     * Returns the EpochDate associated with the workbook. Docs originating from windows machines will be 1900,
     * and docs originating from Apple machines will be 1904.
     */
    private static EpochDate getEpochDate(Workbook workbook) {
        return use1904Windowing(workbook) ? EpochDate.EXCEL_1904 : EpochDate.EXCEL_1900
    }

    /**
     * Returns true if we should use Excel's 1904 date system, false if we should use the 1900 system.
     *
     * Excel represents dates as: "<days since Excel epoch>.<fraction of time into the day>". Annoyingly, Excel epoch
     * is different for different operating systems. For windows machines, "epoch" is Jan 1st 1900. For Apple machines,
     * it's "Jan 1st 1904".
     *
     * So for example: Jan 1st 1904 12:00, is 1462.5 for windows machines and 0.5 for apple machines.
     *
     * https://support.microsoft.com/en-us/office/date-systems-in-excel-e7fe7167-48a9-4b96-bb53-5612a800b487
     */
    private static boolean use1904Windowing(Workbook workbook) {
        switch (workbook) {
            case XSSFWorkbook:  // .xlsx files
                return workbook.isDate1904()
            case HSSFWorkbook:  // .xls files
                return workbook.internalWorkbook?.isUsing1904DateWindowing() ?: false
            case null:
                return false
            default:
                throw new UnsupportedOperationException("Unsupported Excel workbook type [${workbook.class}]")
        }
    }
}
