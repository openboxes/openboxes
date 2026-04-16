package org.pih.warehouse.core.date

import org.apache.poi.ss.usermodel.Workbook

import org.pih.warehouse.core.parser.ParserContext

/**
 * Context object containing the configuration fields for parsing in dates.
 * For a majority of cases the default settings can be used and so this context object will not be required.
 */
class DateParserContext<T> extends ParserContext<T> {

    /**
     * The .xlsx or .xls file used when importing via Excel.
     */
    Workbook excelWorkbook
}
