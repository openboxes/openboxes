package org.pih.warehouse.core

import java.util.Date;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.inventory.Warehouse;

class User extends Person {

	Boolean active;				// default = false?
	String username;			// email or username
	String password;			// encrypted password
	Date lastLoginDate;			// keep track of the user's last login
	Warehouse warehouse;			// keep track of the user's last warehouse
	User manager;				// the user's designated manager 

	static hasMany = [ roles : Role ]
	static mapping = {
		table "`user`"
		roles joinTable: [name:'user_role', column: 'role_id', key: 'user_id']
	}

	static constraints = {
		active(nullable:true)
		manager(nullable:true)
		username(nullable: false, blank:false)
		password(blank:false, password: true)
		lastLoginDate(nullable:true)
		warehouse(nullable:true)
	}

	
	
}
