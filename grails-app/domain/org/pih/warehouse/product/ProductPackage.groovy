package org.pih.warehouse.product

import java.util.Date;

import org.pih.warehouse.auth.AuthService;
import org.pih.warehouse.core.UnitOfMeasure;
import org.pih.warehouse.core.User;

class ProductPackage implements Serializable {

	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		updatedBy = AuthService.currentUser.get()
	}

	String id
	String name
	String description
	String gtin
	Integer quantity
	UnitOfMeasure uom
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	User createdBy
	User updatedBy
	
	static belongsTo = [product : Product]

	static mapping = {
		id generator: 'uuid'
	}
	
    static constraints = {
		//name(nullable:false,unique:true)
		name(nullable:true)
		description(nullable:true)
		gtin(nullable:false)
		uom(nullable:true)
		quantity(nullable:false)
		createdBy(nullable:true)
		updatedBy(nullable:true)
    }
	
	String toString() { return name }
	
}
