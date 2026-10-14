package org.pih.warehouse.api

import org.apache.http.HttpStatus
import testutil.ApiControllerSpec

import org.pih.warehouse.core.LocationRoleDto
import org.pih.warehouse.core.RoleDto
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UserRoleService
import org.pih.warehouse.core.UserRolesDto
import org.pih.warehouse.location.LocationSimpleDto

class UserRoleApiControllerSpec extends ApiControllerSpec<UserRoleApiController> {

    UserRoleService userRoleService

    void setup() {
        userRoleService = Mock(UserRoleService)
        controller.userRoleService = userRoleService
    }

    void 'getUserRoles should return the roles of the user as JSON'() {
        given:
        UserRolesDto userRoles = new UserRolesDto(
                roles: [new RoleDto(id: 'role1', name: 'Admin', roleType: RoleType.ROLE_ADMIN)],
                locationRoles: [new LocationRoleDto(
                        id: 'locationRole1',
                        location: new LocationSimpleDto(id: 'facility1', name: 'Main Warehouse'),
                        role: new RoleDto(id: 'role2', name: 'Manager', roleType: RoleType.ROLE_MANAGER),
                )],
        )

        when:
        controller.getUserRoles('user1', 'facility1')

        then:
        1 * userRoleService.getUserRoles('user1', 'facility1') >> userRoles

        and:
        response.status == HttpStatus.SC_OK
        response.json.data.roles.size() == 1
        response.json.data.roles[0].id == 'role1'
        response.json.data.roles[0].name == 'Admin'
        response.json.data.roles[0].roleType == 'ROLE_ADMIN'
        response.json.data.locationRoles.size() == 1
        response.json.data.locationRoles[0].id == 'locationRole1'
        response.json.data.locationRoles[0].location.id == 'facility1'
        response.json.data.locationRoles[0].location.name == 'Main Warehouse'
        response.json.data.locationRoles[0].role.id == 'role2'
        response.json.data.locationRoles[0].role.roleType == 'ROLE_MANAGER'
    }
}
