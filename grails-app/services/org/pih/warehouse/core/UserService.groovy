/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import com.google.gson.Gson
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.Sql
import org.apache.http.auth.AuthenticationException
import org.hibernate.sql.JoinType
import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.localization.MessageLocalizer

@Transactional
class UserService {

    def authService
    def dataSource
    GrailsApplication grailsApplication
    MessageLocalizer messageLocalizer

    User getUser(String id) {
        return User.get(id)
    }

    /**
     * Persist a newly-created user, along with an initial set of roles.
     *
     * Role IDs must be passed as an explicit `requestedRoleIds` argument,
     * separately from any other user fields, so that the service can run
     * them through {@link #checkCanAddOrRemoveRoles} before they reach the
     * database. (See {@link #updateUser} for the full rationale.)
     */
    User saveUser(User user, String requestingUserId, List<String> requestedRoleIds) {
        if (user.roles) {
            String message = messageLocalizer.localize('user.errors.rolesNotAllowedDirectly.message')
            user.errors.reject('user.errors.rolesNotAllowedDirectly.message', message)
            throw new ValidationException(message, user.errors)
        }
        if (user.locationRoles) {
            String message = messageLocalizer.localize('user.errors.locationRolesNotAllowedDirectly.message')
            user.errors.reject('user.errors.locationRolesNotAllowedDirectly.message', message)
            throw new ValidationException(message, user.errors)
        }
        if (requestedRoleIds) {
            validateAndApplyRoleChanges(User.get(requestingUserId), user, requestedRoleIds)
        }
        return user.save(failOnError: true)
    }

    /**
     * Apply an update to an existing user.
     *
     * Role IDs must be passed as an explicit `requestedRoleIds` argument
     * rather than being left inside the `params` map.
     *
     * Non-role fields (firstName, locale, etc.) are pulled from `params` and
     * applied directly to the user instance before saving. We don't allow this
     * for roles because, uniquely, the ability to set them depends on the
     * requesting user's role and permissions. If left in params, GORM will
     * cheerfully accept and bind them directly to the user, completely ignoring
     * the validation logic in {@link #checkCanAddOrRemoveRoles}.
     *
     * Controllers must extract role IDs and strip them out of the params map
     * before calling in to the service; see `UserController.update` / `save`.
     *
     * {@link #rejectRoleKeysInParams} makes sure they do their homework.
     */
    def updateUser(String userId, String requestingUserId, List<String> requestedRoleIds, Map params) {

        User userInstance = User.get(userId)
        User requestingUser = User.get(requestingUserId)

        rejectRoleKeysInParams(userInstance, params)

        if (requestedRoleIds) {
            validateAndApplyRoleChanges(requestingUser, userInstance, requestedRoleIds)
        }
        if (params.locationRolePairs) {
            validateAndApplyLocationRoleChanges(requestingUser, userInstance, params)
        }

        // Password in the db is different from the one specified
        // so the user must have changed the password.  We need
        // to compare the password with confirm password before
        // setting the new password in the database
        userInstance.properties = params
        // Needed to bypass the password == passwordConfirm validation
        userInstance.passwordConfirm = userInstance.password
        // Do not allow user to set his/her locale to translation mode locale
        def localizationModeLocale = new Locale(grailsApplication.config.openboxes.locale.localizationModeLocale)
        if (params.locale && LocalizationUtil.getLocale(params.locale) == localizationModeLocale) {
            userInstance.errors.rejectValue("locale", "user.errors.cannotSetLocaleToTranslationLocale.message", "You cannot set your default locale for translation mode locale")
            throw new ValidationException("user.errors.cannotSetLocaleToTranslationLocale.message", userInstance.errors)
        }

        return userInstance.save(failOnError: true)
    }

    void changePassword(User user, String password, String passwordConfirm) {
        if (!canUserEditPassword(AuthService.currentUser, user)) {
            String errorMessage = messageLocalizer.localize('errors.accessDenied.message')
            throw new AuthenticationException(errorMessage)
        }

        if (user.password != password) {
            user.password = password?.encodeAsPassword()
            user.passwordConfirm = passwordConfirm?.encodeAsPassword()
            user.save(failOnError: true)
        }
    }

