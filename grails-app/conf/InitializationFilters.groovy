
class InitializationFilters {
	def filters = {
		sessionCheck(controller:'*', action:'*') {
			before = {	
				// Make sure all session variables are initialized
				if (!session.inventoryCategoryFilters)
					session.inventoryCategoryFilters = []; 
					
				if (!session.productCategoryFilters)
					session.productCategoryFilters = [];

			}
		}
	}
}
