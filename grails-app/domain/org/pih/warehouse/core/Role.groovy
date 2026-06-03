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


class Role implements Serializable, Comparable<Role> {

    String id
    RoleType roleType
    String name
    String description

    static constraints = {
        name(nullable: false)
        roleType(nullable: false)
        description(nullable: true, maxSize: 255)
    }

    static mapping = {
        id generator: 'uuid'
    }

    String toString() { return "${roleType?.name}" }

    static Role superuser() {
        Role.findByRoleType(RoleType.ROLE_SUPERUSER)
    }

    static Role admin() {
        Role.findByRoleType(RoleType.ROLE_ADMIN)
    }

    static Role manager() {
        Role.findByRoleType(RoleType.ROLE_MANAGER)
    }

    static Role assistant() {
        Role.findByRoleType(RoleType.ROLE_ASSISTANT)
    }

    static Role browser() {
        Role.findByRoleType(RoleType.ROLE_BROWSER)
    }

    @Override
    int compareTo(Role role) {
        // Sorts a null role last i.e. this role is always greater than a null role
        if (!role) {
            return 1
        }
        // Order by sortOrder, then by id as a stable tiebreaker. The id is
        // a tiebreaker that keeps compareTo consistent with equality so that
        // distinct roles sharing a sortOrder never compare as equal. Without
        // this Comparable-based operations like minus, unique, TreeSet silently
        // collapse roles with the same sortOrder. See OBPIH-7904.
        return (this.roleType?.sortOrder ?: 0) <=> (role.roleType?.sortOrder ?: 0) ?: (this.id <=> role.id)
    }

}



