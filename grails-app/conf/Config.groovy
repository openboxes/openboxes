/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
import it.openutils.log4j.AlternateSMTPAppender;
import grails.util.GrailsUtil
import org.apache.log4j.AsyncAppender
import org.apache.log4j.Level
import org.apache.log4j.net.SMTPAppender
import org.pih.warehouse.log4j.net.DynamicSubjectSMTPAppender

// Locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts
grails.config.locations = [ 
	"classpath:${appName}-config.groovy",
	"classpath:${appName}-config.properties",
	"file:${userHome}/.grails/${appName}-config.groovy",
	"file:${userHome}/.grails/${appName}-config.properties"
]
println "Using configuration locations ${grails.config.locations} [${GrailsUtil.environment}]"

//grails.plugins.reloadConfig.files = []
//grails.plugins.reloadConfig.includeConfigLocations = true
//grails.plugins.reloadConfig.interval = 5000
//grails.plugins.reloadConfig.enabled = true
//grails.plugins.reloadConfig.notifyPlugins = []
//grails.plugins.reloadConfig.automerge = true
//grails.plugins.reloadConfig.notifyWithConfig = true

grails.exceptionresolver.params.exclude = ['password', 'passwordConfirm']

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

// Default mail settings
grails {
	mail { 		
		// By default we enable email.  You can enable/disable email using environment settings below or in your 
		// ${user.home}/openboxes-config.properties file 
		enabled = true			
		from = "info@openboxes.com"
		prefix = "[OpenBoxes]" + "["+GrailsUtil.environment+"]"
		host = "localhost"
		port = "25"
	}
}

/* Indicates which activities are required for a location to allow logins */
openboxes.loginLocation.requiredActivities = ["MANAGE_INVENTORY"]

/* Grails resources plugin */
grails.resources.adhoc.includes = []
grails.resources.adhoc.excludes = ["*"]

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
//grails.views.gsp.keepgenerateddir="/home/jmiranda/git/openboxes/target/generated"
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

/* Default settings for emails sent through the SMTP appender  */
mail.error.server = 'localhost'
mail.error.port = 25
mail.error.from = 'justin@openboxes.com'
mail.error.to = 'errors@openboxes.com'
mail.error.subject = '[OpenBoxes]['+GrailsUtil.environment+']'
mail.error.debug = true

