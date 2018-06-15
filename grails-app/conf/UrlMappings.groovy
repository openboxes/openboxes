import grails.validation.ValidationException
import org.apache.http.auth.AuthenticationException
import org.hibernate.ObjectNotFoundException

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

		"/snapshot/$action?"(controller: "inventorySnapshot")

		"/$controller/$action?/$id?" {
		      constraints {
				 // apply constraints here
			  }
		}

        // REST APIs with complex plural resource names
        "/api/categories"(parseRequest: false) {
            controller = { "categoryApi" }
            action = [GET: "list", POST: "save"]
        }
        "/api/categories/$id"(parseRequest: false) {
            controller = {"categoryApi" }
            action = [GET:"read", POST:"save", PUT:"save", DELETE:"delete"]
        }

        // Standard REST APIs
        "/api/${resource}s"(parseRequest: false) {
            controller = { "${params.resource}Api" }
            action = [GET: "list", POST: "create"]
        }
		"/api/${resource}s/$id"(parseRequest: false) {
            controller = {"${params.resource}Api" }
            action = [GET:"read", POST:"update", PUT:"update", DELETE:"delete"]
        }

        // Anonymous REST APIs like Status, Login, Logout
		"/api/$action/$id?"(controller:"api", parseRequest:false){
			//action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
		}

        "/api/generic/${resource}/"(parseRequest: false) {
            controller = "genericApi"
            action = [GET: "list"]
        }

        "/api/generic/${resource}/$id"(parseRequest: false) {
            controller = "genericApi"
            action = [GET:"read"]
        }



        //"/test/searchByFirstName.json?q=$q"(controller:"test") {
		//	action = [GET:"searchByFirstName"]
		//}
			
		//"/person/name/$q?"(controller:"test") {
		//	action = [GET:"searchByFirstName"]
		//}
		"401"(controller:"errors", action:"handleUnauthorized")
		"404"(controller:"errors", action:"handleNotFound")
        "405"(controller:"errors", action:"handleMethodNotAllowed")
		"500"(controller:"errors", action:"handleException")
        "500"(controller:"errors", action:"handleNotFound", exception: ObjectNotFoundException)
        "500"(controller:"errors", action:"handleValidationErrors", exception: ValidationException)
        "500"(controller:"errors", action:"handleUnauthorized", exception: AuthenticationException)

		//"500"(controller:"errors",action: "handleInvalidDataAccess", exception: MySQLSyntaxErrorException)
		//"500"(controller:"errors", action:"handleInvalidDataAccess", exception: HibernateOptimisticLockingFailureException)
        "/"(controller:"home", action:"index")
	}



}
