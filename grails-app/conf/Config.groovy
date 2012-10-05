/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
import grails.util.GrailsUtil
import org.apache.log4j.AsyncAppender
import org.apache.log4j.Level
import org.apache.log4j.net.SMTPAppender

// Locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts
grails.config.locations = [ 
	//"classpath:${appName}-config.groovy",
	//"classpath:${appName}-config.properties",
	//"file:${userHome}/.grails/${appName}-config.groovy",
	"file:${userHome}/.grails/${appName}-config.properties"
]
println "Using configuration locations ${grails.config.locations} [${GrailsUtil.environment}]"

grails.exceptionresolver.params.exclude = ['password', 'passwordConfirm']

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails { 
	mail { 		
		// By default we enable email.  You can enable/disable email using environment settings below or in your 
		// ${user.home}/openboxes-config.properties file 
		enabled = true			
		from = "openboxes@pih.org"
		prefix = "[OpenBoxes]"
		host = "localhost"
		port = "25"
	}
}

app.loginLocation.requiredActivities = ["MANAGE_INVENTORY"]

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
		xml: ['text/xml', 'application/xml'],
		text: 'text/plain',
		js: 'text/javascript',
		rss: 'application/rss+xml',
		atom: 'application/atom+xml',
		css: 'text/css',
		csv: 'text/csv',
		all: '*/*',
		json: ['application/json','text/json'],
		form: 'application/x-www-form-urlencoded',
		multipartForm: 'multipart/form-data']

// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"
grails.views.enable.jsessionid = true
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// not sure what this does
grails.views.javascript.library="jquery"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'
// Set to true if BootStrap.groovy is failing to add all sample data 
grails.gorm.failOnError = false
// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable fo AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
grails.validateable.packages = [
	'org.pih.warehouse.inventory', 
	'org.pih.warehouse.fulfillment',
	'org.pih.warehouse.order', 
	'org.pih.warehouse.request',
	'org.pih.warehouse.shipment',
]

/* Mail properties */
mail.error.server = 'localhost'
mail.error.port = 25
mail.error.from = 'openboxes@pih.org'
mail.error.to = 'jmiranda@pih.org'
mail.error.subject = '[OpenBoxes][' + GrailsUtil.environment + '] ERROR'
mail.error.debug = false

// set per-environment serverURL stem for creating absolute links
environments {
	development {
		grails.serverURL = "http://localhost:8080/${appName}";
		uiperformance.enabled = false
		grails.mail.enabled = false
	}
	test {  
		grails.serverURL = "http://localhost:8080/${appName}"  
		uiperformance.enabled = false
		grails.mail.enabled = true
	}
	production {  
		grails.serverURL = "http://localhost:8080/${appName}"  
		uiperformance.enabled = false
		grails.mail.enabled = true
	}
	client {
		grails.serverURL = "http://localhost:8080/${appName}";
		uiperformance.enabled = false
		grails.mail.enabled = true
	}
	root {
		grails.serverURL = "http://localhost:8080/${appName}";
		uiperformance.enabled = false
		grails.mail.enabled = true
	}
	
}


