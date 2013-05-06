import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
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
		"401"(controller:"errors", action:"handleUnauthorized")
		"404"(controller:"errors", action:"handleNotFound")
        //"500"(controller:"errors",action: "handleInvalidDataAccess", exception: MySQLSyntaxErrorException)
        //"500"(controller:"errors", action:"handleInvalidDataAccess", exception: HibernateOptimisticLockingFailureException)
		"500"(controller:"errors", action:"handleException")
        "/"(controller:"home", action:"index")
	}



}
