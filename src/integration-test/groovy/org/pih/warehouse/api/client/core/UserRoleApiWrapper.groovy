package org.pih.warehouse.api.client.core

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class UserRoleApiWrapper extends ApiWrapper<UserRoleApi> {

    JsonPath getUserRolesOK(String userId, String facilityId=null) {
        return api.getUserRoles(userId, facilityId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }
}
