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

import grails.gorm.transactions.Transactional

/**
 * Single place to answer "may this user do this at this location?".
 *
 * Every check takes the location explicitly. Nothing here reads the session or the
 * AuthService thread locals, so the same checks work for UI requests, API requests that
 * name a facility in the URL, and background jobs.
 *
 * Kept deliberately framework-agnostic so it can later back a Spring Security
 * PermissionEvaluator / @PreAuthorize expression without changing callers.
 */
@Transactional(readOnly = true)
class AuthorizationService {

    /**
     * @param user the user making the request (may be detached, e.g. from the HTTP session)
     * @param location the location the request acts on. When null, only the user's global roles count.
     * @param minimumRole the lowest primary role (Browser, Assistant, Manager, ...) that is allowed.
     *        Higher primary roles also pass. Null means no primary role is required.
     * @param supplementalRoles non-hierarchical roles (Invoice, Purchase Approver, ...). The user must hold
     *        at least one of them exactly. Empty means none are required.
     */
    boolean hasRole(User user,
                    Location location,
                    RoleType minimumRole,
                    Collection<RoleType> supplementalRoles = []) {
        if (!user?.id) {
            return false
        }

        // Re-read so lazy role collections are available even when the user came from the session
        User currentUser = User.get(user.id)
        if (!currentUser?.active) {
            return false
        }

        Set<RoleType> roleTypes = currentUser.getEffectiveRoles(location)*.roleType as Set<RoleType>

        return hasMinimumRole(roleTypes, minimumRole) && hasAnySupplementalRole(roleTypes, supplementalRoles)
    }

    /**
     * Primary roles are hierarchical: a lower sortOrder is a more powerful role.
     */
    static boolean hasMinimumRole(Set<RoleType> roleTypes, RoleType minimumRole) {
        if (!minimumRole) {
            return true
        }
        List<RoleType> primaryRoleTypes = RoleType.listPrimaryRoleTypes()
        return roleTypes.any { RoleType roleType ->
            roleType in primaryRoleTypes && roleType.sortOrder <= minimumRole.sortOrder
        }
    }

    /**
     * Supplemental roles are NOT hierarchical. Unlike RoleType.expand(), which compares sortOrder and
     * would let any primary role satisfy e.g. ROLE_PURCHASE_APPROVER (sortOrder 50), this requires
     * an exact match.
     */
    static boolean hasAnySupplementalRole(Set<RoleType> roleTypes, Collection<RoleType> supplementalRoles) {
        if (!supplementalRoles) {
            return true
        }
        return supplementalRoles.any { RoleType roleType -> roleType in roleTypes }
    }
}
