package org.pih.warehouse.core

import groovy.sql.Sql;

class UserService {

	def dataSource
	boolean transactional = true
	
	void convertPersonToUser(Integer personId) { 
		def user = User.get(personId) 
		if (!user) { 
			def person = Person.get(personId)
			if (person) {
				Sql sql = new Sql(dataSource)
				sql.execute('insert into user (id, username, password) values (?, ?, ?)', [person?.id, person?.email, 'password'])		
			}
		}
	}

	void convertUserToPerson(Integer personId) {
		def person = Person.get(personId)
		if (person) {
			Sql sql = new Sql(dataSource)
			sql.execute('delete from user where id = ?', [personId])
		}
	}



}
