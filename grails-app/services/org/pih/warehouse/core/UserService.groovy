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

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.sql.Sql
import org.apache.commons.collections.ListUtils
import org.hibernate.sql.JoinType
import org.pih.warehouse.auth.AuthService

@Transactional
class UserService {

    def authService
    def dataSource
    GrailsApplication grailsApplication

    User getUser(String id) {
        return User.get(id)
    }

    User saveUser(User user) {
        return user.save()
    }


    def updateUser(String userId, String currentUserId, Map params) {

        User userInstance = User.get(userId)
        List<Role> updatedRoles = params.roles ? Role.findAllByIdInList(params.list("roles")) : []

        if (params.roles) {
            userInstance.roles = updatedRoles
            params.remove("roles")
        }

        // Password in the db is different from the one specified
        // so the user must have changed the password.  We need
        // to compare the password with confirm password before
        // setting the new password in the database
        userInstance.properties = params
        if (params.changePassword && userInstance.password != params.password) {
            userInstance.password = params?.password?.encodeAsPassword()
            userInstance.passwordConfirm = params?.passwordConfirm?.encodeAsPassword()
        } else {
            // Needed to bypass the password == passwordConfirm validation
            userInstance.passwordConfirm = userInstance.password
        }

        // Do not allow user to set his/her locale to translation mode locale
        def localizationModeLocale = new Locale(grailsApplication.config.openboxes.locale.localizationModeLocale)
        if (params.locale && new Locale(params.locale) == localizationModeLocale) {
            userInstance.errors.rejectValue("locale", "user.errors.cannotSetLocaleToTranslationLocale.message", "You cannot set your default locale for translation mode locale")
            throw new ValidationException("user.errors.cannotSetLocaleToTranslationLocale.message", userInstance.errors)
        }
        // If a non-admin user edits their profile they will not have access to
        // the roles or location roles, so we need to prevent the updateRoles
        // method from being called.
        if (params.locationRolePairs) {
            updateRoles(userInstance, params.locationRolePairs)
        }

        // We need to cache current role and check edit privilege here because the roles association
        // may change once we merge user and request parameters
        if (params.updateRoles) {
            User currentUser = User.load(currentUserId)
            Boolean canEditRoles = canEditUserRoles(currentUser, userInstance)

            // Check to make sure the roles are dirty
            HashSet<Role> currentRoles = new HashSet(userInstance?.roles)
            boolean isRolesDirty = !ListUtils.isEqualList(updatedRoles, currentRoles)
            log.info "User update: ${updatedRoles} vs ${currentRoles}: isDirty=${isRolesDirty}, canEditRoles=${canEditRoles}"
            if (isRolesDirty && !canEditRoles) {
                Object[] args = [currentUser.username, userInstance.username]
                userInstance.errors.rejectValue("roles", "user.errors.cannotEditUserRoles.message", args, "User cannot edit user roles")
                throw new ValidationException("user.errors.cannotEditUserRoles.message", userInstance.errors)
            }
        }
        log.info "User has errors: ${userInstance.hasErrors()} ${userInstance.errors}"
        return userInstance.save(failOnError: true)
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

    private void updateRoles(user, locationRolePairs) {
        def newAndUpdatedRoles = locationRolePairs.keySet().collect { locationId ->
            if (locationRolePairs[locationId]) {
                def location = Location.get(locationId)
                def role = Role.get(locationRolePairs[locationId])
                def existingRole = user.locationRoles.find { it.location == location }
                if (existingRole) {
                    existingRole.role = role
                } else {
                    def newLocationRole = new LocationRole(user: user, location: location, role: role)
                    user.addToLocationRoles(newLocationRole)
                }
            }
        }
        def rolesToRemove = user.locationRoles.findAll { oldRole ->
            !locationRolePairs[oldRole.location.id]
        }
        rolesToRemove.each {
            user.removeFromLocationRoles(it)
        }
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

    Boolean hasRoleApprover(User u) {
        if (u) {
            def user = User.get(u.id)
            def roleTypes = [RoleType.ROLE_APPROVER]
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

    Boolean canEditUserRoles(User currentUser, User otherUser) {
        def location = authService.currentLocation
        return isSuperuser(currentUser) || (currentUser.getHighestRole(location) >= otherUser.getHighestRole(location))
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
            log.info "Is role ${role.roleType} in ${acceptedRoleTypes} = ${acceptedRoleType}"
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
        return User.createCriteria().list() {
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
            order("lastName", "desc")
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
        return user.getEffectiveRoles(currentLocation)
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
        def fullConfig = grailsApplication.config.openboxes.dashboardConfig
        def mainDashboardId = grailsApplication.config.openboxes.dashboardConfig.mainDashboardId
        def resultConfig = [
                // Clone to not overwrite the main config in the line 422
                dashboard: fullConfig.dashboards[id ?: mainDashboardId].clone(),
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

    def saveLocationRole(Location location, LocationRole locationRole, List<Role> roles, User user) {
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
}
