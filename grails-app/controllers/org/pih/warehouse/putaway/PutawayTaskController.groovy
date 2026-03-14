package org.pih.warehouse.putaway

class PutawayTaskController {
    def list() {
        render(view: "/common/react", params: params)
    }
}
