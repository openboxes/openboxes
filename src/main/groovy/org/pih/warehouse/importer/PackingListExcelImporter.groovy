package org.pih.warehouse.importer

import grails.util.Holders
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.DefaultImportCellCollector
import org.grails.plugins.excelimport.ExcelImportService
import org.grails.plugins.excelimport.ExpectedPropertyType

class PackingListExcelImporter extends AbstractExcelImporter implements DataImporter {

    ExcelImportService excelImportService

    static CELL_REPORTER = new DefaultImportCellCollector()

    static Map COLUMN_MAP = [
            sheet    : "Sheet1",
            startRow : 1,
            columnMap: [
                    A: "palletName",
                    B: "boxName",
                    C: "productCode",
                    D: "product.name",
                    E: "lotNumber",
                    F: "expirationDate",
                    G: "binLocation",
                    H: "quantityPicked",
                    I: "recipient"
            ]
    ]

    static Map PROPERTY_MAP = [
            palletName:             ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            boxName:                ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "productCode":          ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "product.name":         ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "lotNumber":            ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "expirationDate":       ([expectedType: ExpectedPropertyType.DateJavaType, defaultValue: null]),
            "binLocation":          ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "quantityPicked":       ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            "recipient":            ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]

    PackingListExcelImporter(String fileName) {
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


    List<Map> toJson() {
        return data.collect { [
                palletName: it?.palletName,
                boxName: it?.boxName,
                productCode: it?.productCode,
                lotNumber: it?.lotNumber,
                expirationDate: it?.expirationDate,
                binLocation: it?.binLocation,
                quantityPicked: it?.quantityPicked,
                recipient: it?.recipient
        ] }
    }
}
