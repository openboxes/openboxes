/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.auth

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

@CompileStatic
@Transactional(readOnly = true)
class AuthService {

    private static ThreadLocal<User> threadLocalUser
    private static ThreadLocal<Location> threadLocalLocation

    void setCurrentUser(User user) {
        if (!threadLocalUser) {
            threadLocalUser = new ThreadLocal<User>()
        }

        // misuse get() to prevent javax.persistence.EntityExistsException
        threadLocalUser.set(user?.id ? User.get(user.id) : null)
    }

    static User getCurrentUser() {
        return threadLocalUser?.get()
    }

    void setCurrentLocation(Location location) {
        if (!threadLocalLocation) {
            threadLocalLocation = new ThreadLocal<Location>()
        }

        // misuse get() to prevent javax.persistence.EntityExistsException
        threadLocalLocation.set(location?.id ? Location.get(location.id) : null)
    }

    static Location getCurrentLocation() {
        return threadLocalLocation?.get()
    }
}
