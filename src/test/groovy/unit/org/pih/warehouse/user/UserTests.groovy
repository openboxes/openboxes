/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.user

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.pih.warehouse.core.User;
// import java.util.Locale;

import grails.test.*

class UserTests extends GrailsUnitTestCase {


	@Before
	void setUp() {
		super.setUp()

		// Set up default user, so we can easily test single properties.
		def user1 = new User(username: 'tester', password: 'password', firstName: 'Tester', lastName: 'Testerson', locale: new Locale("en", "EN") )
		// Make sure we can invoke validate() on our User domain object.
		mockForConstraintsTests(User, [user1])
		mockConfig("openboxes.anonymize.enabled = false")

	}

	@After
	void tearDown() {
		super.tearDown()
	}

    @Test
	void testNullable_shouldPassWhenNullableErrorsDetected() {
		def user = new User()
		assertFalse user.validate()
		assertEquals "nullable", user.errors["username"]
		assertEquals "nullable", user.errors["password"]	
		assertEquals "nullable", user.errors["firstName"]
		assertEquals "nullable", user.errors["lastName"]
		//assertEquals "nullable", user.errors["locale"]
	}

    @Test
	void testUnique_shouldFailWhenUsernameIsNotUnique() {
		// Test user to test uniqueness of nickName property.
		//def user1 = new User(username: 'tester', password: 'password', firstName: 'Tester', lastName: 'Testerson', locale: new Locale("en", "EN"))
		//mockForConstraintsTests(User, [user1])

		def user2 = new User(username: 'tester', password: 'password', firstName: 'Tester', lastName: 'Testerson Jr', locale: new Locale("en", "EN"))
		assertFalse user2.validate()
		assertEquals 'Username is not unique.', 'unique', user2.errors['username']

	}

	@Test
	void testUnique_shouldPassWhenUsernameIsUnique() { 
		//def user1 = new User(username: 'tester', password: 'password', firstName: 'Tester', lastName: 'Testerson', locale: new Locale("en", "EN"))
		//mockForConstraintsTests(User, [user1])

		def user3 = new User(username: 'otherTester', password: 'password', email: "valid@email.com", firstName: 'Tester', lastName: 'Testerson III', locale: new Locale("en", "EN"))
		user3.validate();

		println user3.errors
		assertTrue user3.validate()
	}

    @Test
	void testList_shouldReturnOneUser() {
		mockDomain(User, [new User(username: "username", password: "password", firstName: "First", lastName: "Last", locale: new Locale("en", "EN"))])
		def users = User.list()
		println users
		assertEquals 1, users.size()
		
	}


	/*
	 void testUsernameUnique() {
	 def user1 = new User(username: "user1", password: "password", firstName: "User", lastName: "One")
	 mockForConstraintsTests(User, [user1])
	 def testUser = new User()
	 testUser = new User(username: "user1", password: "password")	
	 assertFalse testUser.validate()
	 assertEquals "unique", testUser.errors["username"]
	 assertEquals "validator", testUser.errors["password"]
	 testUser = new User(username: "user3", password: "passwd")
	 assertTrue testUser.validate()		
	 }
	 void testNotEqual() {
	 def user = new User()
	 user.name = 'testEquals'
	 assertFalse user.validate()
	 assertEquals 'Name is equal to testEquals.', 'notEqual', user.errors['name']
	 user.name = 'test'
	 assertTrue user.validate()
	 }
	 void testBlank() {
	 mockForConstraintsTests(User)
	 def user = new User()
	 assertFalse user.validate()
	 println "=" * 20
	 println "Total number of errors:"
	 println user.errors.errorCount
	 println "=" * 20
	 println "Here are all of the errors:"
	 println user.errors
	 println "=" * 20
	 println "Here are the errors individually:"
	 user.errors.allErrors.each{
	 println it
	 println "-" * 20
	 }
	 assertEquals "blank", user.errors["name"]
	 }
	 */

	void printErrors(user) {
		println "=" * 80
		println "Total number of errors:" +  user.errors.errorCount

		/*
		println "=" * 80
		println "Here are all of the errors:"
		println user.errors
		*/

		println "=" * 80
		println "Here are the errors individually:"
		user.errors.allErrors.each{
			println "* " + it
		}
	}
}

