package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import org.hibernate.ObjectNotFoundException

import org.pih.warehouse.core.mapper.SmartMapper

@Transactional(readOnly = true)
class UserRoleService {

    SmartMapper smartMapper

    /**
     * Returns all global roles of the user. Location roles are filtered to the given facility, if there is one.
     */
    UserRolesDto getUserRoles(String userId, String facilityId) {
        User user = User.get(userId)
        if (!user) {
            throw new ObjectNotFoundException(userId, User.toString())
        }
        if (facilityId && !Location.get(facilityId)) {
            throw new ObjectNotFoundException(facilityId, Location.toString())
        }

        List<Role> roles = user.roles?.sort(false) ?: []

        // Compare ids so we don't fetch every location just to check if it's the facility
        List<LocationRole> locationRoles = user.locationRoles
                ?.findAll { !facilityId || it.location.id == facilityId }
                ?.sort { a, b -> a.location.name <=> b.location.name ?: a.role <=> b.role } ?: []

        return new UserRolesDto(
                roles: smartMapper.mapCollection(roles, RoleDto),
                locationRoles: smartMapper.mapCollection(locationRoles, LocationRoleDto),
        )
    }
}
