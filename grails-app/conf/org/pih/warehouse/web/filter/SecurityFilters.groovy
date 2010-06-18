package org.pih.warehouse.web.filter

class SecurityFilters {
	def filters = {
		loginCheck(controller:'*', action:'*') {
			before = {					
				if(!session.user && !("test".equals(controllerName) || "login".equals(actionName) || "doLogin".equals(actionName))) {
					redirect(controller: 'auth', action:'login')
					return false;
				}
			}
		}
	}
}
