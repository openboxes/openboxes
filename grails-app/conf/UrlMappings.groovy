class UrlMappings {
	static mappings = {
		"/$controller/$action?/$id?" {
		      constraints {
				 // apply constraints here
			  }
		}
		
		"/api/$action/$id"(controller:"api", parseRequest:true){
			//action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
		}
		//"/test/searchByFirstName.json?q=$q"(controller:"test") { 
		//	action = [GET:"searchByFirstName"]
		//}
			
		//"/person/name/$q?"(controller:"test") {
		//	action = [GET:"searchByFirstName"]
		//}
		"/"(controller:"home", action:"index")      
		"500"(view:'/error')
	}



}
