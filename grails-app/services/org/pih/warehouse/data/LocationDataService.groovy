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
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand

class LocationDataService {

    //boolean transactional = true


    /**
     * Validate inventory levels
     */
    Boolean validateData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->



        }
    }

    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { params, index ->

        }

    }

    User createOrUpdateUser(Map params) {
        User user = User.findByUsername(params.username)
        if (!user) {
            user = new User(params)
            user.password = "password"
        }
        else {
            user.properties = params
        }
        return user
    }


    Role [] extractDefaultRoles(String defaultRolesString) {
        String [] defaultRoles = defaultRolesString?.split(",")
        Role [] roles = defaultRoles.collect { String roleTypeName ->
            roleTypeName = roleTypeName.trim()
            Role role = Role.findByName(roleTypeName)
            if (!role) {
                RoleType roleType = RoleType.valueOf(roleTypeName)
                role = Role.findByRoleType(roleType)
            }
            return role
        }
        return roles
    }
}
