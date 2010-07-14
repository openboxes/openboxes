package org.pih.warehouse.auth

class AuthService {
	
	boolean transactional = true
	
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
