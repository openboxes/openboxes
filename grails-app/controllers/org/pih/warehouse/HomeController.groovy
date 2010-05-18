package org.pih.warehouse

class HomeController {

    def index = {       		
    	if (session.user == null) { 
        	flash.message = 'You are not authorized to access this page.  '
    		redirect(controller: 'auth', action: 'login');
    	}
    }
    
    def dashboard = { } 
    
}
