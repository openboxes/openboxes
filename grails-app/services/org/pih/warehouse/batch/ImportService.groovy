/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.batch

import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.InventoryExcelImporter
import org.springframework.validation.Errors


class ImportService {

    def inventoryService

    boolean transactional = true

    /**
     * Reads a file for the given filename and generates an object that mirrors the
     * file.  Also preprocesses the object to make sure that the data is formatted
     * correctly.
     *
     * @param filename
     * @param errors
     * @return
     */
    List prepareData(Location location, String filename, Errors errors) {
        log.debug "Prepare inventory from file " + filename

        def inventoryImporter = new InventoryExcelImporter()
        def inventoryMapList = inventoryImporter.inventoryItems

        inventoryImporter.validate()

        return inventoryMapList
    }


}
