package org.pih.warehouse.core

import grails.test.*
import grails.test.mixin.integration.Integration
import org.junit.Test

// import org.pih.warehouse.auth.AuthService

import testutils.DbHelper;
import static org.junit.Assert.*

@Integration
class UserServiceIntegrationTests {

	def userService 

	@Test
	void test_getAllAdminUsers() { 		
		// Create a new inactive user who has role ROLE_ADMIN
		User adminUser = DbHelper.createAdmin("Firstson", "Miranda III", "fmiranda@pih.org", "fmiranda", "password", false)
		assertNotNull adminUser
		assertFalse adminUser.active
		
		def roleAdmin = Role.findByRoleType(RoleType.ROLE_ADMIN)
		assertNotNull "Should exist", roleAdmin 
		
		def allUsers = User.list()
		def allAdmins = allUsers.findAll { it.roles.contains(roleAdmin) } 
		assertEquals "Should be 2 users with ROLE_ADMIN", 3, allAdmins.size()
		
		// Should return active admins
		def activeAdmins = userService.getAllAdminUsers()
		assertFalse "Should not contain inactive users with ROLE_ADMIN", activeAdmins.contains(adminUser)
		assertEquals 2, activeAdmins.size()
		activeAdmins.each { admin -> 			
			assertTrue "Should have ROLE_ADMIN", admin.roles.contains(roleAdmin)
			assertTrue "Should be active", admin.active
		}
		
		
		
		
	}
}
