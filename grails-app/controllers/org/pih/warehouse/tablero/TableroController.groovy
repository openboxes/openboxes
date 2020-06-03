package org.pih.warehouse.tablero

import grails.converters.JSON

class TableroController {

    def index= {
        render(template: "/common/react")
    }

    def config = {
        render([
                data: grailsApplication.config.openboxes.tablero,
        ] as JSON)
    }

}
