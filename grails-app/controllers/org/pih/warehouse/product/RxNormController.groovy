package org.pih.warehouse.product;

import groovyx.net.http.RESTClient
import static groovyx.net.http.Method.GET


class RxNormController {

	def index = { 
		redirect(action: "list")
	}
	
	def test = { 
		def client = new RESTClient("http://rxnav.nlm.nih.gov/REST/")
		def data = "";
		client.request(GET) {
			uri.path = '/brands'
			//uri.query = [ 'client_id': 'bff71b0439e75797f6af27b220eefe7b9b0b989d' ]
					  
			response.success = { resp, json ->
				println 'request success '
			}
			
			response.failure = { resp ->
				println 'request failed '
			}
		}
		//return divisions;
		
	}

	
}
