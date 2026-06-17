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
import grails.util.Holders
import groovy.transform.CompileStatic
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

@CompileStatic
@Transactional(readOnly = true)
class AuthService {

    static final String SYSTEM_USER_USERNAME_CONFIG_KEY = "openboxes.system.username"
    static final String DEFAULT_SYSTEM_USER_USERNAME = "admin"

    private static ThreadLocal<User> threadLocalUser
    private static ThreadLocal<Location> threadLocalLocation

    private static void setThreadLocalUser(User user) {
        if (!threadLocalUser) {
            threadLocalUser = new ThreadLocal<User>()
        }

        // misuse get() to prevent javax.persistence.EntityExistsException
        threadLocalUser.set(user?.id ? User.get(user.id) : null)
    }

    void setCurrentUser(User user) {
        setThreadLocalUser(user)
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

    /**
     * Returns the user that background jobs authenticate as. The username is configurable via
     * {@code openboxes.system.username} and defaults to the built-in "admin" user.
     *
     * The system user is intentionally allowed to be disabled (active = false). Unlike interactive
     * login, the active flag is not enforced here because this is only used for internal,
     * non-interactive authentication while a job runs.
     *
     * @throws IllegalStateException if no user exists for the configured username
     */
    static User getSystemUser() {
        String username = Holders.config.getProperty(
                SYSTEM_USER_USERNAME_CONFIG_KEY, String, DEFAULT_SYSTEM_USER_USERNAME)
        User systemUser = (User) User.find("from User as u where u.username = :username", [username: username])
        if (!systemUser) {
            throw new IllegalStateException(
                    ("Unable to authenticate background job: no user found for configured system user '${username}'. " +
                            "Set '${SYSTEM_USER_USERNAME_CONFIG_KEY}' to the username of an existing user.").toString())
        }
        return systemUser
    }

    /**
     * Executes the given closure with the system user set as the current user, restoring the
     * previously authenticated user (if any) afterward. Intended for use by background jobs so that
     * any records they create or update are stamped with a valid current user.
     *
     * This is intentionally not transactional: the GORM lookups it performs only require a bound
     * Hibernate session (which jobs establish via withNewSession), and keeping it outside the
     * class-level read-only transaction lets the wrapped services manage their own transactions.
     *
     * @return whatever the closure returns
     */
    static def withSystemUser(Closure closure) {
        User previousUser = getCurrentUser()
        try {
            setThreadLocalUser(getSystemUser())
            return closure.call()
        } finally {
            setThreadLocalUser(previousUser)
        }
    }
}
