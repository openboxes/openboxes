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
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.log4j.net.DynamicSubjectSMTPAppender

// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

 grails.config.locations = ["file:${userHome}/.grails/${appName}-config.properties", "file:${userHome}/.grails/${appName}-config.groovy" ]

if (System.properties["${appName}.config.location"]) {
    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
}

println "Using configuration locations ${grails.config.locations} [${GrailsUtil.environment}]"

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination

// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
    all:           '*/*', // 'all' maps to '*' or the first available format in withFormat
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    hal:           ['application/hal+json','application/hal+xml'],
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "none"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        // filteringCodecForContentType.'text/html' = 'html'
    }
}

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

// The default codec used to encode data with ${}
grails.views.default.codec="html" // none, html, base64
grails.views.gsp.encoding="UTF-8"
//grails.views.gsp.keepgenerateddir="/home/jmiranda/git/openboxes/target/generated"
grails.converters.encoding="UTF-8"
grails.views.enable.jsessionid = true
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// not sure what this does
grails.views.javascript.library="jquery"

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        // filteringCodecForContentType.'text/html' = 'html'
    }
}

grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to true if BootStrap.groovy is failing to add all sample data 
grails.gorm.failOnError = false

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password', 'passwordConfirm']


// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false

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
	        grails.logging.jul.usebridge = true
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
	        grails.logging.jul.usebridge = false
		// Should be configured in openboxes-config.properties
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
}


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

        	// Authentication disabled by default
		username = null
		password = null

		// Disable debug mode by default
		debug = false
	}
}


// log4j configuration
log4j.main = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

	// Used to debug hibernate/SQL queries
	//trace 'org.hibernate.type'
	//debug 'org.hibernate.SQL'

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
			//'org.apache.http.wire',
			'net.sf.ehcache.hibernate',
            //'org.hibernate.SQL',
            //'org.hibernate.type',
            //'org.jumpmind.symmetric.service.impl.PurgeService',
            'org.hibernate.cache',
            'org.apache.ddlutils'

	info    'org.liquibase',
            'com.opensymphony.clickstream',
            'org.codehaus.groovy.grails.web.pages',		// GSP			'com.mchange',
            'org.springframework',
			'org.hibernate',
			'com.mchange',
			'org.pih.warehouse',
			'grails.app',
            'grails.app.controllers',
			'grails.app.bootstrap',
			'grails.app.services',
			'grails.app.task',
            'grails.plugin.springcache',
			'BootStrap',
			'liquibase',
			'com.gargoylesoftware.htmlunit'

   debug 	'org.apache.cxf',
            'grails.plugin.rendering',
		   	'org.apache.commons.mail',
            'grails.plugins.raven',
            'net.kencochrane.raven',
            'grails.plugin.databasemigration',
            //'com.unboundid'
            //'org.hibernate.transaction',
            //'org.jumpmind',
            //'org.hibernate.jdbc',
            //'org.hibernate.SQL',
		   	//'com.gargoylesoftware.htmlunit',
            'org.apache.http.wire'        // shows traffic between htmlunit and server

   //trace    'org.hibernate.type.descriptor.sql.BasicBinder',
   //         'org.hibernate.type'


	root {
		info 'stdout'//, 'smtp'
		additivity = false
	}

}

/* Database Migration plugin */
//grails.plugin.databasemigration.dropOnStart = true
grails.plugin.databasemigration.updateOnStart = true
//grails.plugin.databasemigration.changelogLocation = "grails-app/migrations"
grails.plugin.databasemigration.changelogFileName = "changelog.xml"
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.xml']

/* Indicates which activities are required for a location to allow logins */
openboxes.chooseLocation.requiredActivities = ["MANAGE_INVENTORY"]

/* Grails resources plugin */
//grails.resources.adhoc.includes = []
//grails.resources.adhoc.excludes = ["*"]

// Reload Config Plugin
//grails.plugins.reloadConfig.files = []
//grails.plugins.reloadConfig.includeConfigLocations = true
//grails.plugins.reloadConfig.interval = 5000
//grails.plugins.reloadConfig.enabled = true
//grails.plugins.reloadConfig.notifyPlugins = []
//grails.plugins.reloadConfig.automerge = true
//grails.plugins.reloadConfig.notifyWithConfig = true


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


