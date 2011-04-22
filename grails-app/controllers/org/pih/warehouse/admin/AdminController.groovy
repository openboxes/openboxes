package org.pih.warehouse.admin

import grails.util.GrailsUtil;
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class AdminController {

	def mailService;
	def grailsApplication
	def config = ConfigurationHolder.config
	
    def index = { }
    def plugins = { } 
    def status = { } 
    
	
	def checkSettings = { 
		
		[
			env: GrailsUtil.environment,
			enabled: Boolean.valueOf(grailsApplication.config.grails.mail.enabled),
			from: "${config.grails.mail.from}",
			host: "${config.grails.mail.host}",
			port: "${config.grails.mail.port}"
		]
	}
}
