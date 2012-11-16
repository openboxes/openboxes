/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.auth

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.User;

class AuthService {
	
	boolean transactional = true
	static ThreadLocal<User> currentUser = new ThreadLocal<User>();
	static ThreadLocal<Location> currentLocation = new ThreadLocal<Location>();
	
	/**
	 * Determine whether user is authenticated.  
	 * 
	 * FIXME Should not be using session object in a web-agnostic class.
	 * 
	 * @return	true if user is authenticated, false otherwise
	 */
	def isAuthenticated() {
		return (session.user) 
	}
	
	
	def authenticate(String username, String password) { 
		return true;				
	}
	
	
	def authorize(String username) {		
		return true;
	} 
}
