package org.pih.warehouse.jira

import java.util.SortedSet;

import groovy.net.xmlrpc.*
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.Method;

import org.grails.plugins.wsclient.service.WebService;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Product;


class JiraController {
	def webService
	static api
	static token
	static statuses

	def beforeInterceptor = {
		log.info "beforeInterceptor"
		
		if (api == null) api = getApi()
		if (token == null) token = getToken()
		if (statuses == null) { 
			statuses = getStatuses()
			log.info session.statuses
		}
		else { 
			log.info statuses
			
		}
	}


	def index = {  
		redirect(action: "searchIssues")		 
	}
	
	
	def sampleRestCall = { 
		def url = "http://tickets.pih-emr.org/jira/rest/api/latest/issue/PIMS-1234.json"
	}
	
	/**
	 * Throws an exception, so decided to move onto XML-RPC.
	 */
	def sampleSoapCall = {
		def token = ""
		try { 
			//def wsdlUrl = "http://tickets.pih-emr.org/jira/rpc/soap/jirasoapservice-v2?wsdl"
			//def proxy = webService.getClient(wsdlUrl)
			def proxy = getSoapApi();
			
			log.info(proxy.class.name)
			token = proxy.invokeMethod("jira1.login", ["jmiranda","angel1"]);
			log.info token
		} catch (Exception e) { 
			render e
		}
		render token
	}



	/**
	 *
	 */
	def showVersions = {
		def versions = api.jira1.getVersions(session.token, "PIMS")

		[versions:versions]
	}

	def showIssues = {
		def issues = []
		if (session.issues) {
			issues = session.issues
		}
		[issues:issues]
	}

	def searchIssues = {
		def errors = []
		def issues = []

		if (params.text) {
			issues = getIssues(params.text, 10000, errors)
			session.issues = issues
		}
		else {
			session.issues = []
		}
		[issues:issues, errors: errors]
	}


	def showServerInfo = {
		def serverInfo = api.jira1.getServerInfo(token)
		[serverInfo:serverInfo]
	}

	def showIssue = {
		def issue = api.jira1.getIssue(token, params.id)
		[issue:issue]
	}

	def createIssue = {
	}

	def editIssue = {
		def issue = api.jira1.getIssue(token, params.id)
		[issue:issue]
	}

	def saveIssue = {
	}

	def getToken() {
		if (token == null) {
			if (api == null) {
				api = getApi();
			}
			token = api.jira1.login("jmiranda", "angel1");
		}
		return token
	}
	
	
	def getSoapApi() { 
		return new XMLRPCServerProxy("http://tickets.pih-emr.org/jira/rpc/soap/jirasoapservice-v2?WSDL")
	}
	
	def getApi() {
		return new XMLRPCServerProxy("http://tickets.pih-emr.org/jira/rpc/xmlrpc")
	}

	def getIssues(String text, Integer limit, errors) {
		log.info "searching for new issues: " + text
		def issues = []
		def projects = new Vector()
		projects.add("PIMS");
		try {
			issues = api.jira1.getIssuesFromTextSearchWithProject(token, projects, text, limit)
		}
		catch (Exception e) {
			errors << e.message
		}
		return issues
	}

	def getStatuses() {
		def statuses = []
		try {
			statuses = api.jira1.getStatuses(token)
		} catch (Exception e) {
			
		}
		return statuses
	}
}