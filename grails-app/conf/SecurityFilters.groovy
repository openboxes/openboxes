
class SecurityFilters {
	def filters = {
		loginCheck(controller:'*', action:'*') {
			before = {	
				
				String [] actionsWithAuthUserNotRequired = "login,doLogin,signup,doSignup".split(",");
				String [] actionsWithWarehouseNotRequired = "login,doLogin,signup,doSignup,chooseWarehouse,viewLogo".split(",");
								
				if (params.controller == null) {
					redirect(controller: 'auth', action:'login')   
					return true
				}
				else if(!session.user && !(Arrays.asList(actionsWithAuthUserNotRequired).contains(actionName))) {
					redirect(controller: 'auth', action:'login')
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
