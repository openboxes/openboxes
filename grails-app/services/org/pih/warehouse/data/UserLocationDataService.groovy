/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.data

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand

class UserLocationDataService {

    /**
     * Validate inventory levels
     */
    Boolean validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->

            User user = User.findByUsername(params.username)
            if (!user) {
                command.errors.reject("Row ${index + 1} User ${params.username} not found")
            }

            Location location = Location.findByNameOrLocationNumber(params.locationName, params.locationName)
            if (!location) {
                command.errors.reject("Row ${index + 1} Location ${params.locationName} not found")
            }

            RoleType roleType = RoleType.valueOf(params.roleName)
            if (!roleType) {
                command.errors.reject("Row ${index + 1} Role type ${params.roleName} not found")
            }

            Role role = Role.findByRoleType(roleType)
            if (!role) {
                command.errors.reject("Row ${index + 1} Role for role type ${roleType} not found")
            }

        }
    }

    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->
            User user = User.findByUsername(params.username)
            Location location = Location.findByNameOrLocationNumber(params.locationName, params.locationName)
            RoleType roleType = RoleType.valueOf(params.roleName)
            Role role = Role.findByRoleType(roleType)

            LocationRole locationRole = user.locationRoles.find {
                it.location == location
            }

            if (!locationRole) {
                locationRole = new LocationRole()
                user.addToLocationRoles(locationRole)
            }

            locationRole.location = location
            locationRole.role = role

            user.save(failOnError: true)
        }

    }


}
