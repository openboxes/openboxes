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

import groovy.sql.Sql
import org.pih.warehouse.auth.AuthService

class UserService {

	def dataSource
	boolean transactional = true
	
	User getUser(String id) { 
		return User.get(id)
	}

    Boolean isUserAdmin(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_ADMIN]
            return effectRoles(user).any { roles.contains(it.roleType) }
        }
        return false;
    }

    Boolean isUserManager(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_ASSISTANT]
            return effectRoles(user).any { roles.contains(it.roleType) }
        }
        return false;
    }

    Boolean canUserBrowse(User u) {
        if (u) {
            def user = User.get(u.id)
            def roles = [RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_BROWSER, RoleType.ROLE_ASSISTANT]
            return effectRoles(user).any { roles.contains(it.roleType) }
        }
        return false;
    }

    Boolean isUserInRole(String userId, Collection roles) {
        User user = getUser(userId)
        return effectRoles(user).any { roles.contains(it.roleType) }
    }


    def findPersons(String query, Map params) {
		def criteria = Person.createCriteria()
		def results = criteria.list (params) {
			or { 
				like("firstName", query)
				like("lastName", query)
				like("email", query)
			}
			order("lastName", "desc")
		}
        return results
	}


    def findUsers(String query, Map params) {
        println "findUsers: " + query + " : " + params
        def criteria = User.createCriteria()
        def results = criteria.list (params) {
            if (query) {
                or {
                    like("firstName", query)
                    like("lastName", query)
                    like("email", query)
                    like("username", query)
                }
            }
            if (params.status) {
                eq("active", Boolean.valueOf(params.status))
            }

            order("lastName", "desc")
        }

        return results;
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
	
  
  def getAllAdminUsers() {
	  def recipients = [] 	  
	  def roleAdmin = Role.findByRoleType(RoleType.ROLE_ADMIN)
	  if (roleAdmin) {
		  def criteria = User.createCriteria()
		  recipients = criteria.list {
			  roles {
				  eq("id", roleAdmin.id)
			  }
			  eq("active", true)
		  }
	  }
	  return recipients	  
  }

}
