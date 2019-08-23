/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.importer

import org.grails.plugins.excelimport.ExcelImportUtils
import org.pih.warehouse.product.ProductCatalog
import org.springframework.validation.BeanPropertyBindingResult

class ProductCatalogExcelImporter extends AbstractExcelImporter {

    def inventoryService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'id',
                    'B': 'code',
                    'C': 'name',
                    'C': 'description',

            ]
    ]

    static Map propertyMap = [
            id         : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            code       : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            description: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            name       : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null])
    ]


    ProductCatalogExcelImporter(String fileName) {
        super(fileName)
    }


    List<Map> getData() {
        return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductCatalog productCatalog = createOrUpdateProductCatalog(params)
            if (!productCatalog.validate()) {
                productCatalog.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Product catalog ${productCatalog.name} is invalid: ${error.getFieldError()}")
                }
            }
        }

    }

    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductCatalog productCatalog = createOrUpdateProductCatalog(params)
            if (productCatalog.validate()) {
                productCatalog.save(failOnError: true)
            }
        }
    }


    ProductCatalog createOrUpdateProductCatalog(Map params) {
        ProductCatalog productCatalog = ProductCatalog.findByIdOrName(params.id, params.name)
        if (!productCatalog) {
            productCatalog = new ProductCatalog()
        }
        productCatalog.id = params.id
        productCatalog.code = params.code
        productCatalog.name = params.name
        productCatalog.description = params.description
        return productCatalog
    }


}