/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api.picking

class PickUrlMappings {

    static mappings = {
        "/api/facilities/$facility/pick-tasks" {
            controller = "pickTaskApi"
            action = [GET: "search"]
        }

        "/api/facilities/$facility/pick-tasks/counts" {
            controller = "pickTaskApi"
            action = [GET: "counts"]
        }

        "/api/facilities/$facility/pick-tasks/drop" {
            controller = "pickTaskApi"
            action = [PATCH: "drop"]
        }

        "/api/facilities/$facility/pick-tasks/$id" {
            controller = "pickTaskApi"
            action = [GET: "read", PATCH: "patch"]
        }

        "/api/facilities/$facility/pick-tasks/$id/reallocate" {
            controller = "pickTaskApi"
            action = [POST: "reallocate"]
        }

        // Deprecated: kept for mobile app versions released before the scoped drop endpoint above
        "/api/facilities/$facility/pick-tasks/containers/$outboundContainerId" {
            controller = "pickTaskApi"
            action = [PATCH: "dropContainer"]
        }
    }
}
