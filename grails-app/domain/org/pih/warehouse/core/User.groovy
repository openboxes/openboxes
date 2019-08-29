/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.core

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import util.StringUtil


class User extends Person {

    String id
    Boolean active = false                // default = false?
    String username            // email or username
    String password            // encrypted password
    String passwordConfirm        // password confirm used on signup and password reset
    Locale locale                // the user's locale
    String timezone

    Date lastLoginDate            // keep track of the user's last login
    Location warehouse        // keep track of the user's last warehouse
    //Boolean useSavedLocation		// indicates whether we should use this warehouse when user logs in
    User manager                // the user's designated manager
    Boolean rememberLastLocation
    // indicates whether user would like for the system to remember where they last logged into
    byte[] photo                // profile photo

    List locationRoles
    static hasMany = [roles: Role, locationRoles: LocationRole]
    static mapping = {
        table "`user`"
        roles joinTable: [name: 'user_role', column: 'role_id', key: 'user_id'], cascade: "save-update"
        locationRoles cascade: "all-delete-orphan"
        id generator: 'uuid'
    }
    static transients = ["passwordConfirm"]
    static constraints = {
        active(nullable: true)
        username(blank: false, unique: true, maxSize: 255)
        password(blank: false, minSize: 6, maxSize: 255, validator: { password, obj ->
            def passwordConfirm = obj.properties['passwordConfirm']
            if (passwordConfirm == null) return true // skip matching password validation (only important when setting/resetting pass)
            passwordConfirm == password ? true : ['invalid.matchingpasswords']
        })
        passwordConfirm(blank: false)
        locale(nullable: true)
        timezone(nullable: true)
        lastLoginDate(nullable: true)
        //useSavedLocation(nullable:true)
        warehouse(nullable: true)
        manager(nullable: true)
        rememberLastLocation(nullable: true)
        photo(nullable: true, maxSize: 10485760) // 10 MBs
    }


    /**
     * @return the highest role for this user
     */
    def getHighestRole(Location currentLocation) {
        def roles = getEffectiveRoles(currentLocation)
        return (roles) ? roles.sort().iterator().next() : null
    }


    def getRolesByCurrentLocation(Location currentLocation) {
        if (!currentLocation) return []
        return locationRoles?.findAll { it.location == currentLocation }?.collect { it.role } ?: []
    }

    def getEffectiveRoles(Location currentLocation) {
        def defaultRoles = roles?.collect { it } ?: []
        def rolesByLocation = getRolesByCurrentLocation(currentLocation)
        defaultRoles.addAll(rolesByLocation)
        return defaultRoles
    }

    boolean hasPrimaryRole(Location currentLocation) {
        def roles = getEffectiveRoles(currentLocation)
        return roles.roleType.find { RoleType.listPrimaryRoleTypes().contains(it) }
    }

    /**
     * @return all location role pairs for this user
     */
    def locationRolePairs() {
        def pairs = [:]
        locationRoles.each {
            pairs[it.location.id] = it.role.id
        }
        return pairs
    }

    /**
     * @return all location role pairs for display
     */
    def locationRolesDescription() {
        def roleArray = locationRoles.collect { "${it.location?.name}: ${it.role?.roleType?.name}" }
        roleArray?.join(" | ")
    }


    Map toJson() {
        boolean anonymize = CH.config.openboxes.anonymize.enabled

        return [
                "id"       : id,
                "name"     : name,
                "firstName": firstName,
                "lastName" : (anonymize) ? lastInitial : lastName,
                "email"    : anonymize ? StringUtil.mask(email) : email,
                "username" : anonymize ? StringUtil.mask(username) : username
        ]
    }


}
