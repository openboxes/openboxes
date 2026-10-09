package org.pih.warehouse.core

/**
 * All of the roles of a single user: their global roles, which apply at every location, and their location roles,
 * which apply only at a specific location.
 */
class UserRolesDto {

    List<RoleDto> roles
    List<LocationRoleDto> locationRoles
}
