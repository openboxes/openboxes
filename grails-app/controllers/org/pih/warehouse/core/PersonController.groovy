package org.pih.warehouse.core;

import groovy.sql.Sql

class PersonController {
	def userService

	def scaffold = Person;
	
	def list = {
		def personInstanceList = []
		def personInstanceTotal = 0;
		
		params.max = Math.min(params.max ? params.int('max') : 15, 100)
		
		if (params.q) {
			def term = "%" + params.q + "%"
			personInstanceList = Person.findAllByFirstNameLikeOrLastNameLike(term, term, params)
			personInstanceTotal = Person.countByFirstNameLikeOrLastNameLike(term, term, params);
		}
		else {
			personInstanceList = Person.list(params)
			personInstanceTotal = Person.count()
		}
		
		[personInstanceList: personInstanceList, personInstanceTotal: personInstanceTotal]
	}
		
	def convertPersonToUser = { 	
		userService.convertPersonToUser(params.id)
		redirect(controller: "user", action: "edit", id: params.id)	
	}

	def convertUserToPerson = {
		userService.convertUserToPerson(params.id)
		redirect(controller: "person", action: "show", id: params.id)
	}

}
