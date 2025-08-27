package org.pih.warehouse.api

import grails.converters.JSON

class InventoryApiController {
    def importCsv() {
        String fileData = request.inputStream.text

        if (fileData.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty")
        }

        if (request.contentType != "text/csv") {
            throw new IllegalArgumentException("File must be in CSV format")
        }

        render([data: []] as JSON)
    }
}