// log4j configuration
log4j = {
	
	System.setProperty 'mail.smtp.port', mail.error.port.toString()
    System.setProperty 'mail.smtp.connectiontimeout', "5000"
    System.setProperty 'mail.smtp.timeout', "5000"
	System.setProperty 'mail.smtp.starttls.enable', mail.error.starttls.toString()
	
	// Example of changing the log pattern for the default console    
	appenders {
		//console name:'stdout', layout:pattern(conversionPattern: '%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n')		
		//console name:'stdout', layout:pattern(conversionPattern: '%p %d{ISO8601} %c{4} %m%n')		
		console name:'stdout', layout:pattern(conversionPattern: '%p %X{sessionId} %d{ISO8601} [%c{1}] %m%n')

        if (Boolean.parseBoolean(grails.mail.enabled)) {
            def smtpAppender = new SMTPAppender(
                    name: 'smtp',
                    to: mail.error.to,
                    from: mail.error.from,
                    subject: mail.error.subject,
                    threshold: Level.ERROR,
                    SMTPHost: mail.error.server,
                    SMTPUsername: mail.error.username,
                    SMTPDebug: mail.error.debug.toString(),
                    SMTPPassword: mail.error.password,
                    layout: pattern(conversionPattern:
                            '%d{[dd.MM.yyyy HH:mm:ss.SSS]} [%t] %n%-5p %X{sessionId} %n%c %n%C %n %x %n %m%n'))
            appender smtpAppender

            def asyncAppender = new AsyncAppender(
                    name: 'async',
                    bufferSize: 500,
            )
            asyncAppender.addAppender(smtpAppender)
            appender asyncAppender
        }
	}
			
	error	'org.hibernate.engine.StatefulPersistenceContext.ProxyWarnLog',
            'org.hibernate.impl.SessionFactoryObjectFactory',  // We get some annoying stack trace when cleaning this class up after functional tests
            'com.gargoylesoftware.htmlunit.DefaultCssErrorHandler',
            'com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl'

	warn	'org.mortbay.log',
		'org.codehaus.groovy.grails.web.pages',			// GSP		
		'org.codehaus.groovy.grails.web.servlet',		// controllers
		'org.codehaus.groovy.grails.web.sitemesh',		// layouts
		'org.codehaus.groovy.grails.web.mapping.filter',	// URL mapping
		'org.codehaus.groovy.grails.web.mapping', 		// URL mapping
		'org.codehaus.groovy.grails.commons', 			// core / classloading
		'org.codehaus.groovy.grails.plugins',			// plugins
		'org.codehaus.groovy.grails.orm.hibernate', 		// hibernate integration
		'org.docx4j',
		'org.apache.http.headers',
		'org.apache.ddlutils',
		'org.apache.http.wire',
		'net.sf.ehcache.hibernate'
		
	info	'org.liquibase', 	
		'grails.app.controller',
		'com.mchange',
		'org.springframework',
		'org.hibernate',
		'org.pih.warehouse',
		'grails.app',
		'grails.app.bootstrap',
		'grails.app.service',
		'grails.app.task',
		'BootStrap',
		'liquibase',
         'com.gargoylesoftware.htmlunit'

	debug 	'org.apache.cxf',
            //'org.apache.http.wire',          // shows traffic between htmlunit and server
            //'com.gargoylesoftware.htmlunit'
	
	root {
		error 'stdout', 'async'
		additivity = true
	 }
			
}

// Added by the JQuery Validation plugin:
jqueryValidation.packed = true
jqueryValidation.cdn = false  // false or "microsoft"
jqueryValidation.additionalMethods = false


// Added by the JQuery Validation UI plugin:
jqueryValidationUi {
	errorClass = 'error'
	validClass = 'valid'
	onsubmit = true
	renderErrorsOnTop = true
	
	qTip {
		packed = true
		classes = 'ui-tooltip-red ui-tooltip-shadow ui-tooltip-rounded'  
	}
	
	/*
	  Grails constraints to JQuery Validation rules mapping for client side validation.
	  Constraint not found in the ConstraintsMap will trigger remote AJAX validation.
	*/
	StringConstraintsMap = [
		blank:'required', // inverse: blank=false, required=true
		creditCard:'creditcard',
		email:'email',
		inList:'inList',
		minSize:'minlength',
		maxSize:'maxlength',
		size:'rangelength',
		matches:'matches',
		notEqual:'notEqual',
		url:'url',
		nullable:'required',
		unique:'unique',
		validator:'validator'
	]
	
	// Long, Integer, Short, Float, Double, BigInteger, BigDecimal
	NumberConstraintsMap = [
		min:'min',
		max:'max',
		range:'range',
		notEqual:'notEqual',
		nullable:'required',
		inList:'inList',
		unique:'unique',
		validator:'validator'
	]
	
	CollectionConstraintsMap = [
		minSize:'minlength',
		maxSize:'maxlength',
		size:'rangelength',
		nullable:'required',
		validator:'validator'
	]
	
	DateConstraintsMap = [
		min:'minDate',
		max:'maxDate',
		range:'rangeDate',
		notEqual:'notEqual',
		nullable:'required',
		inList:'inList',
		unique:'unique',
		validator:'validator'
	]
	
	ObjectConstraintsMap = [
		nullable:'required',
		validator:'validator'
	]
	
	CustomConstraintsMap = [
		phone:'true', // International phone number validation
		phoneUS:'true'
	]	
}


// Added by the Joda-Time plugin:
grails.gorm.default.mapping = {
	"user-type" type: org.joda.time.contrib.hibernate.PersistentDateTime, class: org.joda.time.DateTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentDuration, class: org.joda.time.Duration
	"user-type" type: org.joda.time.contrib.hibernate.PersistentInstant, class: org.joda.time.Instant
	"user-type" type: org.joda.time.contrib.hibernate.PersistentInterval, class: org.joda.time.Interval
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalDate, class: org.joda.time.LocalDate
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalTimeAsString, class: org.joda.time.LocalTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalDateTime, class: org.joda.time.LocalDateTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentPeriod, class: org.joda.time.Period
}


/**
 * Global Properties
 */
// default and supported locales
locale.defaultLocale = 'en'
locale.supportedLocales = ['en','fr']


