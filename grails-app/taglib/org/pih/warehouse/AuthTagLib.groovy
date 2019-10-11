/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class AuthTagLib {

    def userService

    def supports = { attrs, body ->
        def warehouseInstance = Location.get(session.warehouse.id)
        def authorized = true

        // Need to handle this case better
        if (!warehouseInstance)
            throw new Exception("Please choose a warehouse")

        // Check if activityCode attribute is supported by given warehouse
        if (attrs?.activityCode)
            authorized = authorized && warehouseInstance.supports(attrs.activityCode as ActivityCode)

        // Check if the activitiesAny attribute has any activities supported by the given warehouse
        if (attrs?.activitiesAny)
            authorized = authorized && attrs.activitiesAny?.any { warehouseInstance.supports(it as ActivityCode) }

        // Check if the activitiesAny attribute has all activities supported by the given warehouse
        if (attrs?.activitiesAll)
            authorized = authorized && attrs.activitiesAll?.every { warehouseInstance.supports(it as ActivityCode) }

        if (authorized) {
            out << body {}
        }
    }

    def hideIfIsNonInventoryManagedAndCanSubmitRequest = {attrs, body ->
        def warehouseInstance = Location.get(session.warehouse.id)
        def authorized = true

        authorized = authorized && !warehouseInstance.supports(ActivityCode.MANAGE_INVENTORY)
        authorized = authorized && warehouseInstance.supports(ActivityCode.SUBMIT_REQUEST)

        // If the location does not have MANAGE_INVENTORY activity code and have SUBMIT_REQUEST activity code, then return empty body
        if (!authorized) {
            out << body {}
        }
    }

    def isSuperuser = { attrs, body ->
        if (userService.isSuperuser(session?.user))
            out << body()
    }
    def isUserAdmin = { attrs, body ->
        if (userService.isUserAdmin(session?.user))
            out << body()
    }
    def isUserManager = { attrs, body ->
        if (userService.isUserManager(session?.user))
            out << body()
    }
    def hasRoleFinance = { attrs, body ->
        if (userService.hasRoleFinance(session?.user)) {
            out << body()
        } else {
            if (attrs.onAccessDenied) {
                out << attrs.onAccessDenied
            }
        }
    }
    def hasRoleApprover = { attrs, body ->
        if (userService.hasRoleApprover(session?.user))
            out << body()
    }

    def hasRoleInvoice = { attrs, body ->
        if (userService.hasRoleInvoice(session?.user))
            out << body()
    }

    def isUserInRole = { attrs, body ->
        if (session.user) {
            def isUserInRole = userService.isUserInRole(session?.user?.id, attrs.roles)
            if (isUserInRole)
                out << body()
        }
    }

    def userRole = { attrs, body ->
        Location location = Location.get(session.warehouse.id)
        out << User.get(attrs.user.id).getHighestRole(location)
    }

    def userPhoto = { attrs, body ->
        def user = User.get(attrs.user.id)
        out << render(template: "/taglib/userPhoto", model: [userInstance: user])
    }

    def hasHighestRoleAuthenticated = { attrs, body ->
        if (userService.hasHighestRole(session?.user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED))
            out << body()
    }

    def hasHigherRoleThanAuthenticated = { attrs, body ->
        if (!userService.hasHighestRole(session?.user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED))
            out << body()
    }
}