// Grails Sentry/Raven plugin
// NOTE: You'll need to enable the plugin and set a DSN using an external config properties file
// (namely, openboxes-config.properties or openboxes-config.groovy)
grails.plugins.raven.active = false
grails.plugins.raven.dsn = "https://PUBLIC_KEY:SECRET_KEY@app.getsentry.com/PROJECT_ID"

// Google analytics and feedback have been removed until I can improve performance.
//google.analytics.enabled = false
//google.analytics.webPropertyID = "UA-xxxxxx-x"

// Feedback mechanism that allows screenshots
//openboxes.feedback.enabled = false

// UserVoice widget
openboxes.uservoice.widget.enabled = true
openboxes.uservoice.widget.position = "right"

// UserVoice widget
openboxes.zopim.widget.enabled = false
openboxes.zopim.widget.url = "//v2.zopim.com/?2T7RMi7ERqr3s8N20KQ3wOBRudcwosBA"

// JIRA Issue Collector
openboxes.jira.issue.collector.enabled = false
openboxes.jira.issue.collector.url = "https://openboxes.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e/en_USgc5zl3-1988229788/6318/12/1.4.10/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=fb813fdb"

// OpenBoxes Feedback
openboxes.mail.feedback.enabled = false
openboxes.mail.feedback.recipients = ["feedback@openboxes.com"]

// OpenBoxes Error Emails (bug reports)
openboxes.mail.errors.enabled = true
openboxes.mail.errors.recipients = ["errors@openboxes.com"]

// Barcode scanner (disabled by default)
openboxes.scannerDetection.enabled = false

// Background jobs
openboxes.jobs.calculateQuantityJob.cronExpression = "0 0 0 * * ?"

// LDAP configuration
openboxes.ldap.enabled = false
openboxes.ldap.context.managerDn = "cn=read-only-admin,dc=example,dc=com"
openboxes.ldap.context.managerPassword = "password"
//openboxes.ldap.context.server = "ldap://ldap.forumsys.com:389"
openboxes.ldap.context.server.host = "ldap.forumsys.com"
openboxes.ldap.context.server.port = 389

// LDAP Search
openboxes.ldap.search.base = "dc=example,dc=com"
openboxes.ldap.search.filter="(uid={0})"
openboxes.ldap.search.searchSubtree = true
openboxes.ldap.search.attributesToReturn = ['mail', 'givenName']

//openboxes.ldap.authorities.retrieveGroupRoles = false
//openboxes.ldap.authorities.groupSearchBase ='DC=example,DC=com'
//openboxes.ldap.authorities.groupSearchFilter = 'member={0}'
//openboxes.ldap.authorities.role.ROLE_ADMIN = "ou=mathematicians,dc=example,dc=com"
//openboxes.ldap.authorities.role.ROLE_MANAGER = "ou=scientists,dc=example,dc=com"
//openboxes.ldap.authorities.role.ROLE_ASSISTANT = "ou=assistants,dc=example,dc=com"
//openboxes.ldap.authorities.role.ROLE_BROWSER = "ou=browsers,dc-example,dc=com"

// Stock Card > Consumption > Reason codes
// Examples: Stock out, Low stock, Expired, Damaged, Could not locate, Insufficient quantity reconditioned
openboxes.stockCard.consumption.reasonCodes = [ ReasonCode.STOCKOUT, ReasonCode.LOW_STOCK, ReasonCode.EXPIRED, ReasonCode.DAMAGED, ReasonCode.COULD_NOT_LOCATE, ReasonCode.INSUFFICIENT_QUANTITY_RECONDITIONED]

// Localization configuration - default and supported locales
openboxes.locale.defaultLocale = 'en'
openboxes.locale.supportedLocales = ['en','fr','es']

// Currency configuration
openboxes.locale.defaultCurrencyCode = "USD"
openboxes.locale.defaultCurrencySymbol = "\$"
//openboxes.locale.supportedCurrencyCodes = ["USD","CFA"]

// Grails doc configuration
grails.doc.title = "OpenBoxes"
grails.doc.subtitle = ""
grails.doc.authors = "Justin Miranda"
grails.doc.license = "Eclipse Public License - Version 1.0"
grails.doc.copyright = ""
grails.doc.footer = ""

// Added by the Joda-Time plugin:
/*
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
}*/