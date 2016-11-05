/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
import org.apache.http.client.utils.URIBuilder
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class SecurityFilters {
	
  static ArrayList controllersWithAuthUserNotRequired = ['test', 'errors']
  static ArrayList actionsWithAuthUserNotRequired = ['status', 'test', 'login', 'logout', 'handleLogin', 'signup', 'handleSignup', 'json', 'updateAuthUserLocale', 'viewLogo']
  static ArrayList actionsWithLocationNotRequired = ['status', 'test', 'login', 'logout', 'handleLogin', 'signup', 'handleSignup', 'json', 'updateAuthUserLocale', 'viewLogo', 'chooseLocation']
	def authService 
	def filters = {
		loginCheck(controller:'*', action:'*') {
			
			afterView = {
				// Clear out current user after rendering the view 
				AuthService.currentUser.set(null)
				AuthService.currentLocation.set(null)
			}
			before = {	
				
				// Set the current user (if there's on in the session)
				if (session.user) { 
					if (!AuthService.currentUser) {  
						AuthService.currentUser = new ThreadLocal<User>()
					}
                    //println "Setting current user " + session.warehouse.id
					AuthService.currentUser.set(User.get(session.user.id))
				}
				
				if (session.warehouse) { 
					if (!AuthService.currentLocation) { 
						AuthService.currentLocation = new ThreadLocal<Location>()
					}
					
					//println "Setting current location " + session.warehouse.id
					AuthService.currentLocation.set(Location.get(session.warehouse.id))
					//println "Getting currentLocation " + AuthService.currentLocation.get()
				}
				
				// Need to bypass security filter when generating a PDF report, otherwise the 
				// generated PDF contains the login screen
				//if (controllerName.equals("report") && (actionName.equals("showTransactionReport") || actionName.equals("showChecklistReport"))) { 
				//	log.info ("User: " + session.user )
				//	log.info ("Location: " + session.location)
				//	return true;
				//}


				// This allows requests for the health monitoring endpoint to pass through without a user
				if (controllerName.equals("api") && actionName.equals("status")) {
					return true
				}
				
				// This allows the megamenu to be g:include'd in the page (allowing for dynamic content to be added) 
				if(controllerName.equals("dashboard") && actionName.equals("megamenu")) { 
					return true
				}
								
				
				// Not sure when this happens								
				if (params.controller == null) {
					redirect(controller: 'auth', action:'login')   
					return true
				}			
				// When a request does not require authentication, we return true
				// FIXME In order to start working on sync use cases, we need to authenticate  	
				else if (controllersWithAuthUserNotRequired.contains(controllerName)) {
					return true;
				}
				// When there's no authenticated user in the session and a request requires authentication 
				// we redirect to the auth login page.  targetUri is the URI the user was trying to get to.
				else if(!session.user && !(actionsWithAuthUserNotRequired.contains(actionName))) {
                    def targetUri = ""
                    // We only want to handle GETs because POSTs would be much more difficult
                    if (request.method == "GET") {
                        targetUri = (request.forwardURI - request.contextPath);
                        if (request.queryString)
                            targetUri += "?" + request.queryString
                    }
                    /*
                    // Handle post
                    else {
                        try {
                            log.info "Using referer as targetUri "
                            URIBuilder builder = new URIBuilder(request.getHeader("referer"))
                            //def queryString = builder.getQueryParams().collectEntries{[it.name, it.val]}.inject([]) { result, entry ->
                            //    result << "${entry.key}=${URLEncoder.encode(entry.value.toString())}"
                            //}.join('&')

                            def params = builder.getQueryParams().inject([:]) {map, param ->
                                map << [(param.name): param.value]
                            }

                            def queryString = params.inject([]) { result, entry ->
                                result << "${entry.key}=${URLEncoder.encode(entry.value.toString())}"
                            }
                            targetUri = (builder.getPath() - request.contextPath);
                            targetUri += "?" + queryString.join("&")

                            println "targetUri: " + targetUri

                        } catch (Exception e) {
                            log.error("Error building targetUri based on referer: " + e.message, e)
                            targetUri = "/dashboard/index?error=true"
                        }
                    }
                    */

                    // Prevent user from being redirected to invalid pages after re-authenticating
                    if (!targetUri.contains("/dashboard/status") && !targetUri.contains("logout")) {
                        log.info "Request requires authentication, saving targetUri = " + targetUri
                        if (targetUri != "/") {
                            flash.message = "Your session has timed out."
                        }
                        session.targetUri = targetUri
                    }
                    else {
                        log.info "Not saving targetUri " + targetUri
                    }


                    redirect(controller: 'auth', action:'login')
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
				if (!session.warehouse && !(actionsWithLocationNotRequired.contains(actionName))) {						
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
					log.info "Location has not been selected, redirecting to chooseLocation ..."
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
