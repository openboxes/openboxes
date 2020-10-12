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
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAttribute
import org.springframework.validation.BeanPropertyBindingResult

class ProductAttributeExcelImporter extends AbstractExcelImporter {

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'productCode',
                    'B': 'attributeCode',
                    'C': 'attributeValue',
                    'D': 'unitOfMeasureCode'
            ]
    ]

    static Map propertyMap = [
            productCode   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            attributeCode : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            attributeValue: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            unitOfMeasureCode: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
    ]


    ProductAttributeExcelImporter(String fileName) {
        super(fileName)
    }


    List<Map> getData() {
        return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }

    void validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductAttribute productAttribute = createOrUpdateProductAttribute(params)
            if (!productAttribute.validate()) {
                productAttribute.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Product attribute ${productAttribute} is invalid: ${error.getFieldError()}")
                }
            }
        }
    }

    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            ProductAttribute productAttribute = createOrUpdateProductAttribute(params)
            if (productAttribute.validate()) {
                productAttribute.product.save(failOnError: true)
            }
        }
    }


    ProductAttribute createOrUpdateProductAttribute(Map params) {
        Product product = Product.findByProductCode(params.productCode)
        Attribute attribute = Attribute.findByCode(params.attributeCode)
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.findByCode(params.unitOfMeasureCode)
        ProductAttribute productAttribute = ProductAttribute.findByProductAndAttribute(product, attribute)
        if (!productAttribute) {
            productAttribute = new ProductAttribute()
            productAttribute.attribute = attribute
            product.addToAttributes(productAttribute)
        }
        productAttribute.value = params.attributeValue
        productAttribute.unitOfMeasure = unitOfMeasure?:attribute?.unitOfMeasureClass?.baseUom
        return productAttribute
    }
}
