package org.pih.warehouse.core
import grails.test.GrailsUnitTestCase

class UserTests extends GrailsUnitTestCase{
  void testLocationRolePairs(){
    def user = new User(id:"u1")
    mockDomain(User, [user])
    def role1 = new Role(id:"r1", roleType:RoleType.ROLE_MANAGER)
    def role2 = new Role(id:"r2", roleType:RoleType.ROLE_BROWSER)
    mockDomain(Role, [role1, role2])
    def location1 = new Location(id:"l1", name:"boston")
    def location2 = new Location(id:"l2", name:"miami")
    mockDomain(Location, [location1, location2])
    def locationRole1 = new LocationRole(role: role1, location: location1)
    def locationRole2 = new LocationRole(role: role2, location: location2)
    mockDomain(LocationRole, [locationRole1, locationRole2])

    assert user.locationRolePairs() == [:]

    user.addToLocationRoles(locationRole1)
    user.addToLocationRoles(locationRole2)
    assert user.locationRolePairs() == [l1: role1.id, l2: role2.id]
    assert user.locationRolesDescription() == "boston: Manager | miami: Browser"
  }
}
