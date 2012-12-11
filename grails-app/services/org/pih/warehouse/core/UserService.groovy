/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core

import groovy.sql.Sql;
import org.pih.warehouse.auth.AuthService


class UserService {

	def dataSource
	boolean transactional = true
	
	User getUser(String id) { 
		return User.get(id)
	}

  Boolean isUserAdmin(User u){
    def user = User.get(u.id)
    def roles = [RoleType.ROLE_ADMIN]
		return  effectRoles(user).any { roles.contains(it.roleType)}
  }
  Boolean isUserManager(User u){
    def user = User.get(u.id)
    def roles = [RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER]
		return effectRoles(user).any { roles.contains(it.roleType)}
  }
  Boolean canUserBrowse(User u){
    def user = User.get(u.id)
    def roles = [RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_BROWSER]
		return effectRoles(user).any { roles.contains(it.roleType)}
  }
	
	Boolean isUserInRole(String userId, Collection roles) { 
		User user = getUser(userId)
		return effectRoles(user).any { roles.contains(it.roleType) }
	}
	
	
	def findPersons(String terms, Map params) { 		
		def criteria = Person.createCriteria()
		def results = criteria.list (params) {
			or { 
				like("firstName", terms)
				like("lastName", terms)
				like("email", terms)
			}
			order("lastName", "desc")
		}
		
	}
	
	
	void convertPersonToUser(String personId) { 
		def user = User.get(personId) 
		if (!user) { 
			def person = Person.get(personId)
			if (person) {
				def encodedPassword = "password"?.encodeAsPassword()
				Sql sql = new Sql(dataSource)
				sql.execute('insert into user (id, username, password) values (?, ?, ?)', [person?.id, person?.email, encodedPassword])		
			}
		}
	}

	void convertUserToPerson(String personId) {
		def person = Person.get(personId)
		if (person) {
			Sql sql = new Sql(dataSource)
			sql.execute('delete from user where id = ?', [personId])
		}
	}
	
	def findUsersByRoleType(RoleType roleType) { 
		def users = []
		def role = Role.findByRoleType(roleType)
		if (role) {
			def criteria = User.createCriteria()
			users = criteria.list {
				roles {
					eq("id", role.id)
				}
			}
		}
		return users;		
	}

  private def rolesForCurrentLocation(user){
    def currentLocation = AuthService.currentLocation?.get()
    if(!currentLocation) return []
    user?.locationRoles?.findAll{it.location == currentLocation}?.collect{it.role} ?: []
  }

  private def effectRoles(user){
    def defaultRoles = user?.roles?.collect{it} ?: []
    defaultRoles.addAll(rolesForCurrentLocation(user))   
    defaultRoles
  }
	

}
