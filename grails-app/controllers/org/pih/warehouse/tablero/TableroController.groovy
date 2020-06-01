package org.pih.warehouse.tablero

import grails.converters.JSON

class TableroController {

    def index= {
        render "This page will be deleted soon"
    }

    def config = {
        render([
                data: grailsApplication.config.openboxes.tablero,
        ] as JSON)
    }

}
