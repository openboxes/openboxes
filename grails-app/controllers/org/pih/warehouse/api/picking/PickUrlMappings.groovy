package org.pih.warehouse.api.picking

class PickUrlMappings {

    static mappings = {
        "/api/facilities/$facility/pick-tasks" {
            controller = "pickTaskApi"
            action = [GET: "search"]
        }
    }
}
