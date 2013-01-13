/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core;

import java.util.Date;

import org.pih.warehouse.auth.AuthService;
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product;

class Tag implements Serializable {

	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		updatedBy = AuthService.currentUser.get()
	}
	
	String id
	String tag
	Date dateCreated;
	Date lastUpdated;
	User createdBy
	User updatedBy
	
	static belongsTo = org.pih.warehouse.product.Product
	
	static mapping = {
		id generator: 'uuid'		
		products joinTable: [name:'product_tag', column: 'product_id', key: 'tag_id'], cascade: 'all-delete-orphan'
	}
	
	static hasMany = [
		products : Product 
	] 
	
	static constraints = {
		tag(nullable:false, maxSize: 255)
		updatedBy(nullable:true)
		createdBy(nullable:true)
	}

}
