package org.pih.warehouse.core

import groovy.sql.Sql;

class UserService {

	def dataSource
	boolean transactional = true
	
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



}
