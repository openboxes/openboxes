package org.pih.warehouse.api.regression.support

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User

/**
 * Creates (idempotently, each in its own committed transaction so the server thread sees it)
 * users with exactly the given global and location-scoped roles, for specs that log in as them.
 *
 * All fixture users share {@link #PASSWORD} and have usernames starting with "test-".
 */
class RegressionRoleFixtures {

    public static final String PASSWORD = 'test-password'

    /**
     * A user with the given global roles plus location-scoped roles.
     * @param locationRoles location id -> role types held only at that location
     */
    static User ensureUser(String username, List<RoleType> globalRoles, Map<String, List<RoleType>> locationRoles) {
        User.withNewTransaction {
            User user = User.findByUsername(username)
            if (!user) {
                user = new User(
                        username: username,
                        password: PASSWORD,
                        firstName: 'Test',
                        lastName: username,
                        email: "${username}@example.com",
                        active: true,
                        rememberLastLocation: false,
                )
            }
            user.active = true
            user.password = PASSWORD
            user.roles?.clear()
            globalRoles.each { RoleType rt -> user.addToRoles(role(rt)) }
            user.locationRoles?.clear()
            user.save(failOnError: true, flush: true)
            locationRoles.each { String locationId, List<RoleType> rts ->
                rts.each { RoleType rt ->
                    user.addToLocationRoles(new LocationRole(user: user, location: Location.get(locationId), role: role(rt)))
                }
            }
            user.save(failOnError: true, flush: true)
            return user
        }
    }

    static String userId(String username) {
        return User.withNewTransaction { User.findByUsername(username)?.id }
    }

    private static Role role(RoleType roleType) {
        Role r = Role.findByRoleType(roleType)
        if (!r) {
            r = new Role(roleType: roleType, name: roleType.name).save(failOnError: true, flush: true)
        }
        return r
    }
}
