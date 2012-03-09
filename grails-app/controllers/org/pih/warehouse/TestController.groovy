package org.pih.warehouse

import java.util.SortedSet;

import groovy.net.xmlrpc.*
import groovy.util.slurpersupport.GPathResult;
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.Method;
import groovyx.net.http.RESTClient;

import org.grails.plugins.wsclient.service.WebService;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Product;

//import groovyx.net.http.*
///import static groovyx.net.http.ContentType.JSON

class TestController {

	def mailService;
	def webService

	def index = {

	}

	
	def testRestClient = { 
		RESTClient restClient = new RESTClient("http://tickets.pih-emr.org/jira/rest/api/latest/issue/PIMS-1234.json")
		def response = restClient.post([:]);
		
		println "Solr response status: ${response.status}"
		assert response.success
		assert response.status == 200
		assert response.data instanceof GPathResult
		assert response.data.lst.int[0].@name == 'status'
		
		
		/*
		def response = restClient.post(
			contentType: XML,
			requestContentType: XML,
			body: {
				add {
					doc {
						field(name:"id", "SOLR1000")
					}
				}
			}
		)*/
	}


	def testWsClient = {
		def wsdlURL = "http://www.w3schools.com/webservices/tempconvert.asmx?WSDL"
		def proxy = webService.getClient(wsdlURL)

		def result = proxy.CelsiusToFahrenheit(0)
		result = "You are probably freezing at ${result} degrees Farhenheit"
		render result
	}


	def testHttpBuilder =  {
		def results = ""
		def http = new HTTPBuilder('http://ajax.googleapis.com')
		
		http.request( GET, JSON ) {
			uri.path = '/ajax/services/search/web'
			uri.query = [ v:'1.0', q: 'Calvin and Hobbes' ]
	
			//headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
	
			// response handler for a success response code:
			response.success = { resp, json ->
				println resp.statusLine
	
				// parse the JSON response object:
				json.responseData.results.each { results += "<li>${it.titleNoFormatting} : ${it.visibleUrl}</li>" }
			}
		}
		render "<ul>" + results + "</ul>"
	}

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
			if (params.to && params.subject) {
				log.info "send html email";
				params.htmlMsg = g.render(template:"/email/emailTemplate", model:['string':'<b>new</b> html string'])				
				mailService.sendHtmlMail(params.subject, params.htmlMsg, params.to)
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
		render items as grails.converters.JSON;
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
		render items as grails.converters.JSON;
	}


	
}
