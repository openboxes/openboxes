package org.pih.warehouse.importer

import grails.util.Holders
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.DefaultImportCellCollector
import org.grails.plugins.excelimport.ExcelImportService
import org.grails.plugins.excelimport.ExpectedPropertyType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.CycleCountImportService
import org.pih.warehouse.product.Product

class CycleCountItemsExcelImporter extends AbstractExcelImporter implements DataImporter {

    ExcelImportService excelImportService
    CycleCountImportService cycleCountImportService

    static CELL_REPORTER = new DefaultImportCellCollector()

    static Map COLUMN_MAP = [
            sheet    : "Sheet1",
            startRow : 1,
            columnMap: [
                    A: "cycleCountId",
                    B: "cycleCountItemId",
                    C: "productCode",
                    D: "product.name",
                    E: "lotNumber",
                    F: "expirationDate",
                    G: "binLocation",
                    H: "quantityCounted",
                    I: "comment",
                    J: "assignee",
                    K: "dateCounted",
            ]
    ]

    static Map PROPERTY_MAP = [
            cycleCountId:             ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            cycleCountItemId:         ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productCode:              ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "product.name":           ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "lotNumber":              ([expectedType: ExpectedPropertyType.StringType, defaultValue: ""]),
            "expirationDate":         ([expectedType: ExpectedPropertyType.DateJavaType, defaultValue: null]),
            "binLocation":            ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "quantityCounted":        ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            "comment":                ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "assignee":               ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "dateCounted":            ([expectedType: ExpectedPropertyType.DateJavaType, defaultValue: null]),
    ]

    CycleCountItemsExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        cycleCountImportService = Holders.grailsApplication.mainContext.getBean(CycleCountImportService)
    }

    @Override
    List<Map> getData() {
        List<Map> data = excelImportService.columns(
                workbook,
                COLUMN_MAP,
                CELL_REPORTER,
                PROPERTY_MAP
        ) as List<Map>

        // 1. Collect all uniques bin location names and product codes
        List<String> locationNames = data*.binLocation.unique()
        List<String> productCodes  = data*.productCode.unique()
        List<String> personNames  = data*.assignee.unique()

        // 2. Fetch all of the necessary data in single database call
        Map<String, Location> locationMap = Location.findAllByNameInList(locationNames).collectEntries {
            [it.name, it]
        }
        Map<String, Product> productMap = Product.findAllByProductCodeInList(productCodes).collectEntries {
            [it.productCode, it]
        }
        Map<String, Person> personMap = Person.findAllByNameOrEmail(personNames).collectEntries {
            [it.name, it]
        }

        // 3. Replace data from importer with the fetched data
        data.each { row ->
            row.binLocation = row.binLocation ? [
                    id: locationMap[row.binLocation]?.id,
                    name: locationMap[row.binLocation]?.name
            ] : null
            row.product = [
                    id: productMap[row.productCode]?.id,
                    name: productMap[row.productCode]?.name,
                    productCode: productMap[row.productCode]?.productCode
            ]
            row.assignee = row.assignee ? [
                    id: personMap[row.assignee]?.id,
                    name: personMap[row.assignee]?.name,
                    label: personMap[row.assignee]?.name,
            ] : null
        }

        return data
    }


    @Override
    void validateData(ImportDataCommand command) {
        cycleCountImportService.validateCountImport(command)
    }

    @Override
    void importData(ImportDataCommand command) {
        throw new UnsupportedOperationException("This operation is not supported")
    }
}