    void assignDefaultRoles(User userInstance) {
        try {
            def defaultRoles = grailsApplication.config.openboxes.signup.defaultRoles
            if (!defaultRoles.isEmpty()) {
                def roleTypes = defaultRoles.split(",")
                roleTypes.each { roleType ->
                    def role = Role.findByRoleType(roleType)
                    userInstance.addToRoles(role)
                }

                if (userInstance.roles) {
                    userInstance.active = Boolean.TRUE
                }
                userInstance.save()
            }
        } catch (Exception e) {
            log.error("Unable to assign default roles: " + e.message, e)
        }
    }

    /**
     * Controllers must never pass role-related keys in a params map.
     *
     * They must be extracted by the controller and passed as separate arguments.
     */
    private void rejectRoleKeysInParams(User user, Map params) {
        if (params.any { key, val -> key?.toString()?.startsWith('roles') }) {
            String message = messageLocalizer.localize('user.errors.rolesNotAllowedDirectly.message')
            user.errors.reject('user.errors.rolesNotAllowedDirectly.message', message)
            throw new ValidationException(message, user.errors)
        }
        if (params.any { key, val -> key?.toString()?.startsWith('locationRoles') }) {
            String message = messageLocalizer.localize('user.errors.locationRolesNotAllowedDirectly.message')
            user.errors.reject('user.errors.locationRolesNotAllowedDirectly.message', message)
            throw new ValidationException(message, user.errors)
        }
    }

    private void validateAndApplyRoleChanges(User requestingUser, User user, List<String> requestedRoleIds) {
        List<Role> requestedRoles = Role.findAllByIdInList(requestedRoleIds)
        checkCanAddOrRemoveRoles(requestingUser, user, user.roles, requestedRoles)

        /*
         * While it may be tempting to directly assign the roles collection,
         * it's safer, GORM/Hibernate-wise, to add/remove them one at a time.
         */
        Set<Role> before = (user.roles ?: []) as Set
        Set<Role> after = requestedRoles as Set
        (before - after).each { user.removeFromRoles(it) }
        (after - before).each { user.addToRoles(it) }
    }

    private void validateAndApplyLocationRoleChanges(User requestingUser, User user, Map params) {
        Map<String, String> locationRolePairs = params.locationRolePairs
        checkCanAddOrRemoveLocationRoles(requestingUser, user, locationRolePairs)

        // remove location roles not present in the update
        user.locationRoles
            ?.findAll { !locationRolePairs[it.location.id] }
            ?.each { user.removeFromLocationRoles(it) }

        // add or update location roles
        locationRolePairs.each { locationId, roleId ->
            if (roleId) {
                Location location = Location.get(locationId)
                Role role = Role.get(roleId)
                LocationRole existing = user.locationRoles?.find { it.location == location }
                if (existing?.role == role) {
                    // nothing to do
                } else if (existing) {
                    // modify existing locationRole with new role type
                    existing.role = role
                } else {
                    // no existing locationRole for this location, add new one
                    user.addToLocationRoles(new LocationRole(user: user, location: location, role: role))
                }
            }
        }

        // GORM ignores this anyway, but it doesn't hurt to clean up when we're done
        params.remove('locationRolePairs')
    }

