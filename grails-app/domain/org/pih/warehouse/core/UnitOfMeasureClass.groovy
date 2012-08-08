package org.pih.warehouse.core

import java.util.Date;

import org.pih.warehouse.auth.AuthService;

class UnitOfMeasureClass {

	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		updatedBy = AuthService.currentUser.get()
	}

	String id
	String name					// area, volume, length, weight, currency
	String code					
	String description 
	Boolean active
	UnitOfMeasureType type
	UnitOfMeasure baseUom
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	User createdBy
	User updatedBy
	
	static mapping = {
		id generator: 'uuid'
	}
	
	static constraints = { 
		name(nullable:false)
		code(nullable:false)
		description(nullable:true)
		active(nullable:true)
		type(nullable:false)
		baseUom(nullable:true)
		
		createdBy(nullable:true)
		updatedBy(nullable:true)
	}
	
	String toString() { return name }
	
}
