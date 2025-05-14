package org.pih.warehouse.importer

import grails.util.Holders
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.DefaultImportCellCollector
import org.grails.plugins.excelimport.ExcelImportService
import org.grails.plugins.excelimport.ExpectedPropertyType

class CycleCountItemsExcelImporter extends AbstractExcelImporter implements DataImporter {

    ExcelImportService excelImportService

    static CELL_REPORTER = new DefaultImportCellCollector()

    static Map COLUMN_MAP = [
            sheet    : "Sheet1",
            startRow : 1,
            columnMap: [
                    A: "cycleCountId",
                    B: "productCode",
                    C: "product.name",
                    D: "lotNumber",
                    E: "expirationDate",
                    F: "binLocation",
                    G: "quantityCounted",
                    H: "comment",
            ]
    ]

    static Map PROPERTY_MAP = [
            cycleCountId:             ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productCode:              ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "product.name":           ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "lotNumber":              ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "expirationDate":         ([expectedType: ExpectedPropertyType.DateJavaType, defaultValue: null]),
            "binLocation":            ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "quantityCounted":        ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            "comment":                ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]

    CycleCountItemsExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")

    }

    @Override
    List<Map> getData() {
        excelImportService.columns(
                workbook,
                COLUMN_MAP,
                CELL_REPORTER,
                PROPERTY_MAP
        )
    }


    @Override
    void validateData(ImportDataCommand command) {
        throw new UnsupportedOperationException("This operation is not supported")
    }

    @Override
    void importData(ImportDataCommand command) {
        throw new UnsupportedOperationException("This operation is not supported")
    }
}
