package org.pih.warehouse.web.filter

class SecurityFilters {
	def filters = {
		loginCheck(controller:'*', action:'*') {
			before = {									
				if (params.controller == null) {
					redirect(controller: 'auth', action:'login')   
					return true
				}
				else if(!session.user && !("login".equals(actionName) || "doLogin".equals(actionName))) {
					redirect(controller: 'auth', action:'login')
					return false;
				}
				else if (!session.warehouse && !("chooseWarehouse".equals(actionName) || "login".equals(actionName) || "doLogin".equals(actionName))) {
					redirect(controller: 'dashboard', action: 'chooseWarehouse')
					return false;
				}
				
			}
		}
	}
}
