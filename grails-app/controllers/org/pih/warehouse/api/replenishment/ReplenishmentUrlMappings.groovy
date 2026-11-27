/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api.replenishment

class ReplenishmentUrlMappings {

    static mappings = {
        "/api/facilities/$facility/replenishments" {
            controller = "replenishmentTaskApi"
            action = [GET: "lists"]
        }

        "/api/facilities/$facility/replenishment-tasks" {
            controller = "replenishmentTaskApi"
            action = [GET: "search"]
        }

        "/api/facilities/$facility/replenishment-tasks/$id" {
            controller = "replenishmentTaskApi"
            action = [GET: "read", PATCH: "patch"]
        }

        "/api/facilities/$facility/replenishment-tasks/containers/$outboundContainerId" {
            controller = "replenishmentTaskApi"
            action = [PATCH: "drop"]
        }
    }
}
