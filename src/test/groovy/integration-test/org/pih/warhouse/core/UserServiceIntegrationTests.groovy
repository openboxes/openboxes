package org.pih.warehouse.core

import grails.testing.services.ServiceUnitTest
import org.junit.Ignore
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.User
import spock.lang.Specification
import testutils.DbHelper
import static org.junit.Assert.*;

//@Ignore
class UserServiceIntegrationTests extends Specification implements  ServiceUnitTest<UserService>{

//	def userService

	void setup() {
//		super.setUp()
	}

	void test_getAllAdminUsers() {
		// Create a new inactive user who has role ROLE_ADMIN
		when:
		User adminUser = DbHelper.findOrCreateAdminUser('Firstson', 'Miranda III', 'fmiranda@pih.org', 'fmiranda', 'password', false)
		then:
		assertNotNull adminUser
		assertFalse adminUser.active

		when:
		def roleAdmin = Role.findByRoleType(RoleType.ROLE_ADMIN)
		then:
		assertNotNull "Should exist", roleAdmin

		when:
		def allUsers = User.list()
		def allAdmins = allUsers.findAll { it.roles.contains(roleAdmin) }
		then:
		assertEquals "Should be 2 users with ROLE_ADMIN", 2, allAdmins.size()
		
		// Should return active admins
		when:
		def activeAdmins = service.getAllAdminUsers()
		then:
		assertFalse "Should not contain inactive users with ROLE_ADMIN", activeAdmins.contains(adminUser)
		assertEquals 1, activeAdmins.size()
		activeAdmins.each { admin -> 			
			assertTrue "Should have ROLE_ADMIN", admin.roles.contains(roleAdmin)
			assertTrue "Should be active", admin.active
		}
	}
}
