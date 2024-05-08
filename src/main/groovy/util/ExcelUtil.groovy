package util

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

class ExcelUtil {
    private ExcelUtil() {}

    static Workbook generateExcel(List<Map> data, Map properties = [:]) {
        Workbook workbook = new HSSFWorkbook()

        String sheetName = properties.sheet ?: "Sheet1"
        Sheet sheet = workbook.createSheet(sheetName)

        List<String> columns = properties.columns ?: data.get(0).keySet().toList()
        Map<String, String> headerLabels = properties.headerLabels ?: columns.collect{[it,it]}

        // create headers
        createExcelRow(sheet, 0, headerLabels, columns)

        // create data
        data.eachWithIndex { Map rowValues, index ->
            createExcelRow(sheet, index + 1, rowValues, columns)
        }

        return workbook
    }

    static void createExcelRow(Sheet sheet, int rowNumber, Map rowValue, List<String> columns) {
        Row excelRow = sheet.createRow(rowNumber)

        columns.eachWithIndex { columnName, index ->
            def cellValue = rowValue.get(columnName) ?: ""
            // POI can't handle objects so we need to convert all objects to strings unless they are numeric
            if (!(cellValue instanceof Number)) {
                cellValue = cellValue.toString()
            }
            excelRow.createCell(index).setCellValue(cellValue)
        }
    }
}
