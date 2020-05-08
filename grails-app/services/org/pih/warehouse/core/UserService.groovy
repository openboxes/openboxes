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

import com.unboundid.ldap.sdk.BindRequest
import com.unboundid.ldap.sdk.BindResult
import com.unboundid.ldap.sdk.DN
import com.unboundid.ldap.sdk.DNEntrySource
import com.unboundid.ldap.sdk.DereferencePolicy
import com.unboundid.ldap.sdk.Entry
import com.unboundid.ldap.sdk.Filter
import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.ResultCode
import com.unboundid.ldap.sdk.SearchRequest
import com.unboundid.ldap.sdk.SearchResult
import com.unboundid.ldap.sdk.SearchResultEntry
import com.unboundid.ldap.sdk.SearchScope
import com.unboundid.ldap.sdk.SimpleBindRequest
import grails.validation.ValidationException
import groovy.sql.Sql
import org.apache.commons.collections.ListUtils
import org.pih.warehouse.auth.AuthService
import util.StringUtil

import javax.net.SocketFactory
import java.security.GeneralSecurityException

class UserService {

    def dataSource
    def grailsApplication
    boolean transactional = true

    User getUser(String id) {
        return User.get(id)
    }


    def updateUser(String userId, String currentUserId, Map params) {

        def userInstance = User.get(userId)

        // Password in the db is different from the one specified
        // so the user must have changed the password.  We need
        // to compare the password with confirm password before
        // setting the new password in the database
        if (params.changePassword && userInstance.password != params.password) {
            userInstance.properties = params
            userInstance.password = params?.password?.encodeAsPassword()
            userInstance.passwordConfirm = params?.passwordConfirm?.encodeAsPassword()
        } else {
            userInstance.properties = params
            // Needed to bypass the password == passwordConfirm validation
            userInstance.passwordConfirm = userInstance.password
        }
        // If a non-admin user edits their profile they will not have access to
        // the roles or location roles, so we need to prevent the updateRoles
        // method from being called.
        if (params.locationRolePairs) {
            updateRoles(userInstance, params.locationRolePairs)
        }

        // We need to cache current role and check edit privilege here because the roles association
        // may change once we merge user and request parameters
        def currentUser = User.load(currentUserId)
        def canEditRoles = canEditUserRoles(currentUser, userInstance)

        // Check to make sure the roles are dirty
        def currentRoles = new HashSet(userInstance?.roles)
        def updatedRoles = Role.findAllByIdInList(params.list("roles"))
        def isRolesDirty = !ListUtils.isEqualList(updatedRoles, currentRoles) && params.updateRoles
        log.info "User update: ${updatedRoles} vs ${currentRoles}: isDirty=${isRolesDirty}, canEditRoles=${canEditRoles}"
        if (isRolesDirty && !canEditRoles) {
            Object[] args = [currentUser.username, userInstance.username]
            userInstance.errors.rejectValue("roles", "user.errors.cannotEditUserRoles.message", args, "User cannot edit user roles")
            throw new ValidationException("user.errors.cannotEditUserRoles.message", userInstance.errors)
        }

        log.info "User has errors: ${userInstance.hasErrors()} ${userInstance.errors}"
        return userInstance.save(failOnError: true)
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


    Boolean canEditUserRoles(User currentUser, User otherUser) {
        def location = AuthService.currentLocation.get()
        return isSuperuser(currentUser) || (currentUser.getHighestRole(location) >= otherUser.getHighestRole(location))
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
        User user = AuthService.currentUser.get()
        return hasRoleFinance(user)
    }

    void assertCurrentUserHasRoleFinance() {
        User user = AuthService.currentUser.get()
        if (!hasRoleFinance(user)) {
            throw new IllegalStateException("User ${user.username} must have ROLE_FINANCE role")
        }
    }

    def findPersons(String[] terms) {
        return findPersons(terms, [:])
    }

    def findPersons(String[] terms, params) {
        def results = Person.createCriteria().list(params) {

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
                sql.execute('insert into user (id, username, password) values (?, ?, ?)', [person?.id, person?.email, encodedPassword])
            }
        }
    }

    void convertUserToPerson(String personId) {
        def person = Person.get(personId)
        if (person) {
            Sql sql = new Sql(dataSource)
            sql.execute('delete from user where id = ?', [personId])
        }
    }

    def findUsersByRoleType(RoleType roleType) {
        def users = []
        def role = Role.findByRoleType(roleType)
        if (role) {
            def criteria = User.createCriteria()
            users = criteria.list {
                eq("active", true)
                roles {
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
                    roles {
                        'in'("id", roleIds)
                    }
                    if (location) {
                        locationRoles {
                            'in'("role.id", roleIds)
                            eq("location.id", location.id)
                        }
                    }
                }
            }
        }
        return users
    }

    private def getEffectiveRoles(User user) {
        def currentLocation = AuthService.currentLocation?.get()
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
}
