package org.pih.warehouse.core

import grails.test.*


class UserServiceTests extends GrailsUnitTestCase{
  def user1
  def user2
  def user3
  def user4
  def role1
  def role2
  def role3
  def service

  void setUp() {
    super.setUp()
    user1 = new User(id:"1231")
    user2 = new User(id:"1232")
    user3 = new User(id:"1233")
    user4 = new User(id:"1234")
    role1 = new Role(roleType: RoleType.ROLE_BROWSER)
    role2 = new Role(roleType: RoleType.ROLE_MANAGER)
    role3 = new Role(roleType: RoleType.ROLE_ADMIN)
    mockDomain(User, [user1, user2, user3, user4])
    mockDomain(Role, [role1, role2, role3])
    user2.addToRoles(role1)
    user3.addToRoles(role2)
    user4.addToRoles(role3)
    service = new UserService()
  }

  void test_userCanBrowse(){
      assert service.canUserBrowse(user1) == false  
      assert service.canUserBrowse(user2) == true  
      assert service.canUserBrowse(user3) == true  
      assert service.canUserBrowse(user4) == true  
  }
  void test_userIsManager(){
      assert service.isUserManager(user1) == false  
      assert service.isUserManager(user2) == false  
      assert service.isUserManager(user3) == true  
      assert service.isUserManager(user4) == true  
  }
  void test_userIsAdmin(){
      assert service.isUserAdmin(user1) == false  
      assert service.isUserAdmin(user2) == false  
      assert service.isUserAdmin(user3) == false  
      assert service.isUserAdmin(user4) == true  
  }
}
