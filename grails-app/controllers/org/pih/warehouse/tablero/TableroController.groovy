package org.pih.warehouse.tablero

import grails.converters.JSON

class TableroController {

    def config = {
        render([
                data: grailsApplication.config.openboxes.tablero,
        ] as JSON)
    }
}
