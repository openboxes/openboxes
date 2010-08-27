class UrlMappings {
	static mappings = {
		"/$controller/$action?/$id?" {
		      constraints {
				 // apply constraints here
			  }
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
