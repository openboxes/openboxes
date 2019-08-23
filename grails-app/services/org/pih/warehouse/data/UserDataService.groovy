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

import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.springframework.validation.BeanPropertyBindingResult

class UserDataService {

    /**
     * Validate inventory levels
     */
    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename

        command.data.eachWithIndex { params, index ->

            if (!params.username) {
                throw new IllegalArgumentException("Row ${index + 1}: username is required")
            }

            User user = createOrUpdateUser(params)

            log.info "User: ${user}"
            if (!user.validate()) {
                user.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("${index + 1}: username = ${user.username} ${error.getFieldError()}")
                }
            }

            // Implicitly validates the default roles
            Role[] defaultRoles = extractDefaultRoles(params.defaultRoles)
        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename

        command.data.eachWithIndex { params, index ->
            User user = createOrUpdateUser(params)
            Role[] defaultRoles = extractDefaultRoles(params.defaultRoles)
            log.info "user ${user.username} default role ${defaultRoles}"

            // Clear existing roles
            user?.roles?.toArray().each { Role role ->
                user.removeFromRoles(role)
            }

            // Add default roles to user
            defaultRoles.each { Role defaultRole ->
                log.info "user ${user.username} default role ${defaultRole}"
                if (!user?.roles?.contains(defaultRole)) {
                    user.addToRoles(defaultRole)
                }
            }
            user.save(failOnError: true)
        }

    }

    User createOrUpdateUser(Map params) {
        User user = User.findByUsername(params.username)
        if (!user) {
            user = new User(params)
            user.active = true
            user.password = "password"
        } else {
            user.properties = params
        }
        return user
    }


    Role[] extractDefaultRoles(String defaultRolesString) {
        String[] defaultRoles = defaultRolesString?.split(",")
        Role[] roles = defaultRoles.collect { String roleTypeName ->
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
