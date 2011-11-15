import org.pih.warehouse.core.Location

class InitializationFilters {
	def locationService
	
	def filters = {
		sessionCheck(controller:'*', action:'*') {
			before = {	
				Location currentLocation = Location.get(session?.warehouse?.id)
				session.loginLocations = locationService.getLoginLocations(currentLocation)
				
				// Make sure all session variables are initialized
				if (!session.inventoryCategoryFilters)
					session.inventoryCategoryFilters = []; 
					
				if (!session.productCategoryFilters)
					session.productCategoryFilters = [];

			}
		}
	}
}
