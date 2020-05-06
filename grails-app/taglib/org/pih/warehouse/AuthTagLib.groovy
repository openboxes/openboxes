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

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class AuthTagLib {

    def userService

    def supports = { attrs, body ->
        def location = Location.load(session.warehouse.id)
        if (location && location.supports(attrs.activityCode)) {
            out << body()
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

    def isUserInRole = { attrs, body ->
        if (session.user) {
            def isUserInRole = userService.isUserInRole(session?.user?.id, attrs.roles)
            if (isUserInRole)
                out << body()
        }
    }

    def userRole = { attrs, body ->
        Location location = Location.get(session.warehouse.id)
        User.withTransaction {
            out << User.get(attrs.user.id).getHighestRole(location)
        }
    }

    def userPhoto = { attrs, body ->
        User.withTransaction {
            def user = User.get(attrs.user.id)
            out << render(template: "/taglib/userPhoto", model: [userInstance: user])
        }
    }


    def authorize = { attrs, body ->
        def authorized = false

        def warehouseInstance = Location.get(session.warehouse.id)

        // Need to handle this case better
        if (!warehouseInstance)
            throw new Exception("Please choose a warehouse")

        // Check if the activity attribute has any activities supported by the given warehouse
        authorized = attrs?.activity?.any { warehouseInstance.supports(it) }

        if (authorized) {
            out << body {}
        }
    }
}
