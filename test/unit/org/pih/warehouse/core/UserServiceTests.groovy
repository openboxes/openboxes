package org.pih.warehouse.core

import grails.test.GrailsUnitTestCase
import org.pih.warehouse.auth.AuthService

class UserServiceTests extends GrailsUnitTestCase {
    def user1
    def user2
    def user3
    def user4
    def user5
    def user6
    def user7
    def role1
    def role2
    def role3
    def role4
    def role5
    def role6
    def locationRole1
    def locationRole2
    def locationRole3
    def locationRole4
    def boston
    def miami
    def service

    void setUp() {
        super.setUp()

        user1 = new User(id: "u1")
        user2 = new User(id: "u2")
        user3 = new User(id: "u3")
        user4 = new User(id: "u4")
        user5 = new User(id: "u5")
        user6 = new User(id: "u6")
        user7 = new User(id: "u7")

        role1 = new Role(roleType: RoleType.ROLE_BROWSER, id: "r1")
        role2 = new Role(roleType: RoleType.ROLE_MANAGER, id: "r2")
        role3 = new Role(roleType: RoleType.ROLE_ADMIN, id: "r3")
        role4 = new Role(roleType: RoleType.ROLE_ASSISTANT, id: "r4")
        role5 = new Role(roleType: RoleType.ROLE_SUPERUSER, id: "r5")
        role6 = new Role(roleType: RoleType.ROLE_FINANCE, id: "r6")

        mockDomain(User, [user1, user2, user3, user4, user5, user6, user7])

        mockDomain(Role, [role1, role2, role3, role4, role5])
        user2.addToRoles(role1)
        user2.addToRoles(role5)
        user3.addToRoles(role2)
        user4.addToRoles(role3)
        user5.addToRoles(role1)
        user7.addToRoles(role4)
        user7.addToRoles(role6)
        boston = new Location(id: "l1", name: "boston")
        miami = new Location(id: "l2", name: "miami")
        mockDomain(Location, [boston, miami])

        locationRole1 = new LocationRole(id: "lr1", user: user5, role: role2, location: boston)
        locationRole2 = new LocationRole(id: "lr2", user: user5, role: role1, location: miami)
        locationRole3 = new LocationRole(id: "lr3", user: user6, role: role1, location: boston)
        locationRole4 = new LocationRole(id: "lr4", user: user1, role: role1, location: miami)
        mockDomain(LocationRole, [locationRole1, locationRole2, locationRole3, locationRole4])
        user5.addToLocationRoles(locationRole1)
        user5.addToLocationRoles(locationRole2)
        user6.addToLocationRoles(locationRole3)
        user1.addToLocationRoles(locationRole4)

        AuthService.currentLocation = new ThreadLocal<Location>()
        AuthService.currentLocation.set(boston)

        service = new UserService()
    }

    void test_userIsSuperuser() {
        assert service.isSuperuser(user1) == false
        assert service.isSuperuser(user2) == true
        assert service.isSuperuser(user3) == false
        assert service.isSuperuser(user4) == false
        assert service.isSuperuser(user5) == false
        assert service.isSuperuser(user6) == false
        assert service.isSuperuser(user7) == false
    }

    void test_userIsAdmin() {
        assert service.isUserAdmin(user1) == false
        assert service.isUserAdmin(user2) == true
        assert service.isUserAdmin(user3) == false
        assert service.isUserAdmin(user4) == true
        assert service.isUserAdmin(user5) == false
        assert service.isUserAdmin(user6) == false
        assert service.isUserAdmin(user7) == false
    }

    void test_userIsManager() {
        assert service.isUserManager(user1) == false
        assert service.isUserManager(user2) == true
        assert service.isUserManager(user3) == true
        assert service.isUserManager(user4) == true
        assert service.isUserManager(user5) == true
        assert service.isUserManager(user6) == false
        assert service.isUserManager(user7) == true
    }

    void test_userCanBrowse() {
        assert service.canUserBrowse(user1) == false
        assert service.canUserBrowse(user2) == true
        assert service.canUserBrowse(user3) == true
        assert service.canUserBrowse(user4) == true
        assert service.canUserBrowse(user5) == true
        assert service.canUserBrowse(user6) == true
        assert service.canUserBrowse(user7) == true
    }

    void test_hasRoleFinance() {
        assert service.hasRoleFinance(user1) == false
        assert service.hasRoleFinance(user2) == false
        assert service.hasRoleFinance(user3) == false
        assert service.hasRoleFinance(user4) == false
        assert service.hasRoleFinance(user5) == false
        assert service.hasRoleFinance(user6) == false
        assert service.hasRoleFinance(user7) == true
    }

    void test_canEditUserRoles() {
        assert service.canEditUserRoles(user2, user3) == true
    }

    void test_getEffectiveRoles() {
        assert service.getEffectiveRoles(user3) == [role2]
        assert service.getEffectiveRoles(user4) == [role3]
        assert service.getEffectiveRoles(user5) == [role1, role2]
    }
}
