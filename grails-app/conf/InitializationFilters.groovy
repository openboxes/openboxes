import org.pih.warehouse.core.Location

class InitializationFilters {
	def locationService
	
	def filters = {
		sessionCheck(controller:'*', action:'*') {
			before = {
				try { 
					Location currentLocation = Location.get(session?.warehouse?.id)
					session.loginLocations = locationService.getLoginLocations(currentLocation)
				} catch (Exception e) { 
					// Only happens when location service is unavailable 	
					log.error "Error retrieving login-able locations: " + e.message
					//session.loginLocations = []
				}
				// Make sure all session variables are initialized
				if (!session.inventoryCategoryFilters)
					session.inventoryCategoryFilters = []; 
					
				if (!session.productCategoryFilters)
					session.productCategoryFilters = [];

			}
		}
	}
}
