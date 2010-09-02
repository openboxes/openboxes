package org.pih.warehouse

import java.util.SortedSet;
import grails.converters.JSON;
import org.pih.warehouse.core.Person;

class TestController {
	
	def mailService;
	
	
	def jQuery = {
	}
	
	def jQueryTabs = {
	} 	
	
	def jQueryDatepicker = {
	}
	
	def yui = {
	}
	
	def testClosure = { 
		
		
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
	
	
	/*	
	def searchByFirstName = {
		def person = Person.findAllByFirstNameIlike(params.q + "%")
		if ( person ) {
			render person as JSON
		} else {
			response.sendError(400, "Person does not exist");
		}
	}*/
	
	def searchByName = {
		log.info params
		def people = new TreeSet();
		try {  
			
			if (params.term) {
								
				people = Person.withCriteria {
					or {
						ilike("firstName", "%" +  params.term + "%")
						ilike("lastName", "%" +  params.term + "%")
						ilike("email", "%" + params.term + "%")
					}
				}
				
				/*
				def searchTerms = params.term.split(" ");			
				searchTerms.each {
					log.info "searchTerm: '${it}' " + it.class.name	
					def peopleWithSimilarFirstName = Person.findAllByFirstNameIlike("%" + it + "%")
					def peopleWithSimilarLastName = Person.findAllByLastNameIlike("%" + it + "%")
					log.info "peopleWithSimilarFirstName: " + peopleWithSimilarFirstName;
					log.info "peopleWithSimilarLastName: " + peopleWithSimilarLastName;
					people.addAll(peopleWithSimilarFirstName)
					people.addAll(peopleWithSimilarLastName)
					log.info "people: " + people
				}*/
				
				people = people.collect() {
					[	value: it.id,
						label: it.firstName + " " + it.lastName,
						desc: "description", 
						icon: "some image"]
				}
			}
			//def jsonItems = [result: people]
			//render jsonItems as JSON;
		} catch (Exception e) { 
			e.printStackTrace();
		
		}
		render people as JSON;
	}
	
	
	
}
