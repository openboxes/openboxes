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
import org.pih.warehouse.data.DataService

@Transactional
class InventoryLevelImportDataService implements ImportDataService {
    DataService dataService

    @Override
    void validateData(ImportDataCommand command) {
        println "validate inventory levels " + command.filename
        command.data.eachWithIndex { row, index ->
            if (!dataService.validateInventoryLevel(row)) {
                command.errors.reject("Row ${index + 1}: Failed validation")
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        println "Import inventory levels " + command.location + " filename " + command.filename
        command.data.eachWithIndex { row, index ->
            dataService.importInventoryLevel(command.location, row, index)
        }
    }
}
