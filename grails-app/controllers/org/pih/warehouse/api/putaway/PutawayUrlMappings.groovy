package org.pih.warehouse.api.putaway

class PutawayUrlMappings {

    static mappings = {
        "/api/facilities/$facility/putaway-tasks" {
            controller = "putawayTaskApi"
            action = [GET: "search", POST: "create"]
        }

        "/api/facilities/$facility/putaway-tasks/$id" {
            controller = "putawayTaskApi"
            action = [GET: "read", PUT: "update", PATCH: "patch", DELETE: "delete"]
        }

        // FIXME Not used for now since we can use the patch method in the previous UrlMapping, but we might want to
        //  bring this back at some point
        "/api/facilities/$facility/putaway-tasks/$id/status" {
            controller = "putawayTaskApi"
            action = [PATCH: "patch"]
        }

    }

}