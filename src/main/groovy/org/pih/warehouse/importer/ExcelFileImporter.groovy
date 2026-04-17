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

import org.pih.warehouse.core.file.UploadedFile
import org.pih.warehouse.core.date.EpochDate
import org.pih.warehouse.core.file.FileExtension

/**
 * Imports XLS and XLSX Excel files.
 */
class ExcelFileImporter extends FileImporter<ExcelFileImporterConfig> {

    @Override
    List<FileExtension> getSupportedFileExtensions() {
        // TODO: Add FileExtension.XLSX back in here once we remove the Grails plugin and can resolve the dependency
        //       hell around POI. We need to either upgrade to the latest POI or bring in the poi-ooxml dependency.
        return [FileExtension.XLS]
    }

    @Override
    FileImporterResult importFileImpl(UploadedFile file, ExcelFileImporterConfig config) {
        Workbook workbook = null
        try {
            workbook = getWorkbook(file)
            Sheet sheet = getSheet(workbook, config.sheetName)

            // We probably don't ever use this, but it will resolve any cells that contain a formula
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator()

            Map<String, String> columnMapping = config.columnMapping
            List<Map<String, Object>> importedRows = []
            for (Row row : sheet) {
                if (row.rowNum < config.startRow) {  // zero-index based
                    continue
                }

                Map<String, Object> importedRow = [:]
                importedRows.add(importedRow)

                for (Cell cell : row) {
                    // Only bother importing cells whose columns are specified in the config
                    String fieldName = getFieldName(cell, columnMapping)
                    if (StringUtils.isBlank(fieldName)) {
                        continue
                    }

                    importedRow.put(fieldName, getCellValue(cell, evaluator))
                }
            }

            // Extract the epoch date that the file uses so that we can properly parse dates in the data binding step.
            EpochDate epochDate = getEpochDate(workbook)

            return new FileImporterResult(
                    rows: importedRows,
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
            default:  // BLANK, ERROR
                // Simply ignore the field if it's not a known type or if it has an invalid value.
                return null
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
            default:  // BLANK, ERROR, FORMULA
                // Simply ignore the field if we couldn't resolve the formula
                return null
        }
    }

    private Workbook getWorkbook(UploadedFile file) {
        // This automatically determines the Excel workbook file type, creating an HSSFWorkbook for .xls files,
        // and an XSSFWorkbook for .xlsx files.
        return WorkbookFactory.create(file.file.inputStream)
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
