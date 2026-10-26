package org.pih.warehouse.picking

class PickTaskController {
    def list() {
        render(view: "/common/react", params: params)
    }
}
