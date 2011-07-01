import grails.util.GrailsUtil
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

println "Using configuration locations ${grails.config.locations} ${GrailsUtil.environment}"

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails { 
	mail { 
		/**
		 * By default we disable email, enable email using environment settings below or in your 
		 * ${user.home}/warehouse-config.properties file 
		 */
		enabled = true			
		from = "info@openboxes.com"
		host = "localhost"
		port = "25"
	}
}

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
grails.validateable.packages = ['org.pih.warehouse.inventory', 'org.pih.warehouse.order']

/* Mail properties */
mail.error.server = 'localhost'
mail.error.port = 25
mail.error.from = 'error@openboxes.com'
mail.error.to = 'jmiranda@pih.org'
mail.error.subject = '[Application Error][' + GrailsUtil.environment + ']'
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
}


// log4j configuration
/*
 log4j = {
 root {
 error()
 additivity = true
 }
 debug 'grails.app'
 }*/

log4j = {
	
	System.setProperty 'mail.smtp.port', mail.error.port.toString()
	System.setProperty 'mail.smtp.starttls.enable', mail.error.starttls.toString()
	
	// Example of changing the log pattern for the default console    
	appenders {
		//console name:'stdout', layout:pattern(conversionPattern: '%p - %C{1}.%M(%L) |%d{ISO8601}| %m%n')		
		console name:'stdout', layout:pattern(conversionPattern: '%p %d{ISO8601} %c{4} %m%n')		
		
		appender new SMTPAppender(
			name: 'smtp', 
			to: mail.error.to, 
			from: mail.error.from,
			subject: mail.error.subject, 
			threshold: Level.FATAL,
			SMTPHost: mail.error.server, 
			SMTPUsername: mail.error.username,
			SMTPDebug: mail.error.debug.toString(), 
			SMTPPassword: mail.error.password,
			layout: pattern(conversionPattern:
			   '%d{[ dd.MM.yyyy HH:mm:ss.SSS]} [%t] %n%-5p %n%c %n%C %n %x %n %m%n'))
	}
			
	error 'org.codehaus.groovy.grails.web.pages',			// GSP
		'org.hibernate.engine.StatefulPersistenceContext.ProxyWarnLog'
	
	
	warn	'org.mortbay.log',
		'org.codehaus.groovy.grails.web.servlet',		// controllers
		'org.codehaus.groovy.grails.web.sitemesh',		// layouts
		'org.codehaus.groovy.grails.web.mapping.filter',	// URL mapping
		'org.codehaus.groovy.grails.web.mapping', 		// URL mapping
		'org.codehaus.groovy.grails.commons', 			// core / classloading
		'org.codehaus.groovy.grails.plugins',			// plugins
		'org.codehaus.groovy.grails.orm.hibernate', 		// hibernate integration
		'org.docx4j',
		'net.sf.ehcache.hibernate'		
	
	info	'org.liquibase', 	
		'com.mchange',
		'org.springframework',
		'org.hibernate',
		'org.pih.warehouse',
		'grails.app',
		'grails.app.controller',
		'grails.app.bootstrap',
		'grails.app.service',
		'grails.app.task'
		'BootStrap'

	debug	'liquibase',
		'org.apache.ddlutils',
		'org.apache.http.headers',
		'org.apache.http.wire'
	
	root {
		error 'stdout', 'smtp'
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
