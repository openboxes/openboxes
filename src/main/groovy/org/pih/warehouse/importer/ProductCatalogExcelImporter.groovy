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

import grails.util.Holders
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExpectedPropertyType
import org.pih.warehouse.product.ProductCatalog
import org.springframework.validation.BeanPropertyBindingResult

class ProductCatalogExcelImporter extends AbstractExcelImporter {

    def excelImportService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'id',
                    'B': 'code',
                    'C': 'name',
                    'D': 'description',

            ]
    ]

    static Map propertyMap = [
            id         : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            code       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            name       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            description: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]


    ProductCatalogExcelImporter(String fileName) {
        super(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
    }


    List<Map> getData() {
        return excelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null, propertyMap)
    }


    Boolean validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductCatalog productCatalog = createOrUpdateProductCatalog(params)
            if (!productCatalog.validate()) {
                productCatalog.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Product catalog ${productCatalog.name} is invalid: ${error.getFieldError()}")
                }
            }
            productCatalog.discard()
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
