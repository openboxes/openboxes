
class SecurityFilters {
	def filters = {
		loginCheck(controller:'*', action:'*') {
			before = {	
				
				log.info params
				String [] controllersWithAuthUserNotRequired = "api,test".split(",");
				String [] actionsWithAuthUserNotRequired = "login,doLogin,signup,doSignup".split(",");
				String [] actionsWithWarehouseNotRequired = "login,doLogin,signup,doSignup,chooseWarehouse,viewLogo".split(",");
								
				if (params.controller == null) {
					log.info "controller is null redirect to auth" 
					redirect(controller: 'auth', action:'login')   
					return true
				}			
				// FIXME In order to start working on sync use cases, we need to authenticate  	
				else if (Arrays.asList(controllersWithAuthUserNotRequired).contains(controllerName)) {
					log.info "api controller" 
					return true;
				}
				else if(!session.user && !(
					Arrays.asList(actionsWithAuthUserNotRequired).contains(actionName))) {
					log.info "redirect to auth" 
					redirect(controller: 'auth', action:'login', params: ['targetUri': (request.forwardURI - request.contextPath)])
					return false;
				}
				
				if (!session.warehouse && !(Arrays.asList(actionsWithWarehouseNotRequired).contains(actionName))) {						
					if (session?.warehouseStillNotSelected) { 
						flash.message = "Please choose a warehouse to begin.";
					}
					session.warehouseStillNotSelected = true;
					redirect(controller: 'dashboard', action: 'chooseWarehouse')
					return false;
				}
				
			}
		}
		/*
		shipmentAccess(controller:'shipment', action:'*') {
			before = {
				def user = session.user;
				log.info "\n\n\nshipmentAccess: " + user;
				render(view: "/errors/accessDenied");
				return false;
				
			}
		}*/

	}
}
