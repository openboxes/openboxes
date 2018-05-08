package org.pih.warehouse.user

import org.junit.Ignore
import org.pih.warehouse.core.*
import grails.test.ControllerUnitTestCase
// import org.springframework.mock.web.MockHttpServletResponse
import testutils.MockBindDataMixin

@Mixin(MockBindDataMixin)
class UserControllerTests extends ControllerUnitTestCase{

    protected void setUp(){
        super.setUp()
        mockBindData()
    }

    @Ignore
    void testUpdateRoles(){
      User user = new User(id:"u1234", password: "password", username:"peter", email: "peter@openboxes.com")
      mockDomain(User, [user])
      Role roleBrowser = new Role(id:"r1", roleType: RoleType.ROLE_BROWSER)
      Role roleManager = new Role(id:"r2", roleType: RoleType.ROLE_MANAGER)
      mockDomain(Role, [roleBrowser, roleManager])
      Location boston = new Location(id:"l1", name:"boston")
      Location miami = new Location(id:"l2", name:"miami")
      Location chicago = new Location(id:"l3", name:"chicago")
      mockDomain(Location, [boston, miami, chicago])
      LocationRole oldRole1 = new LocationRole(id:"lr1", user: user, role: roleBrowser, location: boston)
      LocationRole oldRole2 = new LocationRole(id:"lr2", user: user, role: roleManager, location: miami)
      mockDomain(LocationRole, [oldRole1, oldRole2])
      user.addToLocationRoles(oldRole1)
      user.addToLocationRoles(oldRole2)

      def userServiceMock = mockFor(UserService)
      userServiceMock.demand.updateUser { userId, currentUserId, params ->
        return user
      }
      controller.userService = userServiceMock.createMock()

      assert user.locationRolePairs() == [l1:roleBrowser.id, l2: roleManager.id]

      controller.params.id = user.id
      controller.params.password = user.password
      controller.params.username = user.username
      controller.params.firstName = "peter"
      controller.params.lastName = "Boo"
      controller.params.locale = "en"
      controller.params.locationRolePairs = [l1: roleManager.id, l2: '', l3:roleBrowser.id]
      controller.session.user = user
      def stubMessager = new Expando()
      stubMessager.message = { args -> return "do not exist" }
      controller.metaClass.warehouse = stubMessager;


      controller.update()
      user.errors.each{println(it)}
      assert redirectArgs.action == "edit"

      assert user.locationRolePairs() == [l3: roleBrowser.id, l1:roleManager.id]
      assert user.locationRoles.any{ it.id == oldRole1.id}
    }
}

