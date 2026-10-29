package org.pih.warehouse.api

import org.pih.warehouse.core.UserRoleService
import org.pih.warehouse.core.UserRolesDto

class UserRoleApiController extends BaseApiController {

    UserRoleService userRoleService

    /**
     * Fetches all roles of a user. If a facility is given, location roles are filtered to that facility.
     */
    def getUserRoles(String userId, String facilityId) {
        UserRolesDto userRoles = userRoleService.getUserRoles(userId, facilityId)
        renderResponse(userRoles)
    }
}
