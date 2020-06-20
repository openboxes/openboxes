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

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.springframework.web.multipart.MultipartFile

class ImportDataCommand implements Validateable {
    def filename
    MultipartFile importFile
    String importType
    Boolean importNow = Boolean.FALSE
    Location location
    Date date
    def columnMap
    def data
    //def errors
    def warnings = [:]
    def products = []
    def categories = []
    def inventoryItems = []
    def transaction

    static constraints = {
        date(nullable: true)
        filename(nullable: true)
        importNow(nullable: true)
        importFile(nullable: true, validator: { val, obj ->

            // Allow the import file to be null but not empty
            if ( val == null ) {
                return true
            }

            if ( val.isEmpty() ) {
                return false
            }

            return ['application/vnd.ms-excel', 'text/plain', 'text/csv', 'text/tsv'].any { fileType ->
                val.part.fileItem.contentType == fileType
            }
        })
        importType(nullable: false)
        location(nullable: false)
        columnMap(nullable: true)
        data(nullable: true)
        products(nullable: true)
        categories(nullable: true)
        inventoryItems(nullable: true)
        transaction(nullable: true)
    }

}
