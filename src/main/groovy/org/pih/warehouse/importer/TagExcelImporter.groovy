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

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExpectedPropertyType
import org.pih.warehouse.core.Tag
import org.springframework.validation.BeanPropertyBindingResult

class TagExcelImporter extends AbstractExcelImporter {

    def excelImportService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'id',
                    'B': 'tag'
            ]
    ]

    static Map propertyMap = [
            id : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            tag: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]

    TagExcelImporter(String fileName) {
        super(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
    }

    List<Map> getData() {
        return excelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null, propertyMap)
    }

    void validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            Tag tag = createOrUpdateTag(params)
            if (!tag.validate()) {
                tag.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index + 1}: Tag ${tag.tag} is invalid: ${error.getFieldError()}")
                }
            }
            tag.discard()
        }
    }

    @Transactional
    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            Tag tag = createOrUpdateTag(params)
            if (tag.validate()) {
                tag.save(failOnError: true)
            }
        }
    }


    Tag createOrUpdateTag(Map params) {
        Tag tag = Tag.findByIdOrTag(params.id, params.tag)
        if (!tag) {
            tag = new Tag()
        }
        tag.id = params.id
        tag.tag = params.tag
        return tag
    }


}
