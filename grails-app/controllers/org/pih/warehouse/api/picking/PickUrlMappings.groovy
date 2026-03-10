package org.pih.warehouse.api.picking

class PickUrlMappings {

    static mappings = {
        "/api/facilities/$facility/pick-tasks" {
            controller = "pickTaskApi"
            action = [GET: "search"]
        }

        "/api/facilities/$facility/pick-tasks/$id" {
            controller = "pickTaskApi"
            action = [GET: "read", PATCH: "patch"]
        }

        "/api/facilities/$facility/pick-tasks/$id/reallocate" {
            controller = "pickTaskApi"
            action = [POST: "reallocate"]
        }

        "/api/facilities/$facility/pick-tasks/containers/$outboundContainerId" {
            controller = "pickTaskApi"
            action = [PATCH: "drop"]
        }
    }
}
