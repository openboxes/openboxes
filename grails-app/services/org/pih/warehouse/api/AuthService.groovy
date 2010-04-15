package org.pih.warehouse.api

class AuthService {

    boolean transactional = true

    def checkAuthentication() {
	println "checking if user is authenticated $session.user";
	if(!session.user) {
	    println "user in not authenticated";
	    redirect(controller: "user", action: "login");
	    return false
	} else {
	    println "user is authenticated";
	}
    }
}
