
class DebugFilters {
	
	def filters = {
		logResponseTime(controller:'*', action:'*') {
			def startTime;
			before = {	
				startTime = System.currentTimeMillis();
			}
			after = { 
				def timeInSeconds = (System.currentTimeMillis() - startTime) / 1000;
				println("Response time: " + timeInSeconds + "s [${controllerName}.${actionName}] => "  + params);
				startTime = null;				
			}
		}
	}
}
