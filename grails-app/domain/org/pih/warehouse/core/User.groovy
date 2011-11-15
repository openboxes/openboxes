package org.pih.warehouse.core

import java.util.Date;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.Location;

class User extends Person {

	Boolean active;				// default = false?
	String username;			// email or username
	String password;			// encrypted password
	String passwordConfirm;		// password confirm used on signup and password reset
	Locale locale;				// the user's locale

	Date lastLoginDate;			// keep track of the user's last login
	Location warehouse;		// keep track of the user's last warehouse
	//Boolean useSavedLocation		// indicates whether we should use this warehouse when user logs in 
	User manager;				// the user's designated manager 

	byte [] photo				// profile photo


	static hasMany = [ roles : Role ]
	static mapping = {
		table "`user`"
		roles joinTable: [name:'user_role', column: 'role_id', key: 'user_id']
	}
	static transients = ["passwordConfirm"]
	static constraints = {
		active(nullable:true)
		manager(nullable:true)
		photo(nullable:true, maxSize:10485760) // 10 MBs
		username(nullable: false, blank:false, unique: true, maxSize:255)		
		password(nullable: false, blank: false, minSize: 6, maxSize:255, validator: {password, obj ->
			def passwordConfirm = obj.properties['passwordConfirm']
			if(passwordConfirm == null) return true // skip matching password validation (only important when setting/resetting pass)
			passwordConfirm == password ? true : ['invalid.matchingpasswords']
		})		
		lastLoginDate(nullable:true)
		//useSavedLocation(nullable:true)
		warehouse(nullable:true)
	}

	
	
}
