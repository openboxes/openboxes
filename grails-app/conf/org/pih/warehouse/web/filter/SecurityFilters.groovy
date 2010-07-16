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
				else if (!session.warehouse && !("chooseWarehouse".equals(actionName) || "logout".equals(actionName) 
							|| "login".equals(actionName) || "doLogin".equals(actionName))) {
						
					if (session?.warehouseStillNotSelected) { 
						flash.message = "You must choose a warehouse before selecting a menu option.";
					}
					session.warehouseStillNotSelected = true;
					redirect(controller: 'dashboard', action: 'chooseWarehouse')
					//render(view: "/dashboard/chooseWarehouse")					
					return false;
				}
				
			}
		}
	}
}
