package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest

// import grails.test.GrailsUnitTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.User
import spock.lang.Specification
import static org.junit.Assert.*;

//@Ignore
class UserTests extends Specification implements DomainUnitTest<User> {


    @Before
    void setup() {
//        super.setUp()
        mockConfig("openboxes.anonymize.enabled = false")
    }

    @After
    void tearDown() {
        super.tearDown()
    }


    @Test
    void testLocationRolePairs() {
        when:
        def user = new User(id: "u1")
        mockDomain(User, [user])
        def role1 = new Role(id: "r1", roleType: RoleType.ROLE_MANAGER)
        def role2 = new Role(id: "r2", roleType: RoleType.ROLE_BROWSER)
        mockDomain(Role, [role1, role2])
        def location1 = new Location(id: "l1", name: "boston")
        def location2 = new Location(id: "l2", name: "miami")
        mockDomain(Location, [location1, location2])
        def locationRole1 = new LocationRole(role: role1, location: location1)
        def locationRole2 = new LocationRole(role: role2, location: location2)
        mockDomain(LocationRole, [locationRole1, locationRole2])

        then:
        assert user.locationRolePairs() == [:]

        when:
        user.addToLocationRoles(locationRole1)
        user.addToLocationRoles(locationRole2)
        then:
        assert user.locationRolePairs() == [l1: role1.id, l2: role2.id]
        assert user.locationRolesDescription() == "boston: Manager | miami: Browser"
    }


    @Test
    void getHighestRole() {

        when:
        mockDomain(Role)
        mockDomain(User)
        mockDomain(Location)

        def user = new User(username: "user1", firstName: "Test", lastName: "User", password: "password")
        def adminRole = new Role(roleType: RoleType.ROLE_ADMIN)
        def assistantRole = new Role(roleType: RoleType.ROLE_ASSISTANT)
        def managerRole = new Role(roleType: RoleType.ROLE_MANAGER)
        def browserRole = new Role(roleType: RoleType.ROLE_ASSISTANT)

        then:
        assertNull user.getHighestRole()

        when:
        user.addToRoles(adminRole)
        user.addToRoles(assistantRole)
        user.addToRoles(managerRole)
        user.addToRoles(browserRole)
        then:
        assertEquals adminRole, user.getHighestRole()

        when:
        user.removeFromRoles(adminRole)
        then:
        assertEquals managerRole, user.getHighestRole()


        when:
        user.removeFromRoles(managerRole)
        then:
        assertEquals assistantRole, user.getHighestRole()

        when:
        user.removeFromRoles(assistantRole)
        then:
        assertEquals browserRole, user.getHighestRole()

        when:
        user.removeFromRoles(browserRole)
        then:
        assertNull user.getHighestRole()

    }


    @Test
    void validate_shouldRequirePassword() {
        when:
        mockDomain(User)

        User user1 = new User(username: null, password: null)
        user1.validate()
        println user1.errors

        then:
        assertNotNull user1.errors["username"]
        assertNotNull user1.errors["password"]
        assertNotNull user1.errors["firstName"]
        assertNotNull user1.errors["lastName"]

        when:
        User user2 = new User(username: "", password: "")
        user2.validate()
        println user2.errors

        then:
        assertNotNull user1.errors["username"]
        assertNotNull user1.errors["password"]
        assertNotNull user1.errors["firstName"]
        assertNotNull user1.errors["lastName"]

    }
}
