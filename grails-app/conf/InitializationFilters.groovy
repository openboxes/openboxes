
class InitializationFilters {
	def filters = {
		sessionCheck(controller:'*', action:'*') {
			before = {	
				// Make sure all session variables are initialized
				if (!session.categoryFilters)
					session.categoryFilters = []; 
			}
		}
	}
}
