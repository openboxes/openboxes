package unit.org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User

class UserSpec extends Specification implements DomainUnitTest<User> {

    void 'locationRolePairs should return the expected location role pairs'() {
        given: ' some locations and roles'
        Location location1 = new Location()
        location1.id = 0
        Location location2 = new Location()
        location2.id = 1
        Role role1 = new Role()
        role1.id = 2
        Role role2 = new Role()
        role2.id = 3

        and: 'a user with those roles'
        User user = new User(locationRoles: [
                new LocationRole(location: location1, role: role1),
                new LocationRole(location: location2, role: role2),
        ])

        expect:
        assert user.locationRolePairs() == [
                '0': '2',
                '1': '3',
        ]
    }

    void 'locationRolesDescription should return the expected location role pairs'() {
        given: ' some locations and roles'
        Location location1 = new Location(name: 'boston')
        Location location2 = new Location(name: 'miami')
        Role role1 = new Role(roleType: RoleType.ROLE_MANAGER)
        Role role2 = new Role(roleType: RoleType.ROLE_BROWSER)

        and: 'a user with those roles'
        User user = new User(locationRoles: [
                new LocationRole(location: location1, role: role1),
                new LocationRole(location: location2, role: role2),
        ])

        expect:
        assert user.locationRolesDescription() == "boston: Manager | miami: Browser"
    }

    void 'getHighestRole should return nothing when user has no roles'() {
        given:
        User user = new User(roles: [])
        Location location = new Location()

        expect:
        assert user.getHighestRole(location) == null
    }

    void getHighestRole() {
        given: 'some roles'
        Role lowerPriorityRole = new Role(roleType: RoleType.ROLE_BROWSER)      // 4
        Role higherPriorityRole = new Role(roleType: RoleType.ROLE_MANAGER)     // 2
        Role evenHigherPriorityRole = new Role(roleType: RoleType.ROLE_ADMIN)   // 1
        Role highestPriorityRole = new Role(roleType: RoleType.ROLE_SUPERUSER)  // 0

        and: 'some location roles'
        Location location = new Location(name: "location")
        LocationRole locationRole = new LocationRole(location: location, role: evenHigherPriorityRole)
        Location otherLocation = new Location(name: "otherLocation")
        LocationRole otherLocationRole = new LocationRole(location: otherLocation, role: highestPriorityRole)

        and: 'a user'
        User user = new User()

        when: 'we add some regular roles to the user'
        user.addToRoles(lowerPriorityRole)
        user.addToRoles(higherPriorityRole)

        then: 'the higher priority role should be returned'
        assert user.getHighestRole(null) == higherPriorityRole

        when: 'we add a location role to the user with higher priority than the previous roles'
        user.addToLocationRoles(locationRole)

        then: 'the newly added location role with highest priority should be be returned'
        assert user.getHighestRole(location) == evenHigherPriorityRole

        when: 'we add another location role to the user with the highest priority but in a different location'
        user.addToLocationRoles(otherLocationRole)

        then: 'only roles for the requested location should be factored in'
        assert user.getHighestRole(location) == evenHigherPriorityRole
    }

    void 'validate should return true for a valid user'() {
        given:
        User user = new User(
                username: "username",
                password: "hunter2",
                passwordConfirm: "hunter2",
                firstName: "fname",
                lastName: "lname",
        )

        expect:
        assert user.validate()
    }
}
