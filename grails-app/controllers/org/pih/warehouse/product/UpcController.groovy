package org.pih.warehouse.product;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.params.SyncBasicHttpParams

import grails.converters.*
import groovyx.net.http.RESTClient
import groovy.util.slurpersupport.GPathResult
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.GET
import groovyx.net.http.HTTPBuilder
//import groovyx.net.ws.WSClient

class UpcController {

	def index = { 
		redirect(action: "list")
	}
	
	def listOld = { 
		
		/*
		def client = new WSClient("http://www.webservicex.net/CurrencyConvertor.asmx?WSDL", this.class.classLoader)
		def rate = client.ConversionRate()
		*/
		/*
		def serviceUrl = "http://www.w3schools.com/webservices/tempconvert.asmx?WSDL"
		def proxy = new WSClient(serviceUrl.toString(), this.class.classLoader)
		proxy.initialize();
		def serviceResult = proxy.FahrenheitToCelsius("80")
		println serviceResult
		*/
		
		/*
		def serviceUrl = "http://www.searchupc.com/service/UPCSearch.asmx?wsdl"
		//serviceUrl += "&access_tokenimport groovyx.net.http.
=2C4EAE15-7231-46DC-BA54-6676C748A985" 
		//serviceUrl += "&request_type=1"
		//serviceUrl += "&upc=885909194322" 
		def client = new WSClient(serviceUrl.toString(), this.class.classLoader)
		
		client.initialize();
		//client.accesstoken = "2C4EAE15-7231-46DC-BA54-6676C748A985"
		def getProductRequest = [:]
		getProductRequest.accessToken = "2C4EAE15-7231-46DC-BA54-6676C748A985";
		getProductRequest.upc = "test";
		def serviceResult = client.GetProduct(getProductRequest);
		println serviceResult;
		*/
		
		/*
		def serviceUrl = "http://webservices.amazon.com/AWSECommerceService/AWSECommerceService.wsdl"
		def client = new WSClient(serviceUrl.toString(), this.class.classLoader)
		client.initialize();
		def serviceResult = client.GetProduct("test");
		println serviceResult;
		*/
		
		
		
		/*
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
		*/
		//return divisions;
		
	}
	
	
	def list = { 
		
		def username = "justinmiranda";
		def password = "test123";
		def twitter = new RESTClient( 'https://twitter.com/statuses/' )
		// twitter auth omitted
		twitter.auth.basic( username,password )
		 
		try { // expect an exception from a 404 response:
			twitter.head path : 'public_timeline'
			//assert false, 'Expected exception'
		}
		// The exception is used for flow control but has access to the response as well:
		catch( ex ) { assert ex.response.status == 404 }
		 
		def resp = twitter.get( path : 'public_timeline.json' )
		//assert resp.status == 200
		//assert resp.contentType == JSON.toString()
		//assert ( resp.data instanceof net.sf.json.JSON )
		//assert resp.data.status.size() > 0
		
		//assert twitter.head( path : 'public_timeline.json' ).status == 200
		/*
		http://webservices.amazon.com/onca/xml?Service=AWSECommerceService
		&SubscriptionId=[your subscription ID here]
		&Operation=ItemLookup
		&ItemId=0486411214
		*/
		//render resp
		
	}
	
	
	def testWebService = { 
		
		def url = "http://www.webservicex.net/CurrencyConvertor.asmx?wsdl"
		
		def payload = """
		<?xml version="1.0" encoding="UTF-8" standalone="no"?><SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:http="http://schemas.xmlsoap.org/wsdl/http/" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:s="http://www.w3.org/2001/XMLSchema" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:tns="http://www.webserviceX.NET/" xmlns:tm="http://microsoft.com/wsdl/mime/textMatching/" xmlns:mime="http://schemas.xmlsoap.org/wsdl/mime/" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" ><SOAP-ENV:Body><tns:ConversionRate xmlns:tns="http://www.webserviceX.NET/"><tns:FromCurrency>USD</tns:FromCurrency><tns:ToCurrency>EUR</tns:ToCurrency></tns:ConversionRate></SOAP-ENV:Body></SOAP-ENV:Envelope>
		"""
		
		URI uri = URIUtils.createURI("http", "www.google.com", -1, "/search",
			"q=httpclient&btnG=Google+Search&aq=f&oq=", null);
		HttpGet httpget = new HttpGet(uri);
		System.out.println(httpget.getURI());
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpget);
		
		render response.getEntity().getContent()
		/*		
		def method = new PostMethod(url)
		def client = new HttpClient()
		
		payload = payload.trim()
		method.addRequestHeader("Content-Type","text/xml")
		method.addRequestHeader("Accept","text/xml,application/xml;q=0.9")
		method.setRequestEntity(new StringRequestEntity(payload))
		def statusCode = client.executeMethod(method)
		println "STATUS CODE : ${statusCode}"
		def resultsString = method.getResponseBodyAsString()
		method.releaseConnection()	
		render resultsString;
		*/	
	}

	
}
