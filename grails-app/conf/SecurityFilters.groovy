
class SecurityFilters {
	def filters = {
		loginCheck(controller:'*', action:'*') {
			before = {	
				
				// This allows the left-nav menu to be 'included' in the page (allowing for dynamic content to be added) 
				if(controllerName.equals("dashboard") && (actionName.equals("menu") || actionName.equals("megamenu"))) { 
					return true
				}				
				
				String [] controllersWithAuthUserNotRequired = "api,test".split(",");
				String [] actionsWithAuthUserNotRequired = "test,login,handleLogin,signup,handleSignup,json".split(",");
				String [] actionsWithLocationNotRequired = "test,login,logout,handleLogin,signup,handleSignup,chooseLocation,json".split(",");
				
				// Not sure when this happens								
				if (params.controller == null) {
					redirect(controller: 'auth', action:'login')   
					return true
				}			
				// When a request does not require authentication, we return true
				// FIXME In order to start working on sync use cases, we need to authenticate  	
				else if (Arrays.asList(controllersWithAuthUserNotRequired).contains(controllerName)) {
					return true;
				}
				// When there's no authenticated user in the session and a request requires authentication 
				// we redirect to the auth login page.  targetUri is the URI the user was trying to get to.
				else if(!session.user && !(Arrays.asList(actionsWithAuthUserNotRequired).contains(actionName))) {
					def targetUri = (request.forwardURI - request.contextPath);
					if (request.queryString) 
						targetUri += "?" + request.queryString

					redirect(controller: 'auth', action:'login', params: ['targetUri': targetUri])
					return false;
				}
					
				// When a user has an authenticated, we want to check if they have an active account
				if (session?.user && !session?.user?.active) { 
					session.user = null;
					// FIXME cannot use warehouse tag lib here
					// MissingPropertyException: No such property: warehouse for class: SecurityFilters
					//flash.message = "${warehouse.message(code: 'auth.accountRequestUnderReview.message')}"
					//flash.message = "auth.accountRequestUnderReview.message"
					redirect(controller: 'auth', action:'login')
					return false;
				}
				
				// When a user has not selected a warehouse and they are requesting an action that requires one, 
				// we redirect to the choose warehouse page.
				if (!session.warehouse && !(Arrays.asList(actionsWithLocationNotRequired).contains(actionName))) {						
					//def targetUri = (request.forwardURI - request.contextPath);
					//if (request.queryString)
					//	targetUri += "?" + request.queryString

						
					if (session?.warehouseStillNotSelected) { 
						// FIXME cannot use warehouse tag lib here
						// MissingPropertyException: No such property: warehouse for class: SecurityFilters
						//flash.message = "${warehouse.message(code: 'warehouse.chooseLocationToManage.message')}"
						//flash.message = "warehouse.chooseLocationToManage.message"
					}
					
					session.warehouseStillNotSelected = true;
					//redirect(controller: 'dashboard', action: 'chooseLocation', params: ['targetUri': targetUri])
					redirect(controller: 'dashboard', action: 'chooseLocation')
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
