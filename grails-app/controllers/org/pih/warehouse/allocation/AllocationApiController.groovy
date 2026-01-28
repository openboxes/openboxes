package org.pih.warehouse.allocation

import grails.converters.JSON

class AllocationApiController {
    AllocationService allocationService

    def allocate() {
        def jsonBody = request.JSON ?: [:]
        AllocationRequest allocationRequest = jsonBody.allocationRequest as AllocationRequest
        try {
            def result = allocationService.allocate(allocationRequest)
            render(result as JSON)
        } catch (Exception e) {
            render(status: 500, [errorCode: 500, errorMessage: e.message] as JSON)
        }
    }
}
