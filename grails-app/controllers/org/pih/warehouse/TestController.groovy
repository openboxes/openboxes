package org.pih.warehouse

import java.util.SortedSet;
import grails.converters.JSON;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Product;

//import groovyx.net.http.*
///import static groovyx.net.http.ContentType.JSON

class TestController {
	
	def mailService;

	def index = { 
		
	}
	
	/*
	def testHttpBuilder =  {		
		def http = new HTTPBuilder("http://localhost:8080/amazon")
		
		http.request(Method.GET, JSON) { 
			url.path = '/book/list' 
			response.success = {resp, json -> json.books.each { book -> println book.title } } 
		}
		
	}*/	
	
	def jQueryButton = { 
		
	}
	
	def jQuery = {
	}
	
	def jqueryTabs = {
	} 	
	
	def jQueryDatepicker = {
	}
	
	def yui = {
	}
	
	def testClosure = { 
		
		
	}
	
	def jQueryAutoSuggestTag = { 
		
		
	}
	
	def jQueryAutocomplete = { 
		
	}
	
	def sendEmail = {
		if(request.method == 'POST') {
			log.info "send email test"
			if (params.to && params.msg && params.subject) { 
				log.info "send email";
				mailService.sendMail(params.subject, params.msg, params.to);
				flash.message = "Sent simple email successfully!"
			} else if (params.to && params.htmlMsg && params.subject) { 
				log.info "send html email";
				mailService.sendHtmlMail(params.subject, params.htmlMsg, "text alternative message", params.to)
				flash.message = "Sent HTML email successfully!"
			} else { 			
				flash.message = "Unable to send email";
			}
		}
	}
	
	
	def findProducts = {
		log.info params
		def items = new TreeSet();			
		if (params.term) {							
			items = Product.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")
					ilike("upc", "%" +  params.term + "%")
				}
			}
			if (items) { 
				items = items.collect() {
					[	value: it.id,
						valueText: it.name,
						label: it.name,
						desc: it.description,
						icon: "none"]
				}
			}
			else { 				
				def item =  [ 
					value: 0, 
					valueText : params.term,
					label: "Add a new item for '" + params.term + "'?",
					desc: params.term,
					icon: "none"
				];
				items.add(item)
			}
		}
		render items as JSON;
	}
	
	
	def findPersonByName = {
		log.info params
		def items = new TreeSet();
		try {  
			
			if (params.term) {
								
				items = Person.withCriteria {
					or {
						ilike("firstName", "%" +  params.term + "%")
						ilike("lastName", "%" +  params.term + "%")
						ilike("email", "%" + params.term + "%")
					}
				}
			
				if (items) { 		
					items = items.collect() {
						[	value: it.id,
							valueText: it.firstName + " " + it.lastName,
							label: it.firstName + " " + it.lastName,						
							desc: it.email, 
							icon: it.photo]
					}
				}	
				else { 
					def item =  [
						value: 0,
						valueText : params.term,
						label: "Add new item for '" + params.term + "'?",
						desc: params.term,
						icon: "none"
					];
					items.add(item)
				}			
				
				
			}
		} catch (Exception e) { 
			e.printStackTrace();
		
		}
		render items as JSON;
	}
	
	
	
}