    Boolean isSuperuser(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_SUPERUSER]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false
    }

    Boolean isUserAdmin(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false
    }

    Boolean isUserManager(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_ASSISTANT]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false
    }

    Boolean isUserRequestor(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_REQUESTOR]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false
    }

    Boolean hasHighestRole(User u, String locationId, RoleType roleType) {
        if (u) {
            def user = User.get(u.id)
            Location currentLocation = Location.get(locationId)
            def highestRole = user.getHighestRole(currentLocation)
            return roleType == highestRole?.roleType
        }
        return false
    }

    Boolean canUserBrowse(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_BROWSER, RoleType.ROLE_ASSISTANT]
            return getEffectiveRoles(user).any { roles.contains(it.roleType) }
        }
        return false
    }

    Boolean hasAllRoles(User user, List<RoleType> roleTypes) {
        return getEffectiveRoles(user).all { Role role -> roleTypes.contains(role.roleType) }
    }

    Boolean hasAnyRoles(User user, List<RoleType> roleTypes) {
        return getEffectiveRoles(user).any { Role role -> roleTypes.contains(role.roleType) }
    }

    Boolean hasRoleFinance(User u) {
        if (u) {
            def user = User.get(u.id)
            def roleTypes = [RoleType.ROLE_FINANCE]
            return getEffectiveRoles(user).any { Role role -> roleTypes.contains(role.roleType) }
        }
        return false
    }

    Boolean hasRolePurchaseApprover(User u) {
        if (u) {
            def user = User.get(u.id)
            def roleTypes = [RoleType.ROLE_PURCHASE_APPROVER]
            return getEffectiveRoles(user).any { Role role -> roleTypes.contains(role.roleType) }
        }
        return false
    }

    Boolean hasRoleInvoice(User u) {
        if (u) {
            def user = User.get(u.id)
            def roleTypes = [RoleType.ROLE_INVOICE]
            def co = getEffectiveRoles(user).any { Role role -> roleTypes.contains(role.roleType) }
            return co
        }
        return false
    }

    Boolean hasRoleProductManager(User u) {
        if (u) {
            def user = User.get(u.id)
            def roleTypes = [RoleType.ROLE_PRODUCT_MANAGER]
            def co = getEffectiveRoles(user).any { Role role -> roleTypes.contains(role.roleType) }
            return co
        }
        return false
    }

    Boolean hasRoleBrowser(User u) {
        if (u) {
            def user = User.get(u.id)
            def roleTypes = [RoleType.ROLE_BROWSER]
            return getEffectiveRoles(user).any { Role role -> roleTypes.contains(role.roleType) }
        }
        return false
    }

    // Checks if requestor role exist for any location - for location chooser purposes
    Boolean hasRoleRequestorInAnyLocations(User u) {
        if (u) {
            def user = User.get(u.id)
            def roleTypes = [RoleType.ROLE_REQUESTOR]
            return user.getAllRoles().any { Role role -> roleTypes.contains(role.roleType) }
        }
        return false
    }

    /**
     * Returns true if requestingUser has permission to assign or remove targetRole.
     */
    Boolean canAddOrRemoveRole(User requestingUser, Role targetRole) {
        if (!requestingUser) {
            return false
        }
        if (isSuperuser(requestingUser)) {
            return true
        }

        /*
         * FIXME: we should check the user's highest role at the location
         * of the role being assigned, not just their highest role overall.
         */
        Role currentHighest = requestingUser.getHighestRole(authService.currentLocation)
        if (!currentHighest) {
            return false
        }

        return currentHighest.roleType.sortOrder <= targetRole.roleType.sortOrder
    }

    private void rejectRoleChange(User targetUser, String messageCode, User requestingUser, Role role) {
        String message = messageLocalizer.localize(messageCode, requestingUser?.username, role.name)
        log.warn("Rejected role change on user ${targetUser} by ${requestingUser}: ${messageCode}")
        targetUser.errors.reject(messageCode, [requestingUser?.username, role.name] as Object[], message)
        throw new ValidationException(message, targetUser.errors)
    }

    void checkCanAddOrRemoveRoles(User requestingUser, User targetUser, Collection<Role> before, Collection<Role> after) {
        if (isSuperuser(requestingUser)) {
            return
        }
        Set<Role> added = ((after ?: []) as Set) - ((before ?: []) as Set)
        Set<Role> removed = ((before ?: []) as Set) - ((after ?: []) as Set)
        for (Role role : added) {
            if (!canAddOrRemoveRole(requestingUser, role)) {
                rejectRoleChange(targetUser, 'user.errors.cannotAssignRole.message', requestingUser, role)
            }
        }
        for (Role role : removed) {
            if (!canAddOrRemoveRole(requestingUser, role)) {
                rejectRoleChange(targetUser, 'user.errors.cannotRemoveRole.message', requestingUser, role)
            }
        }
    }

    private void checkCanAddOrRemoveLocationRoles(User requestingUser, User targetUser, Map<String, String> locationRolePairs) {
        List<Role> before = targetUser.locationRoles?.collect { it.role }
        List<Role> after = locationRolePairs.values()
            .findAll { it }
            .collect { Role.get(it) }
            .findAll { it }
        checkCanAddOrRemoveRoles(requestingUser, targetUser, before, after)
    }

    boolean isUserInAllRoles(String userId, Collection roleTypes, String locationId) {
        User user = User.get(userId)
        Location location = Location.get(locationId)

        return user.hasRoles(location, roleTypes)
    }

    Boolean isUserInRole(User user, RoleType roleType) {
        return isUserInRole(user.id, [roleType])
    }

    Boolean isUserInRole(User user, Collection roleTypes) {
        return isUserInRole(user.id, roleTypes)
    }

    Boolean isUserInRole(String userId, Collection roleTypes) {
        Collection acceptedRoleTypes = RoleType.expand(roleTypes)
        User user = getUser(userId)
        return getEffectiveRoles(user).any { Role role ->
            boolean acceptedRoleType = acceptedRoleTypes.contains(role.roleType)
            log.debug "Is role ${role.roleType} in ${acceptedRoleTypes} = ${acceptedRoleType}"
            return acceptedRoleType
        }
    }

    boolean hasRoleFinance() {
        User user = authService.currentUser
        return hasRoleFinance(user)
    }

    void assertCurrentUserHasRoleFinance() {
        User user = authService.currentUser
        if (!hasRoleFinance(user)) {
            throw new IllegalStateException("User ${user.username} must have ROLE_FINANCE role")
        }
    }

    List<User> findUsers(Map params) {
        List<String> terms = params.searchTerm?.split(",| ")
        List<RoleType> roleTypeList = params.roleTypes.collect { RoleType.valueOf(it) }
        List<String> roleIds = roleTypeList ? Role.findAllByRoleTypeInList(roleTypeList).collect{ it.id } : null
        Location location = params.location ? Location.get(params.location) : null

        return User.createCriteria().listDistinct() {
            if (params.active != null) {
                eq("active", Boolean.valueOf(params.active))
            }
            if (terms) {
                or {
                    terms.each { String term ->
                        ilike("firstName", "%" + term + "%")
                        ilike("lastName", "%" + term + "%")
                        ilike("email", "%" + term + "%")
                    }
                }
            }
            if (roleIds) {
                or {
                    roles(JoinType.LEFT_OUTER_JOIN.joinTypeValue)  {
                        'in'("id", roleIds)
                    }
                    if (location) {
                        locationRoles(JoinType.LEFT_OUTER_JOIN.joinTypeValue)  {
                            'in'("role.id", roleIds)
                            eq("location.id", location.id)
                        }
                    }
                }
            }
            order("lastName", "desc")
        }
    }

    def findPersons(String[] terms) {
        return findPersons(terms, [:])
    }

    def findPersons(String[] terms, params) {
        def results = Person.createCriteria().list(params) {
            if (params.status) {
                eq("active", Boolean.valueOf(params.status))
            }
            if (terms) {
                terms.each { term ->
                    or {
                        ilike("firstName", "%" + term + "%")
                        ilike("lastName", "%" + term + "%")
                        ilike("email", "%" + term + "%")
                    }
                }
            }
            order(params.sort ?: "lastName", params.order ?: "desc")
        }
        return results
    }

    def findUsers(String query, Map params) {
        println "findUsers: " + query + " : " + params
        def criteria = User.createCriteria()
        def results = criteria.list(params) {
            if (query) {
                or {
                    like("firstName", query)
                    like("lastName", query)
                    like("email", query)
                    like("username", query)
                }
            }
            if (params.status) {
                eq("active", Boolean.valueOf(params.status))
            }
            // Disabled to allow the user to choose sorting mechanism (should probably add this as the default)
        }

        return results
    }


    void convertPersonToUser(String personId) {
        def user = User.get(personId)
        if (!user) {
            def person = Person.get(personId)
            if (person) {
                def encodedPassword = "password"?.encodeAsPassword()
                Sql sql = new Sql(dataSource)
                sql.execute('insert into user (id, username, password) values (:id, :username, :password)', [id: person?.id, username: person?.email, password: encodedPassword])
            }
        }
    }

    void convertUserToPerson(String personId) {
        def person = Person.get(personId)
        if (person) {
            Sql sql = new Sql(dataSource)
            sql.execute('delete from user where id = :id', [id: personId])
        }
    }

    def findUsersByRoleType(RoleType roleType) {
        def users = []
        def role = Role.findByRoleType(roleType)
        if (role) {
            def criteria = User.createCriteria()
            users = criteria.list {
                eq("active", true)
                roles(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                    eq("id", role.id)
                }
            }
        }
        return users
    }

    def findUsersByRoleTypes(Location location, List<RoleType> roleTypes) {
        def users = []
        def roleList = Role.findAllByRoleTypeInList(roleTypes)
        def roleIds = roleList.collect { it.id }
        if (roleIds) {
            users = User.createCriteria().listDistinct {
                eq("active", true)
                or {
                    roles(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                        'in'("id", roleIds)
                    }
                    if (location) {
                        locationRoles(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                            'in'("role.id", roleIds)
                            eq("location.id", location.id)
                        }
                    }
                }
            }
        }
        return users
    }

    public def getEffectiveRoles(User user) {
        def currentLocation = authService.currentLocation
        return getEffectiveRoles(user, currentLocation)
    }

    public def getEffectiveRoles(User user, Location location) {
        return user.getEffectiveRoles(location)
    }


    def getAllAdminUsers() {
        def recipients = []
        def roleAdmin = Role.findByRoleType(RoleType.ROLE_ADMIN)
        if (roleAdmin) {
            def criteria = User.createCriteria()
            recipients = criteria.list {
                eq("active", true)
                roles {
                    eq("id", roleAdmin.id)
                }
            }
        }
        return recipients
    }

    /**
     *
     * @param username
     * @param password
     * @return
     */
    def authenticate(username, password) {
        return authenticateUsingDatabase(username, password)
    }

    /**
     *
     * @param username
     * @param password
     * @return
     */
    def authenticateUsingDatabase(username, password) {
        def userInstance = User.findByUsernameOrEmail(username, username)
        if (userInstance) {
            return (userInstance.password == password.encodeAsPassword() || userInstance.password == password)
        }
        return false
    }

    def getDashboardConfig(User user, String id) {
        Gson gson = new Gson()
        // Creating a deep copy of the dashboard configuration
        // to avoid overriding config properties.
        // Deep copy is created here using the gson library
        // in the way mentioned in this article: https://www.baeldung.com/java-deep-copy
        def fullConfig = gson.fromJson(
                gson.toJson(Holders.config.openboxes.dashboardConfig),
                Object
        )
        def mainDashboardId = Holders.config.openboxes.dashboardConfig.mainDashboardId
        def resultConfig = [
                dashboard: fullConfig.dashboards[id ?: mainDashboardId],
                dashboardWidgets: fullConfig.dashboardWidgets
        ]
        def userConfig = user.deserializeDashboardConfig()

        if (userConfig && id == mainDashboardId) {
            def personalDashboard = userConfig?.personal
            if (personalDashboard && resultConfig.dashboard?.personal) {
                resultConfig.dashboard.personal = personalDashboard
            }
        }

        return resultConfig
    }

    def updateDashboardConfig(User user, Object config) {
        String stringConfig = user.serializeDashboardConfig(config)
        user.dashboardConfig = stringConfig
        return user.deserializeDashboardConfig()
    }

    def saveLocationRole(Location location, LocationRole locationRole, List<Role> roles, User user, String requestingUserId) {
        /*
         * This method both creates a location role and assigns it. If the
         * requester doesn't have permission to assign the role, stop immediately.
         */
        User requestingUser = User.get(requestingUserId)
        List<Role> before = locationRole ? [locationRole.role] : []
        checkCanAddOrRemoveRoles(requestingUser, user, before, roles)

        // Update existing role
        if (locationRole && roles.size() == 1) {
            locationRole.role = roles.first()
            locationRole.location = location
            user.addToLocationRoles(locationRole)
        } else {
            // Create new roles
            List<LocationRole> locationRoles = LocationRole.findAllByUserAndLocation(user, location)
            roles.each { role ->
                LocationRole foundLocationRole = locationRoles.find { it.role == role }
                if (!foundLocationRole) {
                    foundLocationRole = new LocationRole()
                    foundLocationRole.role = role
                    foundLocationRole.location = location
                    user.addToLocationRoles(foundLocationRole)
                }
            }
        }
        user.save(failOnError: true)
    }

    String deleteLocationRole(LocationRole locationRole, String requestingUserId) {
        if (locationRole) {
            User requestingUser = User.get(requestingUserId)
            checkCanAddOrRemoveRoles(requestingUser, locationRole.user, [locationRole.role], [])
        }
        User user = locationRole?.user
        user?.removeFromLocationRoles(locationRole)
        locationRole?.delete()
        user?.save(failOnError: true)
        return user?.id
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

    Boolean canUserEditPassword(User currentUser, User userToEdit) {
        currentUser?.id == userToEdit.id || isUserAdmin(currentUser)
    }

}
