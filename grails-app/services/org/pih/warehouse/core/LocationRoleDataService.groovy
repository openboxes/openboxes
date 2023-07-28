package org.pih.warehouse.core

import grails.gorm.services.Service

@Service(LocationRole)
abstract class LocationRoleDataService {
    abstract LocationRole get(String id)

    abstract void delete(String id)

    /**
     *
     * @param locationRoleId
     * @return Returns user's id to the controller to display the edit page of the user after successful removal of location role
     */
    String deleteLocationRole(String locationRoleId) {
        LocationRole locationRole = get(locationRoleId)
        User user = locationRole?.user
        user?.removeFromLocationRoles(locationRole)
        delete(locationRole?.id)
        return user?.id
    }
}
