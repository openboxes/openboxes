/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
import org.pih.warehouse.core.Location

class InitializationFilters {
	def locationService
	def productService
	
	def filters = {

		sessionCheck(controller:'*', action:'*') {
			before = {
				try {
					// Make sure all session variables are initialized
					if (!session.warehouse) { 
						Location currentLocation = Location.get(session?.warehouse?.id)
                        session._showTime = true
						session.loginLocations = locationService.getLoginLocations(currentLocation) 			
						session.loginLocationsMap = locationService.getLoginLocationsMap(currentLocation)			
					}
					
					if (!session.rootCategory) { 
						session.rootCategory = productService.getRootCategory()
					} 
					if (!session.inventoryCategoryFilters) { 
						session.inventoryCategoryFilters = [];
					}
					if (!session.productCategoryFilters) {
						session.productCategoryFilters = [];
					}
					
				} catch (Exception e) { 
					log.error "Unable to initialize session variables: " + e.message, e
				}				
									

			}
		}

	}
}