// set per-environment serverURL stem for creating absolute links
environments {
	development {
		grails.serverURL = "http://localhost:8080/${appName}";
		uiperformance.enabled = false
		grails.mail.enabled = false
		mail.error.debug = false
	}
	test {  
		grails.serverURL = "http://localhost:8080/${appName}"  
		uiperformance.enabled = false
		grails.mail.enabled = false
	}
	loadtest {  
		grails.serverURL = "http://localhost:8080/${appName}"  
		uiperformance.enabled = false
		grails.mail.enabled = false
	}
	production {  
		grails.serverURL = "http://localhost:8080/${appName}"
		uiperformance.enabled = false
		grails.mail.enabled = true
        grails.mail.prefix = "[OpenBoxes]"
    }
	staging {  
		grails.serverURL = "http://localhost:8080/${appName}"
		uiperformance.enabled = false
		grails.mail.enabled = true
	}
	qa {  
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
	
	// Used to debug hibernate/SQL queries
	//trace 'org.hibernate.type'
	//debug 'org.hibernate.SQL'
	
	System.setProperty 'mail.smtp.port', mail.error.port.toString()
    System.setProperty 'mail.smtp.connectiontimeout', "5000"
    System.setProperty 'mail.smtp.timeout', "5000"
	
    if (mail.error.starttls)
		System.setProperty 'mail.smtp.starttls.enable', mail.error.starttls.toString()
	
	// Example of changing the log pattern for the default console
	appenders {
		println "grails.mail.enabled: '${grails.mail.enabled.toString()}'"
		//println "mail.error.server: '${mail.error.server}'"
		//println "mail.error.username: '${mail.error.username}'"
		//println "mail.error.password: '${mail.error.password}'"
		
		// Only enable SMTP appender when mail is enabled 
        if (Boolean.parseBoolean(grails.mail.enabled.toString())) {		
	        def smtpAppender
			def conversionPattern = 
				"Date: %d{MMM-dd-yyyy HH:mm:ss.SSS}%n" +
				"Thread: [%t]%n" +
                "Username: %X{username}%n" +
                "Location: %X{location}%n" +
                "Locale: %X{locale}%n" +
				"IP address: %X{ipAddress} http://whatismyipaddress.com/ip/%X{ipAddress}%n" +
				"Request URI: %X{requestUri}%n" +
                "Request URL: %X{requestUrl}%n" +
				"Query string: %X{queryString}%n" +
                "Server: %X{serverUrl}%n" +
                "Clickstream: %X{clickStreamUrl}%n%n" +
                "Stacktrace: %n%m%n"

			// The 'alternate' appender is the best, but only works on localhost w/o authentication
			if ("alternate".equals(mail.error.appender)&&"localhost".equals(mail.error.server)) {
				smtpAppender = new AlternateSMTPAppender(
					name: 'smtp',
					to: mail.error.to,
					from: mail.error.from,
					subject: mail.error.subject + " %m",
					threshold: Level.ERROR,
					//SMTPHost: mail.error.server,
					layout: pattern(conversionPattern: conversionPattern))
			}
			// The 'dynamic' appender allows configurable subject with authenticated mail (e.g. gmail)
			else if ("dynamic".equals(mail.error.appender)) { 
				smtpAppender = new DynamicSubjectSMTPAppender(
					name: 'smtp',
					to: mail.error.to,
					from: mail.error.from,
					subject: mail.error.subject + " %m",
					threshold: Level.ERROR,				
					//SMTPHost: mail.error.server,
					//SMTPUsername: mail.error.username,
					//SMTPPassword: mail.error.password,
					SMTPDebug: mail.error.debug,
					layout: pattern(conversionPattern: conversionPattern))
			}			
			// Default SMTP error appender does not allow configurable subject line 
			else { 				
				smtpAppender = new SMTPAppender(
					name: 'smtp',
					to: mail.error.to,
					from: mail.error.from,
					subject: mail.error.subject + " An application error occurred",
					threshold: Level.ERROR,
					//SMTPHost: mail.error.server,
					//SMTPUsername: mail.error.username,
					SMTPDebug: mail.error.debug,
					//SMTPPassword: mail.error.password,
					layout: pattern(conversionPattern: conversionPattern))
			} 
			
			// These are common attributes for each of the appenders
			if (mail.error.server) smtpAppender.SMTPHost = mail.error.server
			if (mail.error.username) smtpAppender.SMTPUsername = mail.error.username
			if (mail.error.password) smtpAppender.SMTPPassword = mail.error.password
			//if (mail.error.debug) smtpAppender.SMTPDebug = mail.error.debug
			
			println "Using " + mail.error.appender + " SMTP appender " + smtpAppender.class.name
        	appender smtpAppender

            def asyncAppender = new AsyncAppender(name: 'async', bufferSize: 500)
            asyncAppender.addAppender(smtpAppender)
            appender asyncAppender
        }
    }
	
	root {
		error 'stdout', 'smtp'
		additivity = false
	}


	fatal	'com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter',
            'org.grails.plugin.resource.ResourceMeta'

	// We get some annoying stack trace when cleaning this class up after functional tests
	error	'org.hibernate.engine.StatefulPersistenceContext.ProxyWarnLog',
            'org.hibernate.impl.SessionFactoryObjectFactory',  
            'com.gargoylesoftware.htmlunit.DefaultCssErrorHandler',
            'com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl'
            //'org.jumpmind.symmetric.config.PropertiesFactoryBean'

	warn	'org.mortbay.log',
            'org.codehaus.groovy.grails.web.servlet',		// controllers
            'org.codehaus.groovy.grails.web.sitemesh',		// layouts
            'org.codehaus.groovy.grails.web.mapping.filter',	// URL mapping
			'org.codehaus.groovy.grails.web.mapping', 		// URL mapping
            'org.codehaus.groovy.grails.orm.hibernate',
			'org.codehaus.groovy.grails.commons', 			// core / classloading
			'org.codehaus.groovy.grails.plugins',			// plugins
			//'org.codehaus.groovy.grails.orm.hibernate', 		// hibernate integration
			'org.docx4j',
			'org.apache.http.headers',
			'org.apache.ddlutils',
			'org.apache.http.wire',
			'net.sf.ehcache.hibernate',
            'org.hibernate.SQL',
            //'org.hibernate.type',
            'org.hibernate.cache'
            'org.apache.ddlutils'
            //'org.jumpmind.symmetric.service.impl.PurgeService'

	info    'org.liquibase',
            'com.opensymphony.clickstream',
            'org.codehaus.groovy.grails.web.pages',		// GSP			'com.mchange',
            'org.springframework',
			'org.hibernate',
			'org.pih.warehouse',
			'grails.app',
            'grails.app.controller',
			'grails.app.bootstrap',
			'grails.app.service',
			'grails.app.task',
            'grails.plugin.springcache',
			'BootStrap',
			'liquibase',
			'com.gargoylesoftware.htmlunit'
            //'org.jumpmind'

   debug 	'org.apache.cxf',
            'grails.plugin.rendering',

           //'com.gargoylesoftware.htmlunit',
            'org.apache.http.wire'        // shows traffic between htmlunit and server

   trace    'org.hibernate.type.descriptor.sql.BasicBinder'

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


/**
 * Google analytics and feedback have been removed until I can improve performance.
 */
//google.analytics.enabled = false
//grails.feedback.enabled = false

// UserVoice widget
openboxes.uservoice.widget.enabled = true
openboxes.uservoice.widget.position = "bottom-left"

// JIRA Issue Collector
openboxes.jira.issue.collector.enabled = true
openboxes.jira.issue.collector.url = "https://openboxes.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e/en_USgc5zl3-1988229788/6318/12/1.4.10/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=fb813fdb"

/**
 * Global Properties
 */
// default and supported locales
locale.defaultLocale = 'en'
locale.supportedLocales = ['en','fr','es']

/**
 * Grails doc configuration
 */
grails.doc.title = "OpenBoxes"
grails.doc.subtitle = ""
grails.doc.authors = "Justin Miranda"
grails.doc.license = "Eclipse Public License - Version 1.0"
grails.doc.copyright = ""
grails.doc.footer = ""

// Added by the Joda-Time plugin:
grails.gorm.default.mapping = {
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateMidnight, class: org.joda.time.DateMidnight
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateTime, class: org.joda.time.DateTime
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDateTimeZoneAsString, class: org.joda.time.DateTimeZone
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentDurationAsString, class: org.joda.time.Duration
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentInstantAsMillisLong, class: org.joda.time.Instant
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentInterval, class: org.joda.time.Interval
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalDate, class: org.joda.time.LocalDate
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime, class: org.joda.time.LocalDateTime
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentLocalTime, class: org.joda.time.LocalTime
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentPeriodAsString, class: org.joda.time.Period
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentTimeOfDay, class: org.joda.time.TimeOfDay
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentYearMonthDay, class: org.joda.time.YearMonthDay
	"user-type" type: org.jadira.usertype.dateandtime.joda.PersistentYears, class: org.joda.time.Years
}

grails.plugins.dynamicController.mixins = [
    'com.burtbeckwith.grails.plugins.appinfo.IndexControllerMixin':
            'com.burtbeckwith.appinfo_test.AdminManageController',

    'com.burtbeckwith.grails.plugins.appinfo.Log4jControllerMixin' :
            'com.burtbeckwith.appinfo_test.AdminManageController',

    'com.burtbeckwith.grails.plugins.appinfo.SpringControllerMixin' :
            'com.burtbeckwith.appinfo_test.AdminManageController',

    'com.burtbeckwith.grails.plugins.appinfo.HibernateControllerMixin' :
            'com.burtbeckwith.appinfo_test.AdminManageController',

    'com.burtbeckwith.grails.plugins.appinfo.MemoryControllerMixin' :
            'com.burtbeckwith.appinfo_test.AdminManageController',

    'com.burtbeckwith.grails.plugins.appinfo.PropertiesControllerMixin' :
            'com.burtbeckwith.appinfo_test.AdminManageController',

    'com.burtbeckwith.grails.plugins.appinfo.ScopesControllerMixin' :
            'com.burtbeckwith.appinfo_test.AdminManageController',

    'com.burtbeckwith.grails.plugins.appinfo.ThreadsControllerMixin' :
            'com.burtbeckwith.appinfo_test.AdminManageController',

    'app.info.custom.example.MyConfigControllerMixin' :
            'com.burtbeckwith.appinfo_test.AdminManageController'
]